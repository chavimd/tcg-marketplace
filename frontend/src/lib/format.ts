const dateFormatter = new Intl.DateTimeFormat("es-ES", {
  day: "2-digit",
  month: "short",
  year: "numeric",
});

const dateTimeFormatter = new Intl.DateTimeFormat("es-ES", {
  day: "2-digit",
  month: "short",
  hour: "2-digit",
  minute: "2-digit",
});

const euroFormatter = new Intl.NumberFormat("es-ES", {
  style: "currency",
  currency: "EUR",
});

export function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : dateFormatter.format(date);
}

export function formatDateTime(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : dateTimeFormatter.format(date);
}

export function formatEuro(value: number): string {
  return euroFormatter.format(value);
}

/** Stable slug used to pick a per-game accent in the card face. */
export function gameSlug(name: string | null | undefined): string {
  if (!name) return "otro";
  return name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export function gameGlyph(name: string | null | undefined): string {
  if (!name) return "?";
  const clean = name.trim();
  return clean ? clean.charAt(0).toUpperCase() : "?";
}

export function estadoLabel(estado: string): string {
  switch (estado) {
    case "PENDIENTE":
      return "Pendiente";
    case "ACEPTADA":
      return "Aceptada";
    case "RECHAZADA":
      return "Rechazada";
    case "CANCELADA":
      return "Cancelada";
    case "DISPONIBLE":
      return "Disponible";
    case "RESERVADA":
      return "Reservada";
    case "OCULTA":
      return "Oculta";
    default:
      return estado;
  }
}

export function plural(count: number, one: string, many: string): string {
  return count === 1 ? one : many;
}
