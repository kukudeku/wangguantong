package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("online_record")
public class OnlineRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private String memberName;
    private Long computerId;
    private String computerNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private String status;

    @TableField(exist = false)
    private Long runningMinutes;

    @TableField(exist = false)
    private Long chargeHours;

    @TableField(exist = false)
    private BigDecimal currentAmount;

    @TableField(exist = false)
    private BigDecimal memberBalance;

    @TableField(exist = false)
    private String memberLevel;

    @TableField(exist = false)
    private BigDecimal discountRate;

    @TableField(exist = false)
    private Boolean balanceEnough;

    @TableField(exist = false)
    private String warningMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerNo() {
        return computerNo;
    }

    public void setComputerNo(String computerNo) {
        this.computerNo = computerNo;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRunningMinutes() {
        return runningMinutes;
    }

    public void setRunningMinutes(Long runningMinutes) {
        this.runningMinutes = runningMinutes;
    }

    public Long getChargeHours() {
        return chargeHours;
    }

    public void setChargeHours(Long chargeHours) {
        this.chargeHours = chargeHours;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getMemberBalance() {
        return memberBalance;
    }

    public void setMemberBalance(BigDecimal memberBalance) {
        this.memberBalance = memberBalance;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(String memberLevel) {
        this.memberLevel = memberLevel;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Boolean getBalanceEnough() {
        return balanceEnough;
    }

    public void setBalanceEnough(Boolean balanceEnough) {
        this.balanceEnough = balanceEnough;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}
