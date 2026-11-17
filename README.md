# TCG Marketplace — mercado de intercambios

Marketplace de cartas coleccionables de varias franquicias (Pokémon, Yu-Gi-Oh!, Magic…),
centrado en **intercambios entre usuarios**. No incluye ventas ni saldo simulado.

El backend son cinco microservicios Spring Boot que comparten una única base PostgreSQL
(`tcg_db`). Cada servicio es independiente y se comunica a través del gateway.

## Requisitos

- Java 21
- PostgreSQL 16 (base `tcg_db`)
- Un `JWT_SECRET` de al menos 32 bytes, idéntico en todos los servicios

## Servicios

| Servicio | Puerto | Responsabilidad |
|---|---:|---|
| `api-gateway` | 8080 | Punto de entrada HTTP y WebSocket, validación de JWT |
| `catalogo-service` | 8081 | Juegos, catálogo de cartas, publicaciones e inventario |
| `auth-service` | 8082 | Registro, login, JWT y **dueño de las migraciones Flyway** |
| `trade-service` | 8083 | Propuestas de intercambio y transferencia de stock |
| `chat-service` | 8084 | Historial y mensajería STOMP de cada trueque |

`frontend/` es la aplicación web (React + Vite) que consume el gateway. Interfaz en
español, centrada en el intercambio: catálogo, colección, propuestas y chat.

## Base de datos

Base: `tcg_db`. El esquema lo controla **Flyway** desde `auth-service`:

| Migración | Contenido |
|---|---|
| `V1__esquema_inicial.sql` | Esquema base (`usuarios`, `juegos_tcg`, `cartas_catalogo`, `publicacion_inventario`, `propuesta_trade`, `item_trade`, `mensaje_chat`, …) |
| `V2__ajustes_trueques.sql` | Elimina `cartas`, añade `item_trade.cantidad`, elimina el saldo del trueque y valida estados |

Los demás servicios arrancan con `ddl-auto: validate`: **nunca crean ni modifican tablas**.
Para reconstruir la base desde cero basta con crear una base vacía y arrancar `auth-service`.

### Estados y convenciones

- `propuesta_trade.estado`: `PENDIENTE`, `ACEPTADA`, `RECHAZADA`, `CANCELADA`
- `item_trade.rol_item`: `OFERTA`, `SOLICITUD`
- `publicacion_inventario.estado_publicacion`: `DISPONIBLE`, `RESERVADA`, `OCULTA`
- `publicacion_inventario.precio_unitario` debe ser **mayor que 0**
- Cada línea de trueque tiene `cantidad` (entero positivo)

## Configuración

Todas las variables son opcionales en desarrollo, pero se recomienda usar un `.env.local`
(ignorado por Git) con:

| Variable | Ejemplo |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `tcg_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | tu contraseña |
| `JWT_SECRET` | cadena aleatoria de 32+ bytes |
| `FRONTEND_ORIGINS` | `http://localhost:5500,http://localhost:5173` |
| `WEBSOCKET_ALLOWED_ORIGINS` | mismos orígenes |

## Arranque local

Carga las variables y arranca cada servicio en su propia terminal:

```powershell
Get-Content .env.local | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') { Set-Item -Path "env:$($matches[1].Trim())" -Value $matches[2].Trim() }
}

# una terminal por servicio
.\mvnw.cmd spring-boot:run   # ejecutado dentro de cada carpeta
```

Comprueba el estado con `GET http://localhost:<puerto>/actuator/health`.

## Frontend

`frontend/` es una SPA con **React 19 + Vite + TypeScript + Tailwind CSS v4**.
Vite hace proxy de `/api` y `/ws` hacia `localhost:8080`, así que en desarrollo no hace
falta configurar CORS ni URLs absolutas.

```powershell
cd frontend
npm install
npm run dev      # http://localhost:5173
npm run build    # typecheck + build de producción
```

Variables opcionales (`.env.local` en `frontend/`):

| Variable | Uso |
|---|---|
| `VITE_API_BASE_URL` | URL absoluta del gateway si el front se sirve aparte |
| `VITE_WS_URL` | URL `ws(s)://` del chat si no se usa el proxy |

Rutas principales: `/` (inicio), `/catalogo`, `/catalogo/:id`, `/entrar`, `/registro`,
`/coleccion`, `/trueques`, `/trueques/nuevo?publicacion=<id>`, `/trueques/:id` y `/admin`
(solo ADMIN). El chat del trueque se abre por STOMP contra `/ws`.

