package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payment_refund")
public class PaymentRefund {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paymentRecordId;
    private Long foodOrderId;
    private String outRefundNo;
    private BigDecimal amount;
    private String status;
    private String providerRefundNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime refundedTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaymentRecordId() { return paymentRecordId; }
    public void setPaymentRecordId(Long paymentRecordId) { this.paymentRecordId = paymentRecordId; }
    public Long getFoodOrderId() { return foodOrderId; }
    public void setFoodOrderId(Long foodOrderId) { this.foodOrderId = foodOrderId; }
    public String getOutRefundNo() { return outRefundNo; }
    public void setOutRefundNo(String outRefundNo) { this.outRefundNo = outRefundNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProviderRefundNo() { return providerRefundNo; }
    public void setProviderRefundNo(String providerRefundNo) { this.providerRefundNo = providerRefundNo; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public LocalDateTime getRefundedTime() { return refundedTime; }
    public void setRefundedTime(LocalDateTime refundedTime) { this.refundedTime = refundedTime; }
}
