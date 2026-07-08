package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.OnlineRecord;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/online")
public class OnlineController {

    private final OnlineRecordService onlineRecordService;

    public OnlineController(OnlineRecordService onlineRecordService) {
        this.onlineRecordService = onlineRecordService;
    }

    @PostMapping("/start")
    public Result<Void> start(@RequestBody OnlineRecord onlineRecord) {
        try {
            onlineRecordService.startOnline(onlineRecord.getMemberId(), onlineRecord.getComputerId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/end/{recordId}")
    public Result<Void> end(@PathVariable Long recordId) {
        try {
            onlineRecordService.endOnline(recordId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<OnlineRecord>> list(@RequestParam(required = false) String memberName,
                                           @RequestParam(required = false) String computerNo,
                                           @RequestParam(required = false) String status) {
        return Result.success(onlineRecordService.listWithRunningInfo(memberName, computerNo, status));
    }

    @GetMapping("/running")
    public Result<List<OnlineRecord>> running() {
        return Result.success(onlineRecordService.listWithRunningInfo(null, null, "进行中"));
    }
}
