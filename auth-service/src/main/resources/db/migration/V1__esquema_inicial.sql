-- V1: esquema inicial que ya existía en la base tcg_db (creado originalmente en pgAdmin).
-- En bases nuevas se ejecuta completa. En la base actual, Flyway la marca como aplicada
-- (baseline-version = 1) y salta directamente a V2.

CREATE TABLE usuarios (
    id             bigserial PRIMARY KEY,
    username       varchar(50)  NOT NULL,
    email          varchar(100) NOT NULL,
    password_hash  varchar(255) NOT NULL,
    rol            varchar(20)  NOT NULL DEFAULT 'CLIENTE',
    saldo_simulado numeric      NOT NULL DEFAULT 0.00,
    created_at     timestamptz  DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT usuarios_username_key UNIQUE (username),
    CONSTRAINT usuarios_email_key UNIQUE (email)
);

CREATE TABLE juegos_tcg (
    id          serial PRIMARY KEY,
    nombre      varchar(50) NOT NULL,
    descripcion text,
    CONSTRAINT juegos_tcg_nombre_key UNIQUE (nombre)
);

CREATE TABLE cartas_catalogo (
    id             bigserial PRIMARY KEY,
    juego_id       integer      NOT NULL REFERENCES juegos_tcg (id) ON DELETE CASCADE,
    nombre         varchar(100) NOT NULL,
    numero_set     varchar(30)  NOT NULL,
    set_nombre     varchar(100) NOT NULL,
    rareza         varchar(20)  NOT NULL,
    tipo           varchar(50),
    color          varchar(30),
    texto_carta    text,
    imagen_url     text,
    metadata_extra jsonb
);

CREATE INDEX idx_cartas_nombre ON cartas_catalogo (nombre);
CREATE INDEX idx_cartas_set ON cartas_catalogo (numero_set);

CREATE TABLE producto_sellado_catalogo (
    id                bigserial PRIMARY KEY,
    juego_id          integer      NOT NULL REFERENCES juegos_tcg (id) ON DELETE CASCADE,
    nombre            varchar(150) NOT NULL,
    tipo_producto     varchar(50)  NOT NULL,
    set_coleccion     varchar(100),
    precio_referencia numeric      NOT NULL DEFAULT 0.00,
    imagen_url        text
);

CREATE TABLE publicacion_inventario (
    id                  bigserial PRIMARY KEY,
    usuario_id          bigint       NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    carta_id            bigint       REFERENCES cartas_catalogo (id) ON DELETE CASCADE,
    producto_id         bigint       REFERENCES producto_sellado_catalogo (id) ON DELETE CASCADE,
    tipo_item           varchar(20)  NOT NULL,
    estado_conservacion varchar(30)  DEFAULT 'Mint',
    precio_unitario     numeric      NOT NULL,
    stock               integer      NOT NULL,
    estado_publicacion  varchar(30)  NOT NULL DEFAULT 'DISPONIBLE',
    created_at          timestamptz  DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tipo_item CHECK (
        (tipo_item = 'CARTA' AND carta_id IS NOT NULL AND producto_id IS NULL)
        OR (tipo_item = 'PRODUCTO' AND producto_id IS NOT NULL AND carta_id IS NULL)
    ),
    CONSTRAINT publicacion_inventario_stock_check CHECK (stock >= 0)
);

CREATE INDEX idx_publicacion_estado ON publicacion_inventario (estado_publicacion);

CREATE TABLE propuesta_trade (
    id                  bigserial PRIMARY KEY,
    usuario_emisor_id   bigint      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    usuario_receptor_id bigint      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    estado              varchar(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at          timestamptz DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trade_estado ON propuesta_trade (estado);

CREATE TABLE item_trade (
    id             bigserial PRIMARY KEY,
    propuesta_id   bigint      NOT NULL REFERENCES propuesta_trade (id) ON DELETE CASCADE,
    publicacion_id bigint      NOT NULL REFERENCES publicacion_inventario (id) ON DELETE CASCADE,
    rol_item       varchar(20) NOT NULL
);

CREATE TABLE mensaje_chat (
    id           bigserial PRIMARY KEY,
    propuesta_id bigint      NOT NULL REFERENCES propuesta_trade (id) ON DELETE CASCADE,
    emisor_id    bigint      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    contenido    text        NOT NULL,
    created_at   timestamptz DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ordenes (
    id           bigserial PRIMARY KEY,
    comprador_id bigint      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    total        numeric     NOT NULL,
    estado       varchar(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at   timestamptz DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_orden (
    id             bigserial PRIMARY KEY,
    orden_id       bigint  NOT NULL REFERENCES ordenes (id) ON DELETE CASCADE,
    publicacion_id bigint  NOT NULL REFERENCES publicacion_inventario (id) ON DELETE RESTRICT,
    cantidad       integer NOT NULL,
    precio_unitario numeric NOT NULL,
    CONSTRAINT detalle_orden_cantidad_check CHECK (cantidad > 0)
);

-- Tabla suelta del primer borrador. Se elimina en V2.
CREATE TABLE cartas (
    id      bigint       NOT NULL,
    nombre  varchar(255) NOT NULL,
    precio  double precision NOT NULL,
    rareza  varchar(255) NOT NULL,
    stock   integer      NOT NULL,
    juego   varchar(255),
    CONSTRAINT cartas_pkey PRIMARY KEY (id)
);
