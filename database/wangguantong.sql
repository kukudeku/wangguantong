-- 网管通 —— 电竞网吧运营管理系统 数据库脚本
-- 执行方式：在 MySQL 客户端中执行本文件即可创建数据库、表和测试数据

CREATE DATABASE IF NOT EXISTS wangguantong DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE wangguantong;

DROP TABLE IF EXISTS `online_record`;
DROP TABLE IF EXISTS `repair_record`;
DROP TABLE IF EXISTS `reservation_record`;
DROP TABLE IF EXISTS `invitation_record`;
DROP TABLE IF EXISTS `promotion_rule`;
DROP TABLE IF EXISTS `member_sign_in`;
DROP TABLE IF EXISTS `user_coupon`;
DROP TABLE IF EXISTS `sign_in_reward_rule`;
DROP TABLE IF EXISTS `coupon_template`;
DROP TABLE IF EXISTS `payment_refund`;
DROP TABLE IF EXISTS `payment_record`;
DROP TABLE IF EXISTS `food_order`;
DROP TABLE IF EXISTS `food_item`;
DROP TABLE IF EXISTS `recharge_record`;
DROP TABLE IF EXISTS `group_buy_voucher`;
DROP TABLE IF EXISTS `computer`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `admin`;

-- 管理员表
CREATE TABLE `admin` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(50) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名'
) COMMENT='管理员表';

-- 会员表
CREATE TABLE `member` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password VARCHAR(50) NOT NULL DEFAULT '123456' COMMENT '登录密码',
  id_card VARCHAR(30) NOT NULL COMMENT '身份证号',
  name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  balance DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '余额',
  user_type VARCHAR(20) NOT NULL DEFAULT '会员' COMMENT '用户类型：会员 / 散客',
  member_level VARCHAR(20) NOT NULL DEFAULT '普通会员' COMMENT '会员级别：散客 / 普通会员 / 黄金会员 / 钻石会员',
  status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '状态',
  invite_code VARCHAR(20) NOT NULL COMMENT '唯一邀请码',
  inviter_member_id BIGINT NULL COMMENT '邀请人会员ID',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_member_invite_code (invite_code)
) COMMENT='会员表';

-- 电脑机位表
CREATE TABLE `computer` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  area VARCHAR(50) NOT NULL COMMENT '区域',
  price_per_hour DECIMAL(10,2) NOT NULL COMMENT '每小时单价',
  status VARCHAR(20) NOT NULL DEFAULT '空闲' COMMENT '状态',
  remark VARCHAR(200) NULL COMMENT '备注'
) COMMENT='电脑机位表';

-- 点餐商品表
CREATE TABLE `food_item` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '商品名称',
  category VARCHAR(30) NOT NULL DEFAULT '其他' COMMENT '商品分类',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  status VARCHAR(20) NOT NULL DEFAULT '上架' COMMENT '状态：上架 / 下架',
  remark VARCHAR(200) NULL COMMENT '备注'
) COMMENT='点餐商品表';

-- 点餐订单表
CREATE TABLE `food_order` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NULL COMMENT '会员ID，散客可为空',
  member_name VARCHAR(50) NULL COMMENT '会员姓名',
  customer_type VARCHAR(20) NOT NULL COMMENT '顾客类型：会员 / 散客',
  customer_name VARCHAR(50) NOT NULL COMMENT '顾客姓名',
  food_item_id BIGINT NOT NULL COMMENT '商品ID',
  food_item_name VARCHAR(50) NOT NULL COMMENT '商品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  quantity INT NOT NULL COMMENT '数量',
  batch_no VARCHAR(60) NULL COMMENT '购物车订单批次号',
  gross_amount DECIMAL(10,2) NOT NULL COMMENT '优惠前金额',
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
  coupon_name VARCHAR(80) NULL COMMENT '使用的优惠券名称',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  payment_method VARCHAR(20) NOT NULL DEFAULT '余额支付' COMMENT '支付方式：余额支付 / 微信支付 / 支付宝支付',
  status VARCHAR(20) NOT NULL DEFAULT '已下单' COMMENT '状态：已下单 / 已取消',
  create_time DATETIME NOT NULL COMMENT '下单时间'
) COMMENT='点餐订单表';

