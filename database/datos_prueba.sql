-- Datos de prueba del catalogo PediCafe.
-- Ejecutar en una sola sesion de MySQL Workbench con el schema pediCafe seleccionado.
-- La categoria se identifica por su nombre unico; el producto por (categoria, nombre).
-- Las comprobaciones NOT EXISTS hacen idempotentes las ejecuciones secuenciales: las filas
-- coincidentes se omiten y nunca se actualizan ni se eliminan. No ejecutar en paralelo.
-- Las imagenes se guardan como URLs porque Producto.imagenUrl se renderiza como th:src.

START TRANSACTION;

-- Categorias: uqCategoriaNombre impide nombres duplicados en la tabla.
INSERT INTO categoria (nombre, descripcion, activo)
SELECT datos.nombre, datos.descripcion, TRUE
FROM (
    SELECT 'Cafés calientes' AS nombre, 'Bebidas de café preparadas calientes.' AS descripcion
    UNION ALL SELECT 'Bebidas frías', 'Cafés y bebidas refrescantes servidas frías.'
    UNION ALL SELECT 'Tés e infusiones', 'Selección de tés e infusiones calientes.'
    UNION ALL SELECT 'Repostería', 'Panes dulces y repostería recién preparada.'
    UNION ALL SELECT 'Postres', 'Postres individuales para acompañar el café.'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM categoria existente WHERE existente.nombre = datos.nombre
);

-- Cafés calientes
INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Espresso', 'Café intenso de tueste medio, servido en una medida corta.', 1500.00,
       'https://images.unsplash.com/photo-1791669756517-f647abd7baef?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Cafés calientes'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Espresso');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Café negro', 'Café chorreado de la casa, aromático y equilibrado.', 1800.00,
       'https://images.unsplash.com/photo-1564347257970-97aff64f0fd6?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Cafés calientes'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Café negro');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Cappuccino', 'Espresso con leche vaporizada y una capa suave de espuma.', 2400.00,
       'https://images.unsplash.com/photo-1657657366123-b5bf60bc9ff4?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Cafés calientes'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Cappuccino');

-- Bebidas frías
INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Latte frío', 'Espresso con leche fría y hielo, de sabor suave y cremoso.', 2600.00,
       'https://images.unsplash.com/photo-1584286595398-a59f21d313f5?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Bebidas frías'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Latte frío');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Cold brew', 'Café de extracción lenta, servido frío con hielo.', 2500.00,
       'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Bebidas frías'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Cold brew');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Frappé de café', 'Bebida fría de café mezclada con hielo y leche.', 3200.00,
       'https://images.unsplash.com/photo-1769138885048-4f91ed2353a0?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Bebidas frías'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Frappé de café');

-- Tés e infusiones
INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Té verde', 'Té verde de aroma delicado, preparado al momento.', 1800.00,
       'https://images.unsplash.com/photo-1721434676608-23d5f3b9aefd?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Tés e infusiones'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Té verde');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Té de limón', 'Infusión de té con un toque cítrico de limón.', 1700.00,
       'https://images.unsplash.com/photo-1767324672682-e182981f62d4?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Tés e infusiones'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Té de limón');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Infusión relajante', 'Infusión caliente de hierbas, suave y aromática.', 1900.00,
       'https://images.unsplash.com/photo-1633945984522-a19268cc75ad?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Tés e infusiones'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Infusión relajante');

-- Repostería
INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Croissant', 'Croissant de masa hojaldrada, dorado y suave.', 1900.00,
       'https://images.unsplash.com/photo-1759566926618-163d14e00fc8?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Repostería'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Croissant');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Muffin de chocolate', 'Muffin horneado con trozos de chocolate.', 2100.00,
       'https://images.unsplash.com/photo-1682979021485-7b7bdb0ca4b9?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Repostería'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Muffin de chocolate');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Muffin de naranja', 'Muffin esponjoso con un ligero aroma a naranja.', 2000.00,
       'https://images.unsplash.com/photo-1475855841503-917d97ca77b4?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Repostería'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Muffin de naranja');

-- Postres
INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Queque de chocolate', 'Porción de queque húmedo de chocolate.', 2800.00,
       'https://images.unsplash.com/photo-1562585194-aa017445b3ad?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Postres'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Queque de chocolate');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Tiramisú', 'Postre de capas con crema y cacao.', 3200.00,
       'https://images.unsplash.com/photo-1766232333746-b0a2697d6d0d?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Postres'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Tiramisú');

INSERT INTO producto (idCategoria, nombre, descripcion, precio, imagenUrl, activo)
SELECT c.idCategoria, 'Cheesecake de mango', 'Cheesecake cremoso acompañado de mango.', 3300.00,
       'https://images.unsplash.com/photo-1779608993337-fc83f391bbed?auto=format&fit=crop&fm=jpg&q=85&w=1000', TRUE
FROM categoria c
WHERE c.nombre = 'Postres'
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.idCategoria = c.idCategoria AND p.nombre = 'Cheesecake de mango');

COMMIT;

-- Comprobacion posterior: lista las filas de prueba que existan y confirma su categoria.
SELECT c.nombre AS categoria, p.nombre AS producto, p.precio, p.imagenUrl, p.activo
FROM categoria c
JOIN producto p ON p.idCategoria = c.idCategoria
WHERE c.nombre IN ('Cafés calientes', 'Bebidas frías', 'Tés e infusiones', 'Repostería', 'Postres')
ORDER BY c.nombre, p.nombre;
