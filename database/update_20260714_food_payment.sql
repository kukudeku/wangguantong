USE wangguantong;

-- 点餐订单新增支付方式。脚本可重复执行，适用于本地和宝塔中的现有数据库。
SET @payment_method_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'food_order'
    AND COLUMN_NAME = 'payment_method'
);

SET @payment_method_sql = IF(
  @payment_method_exists = 0,
  'ALTER TABLE `food_order` ADD COLUMN `payment_method` VARCHAR(20) NOT NULL DEFAULT ''余额支付'' COMMENT ''支付方式：余额支付 / 微信支付 / 支付宝支付'' AFTER `total_amount`',
  'SELECT 1'
);

PREPARE payment_method_stmt FROM @payment_method_sql;
EXECUTE payment_method_stmt;
DEALLOCATE PREPARE payment_method_stmt;

UPDATE `food_order`
SET `payment_method` = '余额支付'
WHERE `payment_method` IS NULL OR `payment_method` = '';

-- 一次购物车结算只创建一条支付主记录，商品明细继续保存在 food_order。
CREATE TABLE IF NOT EXISTS `payment_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  order_batch_no VARCHAR(60) NOT NULL COMMENT '点餐订单批次号',
  member_id BIGINT NULL COMMENT '会员ID，散客可为空',
  payment_method VARCHAR(20) NOT NULL COMMENT '微信支付 / 支付宝支付',
  out_trade_no VARCHAR(64) NOT NULL COMMENT '商户支付单号',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付总金额',
  refunded_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计成功退款金额',
  status VARCHAR(20) NOT NULL COMMENT '待支付 / 已支付 / 已关闭 / 部分退款 / 退款中 / 已退款',
  provider_trade_no VARCHAR(80) NULL COMMENT '支付平台交易号',
  code_url TEXT NULL COMMENT '微信 Native 支付二维码内容',
  expire_time DATETIME NOT NULL COMMENT '支付失效时间',
  paid_time DATETIME NULL COMMENT '支付成功时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_payment_batch (`order_batch_no`),
  UNIQUE KEY uk_payment_out_trade_no (`out_trade_no`),
  KEY idx_payment_status_expire (`status`, `expire_time`)
) COMMENT='第三方支付主记录';

-- 后台可逐条取消购物车商品，每次退款单独记录，防止重复退款。
CREATE TABLE IF NOT EXISTS `payment_refund` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  payment_record_id BIGINT NOT NULL COMMENT '支付主记录ID',
  food_order_id BIGINT NOT NULL COMMENT '被取消的点餐订单ID',
  out_refund_no VARCHAR(64) NOT NULL COMMENT '商户退款单号',
  amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  status VARCHAR(20) NOT NULL COMMENT '退款中 / 已退款 / 退款失败',
  provider_refund_no VARCHAR(80) NULL COMMENT '支付平台退款号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  refunded_time DATETIME NULL COMMENT '退款成功时间',
  UNIQUE KEY uk_refund_order (`food_order_id`),
  UNIQUE KEY uk_out_refund_no (`out_refund_no`),
  KEY idx_refund_payment (`payment_record_id`)
) COMMENT='第三方支付退款记录';
