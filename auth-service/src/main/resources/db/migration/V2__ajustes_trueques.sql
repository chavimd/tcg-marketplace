-- V2: ajustes para el alcance de trueques.
-- Decisiones acordadas: quitar saldo del trueque, eliminar la tabla cartas,
-- agregar cantidad por línea y validar los valores de estado.

DROP TABLE IF EXISTS cartas;

ALTER TABLE item_trade
    ADD COLUMN cantidad integer NOT NULL DEFAULT 1;

ALTER TABLE item_trade
    ADD CONSTRAINT item_trade_cantidad_check CHECK (cantidad > 0);

ALTER TABLE item_trade
    ADD CONSTRAINT uk_item_trade_propuesta_publicacion_rol
        UNIQUE (propuesta_id, publicacion_id, rol_item);

ALTER TABLE propuesta_trade
    ADD CONSTRAINT chk_propuesta_trade_estado
        CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CANCELADA'));

ALTER TABLE item_trade
    ADD CONSTRAINT chk_item_trade_rol
        CHECK (rol_item IN ('OFERTA', 'SOLICITUD'));

ALTER TABLE publicacion_inventario
    ADD CONSTRAINT chk_publicacion_estado
        CHECK (estado_publicacion IN ('DISPONIBLE', 'RESERVADA', 'OCULTA'));

ALTER TABLE publicacion_inventario
    ADD CONSTRAINT chk_publicacion_precio
        CHECK (precio_unitario > 0);

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_saldo
        CHECK (saldo_simulado >= 0);

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_rol
        CHECK (rol IN ('CLIENTE', 'ADMIN'));

ALTER TABLE cartas_catalogo
    ADD CONSTRAINT uk_cartas_catalogo_juego_set_nombre
        UNIQUE (juego_id, numero_set, nombre);

CREATE INDEX idx_publicacion_usuario_estado
    ON publicacion_inventario (usuario_id, estado_publicacion);

CREATE INDEX idx_item_trade_propuesta
    ON item_trade (propuesta_id);

CREATE INDEX idx_mensaje_chat_propuesta
    ON mensaje_chat (propuesta_id, created_at);
