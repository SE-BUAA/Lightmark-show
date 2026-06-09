SET @add_product_id_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `review` ADD COLUMN `product_id` BIGINT NULL COMMENT ''关联 product.id'' AFTER `order_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'review'
    AND COLUMN_NAME = 'product_id'
);
PREPARE add_product_id_stmt FROM @add_product_id_sql;
EXECUTE add_product_id_stmt;
DEALLOCATE PREPARE add_product_id_stmt;

SET @add_product_id_index_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `review` ADD INDEX `idx_product_id` (`product_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'review'
    AND INDEX_NAME = 'idx_product_id'
);
PREPARE add_product_id_index_stmt FROM @add_product_id_index_sql;
EXECUTE add_product_id_index_stmt;
DEALLOCATE PREPARE add_product_id_index_stmt;

ALTER TABLE `review`
  MODIFY COLUMN `target_type` VARCHAR(20) NULL COMMENT '评论目标类型，如 HOTEL';
