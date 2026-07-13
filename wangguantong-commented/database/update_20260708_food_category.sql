-- 网管通商品分类升级脚本
-- 适合已经部署过旧版点餐功能的数据库。执行一次即可，重复执行不会重复加字段。

USE wangguantong;

-- information_schema.COLUMNS 是 MySQL 保存表结构信息的系统视图。
-- 先统计 category 字段是否存在，再决定是否执行 ALTER TABLE。
SET @food_category_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'food_item'
    AND COLUMN_NAME = 'category'
);
-- 字段不存在时生成加字段 SQL，存在时执行无副作用的 SELECT 1。
SET @food_category_sql = IF(
  @food_category_column_exists = 0,
  'ALTER TABLE `food_item` ADD COLUMN `category` VARCHAR(30) NOT NULL DEFAULT ''其他'' COMMENT ''商品分类'' AFTER `name`',
  'SELECT 1'
);
-- MySQL 动态 SQL 的固定流程：准备 -> 执行 -> 释放。
PREPARE food_category_stmt FROM @food_category_sql;
EXECUTE food_category_stmt;
DEALLOCATE PREPARE food_category_stmt;

-- 给旧商品补分类：将名称和备注合并后用正则匹配，不能识别的保留“其他”。
UPDATE `food_item`
SET category = CASE
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '可乐|雪碧|水|茶|咖啡|饮|奶|汁' THEN '饮料'
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '泡面|饭|面|餐|肠|热狗|汉堡' THEN '餐食'
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '薯片|饼干|糖|巧克力|零食' THEN '零食'
  ELSE '其他'
END
WHERE category IS NULL OR category = '' OR category = '其他';
