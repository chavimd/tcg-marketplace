import { useState } from "react";
import { Link } from "react-router-dom";
import { CardFace } from "../components/CardFace";
import { publicationSubject } from "../components/PublicationCard";
import { Empty, ErrorNote, Loading, StatusBadge } from "../components/ui";
import { api, ApiError } from "../lib/api";
import { formatEuro } from "../lib/format";
import type { Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

export function Collection() {
  const { data, loading, error, reload } = useResource<Publicacion[]>("/api/inventory/me");
  const [updating, setUpdating] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const items = data ?? [];
  const unidades = items.reduce((total, item) => total + item.stock, 0);
  const disponibles = items.filter((item) => item.estadoPublicacion === "DISPONIBLE").length;
  const ocultas = items.filter((item) => item.estadoPublicacion === "OCULTA").length;

  async function alternarVisibilidad(item: Publicacion) {
    const siguiente = item.estadoPublicacion === "OCULTA" ? "DISPONIBLE" : "OCULTA";
    setUpdating(item.id);
    setActionError(null);
    try {
      await api(`/api/inventory/${item.id}/status`, {
        method: "PATCH",
        body: { estado: siguiente },
      });
      reload();
    } catch (cause) {
      setActionError(cause instanceof ApiError ? cause.message : "No se pudo cambiar el estado.");
    } finally {
      setUpdating(null);
    }
  }

  return (
    <section className="shell py-8 sm:py-12">
      <div className="flex flex-wrap items-end justify-between gap-4 border-b border-rule pb-4">
        <div>
          <h1 className="text-[clamp(2.2rem,5vw,3.4rem)]">Mi colección</h1>
          <p className="mt-2 max-w-lg text-sm text-muted">
            Lo que tienes disponible para cambiar. Oculta una carta para sacarla del catálogo sin
            perderla.
          </p>
        </div>
        <Link to="/catalogo" className="btn btn-outline">
          Explorar catálogo
        </Link>
      </div>

      <dl className="grid grid-cols-2 gap-x-6 gap-y-4 border-b border-rule py-5 sm:grid-cols-4">
        {[
          ["Publicaciones", items.length],
          ["Unidades", unidades],
          ["Disponibles", disponibles],
          ["Ocultas", ocultas],
        ].map(([label, value]) => (
          <div key={String(label)}>
            <dt className="text-xs text-muted">{label}</dt>
            <dd className="tabnum text-2xl font-semibold">{value}</dd>
          </div>
        ))}
      </dl>

      {loading ? <Loading label="Cargando tu colección" /> : null}
      {error ? (
        <div className="mt-6">
          <ErrorNote message={error} onRetry={reload} />
        </div>
      ) : null}
      {actionError ? (
        <p className="form-error mt-4" role="alert">
          {actionError}
        </p>
      ) : null}

      {!loading && !error ? (
        items.length === 0 ? (
          <div className="mt-8">
            <Empty title="Tu colección está vacía">
              Nadie puede proponerte un trueque todavía. Pide a un administrador que asigne cartas a
              tu cuenta y vuelve a mirar.
            </Empty>
          </div>
        ) : (
          <div className="mt-7 grid grid-cols-2 gap-x-5 gap-y-9 md:grid-cols-3 lg:grid-cols-4">
            {items.map((item) => {
              const subject = publicationSubject(item);
              return (
                <article key={item.id} className="border-t border-rule pt-3">
                  <CardFace card={subject} />
                  <h2 className="sr-only">{subject.nombre}</h2>
                  <div className="mt-3 flex items-center justify-between gap-2">
                    <StatusBadge estado={item.estadoPublicacion} />
                    <span className="tabnum shrink-0 text-[0.8125rem] font-semibold">
                      {item.stock} ud.
                    </span>
                  </div>
                  <div className="mt-1 flex items-center justify-between text-[0.8125rem] text-muted">
                    <span>Valor ref.</span>
                    <span className="tabnum">{formatEuro(item.precioUnitario)}</span>
                  </div>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm mt-3 w-full"
                    disabled={updating === item.id}
                    onClick={() => alternarVisibilidad(item)}
                  >
                    {updating === item.id
                      ? "Guardando…"
                      : item.estadoPublicacion === "OCULTA"
                        ? "Mostrar en el catálogo"
                        : "Ocultar del catálogo"}
                  </button>
                </article>
              );
            })}
          </div>
        )
      ) : null}
    </section>
  );
}
