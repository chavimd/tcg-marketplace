import { Link, useParams } from "react-router-dom";
import { CardFace } from "../components/CardFace";
import { publicationSubject } from "../components/PublicationCard";
import { ErrorNote, Loading, StatusBadge } from "../components/ui";
import { formatDate, formatEuro } from "../lib/format";
import { useAuth } from "../lib/auth";
import type { Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

export function PublicationDetail() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { data, loading, error, reload } = useResource<Publicacion>(
    id ? `/api/publications/${id}` : null,
  );

  if (loading) {
    return (
      <div className="shell py-12">
        <Loading label="Abriendo la publicación" />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="shell py-12">
        <ErrorNote message={error ?? "No encontramos esta publicación."} onRetry={reload} />
        <Link to="/catalogo" className="btn btn-outline mt-6">
          Volver al catálogo
        </Link>
      </div>
    );
  }

  const subject = publicationSubject(data);
  const isMine = user?.id === data.usuarioId;
  const tradePath = `/trueques/nuevo?publicacion=${data.id}`;
  const ctaHref = user ? tradePath : `/entrar?next=${encodeURIComponent(tradePath)}`;

  const rows: Array<[string, string]> = [
    ["Juego", subject.juego ?? "—"],
    ["Set", subject.setNombre ?? "—"],
    ["Número", subject.numeroSet ?? "—"],
    ["Rareza", subject.rareza ?? "—"],
    ["Tipo", data.carta?.tipo ?? "—"],
    ["Color", data.carta?.color ?? "—"],
    ["Estado", data.estadoConservacion ?? "No indicado"],
    ["Publicada", formatDate(data.createdAt)],
  ];

  return (
    <section className="shell py-8 sm:py-12">
      <nav aria-label="Migas" className="text-sm text-muted">
        <Link to="/catalogo" className="hover:text-ink">
          Catálogo
        </Link>
        <span className="mx-2">/</span>
        <span className="text-ink">{subject.nombre}</span>
      </nav>

      <div className="mt-6 grid gap-10 md:grid-cols-[minmax(0,360px)_1fr] md:gap-14">
        <div>
          <CardFace card={subject} />
          <div className="mt-4 flex items-center justify-between border-t border-rule pt-3">
            <div>
              <p className="text-xs text-muted">Valor de referencia</p>
              <p className="tabnum text-xl font-semibold">{formatEuro(data.precioUnitario)}</p>
            </div>
            <div className="text-right">
              <p className="text-xs text-muted">Disponibles</p>
              <p className="tabnum text-xl font-semibold">{data.stock}</p>
            </div>
          </div>
          <p className="hint mt-2">
            El valor solo sirve para equilibrar el trueque. Aquí no se paga dinero.
          </p>
        </div>

        <div>
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h1 className="text-[clamp(2.2rem,5vw,3.6rem)]">{subject.nombre}</h1>
              <p className="mt-2 text-sm text-muted">
                {[subject.setNombre, subject.numeroSet, subject.rareza].filter(Boolean).join(" · ") ||
                  "Sin datos de set"}
              </p>
            </div>
            <StatusBadge estado={data.estadoPublicacion} />
          </div>

          <p className="mt-5 flex flex-wrap items-center gap-2 text-sm">
            <span className="text-muted">Publicada por</span>
            <span className="font-semibold">@{data.username}</span>
          </p>

          {data.carta?.textoCarta ? (
            <p className="mt-5 max-w-xl text-sm leading-relaxed text-muted">{data.carta.textoCarta}</p>
          ) : null}

          <dl className="mt-8 grid grid-cols-1 gap-x-8 sm:grid-cols-2">
            {rows.map(([label, value]) => (
              <div key={label} className="flex items-baseline justify-between gap-4 border-t border-rule py-2.5">
                <dt className="text-sm text-muted">{label}</dt>
                <dd className="text-sm font-medium">{value}</dd>
              </div>
            ))}
          </dl>

          <div className="mt-8 border border-rule p-5">
            {isMine ? (
              <>
                <p className="text-sm font-semibold">Esta publicación es tuya</p>
                <p className="hint mt-1">
                  Puedes ocultarla o cambiar su estado desde tu colección.
                </p>
                <Link to="/coleccion" className="btn btn-outline mt-4 w-full sm:w-auto">
                  Ir a mi colección
                </Link>
              </>
            ) : (
              <>
                <p className="text-sm font-semibold">¿Te sirve esta carta?</p>
                <p className="hint mt-1">
                  Elige qué ofreces de tu colección y envía la propuesta a @{data.username}.
                </p>
                <Link to={ctaHref} className="btn btn-signal mt-4 w-full sm:w-auto">
                  Proponer intercambio
                </Link>
                {!user ? (
                  <p className="hint mt-2">
                    Te llevamos a iniciar sesión y volvemos aquí con la propuesta lista.
                  </p>
                ) : null}
              </>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
