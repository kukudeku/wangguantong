package com.chinasofti.wangguantong.dto;

import java.math.BigDecimal;

/** 返回给前端的支付发起结果，不包含任何商户密钥。 */
public class PaymentCreateResponse {

    private String businessType;
    private String orderBatchNo;
    private String outTradeNo;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getOrderBatchNo() { return orderBatchNo; }
    public void setOrderBatchNo(String orderBatchNo) { this.orderBatchNo = orderBatchNo; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
