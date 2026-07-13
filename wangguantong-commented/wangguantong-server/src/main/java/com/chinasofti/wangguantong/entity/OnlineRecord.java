package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 上机记录实体，对应 online_record 表。
 *
 * <p>前半部分字段真实保存在数据库中；带 {@link TableField} exist=false 的字段
 * 只在接口返回前临时计算，用于向前端展示实时上机信息。</p>
 */
@TableName("online_record")
public class OnlineRecord {

    /** 上机记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 上机会员 id。 */
    private Long memberId;

    /** 开始上机时保存的会员姓名。 */
    private String memberName;

    /** 使用的电脑 id。 */
    private Long computerId;

    /** 开始上机时保存的电脑编号。 */
    private String computerNo;

    /** 上机开始时间。 */
    private LocalDateTime startTime;

    /** 下机时间；进行中的记录为 null。 */
    private LocalDateTime endTime;

    /** 已经从会员余额扣除的累计费用，不是等待下机才计算的费用。 */
    private BigDecimal totalAmount;

    /** 记录状态：进行中或已完成。 */
    private String status;

    /** 实时上机分钟数，由 Service 根据当前时间计算，不写入数据库。 */
    @TableField(exist = false)
    private Long runningMinutes;

    /** 按“向上取整”得到的计费小时数，最少按 1 小时计费。 */
    @TableField(exist = false)
    private Long chargeHours;

    /** 按当前时长计算的应扣总金额。 */
    @TableField(exist = false)
    private BigDecimal currentAmount;

    /** 当前会员余额快照，方便前端同一接口显示。 */
    @TableField(exist = false)
    private BigDecimal memberBalance;

    /** 当前会员级别。 */
    @TableField(exist = false)
    private String memberLevel;

    /** 实际采用的折扣率，例如 0.9 表示九折。 */
    @TableField(exist = false)
    private BigDecimal discountRate;

    /** 当前余额能否支付下一计费小时。 */
    @TableField(exist = false)
    private Boolean balanceEnough;

    /** 给前端显示的余额提示文本。 */
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
