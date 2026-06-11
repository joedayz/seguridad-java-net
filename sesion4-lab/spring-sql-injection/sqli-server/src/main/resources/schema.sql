DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    nombre       VARCHAR(255) NOT NULL,
    rol          VARCHAR(50)  NOT NULL,
    nota_secreta VARCHAR(255)
);

CREATE TABLE products (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    price DOUBLE       NOT NULL
);
