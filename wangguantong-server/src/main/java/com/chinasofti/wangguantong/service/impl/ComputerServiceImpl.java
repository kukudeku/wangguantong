package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.mapper.ComputerMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import org.springframework.stereotype.Service;

@Service
public class ComputerServiceImpl extends ServiceImpl<ComputerMapper, Computer> implements ComputerService {
}
