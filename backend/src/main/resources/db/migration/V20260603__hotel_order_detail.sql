CREATE TABLE IF NOT EXISTS `hotel_order_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT 'orders.id',
  `room_id` BIGINT NOT NULL,
  `check_in_date` DATE NOT NULL,
  `check_out_date` DATE NOT NULL,
  `room_num` INT NOT NULL,
  `guest_list` JSON NOT NULL COMMENT '[{"name":"","idCard":"","phone":""}]',
  `total_price` DECIMAL(10,2) NOT NULL,
  `points_deducted` INT DEFAULT 0,
  `pay_amount` DECIMAL(10,2) NOT NULL,
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `room_type`
  ADD COLUMN `bed_type` VARCHAR(50) DEFAULT NULL COMMENT 'bed type';

ALTER TABLE `room_type`
  ADD COLUMN `area` VARCHAR(50) DEFAULT NULL COMMENT 'room area';
