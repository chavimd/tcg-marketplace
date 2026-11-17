import { useState } from "react";
import { Link } from "react-router-dom";
import { CardFace } from "../components/CardFace";
import { Empty, ErrorNote, Loading, StatusBadge } from "../components/ui";
import { useAuth } from "../lib/auth";
import { formatDate } from "../lib/format";
import type { EstadoPropuesta, ItemTrade, Propuesta } from "../lib/types";
import { useResource } from "../lib/useResource";

const filtros: Array<{ label: string; value: EstadoPropuesta | "TODAS" }> = [
  { label: "Todas", value: "TODAS" },
  { label: "Pendientes", value: "PENDIENTE" },
  { label: "Aceptadas", value: "ACEPTADA" },
  { label: "Rechazadas", value: "RECHAZADA" },
  { label: "Canceladas", value: "CANCELADA" },
];

function itemSubject(item: ItemTrade) {
  return {
    nombre: item.cartaNombre ?? "Carta sin catalogar",
    setNombre: item.setNombre,
    numeroSet: item.numeroSet,
    rareza: item.rareza,
    juego: item.juego,
    imagenUrl: item.imagenUrl,
  };
}

export function Trades() {
  const { user } = useAuth();
  const { data, loading, error, reload } = useResource<Propuesta[]>("/api/trades");
  const [filtro, setFiltro] = useState<EstadoPropuesta | "TODAS">("TODAS");

  const todas = data ?? [];
  const propuestas = filtro === "TODAS" ? todas : todas.filter((p) => p.estado === filtro);
  const pendientes = todas.filter((p) => p.estado === "PENDIENTE").length;

  return (
    <section className="shell py-8 sm:py-12">
      <div className="flex flex-wrap items-end justify-between gap-4 border-b border-rule pb-4">
        <div>
          <h1 className="text-[clamp(2.2rem,5vw,3.4rem)]">Trueques</h1>
          <p className="mt-2 max-w-lg text-sm text-muted">
            Propuestas enviadas y recibidas. Solo la parte receptora puede aceptar o rechazar; la
            emisora puede cancelar.
          </p>
        </div>
        <p className="text-sm text-muted">
          {pendientes === 0
            ? "Sin pendientes"
            : `${pendientes} ${pendientes === 1 ? "pendiente" : "pendientes"}`}
        </p>
      </div>

      <div className="flex flex-wrap gap-2 border-b border-rule py-3">
        {filtros.map((opcion) => {
          const activa = filtro === opcion.value;
          return (
            <button
              key={opcion.value}
              type="button"
              className={`btn btn-sm ${activa ? "btn-ink" : "btn-outline"}`}
              aria-pressed={activa}
              onClick={() => setFiltro(opcion.value)}
            >
              {opcion.label}
            </button>
          );
        })}
      </div>

      {loading ? <Loading label="Cargando trueques" /> : null}
      {error ? (
        <div className="mt-6">
          <ErrorNote message={error} onRetry={reload} />
        </div>
      ) : null}

      {!loading && !error ? (
        propuestas.length === 0 ? (
          <div className="mt-8">
            <Empty
              title={filtro === "TODAS" ? "Todavía no hay trueques" : "Nada en este estado"}
              action={
                <Link to="/catalogo" className="btn btn-outline">
                  Buscar cartas
                </Link>
              }
            >
              Cuando propongas un intercambio o alguien lo haga contigo, aparecerá aquí con su
              historial de mensajes.
            </Empty>
          </div>
        ) : (
          <ul className="divide-y divide-rule">
            {propuestas.map((propuesta) => {
              const recibida = propuesta.receptor.id === user?.id;
              const contraparte = recibida ? propuesta.emisor : propuesta.receptor;
              const ofertas = propuesta.items.filter((item) => item.rol === "OFERTA");
              const solicitudes = propuesta.items.filter((item) => item.rol === "SOLICITUD");
              const miniaturas = [...ofertas, ...solicitudes].slice(0, 4);

              return (
                <li key={propuesta.id}>
                  <Link
                    to={`/trueques/${propuesta.id}`}
                    className="grid gap-4 py-5 transition-colors hover:bg-paper-2/70 md:grid-cols-[1fr_auto] md:items-center"
                  >
                    <div className="min-w-0 px-1">
                      <div className="flex flex-wrap items-center gap-2 text-xs text-muted">
                        <StatusBadge estado={propuesta.estado} />
                        <span className="tabnum">#{propuesta.id}</span>
                        <span aria-hidden="true">·</span>
                        <span>{recibida ? "Recibida de" : "Enviada a"}</span>
                        <span className="font-semibold text-ink">@{contraparte.username}</span>
                        <span aria-hidden="true">·</span>
                        <span>{formatDate(propuesta.createdAt)}</span>
                      </div>

                      <div className="mt-3 flex flex-wrap items-center gap-2">
                        {miniaturas.map((item) => (
                          <CardFace
                            key={item.id}
                            card={itemSubject(item)}
                            className="w-10 shrink-0"
                            variant="mini"
                          />
                        ))}
                        <p className="ml-1 text-sm text-muted">
                          {propuesta.items.reduce((total, item) => total + item.cantidad, 0)} cartas ·{" "}
                          {ofertas.length} {ofertas.length === 1 ? "ofrecida" : "ofrecidas"} ·{" "}
                          {solicitudes.length} {solicitudes.length === 1 ? "pedida" : "pedidas"}
                        </p>
                      </div>
                    </div>

                    <div className="px-1 text-sm md:text-right">
                      <p className="font-semibold">
                        Abrir <span aria-hidden="true">→</span>
                      </p>
                      <p className="text-muted">Detalle y chat</p>
                    </div>
                  </Link>
                </li>
              );
            })}
          </ul>
        )
      ) : null}
    </section>
  );
}
