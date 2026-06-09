ALTER TABLE orders
  ADD COLUMN changed_once TINYINT DEFAULT 0,
  ADD COLUMN original_order_no VARCHAR(32) NULL;

UPDATE orders
SET changed_once = 0
WHERE changed_once IS NULL;
