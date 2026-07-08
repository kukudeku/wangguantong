package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Admin;
import com.chinasofti.wangguantong.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Admin admin) {
        Admin loginAdmin = adminService.login(admin.getUsername(), admin.getPassword());
        if (loginAdmin == null) {
            return Result.error("账号或密码错误");
        }
        return Result.success(loginAdmin);
    }
}
