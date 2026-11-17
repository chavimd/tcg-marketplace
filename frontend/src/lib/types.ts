export type Rol = "USER" | "ADMIN";

export interface Juego {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface Carta {
  id: number;
  juegoId: number;
  juego: string;
  nombre: string;
  numeroSet: string | null;
  setNombre: string | null;
  rareza: string | null;
  tipo: string | null;
  color: string | null;
  textoCarta: string | null;
  imagenUrl: string | null;
  metadataExtra: Record<string, unknown> | null;
}

export type EstadoPublicacion = "DISPONIBLE" | "RESERVADA" | "OCULTA";

export interface Publicacion {
  id: number;
  usuarioId: number;
  username: string;
  tipoItem: string;
  carta: Carta | null;
  estadoConservacion: string | null;
  precioUnitario: number;
  stock: number;
  estadoPublicacion: EstadoPublicacion;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Usuario {
  id: number;
  username: string;
  email: string;
  rol: Rol;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: Usuario;
}

export type EstadoPropuesta = "PENDIENTE" | "ACEPTADA" | "RECHAZADA" | "CANCELADA";

export interface Participante {
  id: number;
  username: string;
}

export type RolItem = "OFERTA" | "SOLICITUD";

export interface ItemTrade {
  id: number;
  rol: RolItem;
  cantidad: number;
  publicacionId: number;
  cartaId: number | null;
  cartaNombre: string | null;
  numeroSet: string | null;
  setNombre: string | null;
  rareza: string | null;
  imagenUrl: string | null;
  juego: string | null;
  precioUnitario: number;
  estadoConservacion: string | null;
  usuarioId: number;
  username: string;
}

export interface Propuesta {
  id: number;
  estado: EstadoPropuesta;
  createdAt: string;
  emisor: Participante;
  receptor: Participante;
  items: ItemTrade[];
}

export interface MensajeChat {
  id: number;
  tradeId: number;
  senderUsername: string;
  content: string;
  sentAt: string;
}
