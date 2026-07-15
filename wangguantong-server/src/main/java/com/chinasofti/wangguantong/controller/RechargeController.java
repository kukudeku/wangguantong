package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.dto.PaymentCreateResponse;
import com.chinasofti.wangguantong.dto.RechargePaymentRequest;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import com.chinasofti.wangguantong.service.PaymentService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recharge")
public class RechargeController {

    private final RechargeRecordService rechargeRecordService;
    private final PaymentService paymentService;

    public RechargeController(RechargeRecordService rechargeRecordService, PaymentService paymentService) {
        this.rechargeRecordService = rechargeRecordService;
        this.paymentService = paymentService;
    }

    /** 用户端充值必须先完成微信或支付宝付款，支付成功回调后才增加余额。 */
    @PostMapping("/payment")
    public Result<PaymentCreateResponse> createPayment(@RequestBody RechargePaymentRequest request) {
        try {
            return Result.success(paymentService.createRechargePayment(
                    request.getMemberId(), request.getPaymentMethod(), request.getAmount()));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody RechargeRecord rechargeRecord) {
        try {
            rechargeRecordService.recharge(rechargeRecord.getMemberId(), rechargeRecord.getAmount());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<RechargeRecord>> list() {
        return Result.success(rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .orderByDesc(RechargeRecord::getId)));
    }
}
