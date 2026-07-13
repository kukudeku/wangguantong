package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 管理员实体，对应数据库 admin 表。
 * 实体类主要保存数据；MyBatis-Plus 会根据属性名与数据库下划线字段自动映射。
 */
@TableName("admin")
public class Admin {

    /** 管理员主键；AUTO 表示由 MySQL 自增生成。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，例如 admin。 */
    private String username;

    /** 登录密码。课堂项目按要求使用明文，正式项目必须加密保存。 */
    private String password;

    /** 管理员显示名称，例如“系统管理员”。 */
    private String realName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