Criterios de interfaz: el valor de una carta se muestra siempre como **valor de
referencia**; no hay precios de venta, carrito ni saldo.

## Endpoints

### Autenticación

- `POST /api/auth/register` — público
- `POST /api/auth/login` — público
- `GET /api/auth/me` — requiere token

### Catálogo y publicaciones

- `GET /api/games` — público
- `POST /api/games` — ADMIN
- `GET /api/cards?game=&rarity=&q=&page=&size=` — público
- `GET /api/cards/{id}` — público
- `POST /api/cards` — ADMIN
- `GET /api/publications?game=&rarity=&q=` — público; con token excluye tus propias publicaciones
- `GET /api/publications/{id}` — público
- `GET /api/inventory/me` — mis publicaciones
- `POST /api/inventory/grants` — ADMIN; asigna cartas a un usuario
- `PATCH /api/inventory/{id}/status` — `{"estado":"DISPONIBLE"}` o `{"estado":"OCULTA"}`

### Trueques

- `POST /api/trades` — crear propuesta
- `GET /api/trades` — bandeja del usuario
- `GET /api/trades/{id}` — detalle (solo participantes)
- `POST /api/trades/{id}/accept` — solo el receptor
- `POST /api/trades/{id}/reject` — solo el receptor
- `POST /api/trades/{id}/cancel` — solo el emisor

Ejemplo de propuesta:

```json
{
  "receptorUsername": "bob",
  "ofertas": [{ "publicacionId": 12, "cantidad": 1 }],
  "solicitudes": [{ "publicacionId": 27, "cantidad": 2 }]
}
```

Al aceptar, el servicio bloquea las publicaciones implicadas (`SELECT … FOR UPDATE`),
valida dueño, estado `DISPONIBLE` y stock, descuenta las unidades ofrecidas y las suma a la
publicación del receptor (creándola si no existe). Todo ocurre en una sola transacción.

### Chat

- `GET /api/trades/{id}/messages` — historial; solo participantes
- WebSocket `/ws`
  - `CONNECT` con cabecera `Authorization: Bearer <token>`
  - `SEND /app/trades/{id}/messages` con `{"content":"..."}`
  - `SUBSCRIBE /user/queue/trades/{id}`

Cada mensaje se guarda en `mensaje_chat` antes de emitirse, por lo que el historial
sobrevive a reconexiones.

## Pruebas

```powershell
.\mvnw.cmd test    # dentro de cada servicio
```

| Servicio | Tests |
|---|---:|
| `auth-service` | 6 |
| `catalogo-service` | 1 |
| `trade-service` | 6 |
| `chat-service` | 3 |
| `api-gateway` | 7 |

## Datos de demostración

`db/seed.sql` reconstruye un catálogo de tres juegos y un trueque pendiente de ejemplo.
Es un reset de demo, **no** una migración: borra publicaciones, trueques, mensajes,
catálogo y los usuarios sembrados (no toca otras cuentas).

| Usuario | Contraseña | Rol |
|---|---|---|
| `demo` | `password123` | ADMIN |
| `bob` | `password123` | CLIENTE |
| `carol` | `password123` | CLIENTE |
| `dana` | `password123` | CLIENTE |

```powershell
psql -h localhost -U postgres -d tcg_db -f db/seed.sql
```

## Docker Compose

Levanta PostgreSQL 16, los cinco servicios, el frontend en nginx y un servicio `seed` de
una sola ejecución. Requiere `.env.local` en la raíz.

```powershell
docker compose --env-file .env.local up -d --build
docker compose --env-file .env.local --profile seed run --rm seed   # datos demo
```

- Frontend: `http://localhost:3000`
- Gateway: `http://localhost:8080`
- PostgreSQL: `localhost:5433` (para no chocar con el PostgreSQL local)
- El servicio `seed` no corre con `up`; hay que invocarlo explícitamente.

## Estado del proyecto

- [x] Fase 1: backend adaptado al esquema `tcg_db`, trueques completos y chat persistido
- [x] Fase 2: frontend React con catálogo, colección, trueques y chat en vivo
- [x] Datos de ejemplo (juegos, cartas y usuarios de demostración)
- [x] Docker Compose
- [x] Publicación en GitHub
