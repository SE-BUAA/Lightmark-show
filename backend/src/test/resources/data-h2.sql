INSERT INTO `user` (id, phone, email, password, nickname, points, level, status, deleted) VALUES
(1, '13800000000', 'admin@timemark.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 0, 3, 0, 0),
(2, '13900000000', 'user@timemark.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '普通用户', 120, 1, 0, 0);

INSERT INTO role (id, role_name, permission) VALUES
(1, 'ADMIN', '*'),
(2, 'USER', '');

INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

INSERT INTO product (id, product_type, name, price, stock, sold_count, status) VALUES
(1, 'FLIGHT', 'CA1234', 680.00, 50, 12, 1),
(2, 'HOTEL', '上海外滩酒店', 850.00, 20, 8, 1),
(3, 'TRAIN', 'G1次', 553.00, 200, 88, 1);

INSERT INTO orders (id, order_no, user_id, order_type, pay_amount, payment_method, status) VALUES
(1, 'ORD202604180001', 2, 'FLIGHT', 680.00, 'WECHAT', 1),
(2, 'ORD202604180002', 2, 'HOTEL', 750.00, 'ALIPAY', 1),
(3, 'ORD202604180003', 2, 'TRAIN', 553.00, 'WECHAT', 0);

