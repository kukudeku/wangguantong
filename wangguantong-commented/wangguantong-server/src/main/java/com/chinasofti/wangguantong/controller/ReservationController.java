package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.ReservationRecord;
import com.chinasofti.wangguantong.service.ReservationRecordService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 电脑预约接口。
 *
 * <p>预约成功后电脑状态会变为“预约锁定”。用户必须在预约时间前后及时上机，
 * 超时仍未上机的预约由定时任务自动取消并释放电脑。</p>
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationRecordService reservationRecordService;

    public ReservationController(ReservationRecordService reservationRecordService) {
        this.reservationRecordService = reservationRecordService;
    }

    /**
     * 新增预约。
     *
     * <p>这里使用 Map 接收参数，是为了让前端只提交 memberId、computerId、reserveTime，
     * 不必构造完整 ReservationRecord。时间文本统一整理后再交给 Service 校验。</p>
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        try {
            Long memberId = Long.valueOf(String.valueOf(params.get("memberId")));
            Long computerId = Long.valueOf(String.valueOf(params.get("computerId")));
            Object reserveTimeValue = params.get("reserveTime");
            // 浏览器日期组件可能返回 2026-07-10T10:30:00，后端统一改为空格格式。
            String reserveTimeText = reserveTimeValue == null ? null : String.valueOf(reserveTimeValue).replace("T", " ");
            // 只有“年月日 时:分”时补上秒，便于 LocalDateTime 按固定格式解析。
            if (reserveTimeText != null && reserveTimeText.length() == 16) {
                reserveTimeText = reserveTimeText + ":00";
            }
            reservationRecordService.addReservation(memberId, computerId, reserveTimeText);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 用户或管理员主动取消预约，同时释放被锁定的电脑。 */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        try {
            reservationRecordService.cancelReservation(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 从有效预约开始上机，成功后预约状态改为“已上机”。 */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        try {
            reservationRecordService.startFromReservation(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 后台查询预约记录，支持会员、电脑编号和状态组合筛选。 */
    @GetMapping("/list")
    public Result<List<ReservationRecord>> list(@RequestParam(required = false) String memberName,
                                                @RequestParam(required = false) String computerNo,
                                                @RequestParam(required = false) String status) {
        LambdaQueryWrapper<ReservationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(memberName), ReservationRecord::getMemberName, memberName)
                .like(StringUtils.hasText(computerNo), ReservationRecord::getComputerNo, computerNo)
                .eq(StringUtils.hasText(status), ReservationRecord::getStatus, status)
                .orderByDesc(ReservationRecord::getId);
        return Result.success(reservationRecordService.list(wrapper));
    }
}
