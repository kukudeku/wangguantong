package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.RechargeRecord;

import java.math.BigDecimal;

/** 充值业务接口，负责增加余额并保存对应的充值明细。 */
public interface RechargeRecordService extends IService<RechargeRecord> {

    /** 给指定用户充值；散客首次充值后自动转为普通会员。 */
    void recharge(Long memberId, BigDecimal amount);
}
