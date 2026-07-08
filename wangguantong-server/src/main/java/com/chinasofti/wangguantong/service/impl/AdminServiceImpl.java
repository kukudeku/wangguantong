package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Admin;
import com.chinasofti.wangguantong.mapper.AdminMapper;
import com.chinasofti.wangguantong.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin login(String username, String password) {
        // 课堂项目直接明文匹配账号密码，不使用 Spring Security 和 JWT。
        return getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username)
                .eq(Admin::getPassword, password));
    }
}
