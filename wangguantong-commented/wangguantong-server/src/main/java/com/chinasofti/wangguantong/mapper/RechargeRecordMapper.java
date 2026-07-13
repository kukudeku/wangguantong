package com.chinasofti.wangguantong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import org.apache.ibatis.annotations.Mapper;

/** 充值记录表数据访问接口，通用 SQL 由 {@link BaseMapper} 提供。 */
@Mapper
public interface RechargeRecordMapper extends BaseMapper<RechargeRecord> {
}
