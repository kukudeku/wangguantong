package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("promotion_rule")
public class PromotionRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private BigDecimal inviterReward;
    private BigDecimal inviteeReward;
    private String status;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getInviterReward() {
        return inviterReward;
    }

    public void setInviterReward(BigDecimal inviterReward) {
        this.inviterReward = inviterReward;
    }

    public BigDecimal getInviteeReward() {
        return inviteeReward;
    }

    public void setInviteeReward(BigDecimal inviteeReward) {
        this.inviteeReward = inviteeReward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
