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

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationRecordService reservationRecordService;

    public ReservationController(ReservationRecordService reservationRecordService) {
        this.reservationRecordService = reservationRecordService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> params) {
        try {
            Long memberId = Long.valueOf(String.valueOf(params.get("memberId")));
            Long computerId = Long.valueOf(String.valueOf(params.get("computerId")));
            Object reserveTimeValue = params.get("reserveTime");
            String reserveTimeText = reserveTimeValue == null ? null : String.valueOf(reserveTimeValue).replace("T", " ");
            if (reserveTimeText != null && reserveTimeText.length() == 16) {
                reserveTimeText = reserveTimeText + ":00";
            }
            reservationRecordService.addReservation(memberId, computerId, reserveTimeText);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(required = false) Long memberId) {
        try {
            if (memberId == null) {
                reservationRecordService.cancelReservation(id);
            } else {
                reservationRecordService.cancelReservationForMember(id, memberId);
            }
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id,
                              @RequestParam(required = false) Long memberId) {
        try {
            if (memberId == null) {
                reservationRecordService.startFromReservation(id);
            } else {
                reservationRecordService.startFromReservationForMember(id, memberId);
            }
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<ReservationRecord>> list(@RequestParam(required = false) String memberName,
                                                @RequestParam(required = false) String computerNo,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long memberId) {
        LambdaQueryWrapper<ReservationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(memberName), ReservationRecord::getMemberName, memberName)
                .like(StringUtils.hasText(computerNo), ReservationRecord::getComputerNo, computerNo)
                .eq(StringUtils.hasText(status), ReservationRecord::getStatus, status)
                .eq(memberId != null, ReservationRecord::getMemberId, memberId)
                .orderByDesc(ReservationRecord::getId);
        return Result.success(reservationRecordService.list(wrapper));
    }
}
