package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.RepairRecord;

public interface RepairRecordService extends IService<RepairRecord> {

    void report(Long computerId, Long memberId, String problemDescription);

    void process(Long recordId, String status, String processRemark);
}
