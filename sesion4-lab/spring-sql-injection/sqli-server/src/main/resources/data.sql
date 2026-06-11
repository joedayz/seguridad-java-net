-- Usuarios de ejemplo. La "nota_secreta" simula datos confidenciales que solo
-- deberian verse conociendo el email exacto. La inyeccion SQL los expone todos.
INSERT INTO users (email, nombre, rol, nota_secreta) VALUES
    ('ana@acme.com',   'Ana Garcia',  'USER',  'Borrador campana marketing Q3'),
    ('luis@acme.com',  'Luis Perez',  'USER',  'Revision de nomina pendiente'),
    ('admin@acme.com', 'Root Admin',  'ADMIN', 'API_KEY=sk-live-9f3a7c21 (NO COMPARTIR)');

-- Productos de ejemplo para la demo de busqueda con LIKE.
INSERT INTO products (name, price) VALUES
    ('Laptop Pro 15',       1299.99),
    ('Mouse inalambrico',     29.99),
    ('Teclado mecanico',      89.50),
    ('Monitor 27 pulgadas',  349.00),
    ('Licencia interna ERP', 9999.00);
