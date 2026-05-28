ALTER TABLE orders
  MODIFY COLUMN extra_info TEXT NULL;

UPDATE orders
SET total_amount = pay_amount
WHERE total_amount IS NULL;
