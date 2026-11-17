-- V3: Tienda oficial de productos sellados (booster boxes, sobres, bundles, mystery boxes) y órdenes.

UPDATE producto_sellado_catalogo
SET precio_referencia = 1.00
WHERE precio_referencia <= 0;

ALTER TABLE producto_sellado_catalogo
    ADD COLUMN descripcion text,
    ADD COLUMN idioma varchar(10) NOT NULL DEFAULT 'ES',
    ADD COLUMN stock integer NOT NULL DEFAULT 0,
    ADD COLUMN destacado boolean NOT NULL DEFAULT false,
    ADD COLUMN activo boolean NOT NULL DEFAULT true,
    ADD COLUMN created_at timestamptz DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE producto_sellado_catalogo
    ADD CONSTRAINT chk_producto_sellado_stock CHECK (stock >= 0),
    ADD CONSTRAINT chk_producto_sellado_precio CHECK (precio_referencia > 0),
    ADD CONSTRAINT chk_producto_sellado_tipo CHECK (
        tipo_producto IN ('BOOSTER_BOX', 'HALF_BOOSTER_BOX', 'SOBRE', 'BUNDLE', 'MYSTERY_BOX')
    ),
    ADD CONSTRAINT chk_producto_sellado_idioma CHECK (idioma IN ('ES', 'EN', 'JP')),
    ADD CONSTRAINT uk_producto_sellado_juego_nombre UNIQUE (juego_id, nombre);

CREATE INDEX idx_producto_sellado_tipo ON producto_sellado_catalogo (tipo_producto, juego_id);
CREATE INDEX idx_producto_sellado_activo ON producto_sellado_catalogo (activo, stock);

ALTER TABLE ordenes
    ADD COLUMN metodo_pago varchar(40) NOT NULL DEFAULT 'QR_BOLIVIA',
    ADD COLUMN direccion_envio varchar(255),
    ADD COLUMN notas text;

ALTER TABLE ordenes
    ADD CONSTRAINT chk_ordenes_estado CHECK (estado IN ('PENDIENTE', 'PAGADA', 'ENVIADA', 'CANCELADA')),
    ADD CONSTRAINT chk_ordenes_total CHECK (total > 0),
    ADD CONSTRAINT chk_ordenes_metodo_pago CHECK (
        metodo_pago IN ('QR_BOLIVIA', 'TRANSFERENCIA', 'CONTRAENTREGA')
    );

ALTER TABLE detalle_orden
    ALTER COLUMN publicacion_id DROP NOT NULL;

ALTER TABLE detalle_orden
    ADD COLUMN producto_id bigint REFERENCES producto_sellado_catalogo (id) ON DELETE RESTRICT,
    ADD COLUMN nombre_snapshot varchar(180) NOT NULL DEFAULT '';

ALTER TABLE detalle_orden
    ADD CONSTRAINT chk_detalle_orden_item CHECK (publicacion_id IS NOT NULL OR producto_id IS NOT NULL);

CREATE INDEX idx_ordenes_comprador ON ordenes (comprador_id, created_at DESC);
CREATE INDEX idx_detalle_orden_orden ON detalle_orden (orden_id);
