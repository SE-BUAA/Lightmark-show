CREATE TABLE IF NOT EXISTS `invoice_application` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'electronic',
  `title` VARCHAR(100) NOT NULL,
  `tax_no` VARCHAR(50),
  `status` TINYINT DEFAULT 0 COMMENT '0 pending, 1 issued',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `review`
  ADD COLUMN `product_id` BIGINT NOT NULL COMMENT 'product.id' AFTER `order_id`;

ALTER TABLE `review`
  ADD INDEX `idx_product_id` (`product_id`);
