CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    customer_name VARCHAR(128) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(32) NOT NULL
);

INSERT INTO orders (id, user_id, customer_name, total, status) VALUES
    (1001, 'usr_ana', 'Ana Garcia', 249.99, 'SHIPPED'),
    (1002, 'usr_luis', 'Luis Perez', 89.50, 'PENDING'),
    (1003, 'usr_maria', 'Maria Lopez', 1200.00, 'PAID');
