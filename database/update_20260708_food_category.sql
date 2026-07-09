-- 网管通商品分类升级脚本
-- 适合已经部署过旧版点餐功能的数据库。执行一次即可，重复执行不会重复加字段。

USE wangguantong;

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

UPDATE `food_item`
SET category = CASE
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '可乐|雪碧|水|茶|咖啡|饮|奶|汁' THEN '饮料'
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '泡面|饭|面|餐|肠|热狗|汉堡' THEN '餐食'
  WHEN CONCAT(name, IFNULL(remark, '')) REGEXP '薯片|饼干|糖|巧克力|零食' THEN '零食'
  ELSE '其他'
END
WHERE category IS NULL OR category = '' OR category = '其他';
