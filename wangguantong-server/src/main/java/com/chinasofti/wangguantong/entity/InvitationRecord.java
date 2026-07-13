package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("invitation_record")
public class InvitationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String inviteCode;
    private Long inviterMemberId;
    private String inviterMemberName;
    private Long inviteeMemberId;
    private String inviteeMemberName;
    private BigDecimal inviterReward;
    private BigDecimal inviteeReward;
    private String status;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Long getInviterMemberId() {
        return inviterMemberId;
    }

    public void setInviterMemberId(Long inviterMemberId) {
        this.inviterMemberId = inviterMemberId;
    }

    public String getInviterMemberName() {
        return inviterMemberName;
    }

    public void setInviterMemberName(String inviterMemberName) {
        this.inviterMemberName = inviterMemberName;
    }

    public Long getInviteeMemberId() {
        return inviteeMemberId;
    }

    public void setInviteeMemberId(Long inviteeMemberId) {
        this.inviteeMemberId = inviteeMemberId;
    }

    public String getInviteeMemberName() {
        return inviteeMemberName;
    }

    public void setInviteeMemberName(String inviteeMemberName) {
        this.inviteeMemberName = inviteeMemberName;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
