-- 网管通功能扩展脚本：点餐、预约锁定、会员级别折扣
-- 适合已经执行过旧版 wangguantong.sql 的数据库。
-- 如果你不需要保留旧数据，也可以直接重新执行 database/wangguantong.sql。

USE wangguantong;

ALTER TABLE `member`
  ADD COLUMN `user_type` VARCHAR(20) NOT NULL DEFAULT '会员' COMMENT '用户类型：会员 / 散客',
  ADD COLUMN `member_level` VARCHAR(20) NOT NULL DEFAULT '普通会员' COMMENT '会员级别：散客 / 普通会员 / 黄金会员 / 钻石会员';

ALTER TABLE `member`
  ADD COLUMN `username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '登录账号',
  ADD COLUMN `password` VARCHAR(50) NOT NULL DEFAULT '123456' COMMENT '登录密码',
  ADD COLUMN `id_card` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '身份证号';

UPDATE `member` SET username = phone WHERE username = '';
UPDATE `member` SET id_card = username WHERE id_card = '';

CREATE TABLE IF NOT EXISTS `food_item` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '商品名称',
  category VARCHAR(30) NOT NULL DEFAULT '其他' COMMENT '商品分类',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  status VARCHAR(20) NOT NULL DEFAULT '上架' COMMENT '状态：上架 / 下架',
  remark VARCHAR(200) NULL COMMENT '备注'
) COMMENT='点餐商品表';

SET @food_category_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'food_item'
    AND COLUMN_NAME = 'category'
);
SET @food_category_sql = IF(
  @food_category_column_exists = 0,
  'ALTER TABLE `food_item` ADD COLUMN `category` VARCHAR(30) NOT NULL DEFAULT ''其他'' COMMENT ''商品分类'' AFTER `name`',
  'SELECT 1'
);
PREPARE food_category_stmt FROM @food_category_sql;
EXECUTE food_category_stmt;
DEALLOCATE PREPARE food_category_stmt;

CREATE TABLE IF NOT EXISTS `food_order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NULL COMMENT '会员ID，散客可为空',
  member_name VARCHAR(50) NULL COMMENT '会员姓名',
  customer_type VARCHAR(20) NOT NULL COMMENT '顾客类型：会员 / 散客',
  customer_name VARCHAR(50) NOT NULL COMMENT '顾客姓名',
  food_item_id BIGINT NOT NULL COMMENT '商品ID',
  food_item_name VARCHAR(50) NOT NULL COMMENT '商品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  quantity INT NOT NULL COMMENT '数量',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  payment_method VARCHAR(20) NOT NULL DEFAULT '余额支付' COMMENT '支付方式：余额支付 / 微信支付 / 支付宝支付',
  status VARCHAR(20) NOT NULL DEFAULT '已下单' COMMENT '状态：已下单 / 已取消',
  create_time DATETIME NOT NULL COMMENT '下单时间'
) COMMENT='点餐订单表';

CREATE TABLE IF NOT EXISTS `reservation_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  reserve_time DATETIME NOT NULL COMMENT '预约时间',
  status VARCHAR(20) NOT NULL DEFAULT '已预约' COMMENT '状态：已预约 / 已取消 / 已上机',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='预约记录表';

INSERT INTO `food_item` (name, category, price, status, remark)
SELECT '可乐', '饮料', 4.00, '上架', '冰镇饮料'
WHERE NOT EXISTS (SELECT 1 FROM `food_item` WHERE name = '可乐');

INSERT INTO `food_item` (name, category, price, status, remark)
SELECT '泡面', '餐食', 6.00, '上架', '经典桶面'
WHERE NOT EXISTS (SELECT 1 FROM `food_item` WHERE name = '泡面');
