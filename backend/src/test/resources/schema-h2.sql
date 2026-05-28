CREATE TABLE `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `phone` VARCHAR(20),
  `email` VARCHAR(100),
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT '',
  `points` INT DEFAULT 0,
  `level` TINYINT DEFAULT 0,
  `status` TINYINT DEFAULT 0,
  `deleted` TINYINT DEFAULT 0,
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
  id INT AUTO_INCREMENT PRIMARY KEY,
  role_name VARCHAR(20) NOT NULL,
  permission VARCHAR(255)
);

CREATE TABLE user_role (
  user_id BIGINT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_type VARCHAR(20) NOT NULL,
  name VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT DEFAULT 0,
  sold_count INT DEFAULT 0,
  category_tags VARCHAR(255),
  status TINYINT DEFAULT 1,
  extra CLOB,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(32) NOT NULL,
  user_id BIGINT NOT NULL,
  order_type VARCHAR(20) NOT NULL,
  total_amount DECIMAL(10,2),
  points_deduct INT DEFAULT 0,
  pay_amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(20),
  source VARCHAR(50),
  status TINYINT DEFAULT 0,
  pay_deadline TIMESTAMP,
  pay_time TIMESTAMP,
  cancel_reason VARCHAR(255),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE flight_order_detail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  flight_no VARCHAR(20) NOT NULL,
  departure_date DATE NOT NULL,
  passenger_list CLOB,
  baggage VARCHAR(100),
  insurance TINYINT DEFAULT 0
);

CREATE TABLE payment_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  transaction_id VARCHAR(64) NOT NULL,
  payment_method VARCHAR(20) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status TINYINT DEFAULT 0,
  callback_time TIMESTAMP,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE points_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type TINYINT NOT NULL,
  amount INT NOT NULL,
  source VARCHAR(50),
  order_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE admin_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  admin_id BIGINT NOT NULL,
  operation VARCHAR(50) NOT NULL,
  params VARCHAR(500),
  result VARCHAR(20),
  ip VARCHAR(45),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

