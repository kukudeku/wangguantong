package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("member_sign_in")
public class MemberSignIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private LocalDate signDate;
    private Integer consecutiveDays;
    private Long rewardRuleId;
    private Long rewardCouponId;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public LocalDate getSignDate() { return signDate; }
    public void setSignDate(LocalDate signDate) { this.signDate = signDate; }
    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }
    public Long getRewardRuleId() { return rewardRuleId; }
    public void setRewardRuleId(Long rewardRuleId) { this.rewardRuleId = rewardRuleId; }
    public Long getRewardCouponId() { return rewardCouponId; }
    public void setRewardCouponId(Long rewardCouponId) { this.rewardCouponId = rewardCouponId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
