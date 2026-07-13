package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员余额充值接口。
 *
 * <p>用户端的充值是模拟充值，不连接支付宝或微信：确认后直接调用此接口，
 * Service 在一个数据库事务中同时更新余额和写入充值记录。</p>
 */
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    private final RechargeRecordService rechargeRecordService;

    public RechargeController(RechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
    }

    /** 新增一笔充值，真正的金额校验和余额修改在 Service 中完成。 */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody RechargeRecord rechargeRecord) {
        try {
            rechargeRecordService.recharge(rechargeRecord.getMemberId(), rechargeRecord.getAmount());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查询全部充值记录，最新记录排在前面。 */
    @GetMapping("/list")
    public Result<List<RechargeRecord>> list() {
        return Result.success(rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .orderByDesc(RechargeRecord::getId)));
    }
}
