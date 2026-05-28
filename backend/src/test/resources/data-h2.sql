INSERT INTO `user` (id, phone, email, password, nickname, points, level, status, deleted) VALUES
(1, '13800000000', 'admin@timemark.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 0, 3, 0, 0),
(2, '13900000000', 'user@timemark.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '普通用户', 120, 1, 0, 0);

INSERT INTO role (id, role_name, permission) VALUES
(1, 'ADMIN', '*'),
(2, 'USER', '');

INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

INSERT INTO product (id, product_type, name, price, stock, sold_count, status, extra, category_tags) VALUES
(1, 'FLIGHT', 'CA1234', 680.00, 50, 12, 1, null, null),
(2, 'HOTEL', '上海外滩酒店', 850.00, 20, 8, 1, null, null),
(3, 'TRAIN', 'G1次', 553.00, 200, 88, 1, '{"start_station":"北京南","end_station":"上海虹桥","date":"2026-05-27","depart_time":"09:00","arrive_time":"13:28"}', '["高铁","商务座","一等座","二等座"]'),
(4, 'TRAIN', 'D311次', 250.00, 50, 10, 1, '{"start_station":"上海虹桥","end_station":"杭州东","date":"2026-05-27","depart_time":"10:20","arrive_time":"11:18"}', '["动车","一等座","二等座"]'),
(5, 'TRAIN', 'K105次', 168.00, 80, 16, 1, '{"start_station":"北京西","end_station":"郑州","date":"2026-05-28","depart_time":"18:16","arrive_time":"06:12"}', '["普速","硬卧","软卧","硬座"]');

INSERT INTO product (id, product_type, name, price, stock, sold_count, status, extra, category_tags) VALUES
(6, 'VACATION', '三亚海岛五日自由行', 2999.00, 30, 0, 1, '{"destination":"三亚","depart_city":"北京","date":"2026-06-15","days":5,"hotel_level":"五星","summary":"海景酒店连住，含接送机"}' FORMAT JSON, '["海岛","自由行","亲子"]' FORMAT JSON),
(7, 'VACATION', '云南古城六日跟团游', 2580.00, 24, 0, 1, '{"destination":"丽江","depart_city":"上海","date":"2026-06-20","days":6,"hotel_level":"四星","summary":"丽江大理双城，含经典景点门票"}' FORMAT JSON, '["古城","跟团游","摄影"]' FORMAT JSON),
(8, 'VACATION', '成都美食四日私享团', 1880.00, 18, 0, 1, '{"destination":"成都","depart_city":"广州","date":"2026-07-02","days":4,"hotel_level":"四星","summary":"小团出行，城市美食和周边慢游"}' FORMAT JSON, '["美食","私享团","城市"]' FORMAT JSON);

INSERT INTO orders (id, order_no, user_id, order_type, total_amount, pay_amount, payment_method, status) VALUES
(1, 'ORD202604180001', 2, 'FLIGHT', 680.00, 680.00, 'WECHAT', 1),
(2, 'ORD202604180002', 2, 'HOTEL', 750.00, 750.00, 'ALIPAY', 1),
(3, 'ORD202604180003', 2, 'TRAIN', 553.00, 553.00, 'WECHAT', 0);
