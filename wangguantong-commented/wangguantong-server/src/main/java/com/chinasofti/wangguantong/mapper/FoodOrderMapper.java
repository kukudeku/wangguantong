package com.chinasofti.wangguantong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chinasofti.wangguantong.entity.FoodOrder;
import org.apache.ibatis.annotations.Mapper;

/** 点餐订单表数据访问接口，通用 SQL 由 {@link BaseMapper} 提供。 */
@Mapper
public interface FoodOrderMapper extends BaseMapper<FoodOrder> {
}
