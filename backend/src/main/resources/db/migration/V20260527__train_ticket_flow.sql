ALTER TABLE orders
  ADD COLUMN pickup_code VARCHAR(6) NULL AFTER cancel_reason,
  ADD UNIQUE KEY uk_orders_pickup_code (pickup_code);

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'TRAIN', 'G1次', 553.00, 200, 0,
       JSON_ARRAY('高铁', '商务座', '一等座', '二等座'),
       JSON_OBJECT('start_station', '北京南', 'end_station', '上海虹桥', 'date', '2026-05-27', 'depart_time', '09:00', 'arrive_time', '13:28'),
       1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'TRAIN' AND name = 'G1次');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'TRAIN', 'D311次', 250.00, 80, 0,
       JSON_ARRAY('动车', '一等座', '二等座'),
       JSON_OBJECT('start_station', '上海虹桥', 'end_station', '杭州东', 'date', '2026-05-27', 'depart_time', '10:20', 'arrive_time', '11:18'),
       1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'TRAIN' AND name = 'D311次');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'TRAIN', 'K105次', 168.00, 80, 0,
       JSON_ARRAY('普速', '硬卧', '软卧', '硬座'),
       JSON_OBJECT('start_station', '北京西', 'end_station', '郑州', 'date', '2026-05-28', 'depart_time', '18:16', 'arrive_time', '06:12'),
       1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'TRAIN' AND name = 'K105次');
