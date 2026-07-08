package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.Admin;

public interface AdminService extends IService<Admin> {

    Admin login(String username, String password);
}
