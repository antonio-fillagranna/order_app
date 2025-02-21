-- Versão 1 - Criação das tabelas principais
CREATE TABLE client (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cell VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    doc VARCHAR(20),
    street VARCHAR(255) NOT NULL,
    number VARCHAR(10) NOT NULL,
    postalcode VARCHAR(20) NOT NULL,
    neighborhood VARCHAR(100),
    city VARCHAR(20) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    additionalinfo VARCHAR(255)
);

-- Criando a tabela de produtos (substituindo sushi_item)
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(10,2) NOT NULL,
    description TEXT,
    category VARCHAR(100)
);

-- Criando a tabela de pedidos (orders)
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client(id)
);

-- Criando a tabela de itens de pedido (order_item) com referência para product
CREATE TABLE order_item (
    id SERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,  -- Alterado para product_id
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)  -- Refere-se à tabela de products
);
