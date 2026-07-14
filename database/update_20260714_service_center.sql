-- 服务中心升级：原机器报修表扩展为统一服务工单表。
ALTER TABLE `repair_record`
  MODIFY COLUMN `computer_id` BIGINT NULL COMMENT '电脑ID，呼叫网管时可为空',
  MODIFY COLUMN `computer_no` VARCHAR(30) NULL COMMENT '电脑编号',
  ADD COLUMN `service_type` VARCHAR(20) NOT NULL DEFAULT '故障报修' COMMENT '服务类型：呼叫网管 / 故障报修' AFTER `member_name`,
  ADD COLUMN `service_location` VARCHAR(100) NULL COMMENT '用户所在位置' AFTER `service_type`;

UPDATE `repair_record`
SET `service_type` = '故障报修',
    `service_location` = COALESCE(`service_location`, `computer_no`)
WHERE `service_type` IS NULL OR `service_type` = '' OR `service_location` IS NULL;

CREATE INDEX `idx_repair_member_type_status`
  ON `repair_record` (`member_id`, `service_type`, `status`);
