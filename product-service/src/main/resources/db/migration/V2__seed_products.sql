INSERT INTO categories (id, name, description, slug, created_at, updated_at) VALUES 
(1, 'Laptops & Computers', 'High performance enterprise laptops, workstations, and computing gear', 'laptops-computers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Audio & Acoustics', 'Studio quality wireless headphones, monitors, and earbuds', 'audio-acoustics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Smartphones & Mobile', 'Flagship smartphones and mobile accessories', 'smartphones-mobile', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Wearables & Fitness', 'Smartwatches, fitness bands, and health trackers', 'wearables-fitness', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO products (id, name, description, price, sku, category_id, image_url, active, stock_quantity, created_at, updated_at) VALUES 
(1, 'TitanBook Pro 16', '16-inch high-performance developer workstation with 32GB RAM and 1TB NVMe SSD', 2499.99, 'LAP-TITAN-16', 1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600', TRUE, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'AeroBook Ultra 14', 'Ultralight thin laptop featuring 14-inch OLED display and 18-hour battery life', 1299.00, 'LAP-AERO-14', 1, 'https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=600', TRUE, 75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SonicWave Noise-Cancelling Headphones', 'Active noise cancellation with 40-hour wireless battery and high-res audio drivers', 349.50, 'AUD-SONIC-NC', 2, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', TRUE, 120, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'StudioPro Wireless Earbuds', 'True wireless stereo earbuds with spatial audio and wireless charging case', 189.99, 'AUD-STUDIO-EAR', 2, 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600', TRUE, 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Apex Flagship Phone 5G', '6.8-inch Dynamic AMOLED screen, 108MP camera array, and 512GB storage', 1099.00, 'PHN-APEX-5G', 3, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600', TRUE, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Pulse Pro Smartwatch', 'Titanium case smartwatch with heart rate, ECG, and sleep tracking sensors', 399.00, 'WBL-PULSE-PRO', 4, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600', TRUE, 85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'UltraFit Activity Band', 'Waterproof fitness tracker with step counting, sleep metrics, and 14-day battery', 79.95, 'WBL-FIT-BAND', 4, 'https://images.unsplash.com/photo-1576243345690-4e4b79b63288?w=600', TRUE, 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '4K Ultra-Wide Developer Monitor 34', 'Curved 34-inch IPS monitor with 144Hz refresh rate, USB-C 90W PD charging', 699.00, 'MON-CURVED-34', 1, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600', TRUE, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
