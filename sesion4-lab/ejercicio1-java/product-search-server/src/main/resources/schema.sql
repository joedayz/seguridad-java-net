DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category    VARCHAR(100) NOT NULL
);
