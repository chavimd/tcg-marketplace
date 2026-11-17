import { Link } from "react-router-dom";
import { formatBs } from "../lib/format";
import type { Publicacion } from "../lib/types";
import { CardFace, type CardSubject } from "./CardFace";

export function publicationSubject(publicacion: Publicacion): CardSubject {
  const carta = publicacion.carta;
  return {
    nombre: carta?.nombre ?? "Carta sin catalogar",
    setNombre: carta?.setNombre ?? null,
    numeroSet: carta?.numeroSet ?? null,
    rareza: carta?.rareza ?? null,
    juego: carta?.juego ?? null,
    imagenUrl: carta?.imagenUrl ?? null,
  };
}

export function PublicationCard({ publicacion }: { publicacion: Publicacion }) {
  const subject = publicationSubject(publicacion);
  const href = `/catalogo/${publicacion.id}`;

  return (
    <article className="group border-t border-rule pt-3">
      <Link to={href} className="block" aria-label={`Ver ${subject.nombre}`}>
        <CardFace
          card={subject}
          className="transition-transform duration-200 motion-safe:group-hover:-translate-y-0.5"
        />
      </Link>
      {/* The face already prints name and set; below it we only bill the facts. */}
      <h3 className="sr-only">
        <Link to={href}>{subject.nombre}</Link>
      </h3>
      <div className="mt-3 flex items-baseline justify-between gap-3 border-t border-rule pt-2 text-[0.8125rem]">
        <span className="truncate text-muted">
          @{publicacion.username}
          {publicacion.estadoConservacion ? ` · ${publicacion.estadoConservacion}` : ""}
        </span>
        <span className="tabnum shrink-0 font-semibold">{publicacion.stock} ud.</span>
      </div>
      <div className="mt-1 flex items-baseline justify-between gap-3 text-[0.8125rem] text-muted">
        <span>Valor ref.</span>
        <span className="tabnum shrink-0">{formatBs(publicacion.precioUnitario)}</span>
      </div>
    </article>
  );
}
