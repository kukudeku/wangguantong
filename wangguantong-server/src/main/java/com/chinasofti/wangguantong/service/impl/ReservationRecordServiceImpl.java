package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.ReservationRecord;
import com.chinasofti.wangguantong.mapper.ReservationRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import com.chinasofti.wangguantong.service.ReservationRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"空闲".equals(computer.getStatus())) {
            throw new RuntimeException("只有空闲电脑可以预约锁定");
        }

        ReservationRecord record = new ReservationRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        record.setReserveTime(parseReserveTime(reserveTimeText));
        record.setStatus("已预约");
        record.setCreateTime(LocalDateTime.now());
        save(record);

        computer.setStatus("预约锁定");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void startFromReservation(Long id) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"已预约".equals(record.getStatus())) {
            throw new RuntimeException("只有已预约记录可以上机");
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

    private LocalDateTime parseReserveTime(String reserveTimeText) {
        if (reserveTimeText == null || reserveTimeText.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(reserveTimeText, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
