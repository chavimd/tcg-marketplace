-- ============================================================================
-- Datos de demostración de Sobrecambio.
--
-- Este script NO es una migración Flyway. Es un reset de la base para demo:
-- borra trueques, mensajes, publicaciones, catálogo y los usuarios sembrados,
-- y vuelve a crear un catálogo creíble con tres juegos.
--
--   psql -h localhost -U postgres -d tcg_db -f db/seed.sql
--
-- Usuarios demo (contraseña: password123):
--   demo  -> ADMIN
--   bob   -> CLIENTE
--   carol -> CLIENTE
--   dana  -> CLIENTE
-- ============================================================================

SET client_encoding = 'UTF8';

BEGIN;

TRUNCATE TABLE mensaje_chat, item_trade, propuesta_trade, publicacion_inventario,
    cartas_catalogo, juegos_tcg
    RESTART IDENTITY CASCADE;

DELETE FROM usuarios WHERE username IN ('demo', 'bob', 'carol', 'dana');

-- Hash BCrypt de "password123". Los cuatro usuarios comparten la misma clave.
INSERT INTO usuarios (username, email, password_hash, rol) VALUES
    ('demo',  'demo@example.com',
     '$2a$10$aF5GEsnCLlHVzvsoT9y94uUrYIMcueCX2Y1429sKfbiFXa7RRcBzO', 'ADMIN'),
    ('bob',   'bob@example.com',
     '$2a$10$aF5GEsnCLlHVzvsoT9y94uUrYIMcueCX2Y1429sKfbiFXa7RRcBzO', 'CLIENTE'),
    ('carol', 'carol@example.com',
     '$2a$10$aF5GEsnCLlHVzvsoT9y94uUrYIMcueCX2Y1429sKfbiFXa7RRcBzO', 'CLIENTE'),
    ('dana',  'dana@example.com',
     '$2a$10$aF5GEsnCLlHVzvsoT9y94uUrYIMcueCX2Y1429sKfbiFXa7RRcBzO', 'CLIENTE')
ON CONFLICT (username) DO UPDATE SET
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    rol = EXCLUDED.rol;

INSERT INTO juegos_tcg (nombre, descripcion) VALUES
    ('Pokemon', 'Cartas coleccionables de Pokemon. Desde Base Set hasta las ilustraciones alternativas más buscadas.'),
    ('Yu-Gi-Oh!', 'Duelos, monstruos icónicos y rarezas clásicas de Konami.'),
    ('Magic', 'El juego de cartas coleccionables original: tierras, hechizos y planeswalkers.')
ON CONFLICT (nombre) DO UPDATE SET descripcion = EXCLUDED.descripcion;

INSERT INTO cartas_catalogo
    (juego_id, nombre, numero_set, set_nombre, rareza, tipo, color, texto_carta)
