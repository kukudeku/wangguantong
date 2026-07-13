package com.chinasofti.wangguantong.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentStatusResponse {

    private String orderBatchNo;
    private String outTradeNo;
    private String paymentMethod;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String status;
    private LocalDateTime expireTime;
    private LocalDateTime paidTime;

    public String getOrderBatchNo() { return orderBatchNo; }
    public void setOrderBatchNo(String orderBatchNo) { this.orderBatchNo = orderBatchNo; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public LocalDateTime getPaidTime() { return paidTime; }
    public void setPaidTime(LocalDateTime paidTime) { this.paidTime = paidTime; }
}
