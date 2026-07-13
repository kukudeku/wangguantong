-- 网管通用户端登录扩展脚本
-- 适合已经有 member 表，但还没有 username/password/id_card 字段的数据库。

USE wangguantong;

-- 本脚本对三个字段重复使用同一模式：
-- 1. 查询 information_schema；2. 生成动态 ALTER SQL；3. PREPARE/EXECUTE 执行。

-- 补充登录账号字段。
SET @member_username_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'member'
    AND COLUMN_NAME = 'username'
);
SET @member_username_sql = IF(
  @member_username_column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `username` VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''登录账号'' AFTER `id`',
  'SELECT 1'
);
PREPARE member_username_stmt FROM @member_username_sql;
EXECUTE member_username_stmt;
DEALLOCATE PREPARE member_username_stmt;

-- 补充登录密码字段，旧用户默认密码为 123456。
SET @member_password_column_exists = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'member'
    AND COLUMN_NAME = 'password'
);
SET @member_password_sql = IF(
  @member_password_column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `password` VARCHAR(50) NOT NULL DEFAULT ''123456'' COMMENT ''登录密码'' AFTER `username`',
  'SELECT 1'
);
PREPARE member_password_stmt FROM @member_password_sql;
EXECUTE member_password_stmt;
DEALLOCATE PREPARE member_password_stmt;

-- 补充身份证号字段；用户端最终使用身份证号作为登录账号。
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

-- 迁移旧数据：先利用已有账号/手机号填空，再令 username 与 id_card 保持一致。
UPDATE `member` SET username = phone WHERE username = '';
UPDATE `member` SET id_card = username WHERE id_card = '' AND username <> '';
UPDATE `member` SET id_card = phone WHERE id_card = '' AND phone <> '';
UPDATE `member` SET username = id_card WHERE id_card <> '';
