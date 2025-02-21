ALTER TABLE order_item ADD COLUMN unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0;
UPDATE order_item SET unit_price = 0;
