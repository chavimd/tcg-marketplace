# AGENTS.md

Marketplace de **intercambios** de cartas TCG (Pokémon, Yu-Gi-Oh!, Magic). No hay ventas,
carrito ni saldo: todo valor de carta se muestra como “valor de referencia”. Código,
comentarios y UI están en español; respetá los nombres de dominio existentes.

## Arquitectura

- Cinco módulos Spring Boot **independientes**, sin `pom.xml` padre ni agregador:
  `api-gateway` (8080), `catalogo-service` (8081), `auth-service` (8082),
  `trade-service` (8083), `chat-service` (8084). Todos comparten PostgreSQL `tcg_db`.
- `api-gateway` sigue en Spring Boot **3.5.15**; los otros cuatro en **4.1.1**. Java 21.
  Boot 4 renombró `server.error.include-message` a `spring.web.error.include-message`:
  no “unifiques” versiones ni propiedades sin probar.
- `frontend/`: React 19 + Vite + TypeScript + Tailwind v4. Vite (5173) hace proxy de
  `/api` y `/ws` a `localhost:8080`. No hay ESLint; el typecheck corre dentro de
  `npm run build`.

## Comandos

```powershell
# Backend: el wrapper NO está en la raíz. Entrá al módulo y ejecutá ahí.
cd trade-service; .\mvnw.cmd test
cd trade-service; .\mvnw.cmd test -Dtest=TradeServiceTests   # un solo test
cd api-gateway;   .\mvnw.cmd spring-boot:run

# Frontend
cd frontend; npm run dev
npm run typecheck   # tsc --noEmit
npm run build       # typecheck + build de producción
```

- No hay comando raíz para correr todo. Tests: auth 6, catálogo 1, trade 6, chat 3, gateway 7.
- Los tests **no requieren PostgreSQL**: auth/chat usan H2 (`src/test/resources/application.yml`);
  catálogo/trade/gateway son tests unitarios con Mockito.
- `.env.local` **no se carga solo**: Spring Boot no lee ese archivo. Hay que exportar las
  variables a mano (snippet en el README) o usar `docker compose --env-file .env.local`.

## Base de datos y reglas

- Flyway vive **solo** en `auth-service/src/main/resources/db/migration`. Los demás
  servicios arrancan con `ddl-auto: validate` y nunca crean tablas. Todo cambio de esquema
  es una migración `V*` nueva en ese directorio.
- Tablas heredadas sin uso: `ordenes`, `detalle_orden`, `producto_sellado_catalogo`,
  `usuarios.saldo_simulado`. No las uses para features nuevas.
- Estados e invariantes (hay CHECKs en la base):
  - `propuesta_trade.estado`: `PENDIENTE|ACEPTADA|RECHAZADA|CANCELADA`
  - `item_trade.rol_item`: `OFERTA|SOLICITUD`; `cantidad > 0`
  - `publicacion_inventario.estado_publicacion`: `DISPONIBLE|RESERVADA|OCULTA`
  - `publicacion_inventario.precio_unitario > 0`
- Aceptar un trueque transfiere stock con `SELECT … FOR UPDATE` en una sola transacción
  (`TradeService`). No modifiques stock ni publicaciones por fuera de ese flujo.
- Reglas de negocio: solo el receptor acepta/rechaza, solo el emisor cancela, y no se puede
  ofrecer y solicitar la misma `carta.id`.
- En JPQL con filtros opcionales se usan centinelas (`""`, `-1L`): Hibernate rechaza `null`.

## Gateway y seguridad

- **El orden de rutas importa**: `/api/trades/*/messages` (chat) va antes de
  `/api/trades/**` (trade). Cualquier subruta nueva bajo `/api/trades` necesita su ruta
  propia antes del catch-all.
- Cada `SecurityConfig` permite `DispatcherType.ERROR` y `/error`; sin eso los errores
  reales se convierten en 401/500 opacos. No lo quites.

## Datos de prueba y Docker

- `db/seed.sql` reconstruye el catálogo demo y los usuarios `demo` (ADMIN), `bob`, `carol`,
  `dana`, todos con contraseña `password123`. **Borra** trueques, mensajes, publicaciones,
  catálogo y esos usuarios (no toca otras cuentas). No es una migración.
- Local: `psql -h localhost -U postgres -d tcg_db -f db/seed.sql`.
- Docker: `docker compose --env-file .env.local up -d --build` no siembra nada. Los datos
  demo se cargan aparte con `docker compose --env-file .env.local --profile seed run --rm seed`
  (el servicio `seed` está en profile y nunca corre con `up`). El frontend queda en
  `http://localhost:3000` y el gateway en `8080`.
- La db del compose se publica en **5433** para no chocar con un PostgreSQL local en 5432.

## Frontend

- Los tokens de diseño están en `styles.css` (`@theme`). Reutilizá las clases existentes
  (`card-face`, `ticket`, `stamp`, `btn`, `field`) en vez de inventar estilos nuevos.
- Las cartas sin `imagen_url` muestran una placa grabada con el monograma del juego; no
  agregues placeholders de imágenes externas.
- Al proponer un trueque, el front excluye ofrecer la misma `carta.id` que se solicita
  (el backend también la rechaza).

## Verificación manual (gotchas)

- PowerShell 5.1: `curl.exe --data-binary '{"a":1}'` corrompe el JSON. Usá
  `Invoke-RestMethod -ContentType 'application/json'` o `fetch` desde Node.
- Chrome en Windows no baja de ~500 px de ancho: para QA móvil usá CDP
  `Emulation.setDeviceMetricsOverride`, no `--window-size`.
- En la DB el juego se llama `Pokemon` (sin tilde).
- Chat WS: STOMP crudo en `/ws`, header `Authorization: Bearer <token>` en `CONNECT`,
  `SUBSCRIBE /user/queue/trades/{id}`, `SEND /app/trades/{id}/messages`.