-- 第三方支付主记录：购物车中的多条商品共用一次微信或支付宝付款。
CREATE TABLE `payment_record` (
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

CREATE TABLE `payment_refund` (
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

-- 优惠券模板
CREATE TABLE `coupon_template` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(80) NOT NULL COMMENT '优惠券名称',
  min_spend DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低消费金额',
  discount_amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
  valid_days INT NOT NULL COMMENT '领取后有效天数',
  status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用 / 停用',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='优惠券模板表';

-- 连续签到奖励规则
CREATE TABLE `sign_in_reward_rule` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  consecutive_days INT NOT NULL UNIQUE COMMENT '连续签到天数',
  coupon_template_id BIGINT NOT NULL COMMENT '优惠券模板ID',
  coupon_name VARCHAR(80) NOT NULL COMMENT '优惠券名称快照',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='签到奖励规则表';

-- 用户持有的优惠券
CREATE TABLE `user_coupon` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  template_id BIGINT NOT NULL COMMENT '优惠券模板ID',
  coupon_name VARCHAR(80) NOT NULL COMMENT '优惠券名称',
  min_spend DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '最低消费金额',
  discount_amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
  status VARCHAR(20) NOT NULL DEFAULT '可使用' COMMENT '状态：可使用 / 已使用 / 已过期',
  source_type VARCHAR(30) NOT NULL COMMENT '领取来源',
  source_ref_id BIGINT NULL COMMENT '来源记录ID',
  receive_time DATETIME NOT NULL COMMENT '领取时间',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  use_time DATETIME NULL COMMENT '使用时间',
  order_batch_no VARCHAR(60) NULL COMMENT '使用订单批次号'
) COMMENT='用户优惠券表';

-- 用户签到记录
CREATE TABLE `member_sign_in` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  sign_date DATE NOT NULL COMMENT '签到日期',
  consecutive_days INT NOT NULL COMMENT '连续签到天数',
  reward_rule_id BIGINT NULL COMMENT '命中奖励规则ID',
  reward_coupon_id BIGINT NULL COMMENT '发放优惠券ID',
  create_time DATETIME NOT NULL COMMENT '签到时间',
  UNIQUE KEY uk_member_sign_date (member_id, sign_date)
) COMMENT='会员签到记录表';

-- 充值记录表
CREATE TABLE `recharge_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  recharge_type VARCHAR(30) NOT NULL DEFAULT '余额充值' COMMENT '充值方式：余额充值 / 团购券充值',
  reference_no VARCHAR(80) NULL COMMENT '关联单号或团购券码',
  create_time DATETIME NOT NULL COMMENT '充值时间'
) COMMENT='充值记录表';

-- 推广奖励规则由管理员统一设置。
CREATE TABLE `promotion_rule` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  inviter_reward DECIMAL(10,2) NOT NULL DEFAULT 10.00 COMMENT '邀请人奖励',
  invitee_reward DECIMAL(10,2) NOT NULL DEFAULT 5.00 COMMENT '新用户奖励',
  status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用 / 停用',
  update_time DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='推广奖励规则表';

-- 一个新用户只能出现一次，数据库唯一索引用于防止重复发奖。
CREATE TABLE `invitation_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  invite_code VARCHAR(20) NOT NULL COMMENT '注册时使用的邀请码',
  inviter_member_id BIGINT NOT NULL COMMENT '邀请人会员ID',
  inviter_member_name VARCHAR(50) NOT NULL COMMENT '邀请人姓名快照',
  invitee_member_id BIGINT NOT NULL COMMENT '新用户会员ID',
  invitee_member_name VARCHAR(50) NOT NULL COMMENT '新用户姓名快照',
  inviter_reward DECIMAL(10,2) NOT NULL COMMENT '邀请人奖励快照',
  invitee_reward DECIMAL(10,2) NOT NULL COMMENT '新用户奖励快照',
  status VARCHAR(20) NOT NULL DEFAULT '已奖励' COMMENT '状态',
  create_time DATETIME NOT NULL COMMENT '邀请成功时间',
  UNIQUE KEY uk_invitation_invitee (invitee_member_id),
  KEY idx_invitation_inviter (inviter_member_id)
) COMMENT='邀请成功记录表';

