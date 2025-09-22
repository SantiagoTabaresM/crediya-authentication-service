-- Tabla de roles
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Tabla de usuarios
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    last_name VARCHAR(100),
    document VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(150) NOT NULL UNIQUE,
    base_salary BIGINT,
    birth_date DATE,
    address VARCHAR(255),
    role_id INT,
    password VARCHAR(255),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

INSERT INTO roles (name, description) VALUES
('ADMIN', 'Role with full system administration privileges'),
('ADVISOR', 'Role for advisors or support staff'),
('USER', 'Role for regular system users');

INSERT INTO users (name, last_name, document, phone, email, base_salary, birth_date, address, role_id, password) VALUES
('Santiago', 'Tabares', '123456789', '3127612344', 'santiago@example.com', 50000, '1999-01-01', '123 CALLE 132 St', 1, '$2a$10$B.58OnHmqtpRSlk51hDyoO.nGZJuzA7Yqw0KWy0dQuntiGzPd8bRC');