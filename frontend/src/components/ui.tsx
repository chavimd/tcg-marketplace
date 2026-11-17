import type { ReactNode } from "react";
import { estadoLabel } from "../lib/format";

export function StatusBadge({ estado }: { estado: string }) {
  const tone =
    estado === "PENDIENTE"
      ? "border-signal text-signal"
      : estado === "ACEPTADA" || estado === "DISPONIBLE"
        ? "border-ink text-ink"
        : "border-rule text-muted";

  return (
    <span className={`inline-flex items-center border px-1.5 py-0.5 text-[0.6875rem] font-semibold ${tone}`}>
      {estadoLabel(estado)}
    </span>
  );
}

export function Loading({ label = "Cargando" }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 py-10 text-sm text-muted" role="status">
      <span className="h-2 w-2 animate-pulse rounded-full bg-signal" aria-hidden="true" />
      {label}…
    </div>
  );
}

export function ErrorNote({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="border-l-2 border-signal bg-paper-2 px-4 py-3">
      <p className="text-sm">{message}</p>
      {onRetry ? (
        <button type="button" className="btn btn-outline btn-sm mt-3" onClick={onRetry}>
          Reintentar
        </button>
      ) : null}
    </div>
  );
}

export function Empty({
  title,
  children,
  action,
}: {
  title: string;
  children?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="border border-dashed border-rule px-6 py-12 text-center">
      <h3 className="text-2xl">{title}</h3>
      {children ? <p className="mx-auto mt-2 max-w-md text-sm text-muted">{children}</p> : null}
      {action ? <div className="mt-5 flex justify-center">{action}</div> : null}
    </div>
  );
}

export function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;
  return (
    <div className="mt-10 flex items-center justify-between border-t border-rule pt-4">
      <button
        type="button"
        className="btn btn-outline btn-sm"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Anterior
      </button>
      <span className="text-sm text-muted tabnum">
        Página {page + 1} de {totalPages}
      </span>
      <button
        type="button"
        className="btn btn-outline btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Siguiente
      </button>
    </div>
  );
}
