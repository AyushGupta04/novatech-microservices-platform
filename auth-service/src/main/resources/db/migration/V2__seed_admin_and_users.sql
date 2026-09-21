INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

-- Password for both: Admin123! and User123! (BCrypt hashed)
INSERT INTO users (id, email, password, first_name, last_name, enabled, created_at, updated_at) VALUES 
(1, 'admin@ecommerce.com', '$2a$10$RejZLGTtZ9Wq.0Z4HGwxZu9l5ErVydVkyk5TEdO9rUk49nt9poRdu', 'Enterprise', 'Admin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'user@ecommerce.com', '$2a$10$14zNORYwtGmGDo3g6ujtSeoxxjmPxKvt8TK/8PsDZHtJ21oYfxLQq', 'Standard', 'Customer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1),
(1, 2),
(2, 1);
