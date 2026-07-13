package com.chinasofti.wangguantong.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chinasofti.wangguantong.config.PaymentProperties;
import com.chinasofti.wangguantong.dto.PaymentCreateResponse;
import com.chinasofti.wangguantong.dto.PaymentStatusResponse;
import com.chinasofti.wangguantong.entity.FoodOrder;
import com.chinasofti.wangguantong.entity.PaymentRecord;
import com.chinasofti.wangguantong.entity.PaymentRefund;
import com.chinasofti.wangguantong.mapper.FoodOrderMapper;
import com.chinasofti.wangguantong.mapper.PaymentRecordMapper;
import com.chinasofti.wangguantong.mapper.PaymentRefundMapper;
import com.chinasofti.wangguantong.service.CouponService;
import com.chinasofti.wangguantong.service.PaymentService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String WECHAT_PAYMENT = "微信支付";
    private static final String ALIPAY_PAYMENT = "支付宝支付";
    private static final String PENDING = "待支付";
    private static final String PAID = "已支付";
    private static final String CLOSED = "已关闭";
    private static final String PARTIAL_REFUND = "部分退款";
    private static final String REFUNDING = "退款中";
    private static final String REFUNDED = "已退款";
    private static final DateTimeFormatter WECHAT_RFC3339_SECONDS =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX");

    private final PaymentProperties properties;
    private final PaymentRecordMapper paymentRecordMapper;
    private final PaymentRefundMapper paymentRefundMapper;
    private final FoodOrderMapper foodOrderMapper;
    private final CouponService couponService;

    private volatile RSAAutoCertificateConfig wechatConfig;
    private volatile NativePayService nativePayService;
    private volatile RefundService wechatRefundService;
    private volatile NotificationParser wechatNotificationParser;
    private volatile AlipayClient alipayClient;
    private volatile String alipayPublicKey;

    public PaymentServiceImpl(PaymentProperties properties,
                              PaymentRecordMapper paymentRecordMapper,
                              PaymentRefundMapper paymentRefundMapper,
                              FoodOrderMapper foodOrderMapper,
                              CouponService couponService) {
        this.properties = properties;
        this.paymentRecordMapper = paymentRecordMapper;
        this.paymentRefundMapper = paymentRefundMapper;
        this.foodOrderMapper = foodOrderMapper;
        this.couponService = couponService;
    }

    @Override
    public PaymentCreateResponse createPayment(String orderBatchNo,
                                               Long memberId,
                                               String paymentMethod,
                                               BigDecimal amount) {
        if (!(WECHAT_PAYMENT.equals(paymentMethod) || ALIPAY_PAYMENT.equals(paymentMethod))) {
            throw new RuntimeException("只有微信支付或支付宝支付需要创建第三方支付单");
        }
        if (!StringUtils.hasText(orderBatchNo) || amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("支付订单信息不完整");
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentRecord record = new PaymentRecord();
        record.setOrderBatchNo(orderBatchNo);
        record.setMemberId(memberId);
        record.setPaymentMethod(paymentMethod);
        record.setOutTradeNo(createTradeNo());
        record.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        record.setRefundedAmount(BigDecimal.ZERO);
        record.setStatus(amount.compareTo(BigDecimal.ZERO) == 0 ? PAID : PENDING);
        record.setExpireTime(now.plusMinutes(Math.max(1, properties.getOrderExpireMinutes())));
        record.setPaidTime(amount.compareTo(BigDecimal.ZERO) == 0 ? now : null);
        record.setCreateTime(now);
        record.setUpdateTime(now);

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            if (WECHAT_PAYMENT.equals(paymentMethod)) {
                createWechatPrepay(record);
            } else {
                requireAlipayClient();
            }
        }
        paymentRecordMapper.insert(record);
        return toCreateResponse(record);
    }

    @Override
    public PaymentStatusResponse queryPayment(String outTradeNo) {
        PaymentRecord record = requirePayment(outTradeNo);
        if (PENDING.equals(record.getStatus())) {
            if (WECHAT_PAYMENT.equals(record.getPaymentMethod())) {
                queryWechatPayment(record);
            } else if (ALIPAY_PAYMENT.equals(record.getPaymentMethod())) {
                queryAlipayPayment(record);
            }
            record = requirePayment(outTradeNo);
        }
        return toStatusResponse(record);
    }

    @Override
    public byte[] createWechatQrCode(String outTradeNo) {
        PaymentRecord record = requirePayment(outTradeNo);
        if (!WECHAT_PAYMENT.equals(record.getPaymentMethod()) || !StringUtils.hasText(record.getCodeUrl())) {
            throw new RuntimeException("该支付单没有微信支付二维码");
        }
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(record.getCodeUrl(), BarcodeFormat.QR_CODE, 320, 320, hints);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("微信支付二维码生成失败");
        }
    }

    @Override
    public String createAlipayPage(String outTradeNo) {
        PaymentRecord record = requirePayment(outTradeNo);
        if (!ALIPAY_PAYMENT.equals(record.getPaymentMethod())) {
            throw new RuntimeException("该支付单不是支付宝支付");
        }
        if (!PENDING.equals(record.getStatus())) {
            throw new RuntimeException("该支付单当前状态为" + record.getStatus());
        }

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(record.getOutTradeNo());
        model.setTotalAmount(money(record.getAmount()));
        model.setSubject("网管通点餐-" + record.getOrderBatchNo());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeExpire(record.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(properties.getAlipay().getNotifyUrl());
        if (StringUtils.hasText(properties.getAlipay().getReturnUrl())) {
            request.setReturnUrl(properties.getAlipay().getReturnUrl());
        }
        try {
            AlipayTradePagePayResponse response = requireAlipayClient().pageExecute(request);
            if (!response.isSuccess() && !StringUtils.hasText(response.getBody())) {
                throw new RuntimeException(alipayError(response.getSubMsg(), response.getMsg()));
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝收银台创建失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatPayNotify(Map<String, String> headers, String body) {
        RequestParam requestParam = createWechatRequestParam(headers, body);
        Transaction transaction = requireWechatParser().parse(requestParam, Transaction.class);
        PaymentRecord record = requirePayment(transaction.getOutTradeNo());
        validateWechatTransaction(record, transaction);
        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            markPaid(record, transaction.getTransactionId(), parseWechatTime(transaction.getSuccessTime()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWechatRefundNotify(Map<String, String> headers, String body) {
        RequestParam requestParam = createWechatRequestParam(headers, body);
        RefundNotification notification = requireWechatParser().parse(requestParam, RefundNotification.class);
        PaymentRefund refund = paymentRefundMapper.selectOne(new LambdaQueryWrapper<PaymentRefund>()
                .eq(PaymentRefund::getOutRefundNo, notification.getOutRefundNo())
                .last("LIMIT 1"));
        if (refund == null) {
            throw new RuntimeException("退款记录不存在");
        }
        if (notification.getAmount() == null
                || notification.getAmount().getRefund() == null
                || notification.getAmount().getRefund() != toCents(refund.getAmount())) {
            throw new RuntimeException("微信退款通知金额不一致");
        }
        if (Status.SUCCESS.equals(notification.getRefundStatus())) {
            completeRefund(refund, notification.getRefundId(), parseWechatTime(notification.getSuccessTime()));
        } else if (Status.ABNORMAL.equals(notification.getRefundStatus())
                || Status.CLOSED.equals(notification.getRefundStatus())) {
            failRefund(refund);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayNotify(Map<String, String> parameters) {
        try {
            if (!AlipaySignature.rsaCheckV1(parameters, requireAlipayPublicKey(), StandardCharsets.UTF_8.name(), "RSA2")) {
                throw new RuntimeException("支付宝通知验签失败");
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝通知验签失败：" + e.getMessage());
        }

        String outTradeNo = parameters.get("out_trade_no");
        PaymentRecord record = requirePayment(outTradeNo);
        if (!properties.getAlipay().getAppId().equals(parameters.get("app_id"))) {
            throw new RuntimeException("支付宝通知应用编号不一致");
        }
        if (StringUtils.hasText(properties.getAlipay().getSellerId())
                && !properties.getAlipay().getSellerId().equals(parameters.get("seller_id"))) {
            throw new RuntimeException("支付宝通知收款账号不一致");
        }
        BigDecimal notifyAmount = new BigDecimal(parameters.getOrDefault("total_amount", "-1"));
        if (record.getAmount().compareTo(notifyAmount) != 0) {
            throw new RuntimeException("支付宝通知金额不一致");
        }
        String tradeStatus = parameters.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            markPaid(record, parameters.get("trade_no"), LocalDateTime.now());
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            closeLocalPayment(record);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void refundOrder(FoodOrder order) {
        if (order == null || order.getId() == null || !StringUtils.hasText(order.getBatchNo())) {
            throw new RuntimeException("订单没有可退款的第三方支付记录");
        }
        PaymentRecord payment = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOrderBatchNo, order.getBatchNo())
                .last("LIMIT 1"));
        if (payment == null || !(PAID.equals(payment.getStatus()) || PARTIAL_REFUND.equals(payment.getStatus()))) {
            throw new RuntimeException("订单尚未支付或当前不能退款");
        }
        Long refundingCount = paymentRefundMapper.selectCount(new LambdaQueryWrapper<PaymentRefund>()
                .eq(PaymentRefund::getPaymentRecordId, payment.getId())
                .eq(PaymentRefund::getStatus, REFUNDING));
        if (refundingCount > 0) {
            throw new RuntimeException("该批次有退款正在处理，请稍后再试");
        }

        PaymentRefund refund = paymentRefundMapper.selectOne(new LambdaQueryWrapper<PaymentRefund>()
                .eq(PaymentRefund::getFoodOrderId, order.getId())
                .last("LIMIT 1"));
        if (refund != null && REFUNDED.equals(refund.getStatus())) {
            throw new RuntimeException("该订单已经退款");
        }

        BigDecimal refundAmount = order.getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            completeZeroAmountRefund(order, payment);
            return;
        }
        if (payment.getRefundedAmount().add(refundAmount).compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("退款金额超过原支付金额");
        }

        LocalDateTime now = LocalDateTime.now();
        if (refund == null) {
            refund = new PaymentRefund();
            refund.setPaymentRecordId(payment.getId());
            refund.setFoodOrderId(order.getId());
            refund.setOutRefundNo(createRefundNo());
            refund.setAmount(refundAmount);
            refund.setStatus(REFUNDING);
            refund.setCreateTime(now);
            refund.setUpdateTime(now);
            paymentRefundMapper.insert(refund);
        } else {
            refund.setStatus(REFUNDING);
            refund.setUpdateTime(now);
            paymentRefundMapper.updateById(refund);
        }
        order.setStatus(REFUNDING);
        foodOrderMapper.updateById(order);
        payment.setStatus(REFUNDING);
        payment.setUpdateTime(now);
        paymentRecordMapper.updateById(payment);

        if (WECHAT_PAYMENT.equals(payment.getPaymentMethod())) {
            requestWechatRefund(payment, refund);
        } else if (ALIPAY_PAYMENT.equals(payment.getPaymentMethod())) {
            requestAlipayRefund(payment, refund);
        } else {
            throw new RuntimeException("支付方式不支持原路退款");
        }
    }

    @Override
    public void fillPaymentStatuses(List<FoodOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<String> batchNos = orders.stream().map(FoodOrder::getBatchNo)
                .filter(StringUtils::hasText).distinct().toList();
        if (batchNos.isEmpty()) {
            return;
        }
        Map<String, PaymentRecord> paymentMap = new HashMap<>();
        paymentRecordMapper.selectList(new LambdaQueryWrapper<PaymentRecord>()
                .in(PaymentRecord::getOrderBatchNo, batchNos))
                .forEach(record -> paymentMap.put(record.getOrderBatchNo(), record));
        orders.forEach(order -> {
            PaymentRecord payment = paymentMap.get(order.getBatchNo());
            order.setPaymentStatus(payment == null ? "已支付" : payment.getStatus());
            if (payment != null) {
                order.setPaymentOutTradeNo(payment.getOutTradeNo());
                order.setPaymentAmount(payment.getAmount());
            }
        });
    }

    /** 每分钟关闭超时未付款订单，并归还已占用的优惠券。 */
    @Scheduled(fixedDelay = 60000)
    public void closeExpiredPayments() {
        List<PaymentRecord> expired = paymentRecordMapper.selectList(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getStatus, PENDING)
                .lt(PaymentRecord::getExpireTime, LocalDateTime.now()));
        for (PaymentRecord payment : expired) {
            try {
                // 先查平台，避免“用户刚付款、回调尚未到达”时误关订单。
                if (WECHAT_PAYMENT.equals(payment.getPaymentMethod())) {
                    queryWechatPayment(payment);
                } else {
                    queryAlipayPayment(payment);
                }
                PaymentRecord latest = requirePayment(payment.getOutTradeNo());
                if (PENDING.equals(latest.getStatus())) {
                    closeProviderPayment(latest);
                    closeLocalPayment(latest);
                }
            } catch (RuntimeException ignored) {
                // 网络暂时异常时保留订单，下次定时任务继续处理。
            }
        }
    }

    private void createWechatPrepay(PaymentRecord record) {
        PaymentProperties.Wechat wechat = properties.getWechat();
        requireWechatServices();
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wechat.getAppId());
        request.setMchid(wechat.getMerchantId());
        request.setDescription("网管通点餐-" + record.getOrderBatchNo());
        request.setOutTradeNo(record.getOutTradeNo());
        request.setNotifyUrl(wechat.getNotifyUrl());
        request.setTimeExpire(formatWechatExpireTime(record.getExpireTime(), ZoneId.systemDefault()));
        com.wechat.pay.java.service.payments.nativepay.model.Amount amount =
                new com.wechat.pay.java.service.payments.nativepay.model.Amount();
        amount.setTotal(Math.toIntExact(toCents(record.getAmount())));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        PrepayResponse response = nativePayService.prepay(request);
        if (response == null || !StringUtils.hasText(response.getCodeUrl())) {
            throw new RuntimeException("微信支付下单失败：未返回二维码");
        }
        record.setCodeUrl(response.getCodeUrl());
    }

    private void queryWechatPayment(PaymentRecord record) {
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setOutTradeNo(record.getOutTradeNo());
        request.setMchid(properties.getWechat().getMerchantId());
        Transaction transaction = requireWechatServices().queryOrderByOutTradeNo(request);
        validateWechatTransaction(record, transaction);
        if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            markPaid(record, transaction.getTransactionId(), parseWechatTime(transaction.getSuccessTime()));
        } else if (Transaction.TradeStateEnum.CLOSED.equals(transaction.getTradeState())
                || Transaction.TradeStateEnum.REVOKED.equals(transaction.getTradeState())
                || Transaction.TradeStateEnum.PAYERROR.equals(transaction.getTradeState())) {
            closeLocalPayment(record);
        }
    }

    private void queryAlipayPayment(PaymentRecord record) {
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(record.getOutTradeNo());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = requireAlipayClient().execute(request);
            if (!response.isSuccess()) {
                if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                    return;
                }
                throw new RuntimeException(alipayError(response.getSubMsg(), response.getMsg()));
            }
            if (record.getAmount().compareTo(new BigDecimal(response.getTotalAmount())) != 0) {
                throw new RuntimeException("支付宝查询金额不一致");
            }
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())) {
                markPaid(record, response.getTradeNo(), LocalDateTime.now());
            } else if ("TRADE_CLOSED".equals(response.getTradeStatus())) {
                closeLocalPayment(record);
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝订单查询失败：" + e.getMessage());
        }
    }

    private void requestWechatRefund(PaymentRecord payment, PaymentRefund refund) {
        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(payment.getOutTradeNo());
        request.setOutRefundNo(refund.getOutRefundNo());
        request.setReason("点餐订单取消");
        if (StringUtils.hasText(properties.getWechat().getRefundNotifyUrl())) {
            request.setNotifyUrl(properties.getWechat().getRefundNotifyUrl());
        }
        AmountReq amount = new AmountReq();
        amount.setRefund(toCents(refund.getAmount()));
        amount.setTotal(toCents(payment.getAmount()));
        amount.setCurrency("CNY");
        request.setAmount(amount);
        Refund result = requireWechatRefundService().create(request);
        if (Status.SUCCESS.equals(result.getStatus())) {
            completeRefund(refund, result.getRefundId(), parseWechatTime(result.getSuccessTime()));
        } else if (!Status.PROCESSING.equals(result.getStatus())) {
            failRefund(refund);
            throw new RuntimeException("微信退款申请失败，状态：" + result.getStatus());
        }
    }

    private void requestAlipayRefund(PaymentRecord payment, PaymentRefund refund) {
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payment.getOutTradeNo());
        model.setOutRequestNo(refund.getOutRefundNo());
        model.setRefundAmount(money(refund.getAmount()));
        model.setRefundReason("点餐订单取消");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = requireAlipayClient().execute(request);
            if (!response.isSuccess()) {
                failRefund(refund);
                throw new RuntimeException(alipayError(response.getSubMsg(), response.getMsg()));
            }
            completeRefund(refund, response.getTradeNo(), LocalDateTime.now());
        } catch (AlipayApiException e) {
            failRefund(refund);
            throw new RuntimeException("支付宝退款失败：" + e.getMessage());
        }
    }

    private void markPaid(PaymentRecord record, String providerTradeNo, LocalDateTime paidTime) {
        if (PAID.equals(record.getStatus()) || PARTIAL_REFUND.equals(record.getStatus())
                || REFUNDING.equals(record.getStatus()) || REFUNDED.equals(record.getStatus())) {
            return;
        }
        record.setStatus(PAID);
        record.setProviderTradeNo(providerTradeNo);
        record.setPaidTime(paidTime == null ? LocalDateTime.now() : paidTime);
        record.setUpdateTime(LocalDateTime.now());
        paymentRecordMapper.updateById(record);
        foodOrderMapper.update(null, new LambdaUpdateWrapper<FoodOrder>()
                .eq(FoodOrder::getBatchNo, record.getOrderBatchNo())
                .eq(FoodOrder::getStatus, PENDING)
                .set(FoodOrder::getStatus, "已下单"));
    }

    private void completeRefund(PaymentRefund refund, String providerRefundNo, LocalDateTime refundedTime) {
        if (REFUNDED.equals(refund.getStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        refund.setStatus(REFUNDED);
        refund.setProviderRefundNo(providerRefundNo);
        refund.setRefundedTime(refundedTime == null ? now : refundedTime);
        refund.setUpdateTime(now);
        paymentRefundMapper.updateById(refund);

        FoodOrder order = foodOrderMapper.selectById(refund.getFoodOrderId());
        if (order != null) {
            order.setStatus("已取消");
            foodOrderMapper.updateById(order);
        }
        PaymentRecord payment = paymentRecordMapper.selectById(refund.getPaymentRecordId());
        BigDecimal refundedAmount = payment.getRefundedAmount().add(refund.getAmount());
        payment.setRefundedAmount(refundedAmount);
        payment.setStatus(refundedAmount.compareTo(payment.getAmount()) >= 0 ? REFUNDED : PARTIAL_REFUND);
        payment.setUpdateTime(now);
        paymentRecordMapper.updateById(payment);
        restoreCouponWhenBatchCanceled(payment.getOrderBatchNo());
    }

    private void completeZeroAmountRefund(FoodOrder order, PaymentRecord payment) {
        order.setStatus("已取消");
        foodOrderMapper.updateById(order);
        restoreCouponWhenBatchCanceled(payment.getOrderBatchNo());
    }

    private void failRefund(PaymentRefund refund) {
        refund.setStatus("退款失败");
        refund.setUpdateTime(LocalDateTime.now());
        paymentRefundMapper.updateById(refund);
        FoodOrder order = foodOrderMapper.selectById(refund.getFoodOrderId());
        if (order != null && REFUNDING.equals(order.getStatus())) {
            order.setStatus("已下单");
            foodOrderMapper.updateById(order);
        }
        PaymentRecord payment = paymentRecordMapper.selectById(refund.getPaymentRecordId());
        payment.setStatus(payment.getRefundedAmount().compareTo(BigDecimal.ZERO) > 0 ? PARTIAL_REFUND : PAID);
        payment.setUpdateTime(LocalDateTime.now());
        paymentRecordMapper.updateById(payment);
    }

    private void closeProviderPayment(PaymentRecord payment) {
        if (WECHAT_PAYMENT.equals(payment.getPaymentMethod())) {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setOutTradeNo(payment.getOutTradeNo());
            request.setMchid(properties.getWechat().getMerchantId());
            requireWechatServices().closeOrder(request);
            return;
        }
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payment.getOutTradeNo());
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(model);
        try {
            AlipayTradeCloseResponse response = requireAlipayClient().execute(request);
            if (!response.isSuccess() && !"ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                throw new RuntimeException(alipayError(response.getSubMsg(), response.getMsg()));
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝订单关闭失败：" + e.getMessage());
        }
    }

    private void closeLocalPayment(PaymentRecord payment) {
        if (!PENDING.equals(payment.getStatus())) {
            return;
        }
        payment.setStatus(CLOSED);
        payment.setUpdateTime(LocalDateTime.now());
        paymentRecordMapper.updateById(payment);
        foodOrderMapper.update(null, new LambdaUpdateWrapper<FoodOrder>()
                .eq(FoodOrder::getBatchNo, payment.getOrderBatchNo())
                .eq(FoodOrder::getStatus, PENDING)
                .set(FoodOrder::getStatus, "已取消"));
        couponService.restoreCoupon(payment.getOrderBatchNo());
    }

    private void restoreCouponWhenBatchCanceled(String batchNo) {
        Long activeCount = foodOrderMapper.selectCount(new LambdaQueryWrapper<FoodOrder>()
                .eq(FoodOrder::getBatchNo, batchNo)
                .ne(FoodOrder::getStatus, "已取消"));
        if (activeCount == 0) {
            couponService.restoreCoupon(batchNo);
        }
    }

    private void validateWechatTransaction(PaymentRecord record, Transaction transaction) {
        if (transaction == null || !record.getOutTradeNo().equals(transaction.getOutTradeNo())) {
            throw new RuntimeException("微信支付通知订单号不一致");
        }
        if (!properties.getWechat().getMerchantId().equals(transaction.getMchid())
                || !properties.getWechat().getAppId().equals(transaction.getAppid())) {
            throw new RuntimeException("微信支付通知商户信息不一致");
        }
        if (transaction.getAmount() == null || transaction.getAmount().getTotal() == null
                || transaction.getAmount().getTotal().longValue() != toCents(record.getAmount())) {
            throw new RuntimeException("微信支付通知金额不一致");
        }
    }

    private RequestParam createWechatRequestParam(Map<String, String> headers, String body) {
        return new RequestParam.Builder()
                .serialNumber(header(headers, "Wechatpay-Serial"))
                .signature(header(headers, "Wechatpay-Signature"))
                .nonce(header(headers, "Wechatpay-Nonce"))
                .timestamp(header(headers, "Wechatpay-Timestamp"))
                .body(body)
                .build();
    }

    private synchronized NativePayService requireWechatServices() {
        if (nativePayService != null) {
            return nativePayService;
        }
        PaymentProperties.Wechat wechat = properties.getWechat();
        if (!wechat.isEnabled()) {
            throw new RuntimeException("微信支付尚未配置，请联系管理员");
        }
        requireText(wechat.getMerchantId(), "WECHAT_PAY_MCH_ID");
        requireText(wechat.getAppId(), "WECHAT_PAY_APP_ID");
        requireText(wechat.getMerchantSerialNumber(), "WECHAT_PAY_MERCHANT_SERIAL_NUMBER");
        requireFile(wechat.getPrivateKeyPath(), "WECHAT_PAY_PRIVATE_KEY_PATH");
        requireText(wechat.getApiV3Key(), "WECHAT_PAY_API_V3_KEY");
        requireText(wechat.getNotifyUrl(), "WECHAT_PAY_NOTIFY_URL");
        wechatConfig = new RSAAutoCertificateConfig.Builder()
                .merchantId(wechat.getMerchantId())
                .privateKeyFromPath(wechat.getPrivateKeyPath())
                .merchantSerialNumber(wechat.getMerchantSerialNumber())
                .apiV3Key(wechat.getApiV3Key())
                .build();
        nativePayService = new NativePayService.Builder().config(wechatConfig).build();
        wechatRefundService = new RefundService.Builder().config(wechatConfig).build();
        wechatNotificationParser = new NotificationParser(wechatConfig);
        return nativePayService;
    }

    private RefundService requireWechatRefundService() {
        requireWechatServices();
        return wechatRefundService;
    }

    private NotificationParser requireWechatParser() {
        requireWechatServices();
        return wechatNotificationParser;
    }

    private synchronized AlipayClient requireAlipayClient() {
        if (alipayClient != null) {
            return alipayClient;
        }
        PaymentProperties.Alipay alipay = properties.getAlipay();
        if (!alipay.isEnabled()) {
            throw new RuntimeException("支付宝支付尚未配置，请联系管理员");
        }
        requireText(alipay.getAppId(), "ALIPAY_APP_ID");
        requireFile(alipay.getPrivateKeyPath(), "ALIPAY_PRIVATE_KEY_PATH");
        requireFile(alipay.getPublicKeyPath(), "ALIPAY_PUBLIC_KEY_PATH");
        requireText(alipay.getNotifyUrl(), "ALIPAY_NOTIFY_URL");
        try {
            String privateKey = readKeyFile(alipay.getPrivateKeyPath());
            alipayPublicKey = readKeyFile(alipay.getPublicKeyPath());
            alipayClient = new DefaultAlipayClient(alipay.getGatewayUrl(), alipay.getAppId(), privateKey,
                    "json", StandardCharsets.UTF_8.name(), alipayPublicKey, "RSA2");
            return alipayClient;
        } catch (IOException e) {
            throw new RuntimeException("支付宝密钥文件读取失败：" + e.getMessage());
        }
    }

    private String requireAlipayPublicKey() {
        requireAlipayClient();
        return alipayPublicKey;
    }

    private PaymentRecord requirePayment(String outTradeNo) {
        if (!StringUtils.hasText(outTradeNo)) {
            throw new RuntimeException("支付单号不能为空");
        }
        PaymentRecord record = paymentRecordMapper.selectOne(new LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getOutTradeNo, outTradeNo)
                .last("LIMIT 1"));
        if (record == null) {
            throw new RuntimeException("支付记录不存在");
        }
        return record;
    }

    private PaymentCreateResponse toCreateResponse(PaymentRecord record) {
        PaymentCreateResponse response = new PaymentCreateResponse();
        response.setOrderBatchNo(record.getOrderBatchNo());
        response.setOutTradeNo(record.getOutTradeNo());
        response.setPaymentMethod(record.getPaymentMethod());
        response.setAmount(record.getAmount());
        response.setStatus(record.getStatus());
        return response;
    }

    private PaymentStatusResponse toStatusResponse(PaymentRecord record) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setOrderBatchNo(record.getOrderBatchNo());
        response.setOutTradeNo(record.getOutTradeNo());
        response.setPaymentMethod(record.getPaymentMethod());
        response.setAmount(record.getAmount());
        response.setRefundedAmount(record.getRefundedAmount());
        response.setStatus(record.getStatus());
        response.setExpireTime(record.getExpireTime());
        response.setPaidTime(record.getPaidTime());
        return response;
    }

    private String createTradeNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String createRefundNo() {
        return "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private long toCents(BigDecimal amount) {
        try {
            return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException e) {
            throw new RuntimeException("支付金额最多保留两位小数");
        }
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream()
                .filter(entry -> name.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("微信支付通知缺少请求头：" + name));
    }

    private LocalDateTime parseWechatTime(String text) {
        if (!StringUtils.hasText(text)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    static String formatWechatExpireTime(LocalDateTime expireTime, ZoneId zoneId) {
        return expireTime.atZone(zoneId).format(WECHAT_RFC3339_SECONDS);
    }

    private String readKeyFile(String path) throws IOException {
        String value = Files.readString(Path.of(path), StandardCharsets.UTF_8);
        return value.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
    }

    private void requireText(String value, String environmentName) {
        if (!StringUtils.hasText(value)) {
            throw new RuntimeException("支付配置缺少环境变量：" + environmentName);
        }
    }

    private void requireFile(String path, String environmentName) {
        requireText(path, environmentName);
        if (!Files.isRegularFile(Path.of(path))) {
            throw new RuntimeException("支付密钥文件不存在：" + path);
        }
    }

    private String alipayError(String subMessage, String message) {
        return "支付宝接口调用失败：" + (StringUtils.hasText(subMessage) ? subMessage : message);
    }
}
