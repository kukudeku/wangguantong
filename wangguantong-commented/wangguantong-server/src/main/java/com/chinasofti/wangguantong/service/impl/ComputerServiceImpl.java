package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.mapper.ComputerMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import org.springframework.stereotype.Service;

/**
 * 电脑机位业务实现。
 *
 * <p>{@code @Service} 让 Spring 创建并管理这个对象；继承 {@link ServiceImpl}
 * 后可直接使用 {@code list()}、{@code getById()}、{@code save()}、{@code updateById()} 等方法。</p>
 */
@Service
public class ComputerServiceImpl extends ServiceImpl<ComputerMapper, Computer> implements ComputerService {
}
