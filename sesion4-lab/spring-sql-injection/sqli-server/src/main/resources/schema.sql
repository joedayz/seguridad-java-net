DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    nombre       VARCHAR(255) NOT NULL,
    rol          VARCHAR(50)  NOT NULL,
    nota_secreta VARCHAR(255)
);
