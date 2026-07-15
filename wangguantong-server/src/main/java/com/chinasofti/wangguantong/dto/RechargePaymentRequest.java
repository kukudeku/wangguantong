package com.chinasofti.wangguantong.dto;

import java.math.BigDecimal;

/** 用户发起余额充值支付时提交的参数。 */
public class RechargePaymentRequest {

    private Long memberId;
    private BigDecimal amount;
    private String paymentMethod;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
