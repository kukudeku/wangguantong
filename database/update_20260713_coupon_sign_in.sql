-- 网管通功能升级：优惠券、连续签到奖励、点餐满减
-- 适用于已有本地或宝塔数据库，可重复执行。

USE wangguantong;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'batch_no');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `food_order` ADD COLUMN `batch_no` VARCHAR(60) NULL COMMENT ''购物车订单批次号'' AFTER `quantity`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'gross_amount');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `food_order` ADD COLUMN `gross_amount` DECIMAL(10,2) NULL COMMENT ''优惠前金额'' AFTER `batch_no`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'discount_amount');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `food_order` ADD COLUMN `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT ''优惠金额'' AFTER `gross_amount`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'coupon_name');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `food_order` ADD COLUMN `coupon_name` VARCHAR(80) NULL COMMENT ''优惠券名称'' AFTER `discount_amount`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `food_order` SET gross_amount = total_amount WHERE gross_amount IS NULL;

CREATE TABLE IF NOT EXISTS `coupon_template` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  min_spend DECIMAL(10,2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(10,2) NOT NULL,
  valid_days INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT '启用',
  remark VARCHAR(200) NULL,
  create_time DATETIME NOT NULL
) COMMENT='优惠券模板表';

CREATE TABLE IF NOT EXISTS `sign_in_reward_rule` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  consecutive_days INT NOT NULL UNIQUE,
  coupon_template_id BIGINT NOT NULL,
  coupon_name VARCHAR(80) NOT NULL,
  create_time DATETIME NOT NULL
) COMMENT='签到奖励规则表';

CREATE TABLE IF NOT EXISTS `user_coupon` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,
  coupon_name VARCHAR(80) NOT NULL,
  min_spend DECIMAL(10,2) NOT NULL DEFAULT 0,
  discount_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT '可使用',
  source_type VARCHAR(30) NOT NULL,
  source_ref_id BIGINT NULL,
  receive_time DATETIME NOT NULL,
  expire_time DATETIME NOT NULL,
  use_time DATETIME NULL,
  order_batch_no VARCHAR(60) NULL
) COMMENT='用户优惠券表';

CREATE TABLE IF NOT EXISTS `member_sign_in` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  sign_date DATE NOT NULL,
  consecutive_days INT NOT NULL,
  reward_rule_id BIGINT NULL,
  reward_coupon_id BIGINT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_member_sign_date (member_id, sign_date)
) COMMENT='会员签到记录表';

INSERT INTO `coupon_template` (name, min_spend, discount_amount, valid_days, status, remark, create_time)
SELECT '签到 1 元券', 5.00, 1.00, 7, '启用', '连续签到 1 天奖励', NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon_template WHERE name = '签到 1 元券');
INSERT INTO `coupon_template` (name, min_spend, discount_amount, valid_days, status, remark, create_time)
SELECT '签到 3 元券', 10.00, 3.00, 7, '启用', '连续签到 3 天奖励', NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon_template WHERE name = '签到 3 元券');
INSERT INTO `coupon_template` (name, min_spend, discount_amount, valid_days, status, remark, create_time)
SELECT '签到 5 元券', 15.00, 5.00, 10, '启用', '连续签到 7 天奖励', NOW()
WHERE NOT EXISTS (SELECT 1 FROM coupon_template WHERE name = '签到 5 元券');

INSERT INTO `sign_in_reward_rule` (consecutive_days, coupon_template_id, coupon_name, create_time)
SELECT 1, id, name, NOW() FROM coupon_template WHERE name = '签到 1 元券'
  AND NOT EXISTS (SELECT 1 FROM sign_in_reward_rule WHERE consecutive_days = 1) LIMIT 1;
INSERT INTO `sign_in_reward_rule` (consecutive_days, coupon_template_id, coupon_name, create_time)
SELECT 3, id, name, NOW() FROM coupon_template WHERE name = '签到 3 元券'
  AND NOT EXISTS (SELECT 1 FROM sign_in_reward_rule WHERE consecutive_days = 3) LIMIT 1;
INSERT INTO `sign_in_reward_rule` (consecutive_days, coupon_template_id, coupon_name, create_time)
SELECT 7, id, name, NOW() FROM coupon_template WHERE name = '签到 5 元券'
  AND NOT EXISTS (SELECT 1 FROM sign_in_reward_rule WHERE consecutive_days = 7) LIMIT 1;
