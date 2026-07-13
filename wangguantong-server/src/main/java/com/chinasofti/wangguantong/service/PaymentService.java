package com.chinasofti.wangguantong.service;

import com.chinasofti.wangguantong.dto.PaymentCreateResponse;
import com.chinasofti.wangguantong.dto.PaymentStatusResponse;
import com.chinasofti.wangguantong.entity.FoodOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    PaymentCreateResponse createPayment(String orderBatchNo, Long memberId, String paymentMethod, BigDecimal amount);

    PaymentStatusResponse queryPayment(String outTradeNo);

    byte[] createWechatQrCode(String outTradeNo);

    String createAlipayPage(String outTradeNo);

    void handleWechatPayNotify(Map<String, String> headers, String body);

    void handleWechatRefundNotify(Map<String, String> headers, String body);

    void handleAlipayNotify(Map<String, String> parameters);

    void refundOrder(FoodOrder order);

    void fillPaymentStatuses(List<FoodOrder> orders);
}
