INSERT INTO orders (id, order_number, user_id, customer_email, shipping_address, total_amount, status, created_at, updated_at) VALUES 
(1, 'ORD-20260901-0001', 2, 'user@ecommerce.com', '123 Market Street, Suite 400, San Francisco, CA 94105', 349.50, 'DELIVERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (id, order_id, product_id, sku, product_name, unit_price, quantity, subtotal, created_at) VALUES 
(1, 1, 3, 'AUD-SONIC-NC', 'SonicWave Noise-Cancelling Headphones', 349.50, 1, 349.50, CURRENT_TIMESTAMP);

INSERT INTO order_status_history (id, order_id, from_status, to_status, notes, changed_at) VALUES 
(1, 1, 'PENDING', 'CONFIRMED', 'Order confirmed automatically', CURRENT_TIMESTAMP),
(2, 1, 'CONFIRMED', 'PROCESSING', 'Fulfillment started', CURRENT_TIMESTAMP),
(3, 1, 'PROCESSING', 'SHIPPED', 'Shipped via Express Logistics tracking #EX12345', CURRENT_TIMESTAMP),
(4, 1, 'SHIPPED', 'DELIVERED', 'Delivered at front door', CURRENT_TIMESTAMP);