SELECT j.id, v.nombre, v.numero_set, v.set_nombre, v.rareza, v.tipo, v.color, v.texto_carta
FROM (VALUES
    -- Pokemon
    ('Pokemon', 'Charizard', '4/102', 'Base Set', 'Holo Rare', 'Fire', 'Rojo',
     'Ataca con Lanzallamas y arrasa el banquillo rival.'),
    ('Pokemon', 'Blastoise', '2/102', 'Base Set', 'Holo Rare', 'Water', 'Azul',
     'Cañones de agua a presión sobre el tablero.'),
    ('Pokemon', 'Pikachu', '58/102', 'Base Set', 'Common', 'Lightning', 'Amarillo',
     'El ratón eléctrico más reconocible del juego.'),
    ('Pokemon', 'Mewtwo', '10/102', 'Base Set', 'Holo Rare', 'Psychic', 'Morado',
     'Poder psíquico puro, sin evolución.'),
    ('Pokemon', 'Umbreon VMAX', '215/203', 'Evolving Skies', 'Secret Rare', 'Darkness', 'Negro',
     'Ilustración alternativa muy perseguida por coleccionistas.'),
    ('Pokemon', 'Rayquaza VMAX', '218/203', 'Evolving Skies', 'Secret Rare', 'Dragon', 'Verde',
     'Dragón legendario en su versión más codiciada.'),

    -- Yu-Gi-Oh!
    ('Yu-Gi-Oh!', 'Blue-Eyes White Dragon', 'LOB-001', 'Legend of Blue Eyes', 'Ultra Rare', 'Dragon', 'Blanco',
     'El dragón blanco de ojos azules, icono del juego.'),
    ('Yu-Gi-Oh!', 'Dark Magician', 'LOB-005', 'Legend of Blue Eyes', 'Ultra Rare', 'Spellcaster', 'Morado',
     'El mago definitivo en ataque y defensa.'),
    ('Yu-Gi-Oh!', 'Red-Eyes Black Dragon', 'LOB-070', 'Legend of Blue Eyes', 'Ultra Rare', 'Dragon', 'Negro',
     'Un dragón agresivo con mucho potencial.'),
    ('Yu-Gi-Oh!', 'Exodia the Forbidden One', 'LOB-124', 'Legend of Blue Eyes', 'Ultra Rare', 'Spellcaster', 'Dorado',
     'Una de las cinco piezas prohibidas.'),
    ('Yu-Gi-Oh!', 'Dark Magician Girl', 'MFC-000', 'Magician''s Force', 'Secret Rare', 'Spellcaster', 'Rosa',
     'Aprende de cada mago oscuro en el cementerio.'),
    ('Yu-Gi-Oh!', 'Elemental HERO Neos', 'POTD-EN001', 'Power of the Duelist', 'Super Rare', 'Warrior', 'Blanco',
     'Héroe elemental que llega desde el espacio.'),

    -- Magic
    ('Magic', 'Black Lotus', '232', 'Alpha', 'Mythic Rare', 'Artifact', 'Incoloro',
     'La carta más famosa del juego. Valor de referencia, no venta.'),
    ('Magic', 'Lightning Bolt', '161', 'Alpha', 'Common', 'Instant', 'Rojo',
     'Tres daños por un maná rojo.'),
    ('Magic', 'Counterspell', '54', 'Alpha', 'Common', 'Instant', 'Azul',
     'Contrarresta el hechizo objetivo.'),
    ('Magic', 'Shivan Dragon', '171', 'Alpha', 'Rare', 'Creature', 'Rojo',
     'Dragón clásico con vuelo y crecimiento de fuerza.'),
    ('Magic', 'Ragavan, Nimble Pilferer', '138', 'Modern Horizons 2', 'Mythic Rare', 'Creature', 'Rojo',
     'Mono travieso que acelera el maná.'),
    ('Magic', 'Wrenn and Six', '217', 'Modern Horizons', 'Mythic Rare', 'Planeswalker', 'Verde',
     'Planeswalker de dos manás con retorno de tierras.')
) AS v(juego, nombre, numero_set, set_nombre, rareza, tipo, color, texto_carta)
JOIN juegos_tcg j ON j.nombre = v.juego
ON CONFLICT (juego_id, numero_set, nombre) DO UPDATE SET
    set_nombre = EXCLUDED.set_nombre,
    rareza = EXCLUDED.rareza,
    tipo = EXCLUDED.tipo,
    color = EXCLUDED.color,
    texto_carta = EXCLUDED.texto_carta;

-- Inventario público. Los precios son "valor de referencia" para el trueque.
INSERT INTO publicacion_inventario
    (usuario_id, carta_id, tipo_item, estado_conservacion, precio_unitario, stock, estado_publicacion)
