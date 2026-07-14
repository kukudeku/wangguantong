package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.RepairRecord;
import com.chinasofti.wangguantong.service.RepairRecordService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/repair")
public class RepairController {

    private final RepairRecordService repairRecordService;

    public RepairController(RepairRecordService repairRecordService) {
        this.repairRecordService = repairRecordService;
    }

    @GetMapping("/list")
    public Result<List<RepairRecord>> list(@RequestParam(required = false) String computerNo,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long memberId,
                                           @RequestParam(required = false) String serviceType) {
        return Result.success(repairRecordService.list(new LambdaQueryWrapper<RepairRecord>()
                .like(StringUtils.hasText(computerNo), RepairRecord::getComputerNo, computerNo)
                .eq(StringUtils.hasText(status), RepairRecord::getStatus, status)
                .eq(memberId != null, RepairRecord::getMemberId, memberId)
                .eq(StringUtils.hasText(serviceType), RepairRecord::getServiceType, serviceType)
                .orderByDesc(RepairRecord::getId)));
    }

    @PostMapping("/report")
    public Result<Void> report(@RequestBody RepairRecord record) {
        try {
            repairRecordService.report(record.getComputerId(), record.getMemberId(), record.getServiceType(),
                    record.getServiceLocation(), record.getProblemDescription());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/process")
    public Result<Void> process(@RequestBody RepairRecord record) {
        try {
            repairRecordService.process(record.getId(), record.getStatus(), record.getProcessRemark());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
