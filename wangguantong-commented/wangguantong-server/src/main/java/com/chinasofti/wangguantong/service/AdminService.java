package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.Admin;

/**
 * 管理员业务接口。
 *
 * <p>继承 {@link IService} 后，会自动拥有按主键查询、新增、修改、删除等常用方法；
 * 这里只声明项目额外需要的“管理员登录”业务。</p>
 */
public interface AdminService extends IService<Admin> {

    /**
     * 使用管理员账号和密码查询匹配的管理员。
     *
     * @return 匹配成功返回管理员对象，匹配失败返回 {@code null}
     */
    Admin login(String username, String password);
}
