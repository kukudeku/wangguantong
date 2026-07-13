-- 网管通会员身份证号登录升级脚本
-- 适合已经部署过用户端登录功能，但 member 表还没有 id_card 字段的数据库。

USE wangguantong;

-- 先读取系统表，判断 id_card 字段是否已经存在。
SET @member_id_card_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'member'
    AND COLUMN_NAME = 'id_card'
);
-- 动态 SQL 保证重复执行本脚本时不会重复添加同名字段。
SET @member_id_card_sql = IF(
  @member_id_card_column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `id_card` VARCHAR(30) NOT NULL DEFAULT '''' COMMENT ''身份证号'' AFTER `password`',
  'SELECT 1'
);
-- 执行动态 SQL 并释放预处理语句。
PREPARE member_id_card_stmt FROM @member_id_card_sql;
EXECUTE member_id_card_stmt;
DEALLOCATE PREPARE member_id_card_stmt;

-- 依次尝试用旧账号、手机号填充空身份证号，最后统一把登录账号改成身份证号。
UPDATE `member` SET id_card = username WHERE id_card = '' AND username <> '';
UPDATE `member` SET id_card = phone WHERE id_card = '' AND phone <> '';
UPDATE `member` SET username = id_card WHERE id_card <> '';
