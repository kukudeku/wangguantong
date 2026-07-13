-- 网管通功能升级：团购券充值、换机分段计费、机器报修
-- 适用于已经存在数据的本地或宝塔 MySQL 数据库，可重复执行。

USE wangguantong;

SET @column_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'recharge_record' AND COLUMN_NAME = 'recharge_type'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `recharge_record` ADD COLUMN `recharge_type` VARCHAR(30) NOT NULL DEFAULT ''余额充值'' COMMENT ''充值方式'' AFTER `amount`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'recharge_record' AND COLUMN_NAME = 'reference_no'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `recharge_record` ADD COLUMN `reference_no` VARCHAR(80) NULL COMMENT ''关联单号或团购券码'' AFTER `recharge_type`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'online_record' AND COLUMN_NAME = 'segment_start_time'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `online_record` ADD COLUMN `segment_start_time` DATETIME NULL COMMENT ''当前机位计费段开始时间'' AFTER `total_amount`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'online_record' AND COLUMN_NAME = 'segment_paid_amount'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `online_record` ADD COLUMN `segment_paid_amount` DECIMAL(10,2) NULL COMMENT ''当前机位计费段已扣金额'' AFTER `segment_start_time`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'online_record' AND COLUMN_NAME = 'computer_history'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `online_record` ADD COLUMN `computer_history` VARCHAR(500) NULL COMMENT ''本次上机的换机轨迹'' AFTER `segment_paid_amount`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `online_record`
SET segment_start_time = start_time,
    segment_paid_amount = total_amount,
    computer_history = computer_no
WHERE segment_start_time IS NULL;

CREATE TABLE IF NOT EXISTS `group_buy_voucher` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  voucher_code VARCHAR(80) NOT NULL UNIQUE COMMENT '团购券码',
  amount DECIMAL(10,2) NOT NULL COMMENT '可充值金额',
  status VARCHAR(20) NOT NULL DEFAULT '未使用' COMMENT '状态：未使用 / 已使用 / 已禁用',
  used_member_id BIGINT NULL COMMENT '核销会员ID',
  used_member_name VARCHAR(50) NULL COMMENT '核销会员姓名',
  used_time DATETIME NULL COMMENT '核销时间',
  expire_time DATETIME NULL COMMENT '过期时间',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='团购充值券表';

CREATE TABLE IF NOT EXISTS `repair_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  member_id BIGINT NULL COMMENT '报修用户ID',
  member_name VARCHAR(50) NULL COMMENT '报修人',
  problem_description VARCHAR(500) NOT NULL COMMENT '故障说明',
  status VARCHAR(20) NOT NULL DEFAULT '待处理' COMMENT '状态：待处理 / 处理中 / 已完成 / 已取消',
  process_remark VARCHAR(500) NULL COMMENT '处理说明',
  create_time DATETIME NOT NULL COMMENT '报修时间',
  process_time DATETIME NULL COMMENT '开始处理时间',
  finish_time DATETIME NULL COMMENT '完成或取消时间'
) COMMENT='机器报修记录表';

INSERT INTO `group_buy_voucher` (voucher_code, amount, status, expire_time, remark, create_time)
SELECT 'WGT-TEST-50', 50.00, '未使用', DATE_ADD(NOW(), INTERVAL 1 YEAR), '演示团购券', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `group_buy_voucher` WHERE voucher_code = 'WGT-TEST-50');