-- 团购券表：每个券码只能核销一次
CREATE TABLE `group_buy_voucher` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  voucher_code VARCHAR(80) NOT NULL UNIQUE COMMENT '团购券码',
  amount DECIMAL(10,2) NOT NULL COMMENT '可充值金额',
  status VARCHAR(20) NOT NULL DEFAULT '未使用' COMMENT '状态：未使用 / 已使用 / 已禁用',
  used_member_id BIGINT NULL COMMENT '核销会员ID',
  used_member_name VARCHAR(50) NULL COMMENT '核销会员姓名',
  used_time DATETIME NULL COMMENT '核销时间',
  expire_time DATETIME NULL COMMENT '过期时间，为空表示长期有效',
  remark VARCHAR(200) NULL COMMENT '备注',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='团购充值券表';

-- 预约记录表
CREATE TABLE `reservation_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  reserve_time DATETIME NOT NULL COMMENT '预约时间',
  status VARCHAR(20) NOT NULL DEFAULT '已预约' COMMENT '状态：已预约 / 已取消 / 已上机',
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='预约记录表';

-- 上机记录表
CREATE TABLE `online_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NULL COMMENT '结束时间',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已扣消费金额',
  segment_start_time DATETIME NULL COMMENT '当前机位计费段开始时间',
  segment_paid_amount DECIMAL(10,2) NULL COMMENT '当前机位计费段已扣金额',
  computer_history VARCHAR(500) NULL COMMENT '本次上机的换机轨迹',
  status VARCHAR(20) NOT NULL COMMENT '状态'
) COMMENT='上机记录表';

