package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.ReservationRecord;
import com.chinasofti.wangguantong.mapper.ReservationRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import com.chinasofti.wangguantong.service.ReservationRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReservationRecordServiceImpl extends ServiceImpl<ReservationRecordMapper, ReservationRecord> implements ReservationRecordService {

    private final MemberService memberService;
    private final ComputerService computerService;
    private final OnlineRecordService onlineRecordService;

    public ReservationRecordServiceImpl(MemberService memberService,
                                        ComputerService computerService,
                                        OnlineRecordService onlineRecordService) {
        this.memberService = memberService;
        this.computerService = computerService;
        this.onlineRecordService = onlineRecordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReservation(Long memberId, Long computerId, String reserveTimeText) {
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (!"正常".equals(member.getStatus())) {
            throw new RuntimeException("会员状态不是正常，不能预约");
        }
        long activeCount = count(new LambdaQueryWrapper<ReservationRecord>()
                .eq(ReservationRecord::getMemberId, memberId)
                .eq(ReservationRecord::getStatus, "已预约")
                .ge(ReservationRecord::getReserveTime, LocalDateTime.now()));
        if (activeCount > 0) {
            throw new RuntimeException("您已有待上机的预约，请先使用或取消原预约");
        }

        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"空闲".equals(computer.getStatus())) {
            throw new RuntimeException("只有空闲电脑可以预约锁定");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reserveTime = parseReserveTime(reserveTimeText);
        if (reserveTime.isBefore(now)) {
            throw new RuntimeException("预约时间不能早于当前时间");
        }
        if (reserveTime.isAfter(now.plusHours(1))) {
            throw new RuntimeException("用户预约只能预约最近 1 小时内的时间");
        }

        ReservationRecord record = new ReservationRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        record.setReserveTime(reserveTime);
        record.setStatus("已预约");
        record.setCreateTime(now);
        save(record);

        computer.setStatus("预约锁定");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id) {
        cancelReservation(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservationForMember(Long id, Long memberId) {
        if (memberId == null) {
            throw new RuntimeException("用户信息不存在，请重新登录");
        }
        cancelReservation(id, memberId);
    }

    private void cancelReservation(Long id, Long expectedMemberId) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
        checkReservationOwner(record, expectedMemberId);
        if (!"已预约".equals(record.getStatus())) {
            throw new RuntimeException("只有已预约记录可以取消");
        }

        record.setStatus("已取消");
        updateById(record);

        Computer computer = computerService.getById(record.getComputerId());
        if (computer != null && "预约锁定".equals(computer.getStatus())) {
            computer.setStatus("空闲");
            computerService.updateById(computer);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ReservationExpiredException.class)
    public void startFromReservation(Long id) {
        startFromReservation(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ReservationExpiredException.class)
    public void startFromReservationForMember(Long id, Long memberId) {
        if (memberId == null) {
            throw new RuntimeException("用户信息不存在，请重新登录");
        }
        startFromReservation(id, memberId);
    }

    private void startFromReservation(Long id, Long expectedMemberId) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
        checkReservationOwner(record, expectedMemberId);
        if (!"已预约".equals(record.getStatus())) {
            throw new RuntimeException("只有已预约记录可以上机");
        }
        if (record.getReserveTime() != null && record.getReserveTime().isBefore(LocalDateTime.now())) {
            record.setStatus("已取消");
            updateById(record);
            Computer reservedComputer = computerService.getById(record.getComputerId());
            if (reservedComputer != null && "预约锁定".equals(reservedComputer.getStatus())) {
                reservedComputer.setStatus("空闲");
                computerService.updateById(reservedComputer);
            }
            throw new ReservationExpiredException("预约已超时，系统已自动取消");
        }

        Computer computer = computerService.getById(record.getComputerId());
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"预约锁定".equals(computer.getStatus())) {
            throw new RuntimeException("电脑不是预约锁定状态");
        }

        // 先临时释放为“空闲”，复用普通上机逻辑，成功后会自动变为“使用中”。
        computer.setStatus("空闲");
        computerService.updateById(computer);
        onlineRecordService.startOnline(record.getMemberId(), record.getComputerId());

        record.setStatus("已上机");
        updateById(record);
    }

    private void checkReservationOwner(ReservationRecord record, Long expectedMemberId) {
        if (expectedMemberId != null && !expectedMemberId.equals(record.getMemberId())) {
            throw new RuntimeException("只能操作本人预约的电脑");
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<ReservationRecord> expiredRecords = list(new LambdaQueryWrapper<ReservationRecord>()
                .eq(ReservationRecord::getStatus, "已预约")
                .lt(ReservationRecord::getReserveTime, now));
        for (ReservationRecord record : expiredRecords) {
            record.setStatus("已取消");
            updateById(record);

            Computer computer = computerService.getById(record.getComputerId());
            if (computer != null && "预约锁定".equals(computer.getStatus())) {
                computer.setStatus("空闲");
                computerService.updateById(computer);
            }
        }
    }

    private LocalDateTime parseReserveTime(String reserveTimeText) {
        if (reserveTimeText == null || reserveTimeText.trim().isEmpty()) {
            throw new RuntimeException("请选择预约时间");
        }
        return LocalDateTime.parse(reserveTimeText, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static class ReservationExpiredException extends RuntimeException {
        ReservationExpiredException(String message) {
            super(message);
        }
    }
}
