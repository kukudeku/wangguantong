-- 网管通会员身份证号登录升级脚本
-- 适合已经部署过用户端登录功能，但 member 表还没有 id_card 字段的数据库。

USE wangguantong;

SET @member_id_card_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'member'
    AND COLUMN_NAME = 'id_card'
);
SET @member_id_card_sql = IF(
  @member_id_card_column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `id_card` VARCHAR(30) NOT NULL DEFAULT '''' COMMENT ''身份证号'' AFTER `password`',
  'SELECT 1'
);
PREPARE member_id_card_stmt FROM @member_id_card_sql;
EXECUTE member_id_card_stmt;
DEALLOCATE PREPARE member_id_card_stmt;

UPDATE `member` SET id_card = username WHERE id_card = '' AND username <> '';
UPDATE `member` SET id_card = phone WHERE id_card = '' AND phone <> '';
UPDATE `member` SET username = id_card WHERE id_card <> '';