-- 机器报修记录表
CREATE TABLE `repair_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  member_id BIGINT NULL COMMENT '报修用户ID，管理员报修可为空',
  member_name VARCHAR(50) NULL COMMENT '报修人',
  problem_description VARCHAR(500) NOT NULL COMMENT '故障说明',
  status VARCHAR(20) NOT NULL DEFAULT '待处理' COMMENT '状态：待处理 / 处理中 / 已完成 / 已取消',
  process_remark VARCHAR(500) NULL COMMENT '处理说明',
  create_time DATETIME NOT NULL COMMENT '报修时间',
  process_time DATETIME NULL COMMENT '开始处理时间',
  finish_time DATETIME NULL COMMENT '完成或取消时间'
) COMMENT='机器报修记录表';

-- 管理员账号：admin / 123456
INSERT INTO `admin` (username, password, real_name) VALUES ('admin', '123456', '管理员');

-- 会员测试数据
INSERT INTO `member` (username, password, id_card, name, phone, balance, user_type, member_level, status, invite_code, create_time) VALUES
('110101199001011234', '123456', '110101199001011234', '张三', '13800000001', 100.00, '会员', '普通会员', '正常', 'WG000001', NOW()),
('110101199002022345', '123456', '110101199002022345', '李四', '13800000002', 60.00, '会员', '黄金会员', '正常', 'WG000002', NOW()),
('110101199003033456', '123456', '110101199003033456', '王五', '13800000003', 0.00, '散客', '散客', '正常', 'WG000003', NOW()),
('110101199004044567', '123456', '110101199004044567', '赵六', '13800000004', 0.00, '会员', '普通会员', '禁用', 'WG000004', NOW());

-- 默认推广奖励：邀请人 10 元，新用户 5 元。
INSERT INTO `promotion_rule` (inviter_reward, invitee_reward, status, update_time) VALUES
(10.00, 5.00, '启用', NOW());

-- 电脑测试数据
INSERT INTO `computer` (computer_no, area, price_per_hour, status, remark) VALUES
('A001', '电竞一区', 6.00, '空闲', '高配电脑'),
('A002', '电竞一区', 6.00, '使用中', '高配电脑'),
('B001', '电竞二区', 5.00, '空闲', '普通电脑'),
('B002', '电竞二区', 5.00, '维修中', '键盘维修'),
('C001', '电竞三区', 8.00, '预约锁定', '已被预约');

-- 点餐商品测试数据
INSERT INTO `food_item` (name, category, price, status, remark) VALUES
('可乐', '饮料', 4.00, '上架', '冰镇饮料'),
('泡面', '餐食', 6.00, '上架', '经典桶面'),
('烤肠', '餐食', 3.00, '上架', '热食'),
('矿泉水', '饮料', 2.00, '上架', '常温饮料');

-- 点餐订单测试数据
INSERT INTO `food_order` (member_id, member_name, customer_type, customer_name, food_item_id, food_item_name, price, quantity, gross_amount, discount_amount, total_amount, payment_method, status, create_time) VALUES
(1, '张三', '会员', '张三', 1, '可乐', 4.00, 2, 8.00, 0.00, 8.00, '余额支付', '已下单', NOW()),
(NULL, NULL, '散客', '散客001', 2, '泡面', 6.00, 1, 6.00, 0.00, 6.00, '微信支付', '已下单', NOW());

-- 默认签到奖励：第 1、3、7 天领取不同点餐优惠券
INSERT INTO `coupon_template` (name, min_spend, discount_amount, valid_days, status, remark, create_time) VALUES
('签到 1 元券', 5.00, 1.00, 7, '启用', '连续签到 1 天奖励', NOW()),
('签到 3 元券', 10.00, 3.00, 7, '启用', '连续签到 3 天奖励', NOW()),
('签到 5 元券', 15.00, 5.00, 10, '启用', '连续签到 7 天奖励', NOW());

INSERT INTO `sign_in_reward_rule` (consecutive_days, coupon_template_id, coupon_name, create_time) VALUES
(1, 1, '签到 1 元券', NOW()),
(3, 2, '签到 3 元券', NOW()),
(7, 3, '签到 5 元券', NOW());

-- 充值记录测试数据
INSERT INTO `recharge_record` (member_id, member_name, amount, recharge_type, create_time) VALUES
(1, '张三', 50.00, '余额充值', NOW()),
(2, '李四', 30.00, '余额充值', NOW());

-- 团购券测试数据：登录用户端后可使用 WGT-TEST-50 核销 50 元
INSERT INTO `group_buy_voucher` (voucher_code, amount, status, expire_time, remark, create_time) VALUES
('WGT-TEST-50', 50.00, '未使用', DATE_ADD(NOW(), INTERVAL 1 YEAR), '演示团购券', NOW());

-- 预约记录测试数据
INSERT INTO `reservation_record` (member_id, member_name, computer_id, computer_no, reserve_time, status, create_time) VALUES
(2, '李四', 5, 'C001', DATE_ADD(NOW(), INTERVAL 30 MINUTE), '已预约', NOW());

-- 上机记录测试数据：A002 当前使用中，方便演示下机结算
INSERT INTO `online_record` (member_id, member_name, computer_id, computer_no, start_time, end_time, total_amount, segment_start_time, segment_paid_amount, computer_history, status) VALUES
(1, '张三', 2, 'A002', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 6.00, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 6.00, 'A002', '进行中'),
(2, '李四', 3, 'B001', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 5.00, DATE_SUB(NOW(), INTERVAL 2 HOUR), 5.00, 'B001', '已完成');
