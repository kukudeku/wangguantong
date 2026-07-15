USE wangguantong;

-- 支付主记录区分点餐与余额充值；旧记录均为点餐支付。
SET @business_type_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'payment_record'
    AND COLUMN_NAME = 'business_type'
);

SET @business_type_sql = IF(
  @business_type_exists = 0,
  'ALTER TABLE `payment_record` ADD COLUMN `business_type` VARCHAR(20) NOT NULL DEFAULT ''点餐'' COMMENT ''业务类型：点餐 / 余额充值'' AFTER `id`',
  'SELECT 1'
);

PREPARE business_type_stmt FROM @business_type_sql;
EXECUTE business_type_stmt;
DEALLOCATE PREPARE business_type_stmt;

UPDATE `payment_record`
SET `business_type` = '点餐'
WHERE `business_type` IS NULL OR `business_type` = '';

-- 支付平台回调单号用于审计，并辅助识别每一笔真实充值。
SET @recharge_reference_index_exists = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'recharge_record'
    AND INDEX_NAME = 'idx_recharge_reference_no'
);

SET @recharge_reference_index_sql = IF(
  @recharge_reference_index_exists = 0,
  'CREATE INDEX `idx_recharge_reference_no` ON `recharge_record` (`reference_no`)',
  'SELECT 1'
);

PREPARE recharge_reference_index_stmt FROM @recharge_reference_index_sql;
EXECUTE recharge_reference_index_stmt;
DEALLOCATE PREPARE recharge_reference_index_stmt;
