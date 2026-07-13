package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Admin;
import com.chinasofti.wangguantong.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员登录接口。
 *
 * <p>本项目按课堂作业要求不使用 Spring Security 和 JWT。
 * 登录成功后直接把管理员信息返回给前端，由前端保存到 localStorage。</p>
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    /** 通过构造方法注入业务层，Controller 不直接操作数据库。 */
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 管理员登录。
     *
     * @param admin 前端 JSON 中的 username 和 password 会自动填入此对象
     * @return 登录成功返回管理员信息，失败返回可读错误提示
     */
    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Admin admin) {
        // 账号密码匹配逻辑放在 Service 中，Controller 只负责接收和返回。
        Admin loginAdmin = adminService.login(admin.getUsername(), admin.getPassword());
        if (loginAdmin == null) {
            return Result.error("账号或密码错误");
        }
        return Result.success(loginAdmin);
    }
}
