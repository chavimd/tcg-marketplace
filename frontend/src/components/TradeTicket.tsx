import type { Publicacion } from "../lib/types";
import { CardFace } from "./CardFace";
import { publicationSubject } from "./PublicationCard";

interface TradeTicketProps {
  ofertas: Publicacion[];
  solicitudes: Publicacion[];
  folio?: string;
}

/**
 * The signature object of the product: a swap ticket with a perforated seam and a stamp.
 * Reused in the hero and in the trade detail so the metaphor stays consistent.
 */
export function TradeTicket({ ofertas, solicitudes, folio = "0042" }: TradeTicketProps) {
  return (
    <div className="ticket w-full" style={{ ["--ticket-notch" as string]: "var(--color-ink)" }}>
      <div className="flex items-center justify-between border-b border-rule px-4 py-2.5">
        <span className="text-[0.6875rem] font-semibold text-muted">Boleto de trueque</span>
        <span className="tabnum text-[0.6875rem] text-muted">Nº {folio}</span>
      </div>

      <div className="relative grid grid-cols-2">
        <span className="ticket__notch" style={{ top: 0 }} aria-hidden="true" />
        <span className="ticket__notch" style={{ top: "100%" }} aria-hidden="true" />

        <div className="p-4 pr-3">
          <p className="display text-2xl">Ofreces</p>
          <ul className="mt-3 space-y-3">
            {(ofertas.length ? ofertas : [null, null]).slice(0, 2).map((publicacion, index) => (
              <li key={publicacion?.id ?? `oferta-${index}`} className="flex items-center gap-2.5">
                {publicacion ? (
                  <CardFace card={publicationSubject(publicacion)} className="w-10 shrink-0" variant="mini" />
                ) : (
                  <div className="aspect-[3/4] w-10 shrink-0 rounded border border-dashed border-rule" />
                )}
                <div className="min-w-0">
                  <p className="truncate text-[0.8125rem] font-medium">
                    {publicacion ? publicationSubject(publicacion).nombre : "Tu carta"}
                  </p>
                  <p className="truncate text-[0.6875rem] text-muted">
                    {publicacion ? (publicationSubject(publicacion).setNombre ?? "—") : "Sin elegir"}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="ticket__seam p-4 pl-3">
          <p className="display text-2xl">Pides</p>
          <ul className="mt-3 space-y-3">
            {(solicitudes.length ? solicitudes : [null, null]).slice(0, 2).map((publicacion, index) => (
              <li key={publicacion?.id ?? `solicitud-${index}`} className="flex items-center gap-2.5">
                {publicacion ? (
                  <CardFace card={publicationSubject(publicacion)} className="w-10 shrink-0" variant="mini" />
                ) : (
                  <div className="aspect-[3/4] w-10 shrink-0 rounded border border-dashed border-rule" />
                )}
                <div className="min-w-0">
                  <p className="truncate text-[0.8125rem] font-medium">
                    {publicacion ? publicationSubject(publicacion).nombre : "Su carta"}
                  </p>
                  <p className="truncate text-[0.6875rem] text-muted">
                    {publicacion ? (publicationSubject(publicacion).setNombre ?? "—") : "Sin elegir"}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="flex items-center justify-between gap-3 border-t border-rule px-4 py-3">
        <span className="stamp">Trueque abierto</span>
        <span className="text-right text-[0.6875rem] leading-tight text-muted">
          Sin pagos
          <br />
          Sin saldo
        </span>
      </div>
    </div>
  );
}
