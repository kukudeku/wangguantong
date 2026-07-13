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

/**
 * 后台首页统计接口。
 *
 * <p>这里没有建立复杂的统计表，而是直接从业务表查询并汇总，
 * 更适合数据量较小的课堂演示项目。</p>
 */
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

    /**
     * 返回首页所需的全部统计数字。
     *
     * <p>使用“今天 00:00（包含）到明天 00:00（不包含）”作为今日范围，
     * 可以避免手动拼接 23:59:59 带来的毫秒边界问题。</p>
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        // 计算今天的左闭右开时间区间：[todayStart, tomorrowStart)。
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

        // 先查询今日所有充值记录，再用 BigDecimal 相加，保证金额计算精确。
        List<RechargeRecord> todayRechargeList = rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .ge(RechargeRecord::getCreateTime, todayStart)
                .lt(RechargeRecord::getCreateTime, tomorrowStart));
        BigDecimal todayRechargeAmount = todayRechargeList.stream()
                .map(RechargeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日上机次数按上机开始时间统计，不要求该记录已经结束。
        long todayOnlineCount = onlineRecordService.count(new LambdaQueryWrapper<OnlineRecord>()
                .ge(OnlineRecord::getStartTime, todayStart)
                .lt(OnlineRecord::getStartTime, tomorrowStart));

        // Map 的 key 与前端 dashboard 页面读取的字段名保持一致。
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
