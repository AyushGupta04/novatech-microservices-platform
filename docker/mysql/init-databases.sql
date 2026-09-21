-- Initialize multiple databases for microservices
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS inventory_db;
CREATE DATABASE IF NOT EXISTS cart_db;
CREATE DATABASE IF NOT EXISTS order_db;

-- Ensure user exists and has permissions
CREATE USER IF NOT EXISTS 'ecommerce_user'@'%' IDENTIFIED BY 'ecommerce_password';
GRANT ALL PRIVILEGES ON auth_db.* TO 'ecommerce_user'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'ecommerce_user'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'ecommerce_user'@'%';
GRANT ALL PRIVILEGES ON cart_db.* TO 'ecommerce_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'ecommerce_user'@'%';

FLUSH PRIVILEGES;
