package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.OnlineRecord;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final MemberService memberService;
    private final ComputerService computerService;
    private final RechargeRecordService rechargeRecordService;
    private final OnlineRecordService onlineRecordService;

    public DashboardController(MemberService memberService,
                               ComputerService computerService,
                               RechargeRecordService rechargeRecordService,
                               OnlineRecordService onlineRecordService) {
        this.memberService = memberService;
        this.computerService = computerService;
        this.rechargeRecordService = rechargeRecordService;
        this.onlineRecordService = onlineRecordService;
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

        List<RechargeRecord> todayRechargeList = rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .ge(RechargeRecord::getCreateTime, todayStart)
                .lt(RechargeRecord::getCreateTime, tomorrowStart));
        BigDecimal todayRechargeAmount = todayRechargeList.stream()
                .map(RechargeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long todayOnlineCount = onlineRecordService.count(new LambdaQueryWrapper<OnlineRecord>()
                .ge(OnlineRecord::getStartTime, todayStart)
                .lt(OnlineRecord::getStartTime, tomorrowStart));

        Map<String, Object> data = new HashMap<>();
        data.put("memberCount", memberService.count());
        data.put("computerCount", computerService.count());
        data.put("freeComputerCount", computerService.count(new LambdaQueryWrapper<Computer>().eq(Computer::getStatus, "空闲")));
        data.put("usingComputerCount", computerService.count(new LambdaQueryWrapper<Computer>().eq(Computer::getStatus, "使用中")));
        data.put("todayRechargeAmount", todayRechargeAmount);
        data.put("todayOnlineCount", todayOnlineCount);
        return Result.success(data);
    }
}
