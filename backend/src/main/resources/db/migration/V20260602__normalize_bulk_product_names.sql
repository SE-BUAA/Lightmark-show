UPDATE product
SET name = REPLACE(name, '批量火车票-', '')
WHERE product_type = 'TRAIN'
  AND name LIKE '批量火车票-%';
