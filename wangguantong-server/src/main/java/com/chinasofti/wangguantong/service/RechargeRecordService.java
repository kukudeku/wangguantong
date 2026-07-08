package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.RechargeRecord;

import java.math.BigDecimal;

public interface RechargeRecordService extends IService<RechargeRecord> {

    void recharge(Long memberId, BigDecimal amount);
}
