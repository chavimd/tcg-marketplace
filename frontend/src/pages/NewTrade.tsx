import { useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { CardFace } from "../components/CardFace";
import { publicationSubject } from "../components/PublicationCard";
import { Empty, ErrorNote, Loading } from "../components/ui";
import { api, ApiError, queryString } from "../lib/api";
import { formatBs, plural } from "../lib/format";
import type { Propuesta, Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

export function NewTrade() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const publicationId = Number.parseInt(params.get("publicacion") ?? "", 10);

  const publicacion = useResource<Publicacion>(
    Number.isFinite(publicationId) && publicationId > 0 ? `/api/publications/${publicationId}` : null,
  );
  const inventario = useResource<Publicacion[]>(`/api/inventory/me${queryString({})}`);

  const [seleccion, setSeleccion] = useState<Record<number, number>>({});
  const [cantidadPedida, setCantidadPedida] = useState(1);
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const disponibles = useMemo(
    () =>
      (inventario.data ?? []).filter(
        (item) =>
          item.estadoPublicacion === "DISPONIBLE" &&
          item.stock > 0 &&
          item.id !== publicacion.data?.id &&
          // El backend rechaza ofrecer y pedir la misma carta.
          item.carta?.id !== publicacion.data?.carta?.id,
      ),
    [inventario.data, publicacion.data?.id, publicacion.data?.carta?.id],
  );

  const ofertaTotal = useMemo(() => {
    return Object.entries(seleccion).reduce((total, [id, cantidad]) => {
      const item = disponibles.find((candidate) => candidate.id === Number(id));
      return total + (item ? item.precioUnitario * cantidad : 0);
    }, 0);
  }, [seleccion, disponibles]);

  const pedidoTotal = publicacion.data ? publicacion.data.precioUnitario * cantidadPedida : 0;
  const seleccionados = Object.keys(seleccion).length;

  function alternar(item: Publicacion) {
    setSeleccion((current) => {
      const next = { ...current };
      if (next[item.id]) {
        delete next[item.id];
      } else {
        next[item.id] = 1;
      }
      return next;
    });
  }

  function cambiarCantidad(item: Publicacion, cantidad: number) {
    const value = Math.min(Math.max(1, Number.isFinite(cantidad) ? cantidad : 1), item.stock);
    setSeleccion((current) => ({ ...current, [item.id]: value }));
  }

  async function enviar() {
    if (!publicacion.data) return;
    if (seleccionados === 0) {
      setError("Elige al menos una carta de tu colección para ofrecer.");
      return;
    }
    setError(null);
    setEnviando(true);
    try {
      const propuesta = await api<Propuesta>("/api/trades", {
        method: "POST",
        body: {
          receptorUsername: publicacion.data.username,
          ofertas: Object.entries(seleccion).map(([id, cantidad]) => ({
            publicacionId: Number(id),
            cantidad,
          })),
          solicitudes: [{ publicacionId: publicacion.data.id, cantidad: cantidadPedida }],
        },
      });
      navigate(`/trueques/${propuesta.id}`);
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : "No se pudo enviar la propuesta.");
    } finally {
      setEnviando(false);
    }
  }

  if (publicacion.loading) {
    return (
      <div className="shell py-12">
        <Loading label="Preparando la propuesta" />
      </div>
    );
  }

  if (publicacion.error || !publicacion.data) {
    return (
      <div className="shell py-12">
        <ErrorNote
          message={publicacion.error ?? "No encontramos esa publicación."}
          onRetry={publicacion.reload}
        />
        <Link to="/catalogo" className="btn btn-outline mt-6">
          Volver al catálogo
        </Link>
      </div>
    );
  }

  const objetivo = publicacion.data;
  const subject = publicationSubject(objetivo);

  return (
    <section className="shell py-8 sm:py-12">
      <nav aria-label="Migas" className="text-sm text-muted">
        <Link to={`/catalogo/${objetivo.id}`} className="hover:text-ink">
          {subject.nombre}
        </Link>
        <span className="mx-2">/</span>
        <span className="text-ink">Nueva propuesta</span>
      </nav>

      <div className="mt-5 flex flex-wrap items-end justify-between gap-4 border-b border-rule pb-4">
        <div>
          <h1 className="text-[clamp(2.2rem,5vw,3.4rem)]">Nueva propuesta</h1>
          <p className="mt-2 text-sm text-muted">
            Para @{objetivo.username}. Elige qué ofreces y cuántas unidades pides.
          </p>
        </div>
        <p className="text-sm text-muted">
          {seleccionados} {seleccionados === 1 ? "carta ofrecida" : "cartas ofrecidas"}
        </p>
      </div>

      <div className="mt-8 grid gap-10 lg:grid-cols-2 lg:gap-0">
        <div className="lg:pr-10">
          <h2 className="text-2xl">Ofreces</h2>
          <p className="hint mt-1">Solo aparecen tus publicaciones disponibles.</p>

          {inventario.loading ? <Loading label="Cargando tu colección" /> : null}
          {inventario.error ? (
            <div className="mt-4">
              <ErrorNote message={inventario.error} onRetry={inventario.reload} />
            </div>
          ) : null}

          {!inventario.loading && !inventario.error ? (
            disponibles.length === 0 ? (
              <div className="mt-4">
                <Empty
                  title="No tienes cartas disponibles"
                  action={
                    <Link to="/coleccion" className="btn btn-outline">
                      Ver mi colección
                    </Link>
                  }
                >
                  Para ofrecer algo necesitas publicaciones en estado disponible. Pide a un
                  administrador que asigne cartas a tu cuenta.
                </Empty>
              </div>
            ) : (
              <ul className="mt-4 max-h-[30rem] divide-y divide-rule overflow-y-auto border-y border-rule pr-1">
                {disponibles.map((item) => {
                  const itemSubject = publicationSubject(item);
                  const active = Boolean(seleccion[item.id]);
                  return (
                    <li key={item.id} className="py-3">
                      <div className="flex items-start gap-3">
                        <label className="flex flex-1 cursor-pointer items-start gap-3">
                          <input
                            type="checkbox"
                            className="mt-1.5 accent-signal"
                            checked={active}
                            onChange={() => alternar(item)}
                          />
                          <CardFace card={itemSubject} className="w-12 shrink-0" variant="mini" />
                          <span className="min-w-0">
                            <span className="block truncate text-sm font-semibold">
                              {itemSubject.nombre}
                            </span>
                            <span className="block truncate text-xs text-muted">
                              {itemSubject.setNombre ?? "Sin set"} · {item.stock} disp.
                            </span>
                            <span className="tabnum block text-xs text-muted">
                              {formatBs(item.precioUnitario)}
                            </span>
                          </span>
                        </label>

                        {active ? (
                          <label className="flex items-center gap-2 text-xs">
                            <span className="sr-only">Cantidad de {itemSubject.nombre}</span>
                            <input
                              type="number"
                              className="field h-9 w-16 text-center"
                              min={1}
                              max={item.stock}
                              value={seleccion[item.id]}
                              onChange={(event) => cambiarCantidad(item, Number(event.target.value))}
                            />
                            <span className="text-muted">ud.</span>
                          </label>
                        ) : null}
                      </div>
                    </li>
                  );
                })}
              </ul>
            )
          ) : null}
        </div>

        <div className="border-t border-rule pt-8 lg:border-l lg:border-t-0 lg:pl-10 lg:pt-0">
          <h2 className="text-2xl">Pides</h2>
          <p className="hint mt-1">
            Puedes pedir hasta {objetivo.stock} {plural(objetivo.stock, "unidad", "unidades")}.
          </p>

          <div className="mt-4 flex gap-4 border-y border-rule py-4">
            <CardFace card={subject} className="w-24 shrink-0" variant="mini" />
            <div className="min-w-0 flex-1">
              <p className="truncate text-lg font-semibold">{subject.nombre}</p>
              <p className="truncate text-sm text-muted">
                {subject.setNombre ?? "Sin set"} · {subject.rareza ?? "Sin rareza"}
              </p>
              <p className="tabnum mt-1 text-sm text-muted">
                {formatBs(objetivo.precioUnitario)} por unidad
              </p>

              <label className="mt-3 flex items-center gap-2 text-sm">
                <span>Cantidad</span>
                <input
                  type="number"
                  className="field h-9 w-20 text-center"
                  min={1}
                  max={objetivo.stock}
                  value={cantidadPedida}
                  onChange={(event) =>
                    setCantidadPedida(
                      Math.min(Math.max(1, Number(event.target.value) || 1), objetivo.stock),
                    )
                  }
                />
                <span className="text-muted">de {objetivo.stock}</span>
              </label>
            </div>
          </div>

          <dl className="mt-4 space-y-2 text-sm">
            <div className="flex items-center justify-between gap-3">
              <dt className="text-muted">Valor de lo que ofreces</dt>
              <dd className="tabnum font-semibold">{formatBs(ofertaTotal)}</dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-muted">Valor de lo que pides</dt>
              <dd className="tabnum font-semibold">{formatBs(pedidoTotal)}</dd>
            </div>
          </dl>
          <p className="hint mt-2">
            El valor es solo una referencia para equilibrar el cambio; no se transfiere dinero.
          </p>

          {error ? (
            <p className="form-error mt-4" role="alert">
              {error}
            </p>
          ) : null}

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              type="button"
              className="btn btn-signal"
              onClick={enviar}
              disabled={enviando || seleccionados === 0}
            >
              {enviando ? "Enviando…" : "Enviar propuesta"}
            </button>
            <Link to={`/catalogo/${objetivo.id}`} className="btn btn-outline">
              Cancelar
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}
