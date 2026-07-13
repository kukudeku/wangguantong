-- 网管通功能升级：邀请码推广计划
-- 适用于已有本地或宝塔数据库，可重复执行。

USE wangguantong;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND COLUMN_NAME = 'invite_code');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `invite_code` VARCHAR(20) NULL COMMENT ''唯一邀请码'' AFTER `status`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND COLUMN_NAME = 'inviter_member_id');
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE `member` ADD COLUMN `inviter_member_id` BIGINT NULL COMMENT ''邀请人会员ID'' AFTER `invite_code`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `member`
SET `invite_code` = CONCAT('WG', LPAD(id, 6, '0'))
WHERE `invite_code` IS NULL OR `invite_code` = '';

SET @index_exists = (SELECT COUNT(1) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND INDEX_NAME = 'uk_member_invite_code');
SET @sql = IF(@index_exists = 0,
  'ALTER TABLE `member` ADD UNIQUE KEY `uk_member_invite_code` (`invite_code`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `promotion_rule` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  inviter_reward DECIMAL(10,2) NOT NULL DEFAULT 10.00 COMMENT '邀请人奖励',
  invitee_reward DECIMAL(10,2) NOT NULL DEFAULT 5.00 COMMENT '新用户奖励',
  status VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态：启用 / 停用',
  update_time DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='推广奖励规则表';

CREATE TABLE IF NOT EXISTS `invitation_record` (
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
  UNIQUE KEY uk_invitation_invitee (`invitee_member_id`),
  KEY idx_invitation_inviter (`inviter_member_id`)
) COMMENT='邀请成功记录表';

INSERT INTO `promotion_rule` (inviter_reward, invitee_reward, status, update_time)
SELECT 10.00, 5.00, '启用', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `promotion_rule`);