SELECT u.id, c.id, 'CARTA', v.estado_conservacion, v.precio, v.stock, v.estado
FROM (VALUES
    ('bob',   'Charizard',              '4/102',   'Base Set',           'Near Mint',     2400.00, 1, 'DISPONIBLE'),
    ('bob',   'Pikachu',                '58/102',  'Base Set',           'Lightly Played', 180.00, 3, 'DISPONIBLE'),
    ('bob',   'Blastoise',              '2/102',   'Base Set',           'Near Mint',     1450.00, 1, 'DISPONIBLE'),
    ('bob',   'Black Lotus',            '232',     'Alpha',              'Played',      690000.00, 1, 'DISPONIBLE'),

    ('carol', 'Umbreon VMAX',           '215/203', 'Evolving Skies',     'Near Mint',     5500.00, 1, 'DISPONIBLE'),
    ('carol', 'Rayquaza VMAX',          '218/203', 'Evolving Skies',     'Near Mint',     3800.00, 1, 'DISPONIBLE'),
    ('carol', 'Dark Magician',          'LOB-005', 'Legend of Blue Eyes','Near Mint',      700.00, 2, 'DISPONIBLE'),
    ('carol', 'Blue-Eyes White Dragon', 'LOB-001', 'Legend of Blue Eyes','Lightly Played', 950.00, 1, 'DISPONIBLE'),

    ('dana',  'Dark Magician Girl',     'MFC-000', 'Magician''s Force',  'Near Mint',     2200.00, 1, 'DISPONIBLE'),
    ('dana',  'Exodia the Forbidden One','LOB-124','Legend of Blue Eyes','Played',         850.00, 1, 'DISPONIBLE'),
    ('dana',  'Ragavan, Nimble Pilferer','138',    'Modern Horizons 2',  'Near Mint',      600.00, 2, 'DISPONIBLE'),
    ('dana',  'Wrenn and Six',          '217',     'Modern Horizons',    'Near Mint',      750.00, 1, 'DISPONIBLE'),
    ('dana',  'Mewtwo',                 '10/102',  'Base Set',           'Near Mint',     1000.00, 1, 'DISPONIBLE'),

    ('demo',  'Lightning Bolt',         '161',     'Alpha',              'Near Mint',     3200.00, 4, 'DISPONIBLE'),
    ('demo',  'Counterspell',           '54',      'Alpha',              'Lightly Played', 1200.00, 6, 'OCULTA')
) AS v(username, nombre, numero_set, set_nombre, estado_conservacion, precio, stock, estado)
JOIN usuarios u ON u.username = v.username
JOIN cartas_catalogo c
    ON c.nombre = v.nombre AND c.numero_set = v.numero_set AND c.set_nombre = v.set_nombre;

-- Un trueque pendiente de ejemplo: bob ofrece un Pikachu por el Dark Magician de carol.
INSERT INTO propuesta_trade (usuario_emisor_id, usuario_receptor_id, estado, created_at)
SELECT e.id, r.id, 'PENDIENTE', now() - interval '2 hours'
FROM usuarios e, usuarios r
WHERE e.username = 'bob' AND r.username = 'carol';

INSERT INTO item_trade (propuesta_id, publicacion_id, rol_item, cantidad)
SELECT (SELECT max(id) FROM propuesta_trade), p.id, 'OFERTA', 1
FROM publicacion_inventario p
WHERE p.usuario_id = (SELECT id FROM usuarios WHERE username = 'bob')
  AND p.carta_id = (SELECT id FROM cartas_catalogo WHERE nombre = 'Pikachu' AND numero_set = '58/102');

INSERT INTO item_trade (propuesta_id, publicacion_id, rol_item, cantidad)
SELECT (SELECT max(id) FROM propuesta_trade), p.id, 'SOLICITUD', 1
FROM publicacion_inventario p
WHERE p.usuario_id = (SELECT id FROM usuarios WHERE username = 'carol')
  AND p.carta_id = (SELECT id FROM cartas_catalogo WHERE nombre = 'Dark Magician' AND numero_set = 'LOB-005');

INSERT INTO mensaje_chat (propuesta_id, emisor_id, contenido, created_at)
SELECT (SELECT max(id) FROM propuesta_trade),
       (SELECT id FROM usuarios WHERE username = 'bob'),
       'Hola, ¿te interesa cambiar tu Dark Magician por mi Pikachu?',
       now() - interval '1 hour';

COMMIT;
