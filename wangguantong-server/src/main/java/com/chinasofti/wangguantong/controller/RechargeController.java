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

@RestController
@RequestMapping("/recharge")
public class RechargeController {

    private final RechargeRecordService rechargeRecordService;

    public RechargeController(RechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
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
