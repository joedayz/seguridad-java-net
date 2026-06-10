-- Datos de negocio de ejemplo. Los crea el usuario admin (vault-admin) al iniciar.
-- Los usuarios dinamicos que emite Vault solo reciben permiso de SELECT sobre estas tablas.
CREATE TABLE IF NOT EXISTS products (
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(120) NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

INSERT INTO products (name, price) VALUES
    ('Curso de Seguridad Java/.NET', 499.00),
    ('Licencia HashiCorp Vault',     1200.00),
    ('Soporte Premium 1 ano',         3500.00)
ON CONFLICT DO NOTHING;
