package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员/用户实体，对应 member 表。
 *
 * <p>表名使用反引号是因为 member 在部分数据库环境中可能与关键字或系统对象冲突。</p>
 */
@TableName("`member`")
public class Member {

    /** 会员主键。充值、上机、预约、点餐都通过此 id 关联会员。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名。当前项目为了兼容旧字段，与身份证号保持一致。 */
    private String username;

    /** 用户登录密码。课堂项目未加密，不能直接用于正式生产环境。 */
    private String password;

    /** 身份证号，也是用户端实际登录账号。 */
    private String idCard;

    /** 会员姓名。 */
    private String name;

    /** 手机号，用于后台查询和用户资料展示。 */
    private String phone;

    /** 可用余额；上机和点餐实时从这里扣减。 */
    private BigDecimal balance;

    /** 用户类型：会员或散客。散客充值后会转为会员。 */
    private String userType;

    /** 会员级别：普通会员、黄金会员、钻石会员。 */
    private String memberLevel;

    /** 账号状态：正常或禁用。 */
    private String status;

    /** 注册时间。 */
    private LocalDateTime createTime;

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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
