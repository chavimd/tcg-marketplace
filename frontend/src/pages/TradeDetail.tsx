import { Client } from "@stomp/stompjs";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { CardFace } from "../components/CardFace";
import { ErrorNote, Loading, StatusBadge } from "../components/ui";
import { api, ApiError, getToken, websocketUrl } from "../lib/api";
import { useAuth } from "../lib/auth";
import { formatDateTime, formatBs } from "../lib/format";
import type { ItemTrade, MensajeChat, Propuesta } from "../lib/types";
import { useResource } from "../lib/useResource";

function subjectOf(item: ItemTrade) {
  return {
    nombre: item.cartaNombre ?? "Carta sin catalogar",
    setNombre: item.setNombre,
    numeroSet: item.numeroSet,
    rareza: item.rareza,
    juego: item.juego,
    imagenUrl: item.imagenUrl,
  };
}

function valorTotal(items: ItemTrade[]): number {
  return items.reduce((total, item) => total + item.precioUnitario * item.cantidad, 0);
}

export function TradeDetail() {
  const { id } = useParams<{ id: string }>();
  const tradeId = Number.parseInt(id ?? "", 10);
  const { user } = useAuth();

  const propuesta = useResource<Propuesta>(Number.isFinite(tradeId) ? `/api/trades/${tradeId}` : null);
  const historial = useResource<MensajeChat[]>(
    Number.isFinite(tradeId) ? `/api/trades/${tradeId}/messages` : null,
  );

  const [mensajes, setMensajes] = useState<MensajeChat[]>([]);
  const [conectado, setConectado] = useState(false);
  const [chatError, setChatError] = useState<string | null>(null);
  const [borrador, setBorrador] = useState("");
  const [accionando, setAccionando] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const clientRef = useRef<Client | null>(null);
  const finalRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (historial.data) {
      setMensajes(historial.data);
    }
  }, [historial.data]);

  useEffect(() => {
    const current = propuesta.data;
    if (!current) return;
    const token = getToken();
    if (!token) return;

    const client = new Client({
      brokerURL: websocketUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
    });

    client.onConnect = () => {
      setConectado(true);
      setChatError(null);
      client.subscribe(`/user/queue/trades/${current.id}`, (frame) => {
        try {
          const incoming = JSON.parse(frame.body) as MensajeChat;
          setMensajes((existing) =>
            existing.some((message) => message.id === incoming.id)
              ? existing
              : [...existing, incoming],
          );
        } catch {
          setChatError("Llegó un mensaje que no se pudo leer.");
        }
      });
    };

    client.onWebSocketClose = () => setConectado(false);
    client.onStompError = () => setChatError("El chat no pudo conectarse.");

    client.activate();
    clientRef.current = client;

    return () => {
      clientRef.current = null;
      setConectado(false);
      void client.deactivate();
    };
  }, [propuesta.data]);

  useEffect(() => {
    finalRef.current?.scrollIntoView({ block: "nearest" });
  }, [mensajes.length]);

  if (propuesta.loading) {
    return (
      <div className="shell py-12">
        <Loading label="Abriendo el trueque" />
      </div>
    );
  }

  if (propuesta.error || !propuesta.data) {
    return (
      <div className="shell py-12">
        <ErrorNote
          message={propuesta.error ?? "No encontramos este trueque."}
          onRetry={propuesta.reload}
        />
        <Link to="/trueques" className="btn btn-outline mt-6">
          Volver a trueques
        </Link>
      </div>
    );
  }

  const data = propuesta.data;
  const ofertas = data.items.filter((item) => item.rol === "OFERTA");
  const solicitudes = data.items.filter((item) => item.rol === "SOLICITUD");
  const soyReceptor = user?.id === data.receptor.id;
  const soyEmisor = user?.id === data.emisor.id;
  const pendiente = data.estado === "PENDIENTE";

  async function ejecutar(accion: "accept" | "reject" | "cancel") {
    const pregunta =
      accion === "accept"
        ? "Al aceptar se mueve el stock entre las dos colecciones. ¿Continuar?"
        : accion === "reject"
          ? "¿Rechazar esta propuesta?"
          : "¿Cancelar esta propuesta?";

    if (!window.confirm(pregunta)) return;

    setAccionando(true);
    setActionError(null);
    try {
      await api<Propuesta>(`/api/trades/${data.id}/${accion}`, { method: "POST" });
      propuesta.reload();
    } catch (cause) {
      setActionError(cause instanceof ApiError ? cause.message : "No se pudo completar la acción.");
    } finally {
      setAccionando(false);
    }
  }

  function enviarMensaje(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const texto = borrador.trim();
    const client = clientRef.current;
    if (!texto || !client?.connected) return;
    client.publish({
      destination: `/app/trades/${data.id}/messages`,
      body: JSON.stringify({ content: texto }),
    });
    setBorrador("");
  }

  return (
    <section className="shell py-8 sm:py-12">
      <nav aria-label="Migas" className="text-sm text-muted">
        <Link to="/trueques" className="hover:text-ink">
          Trueques
        </Link>
        <span className="mx-2">/</span>
        <span className="text-ink tabnum">#{data.id}</span>
      </nav>

      <div className="mt-5 flex flex-wrap items-start justify-between gap-4 border-b border-rule pb-4">
        <div>
          <h1 className="text-[clamp(2.2rem,5vw,3.4rem)]">Trueque #{data.id}</h1>
          <p className="mt-2 text-sm text-muted">
            @{data.emisor.username} propone a @{data.receptor.username} ·{" "}
            {formatDateTime(data.createdAt)}
          </p>
        </div>
        <StatusBadge estado={data.estado} />
      </div>

      <div className="mt-8 grid gap-10 md:grid-cols-2 md:gap-0">
        <div className="md:pr-10">
          <div className="flex items-baseline justify-between gap-3">
            <h2 className="text-2xl">
              Ofrece <span className="normal-case">@{data.emisor.username}</span>
            </h2>
            <span className="tabnum text-sm text-muted">{formatBs(valorTotal(ofertas))}</span>
          </div>
          <ul className="mt-4 divide-y divide-rule border-y border-rule">
            {ofertas.length === 0 ? (
              <li className="py-4 text-sm text-muted">Sin cartas ofrecidas.</li>
            ) : (
              ofertas.map((item) => (
                <li key={item.id} className="flex items-center gap-3 py-3">
                  <CardFace card={subjectOf(item)} className="w-12 shrink-0" variant="mini" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold">{subjectOf(item).nombre}</p>
                    <p className="truncate text-xs text-muted">
                      {subjectOf(item).setNombre ?? "Sin set"}
                      {item.estadoConservacion ? ` · ${item.estadoConservacion}` : ""}
                    </p>
                  </div>
                  <div className="shrink-0 text-right">
                    <p className="tabnum text-sm font-semibold">{item.cantidad} ud.</p>
                    <p className="tabnum text-xs text-muted">
                      {formatBs(item.precioUnitario * item.cantidad)}
                    </p>
                  </div>
                </li>
              ))
            )}
          </ul>
        </div>

        <div className="border-t border-rule pt-8 md:border-l md:border-t-0 md:pl-10 md:pt-0">
          <div className="flex items-baseline justify-between gap-3">
            <h2 className="text-2xl">
              Pide a <span className="normal-case">@{data.receptor.username}</span>
            </h2>
            <span className="tabnum text-sm text-muted">{formatBs(valorTotal(solicitudes))}</span>
          </div>
          <ul className="mt-4 divide-y divide-rule border-y border-rule">
            {solicitudes.length === 0 ? (
              <li className="py-4 text-sm text-muted">Sin cartas solicitadas.</li>
            ) : (
              solicitudes.map((item) => (
                <li key={item.id} className="flex items-center gap-3 py-3">
                  <CardFace card={subjectOf(item)} className="w-12 shrink-0" variant="mini" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-semibold">{subjectOf(item).nombre}</p>
                    <p className="truncate text-xs text-muted">
                      {subjectOf(item).setNombre ?? "Sin set"}
                      {item.estadoConservacion ? ` · ${item.estadoConservacion}` : ""}
                    </p>
                  </div>
                  <div className="shrink-0 text-right">
                    <p className="tabnum text-sm font-semibold">{item.cantidad} ud.</p>
                    <p className="tabnum text-xs text-muted">
                      {formatBs(item.precioUnitario * item.cantidad)}
                    </p>
                  </div>
                </li>
              ))
            )}
          </ul>
        </div>
      </div>

      {pendiente && (soyReceptor || soyEmisor) ? (
        <div className="mt-8 flex flex-col gap-3 border border-rule p-4 sm:flex-row sm:items-center">
          <p className="flex-1 text-sm">
            {soyReceptor
              ? "Esta propuesta espera tu respuesta."
              : "Esperando la respuesta de la otra parte."}
          </p>
          <div className="flex flex-wrap gap-3">
            {soyReceptor ? (
              <>
                <button
                  type="button"
                  className="btn btn-signal"
                  disabled={accionando}
                  onClick={() => ejecutar("accept")}
                >
                  {accionando ? "Procesando…" : "Aceptar trueque"}
                </button>
                <button
                  type="button"
                  className="btn btn-outline"
                  disabled={accionando}
                  onClick={() => ejecutar("reject")}
                >
                  Rechazar
                </button>
              </>
            ) : null}
            {soyEmisor ? (
              <button
                type="button"
                className="btn btn-outline"
                disabled={accionando}
                onClick={() => ejecutar("cancel")}
              >
                Cancelar propuesta
              </button>
            ) : null}
          </div>
        </div>
      ) : null}

      {actionError ? (
        <p className="form-error mt-3" role="alert">
          {actionError}
        </p>
      ) : null}

      <div className="mt-10">
        <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-rule pb-3">
          <h2 className="text-2xl">Conversación</h2>
          <p className="flex items-center gap-2 text-xs text-muted">
            <span
              className={`h-2 w-2 rounded-full ${conectado ? "bg-signal" : "bg-rule"}`}
              aria-hidden="true"
            />
            {conectado ? "En vivo" : "Conectando…"}
          </p>
        </div>

        <div className="mt-4 flex h-80 flex-col overflow-y-auto border border-rule bg-paper-2/40 p-4">
          {historial.loading ? <p className="text-sm text-muted">Cargando mensajes…</p> : null}
          {historial.error ? <ErrorNote message={historial.error} /> : null}
          {!historial.loading && mensajes.length === 0 ? (
            <p className="m-auto max-w-xs text-center text-sm text-muted">
              Todavía no hay mensajes. Usa la conversación para acordar el estado de las cartas.
            </p>
          ) : null}

          <ul className="space-y-3">
            {mensajes.map((mensaje) => {
              const propio = mensaje.senderUsername === user?.username;
              return (
                <li key={mensaje.id} className={`flex ${propio ? "justify-end" : "justify-start"}`}>
                  <div
                    className={`max-w-[80%] rounded px-3 py-2 text-sm ${
                      propio ? "bg-ink text-paper" : "border border-rule bg-paper"
                    }`}
                  >
                    {!propio ? (
                      <p className="text-[0.6875rem] font-semibold text-muted">
                        @{mensaje.senderUsername}
                      </p>
                    ) : null}
                    <p className="whitespace-pre-wrap break-words">{mensaje.content}</p>
                    <p
                      className={`mt-1 text-[0.625rem] ${propio ? "text-paper/50" : "text-muted"}`}
                    >
                      {formatDateTime(mensaje.sentAt)}
                    </p>
                  </div>
                </li>
              );
            })}
          </ul>
          <div ref={finalRef} />
        </div>

        {chatError ? (
          <p className="form-error mt-2" role="alert">
            {chatError}
          </p>
        ) : null}

        <form className="mt-3 flex gap-2" onSubmit={enviarMensaje}>
          <label className="flex-1">
            <span className="sr-only">Mensaje</span>
            <input
              className="field"
              placeholder="Escribe un mensaje"
              maxLength={2000}
              value={borrador}
              onChange={(event) => setBorrador(event.target.value)}
            />
          </label>
          <button type="submit" className="btn btn-ink" disabled={!conectado || !borrador.trim()}>
            Enviar
          </button>
        </form>
      </div>
    </section>
  );
}
