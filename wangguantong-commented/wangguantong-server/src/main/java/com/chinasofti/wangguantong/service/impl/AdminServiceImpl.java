package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Admin;
import com.chinasofti.wangguantong.mapper.AdminMapper;
import com.chinasofti.wangguantong.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * 管理员业务实现。
 * {@link ServiceImpl} 已经实现了基础 CRUD，本类只补充登录查询。
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin login(String username, String password) {
        // 课堂项目直接明文匹配账号密码，不使用 Spring Security 和 JWT。
        // LambdaQueryWrapper 使用 Java 方法引用指定字段，避免手写数据库列名。
        return getOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username)
                .eq(Admin::getPassword, password));
    }
}
