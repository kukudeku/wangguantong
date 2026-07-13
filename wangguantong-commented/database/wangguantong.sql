-- 网管通 —— 电竞网吧运营管理系统 数据库脚本
-- 执行方式：在 MySQL 客户端中执行本文件即可创建数据库、表和测试数据

CREATE DATABASE IF NOT EXISTS wangguantong DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 后面的建表和测试数据插入都在 wangguantong 数据库中执行。
USE wangguantong;

-- 先删除旧表，保证脚本可从干净结构重新初始化。
-- 被业务表引用/依赖的数据表先删，基础资料表后删，便于以后加入外键时仍保持合理顺序。
DROP TABLE IF EXISTS `online_record`;
DROP TABLE IF EXISTS `reservation_record`;
DROP TABLE IF EXISTS `food_order`;
DROP TABLE IF EXISTS `food_item`;
DROP TABLE IF EXISTS `recharge_record`;
DROP TABLE IF EXISTS `computer`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `admin`;

-- 管理员表：只用于 /admin 后台登录。
CREATE TABLE `admin` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(50) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名'
) COMMENT='管理员表';

-- 会员表：同时保存用户登录资料、会员属性和账户余额。
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
  create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT='会员表';

-- 电脑机位表：status 决定座位图颜色以及是否允许上机/预约。
CREATE TABLE `computer` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  area VARCHAR(50) NOT NULL COMMENT '区域',
  price_per_hour DECIMAL(10,2) NOT NULL COMMENT '每小时单价',
  status VARCHAR(20) NOT NULL DEFAULT '空闲' COMMENT '状态',
  remark VARCHAR(200) NULL COMMENT '备注'
) COMMENT='电脑机位表';

-- 点餐商品表：只有 status=上架 的商品会出现在用户端。
CREATE TABLE `food_item` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '商品名称',
  category VARCHAR(30) NOT NULL DEFAULT '其他' COMMENT '商品分类',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  status VARCHAR(20) NOT NULL DEFAULT '上架' COMMENT '状态：上架 / 下架',
  remark VARCHAR(200) NULL COMMENT '备注'
) COMMENT='点餐商品表';

-- 点餐订单表：保存下单时的商品名称和价格快照，避免商品改价影响历史订单。
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
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  status VARCHAR(20) NOT NULL DEFAULT '已下单' COMMENT '状态：已下单 / 已取消',
  create_time DATETIME NOT NULL COMMENT '下单时间'
) COMMENT='点餐订单表';

-- 充值记录表：每次增加余额都留下独立明细。
CREATE TABLE `recharge_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  create_time DATETIME NOT NULL COMMENT '充值时间'
) COMMENT='充值记录表';

-- 预约记录表：状态与 computer.status 配合，控制机位锁定和释放。
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

-- 上机记录表：total_amount 是已经实时扣除的累计金额，不是等下机后才计算。
CREATE TABLE `online_record` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  member_id BIGINT NOT NULL COMMENT '会员ID',
  member_name VARCHAR(50) NOT NULL COMMENT '会员姓名',
  computer_id BIGINT NOT NULL COMMENT '电脑ID',
  computer_no VARCHAR(30) NOT NULL COMMENT '电脑编号',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NULL COMMENT '结束时间',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已扣消费金额',
  status VARCHAR(20) NOT NULL COMMENT '状态'
) COMMENT='上机记录表';

-- ===== 以下是课堂演示测试数据 =====
-- 管理员账号：admin / 123456
INSERT INTO `admin` (username, password, real_name) VALUES ('admin', '123456', '管理员');

-- 会员测试数据
INSERT INTO `member` (username, password, id_card, name, phone, balance, user_type, member_level, status, create_time) VALUES
('110101199001011234', '123456', '110101199001011234', '张三', '13800000001', 100.00, '会员', '普通会员', '正常', NOW()),
('110101199002022345', '123456', '110101199002022345', '李四', '13800000002', 60.00, '会员', '黄金会员', '正常', NOW()),
('110101199003033456', '123456', '110101199003033456', '王五', '13800000003', 0.00, '散客', '散客', '正常', NOW()),
('110101199004044567', '123456', '110101199004044567', '赵六', '13800000004', 0.00, '会员', '普通会员', '禁用', NOW());

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
INSERT INTO `food_order` (member_id, member_name, customer_type, customer_name, food_item_id, food_item_name, price, quantity, total_amount, status, create_time) VALUES
(1, '张三', '会员', '张三', 1, '可乐', 4.00, 2, 8.00, '已下单', NOW()),
(NULL, NULL, '散客', '散客001', 2, '泡面', 6.00, 1, 6.00, '已下单', NOW());

-- 充值记录测试数据
INSERT INTO `recharge_record` (member_id, member_name, amount, create_time) VALUES
(1, '张三', 50.00, NOW()),
(2, '李四', 30.00, NOW());

-- 预约记录测试数据
INSERT INTO `reservation_record` (member_id, member_name, computer_id, computer_no, reserve_time, status, create_time) VALUES
(2, '李四', 5, 'C001', DATE_ADD(NOW(), INTERVAL 30 MINUTE), '已预约', NOW());

-- 上机记录测试数据：A002 当前使用中，方便演示下机结算
INSERT INTO `online_record` (member_id, member_name, computer_id, computer_no, start_time, end_time, total_amount, status) VALUES
(1, '张三', 2, 'A002', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, 0.00, '进行中'),
(2, '李四', 3, 'B001', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 5.00, '已完成');
