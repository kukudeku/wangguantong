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

/**
 * 上机、下机和上机记录查询接口。
 *
 * <p>Controller 不负责计费计算。开始上机、实时扣费、余额不足自动下机等规则
 * 全部集中在 OnlineRecordService，避免多个入口出现不同算法。</p>
 */
@RestController
@RequestMapping("/online")
public class OnlineController {

    private final OnlineRecordService onlineRecordService;

    public OnlineController(OnlineRecordService onlineRecordService) {
        this.onlineRecordService = onlineRecordService;
    }

    /** 用户或管理员开始上机，只需提交 memberId 和 computerId。 */
    @PostMapping("/start")
    public Result<Void> start(@RequestBody OnlineRecord onlineRecord) {
        try {
            onlineRecordService.startOnline(onlineRecord.getMemberId(), onlineRecord.getComputerId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 手动下机并释放电脑。recordId 是正在进行的上机记录主键。 */
    @PostMapping("/end/{recordId}")
    public Result<Void> end(@PathVariable Long recordId) {
        try {
            onlineRecordService.endOnline(recordId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询上机记录，并为进行中的记录动态补充当前时长、应扣金额和余额提示。
     */
    @GetMapping("/list")
    public Result<List<OnlineRecord>> list(@RequestParam(required = false) String memberName,
                                           @RequestParam(required = false) String computerNo,
                                           @RequestParam(required = false) String status) {
        return Result.success(onlineRecordService.listWithRunningInfo(memberName, computerNo, status));
    }

    /** 查询全部正在上机的记录，供后台“当前上机”页使用。 */
    @GetMapping("/running")
    public Result<List<OnlineRecord>> running() {
        return Result.success(onlineRecordService.listWithRunningInfo(null, null, "进行中"));
    }
}
