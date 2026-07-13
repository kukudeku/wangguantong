package com.chinasofti.wangguantong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chinasofti.wangguantong.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员数据访问接口。
 * {@link BaseMapper} 已提供按主键查询、新增、修改、删除等常用 SQL，无需手写 XML。
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
