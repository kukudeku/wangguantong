package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.dto.PaymentStatusResponse;
import com.chinasofti.wangguantong.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/status/{outTradeNo}")
    public Result<PaymentStatusResponse> status(@PathVariable String outTradeNo) {
        try {
            return Result.success(paymentService.queryPayment(outTradeNo));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/wechat/qrcode/{outTradeNo}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> wechatQrCode(@PathVariable String outTradeNo) {
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(paymentService.createWechatQrCode(outTradeNo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping(value = "/alipay/page/{outTradeNo}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> alipayPage(@PathVariable String outTradeNo) {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body(paymentService.createAlipayPage(outTradeNo));
        } catch (RuntimeException e) {
            String escaped = e.getMessage().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            return ResponseEntity.badRequest()
                    .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                    .body("<!doctype html><meta charset=\"utf-8\"><title>支付失败</title><p>" + escaped + "</p>");
        }
    }

    @PostMapping("/notify/wechat")
    public ResponseEntity<Map<String, String>> wechatNotify(@RequestHeader Map<String, String> headers,
                                                            @RequestBody String body) {
        try {
            paymentService.handleWechatPayNotify(headers, body);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (RuntimeException e) {
            log.warn("微信支付回调处理失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", e.getMessage()));
        }
    }

    @PostMapping("/notify/wechat/refund")
    public ResponseEntity<Map<String, String>> wechatRefundNotify(@RequestHeader Map<String, String> headers,
                                                                  @RequestBody String body) {
        try {
            paymentService.handleWechatRefundNotify(headers, body);
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (RuntimeException e) {
            log.warn("微信退款回调处理失败：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", "FAIL", "message", e.getMessage()));
        }
    }

    @PostMapping(value = "/notify/alipay", produces = MediaType.TEXT_PLAIN_VALUE)
    public String alipayNotify(@RequestParam Map<String, String> parameters) {
        try {
            paymentService.handleAlipayNotify(parameters);
            return "success";
        } catch (RuntimeException e) {
            log.warn("支付宝支付回调处理失败：{}", e.getMessage());
            return "failure";
        }
    }
}
