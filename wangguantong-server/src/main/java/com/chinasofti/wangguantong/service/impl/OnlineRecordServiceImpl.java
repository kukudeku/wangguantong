package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.OnlineRecord;
import com.chinasofti.wangguantong.mapper.OnlineRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OnlineRecordServiceImpl extends ServiceImpl<OnlineRecordMapper, OnlineRecord> implements OnlineRecordService {

    private static final Logger log = LoggerFactory.getLogger(OnlineRecordServiceImpl.class);

    private final MemberService memberService;
    private final ComputerService computerService;

    public OnlineRecordServiceImpl(MemberService memberService, ComputerService computerService) {
        this.memberService = memberService;
        this.computerService = computerService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOnline(Long memberId, Long computerId) {
        if (memberId == null) {
            throw new RuntimeException("请选择会员");
        }
        if (computerId == null) {
            throw new RuntimeException("请选择电脑");
        }

        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (!"正常".equals(member.getStatus())) {
            throw new RuntimeException("会员状态不是正常，不能上机");
        }
        long runningCount = count(new LambdaQueryWrapper<OnlineRecord>()
                .eq(OnlineRecord::getMemberId, memberId)
                .eq(OnlineRecord::getStatus, "进行中"));
        if (runningCount > 0) {
            throw new RuntimeException("该会员已有正在进行的上机记录");
        }

        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"空闲".equals(computer.getStatus())) {
            throw new RuntimeException("电脑不是空闲状态，不能上机");
        }

        BigDecimal firstAmount = calculateAmount(computer.getPricePerHour(), 1, member);
        if (member.getBalance().compareTo(firstAmount) < 0) {
            throw new RuntimeException("会员余额不足，请先充值");
        }

        member.setBalance(member.getBalance().subtract(firstAmount));
        memberService.updateById(member);

        OnlineRecord record = new OnlineRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        record.setStartTime(LocalDateTime.now());
        record.setTotalAmount(firstAmount);
        record.setStatus("进行中");
        save(record);

        computer.setStatus("使用中");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endOnline(Long recordId) {
        OnlineRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("上机记录不存在");
        }
        if (!"进行中".equals(record.getStatus())) {
            throw new RuntimeException("该记录不是进行中状态");
        }

        Member member = memberService.getById(record.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        Computer computer = computerService.getById(record.getComputerId());
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }

        LocalDateTime endTime = LocalDateTime.now();
        settleRunningCharge(record, member, computer, endTime);
        if (!"进行中".equals(record.getStatus())) {
            return;
        }

        record.setEndTime(endTime);
        record.setStatus("已完成");
        updateById(record);

        computer.setStatus("空闲");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OnlineRecord> listWithRunningInfo(String memberName, String computerNo, String status) {
        LambdaQueryWrapper<OnlineRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(memberName != null && !memberName.trim().isEmpty(), OnlineRecord::getMemberName, memberName)
                .like(computerNo != null && !computerNo.trim().isEmpty(), OnlineRecord::getComputerNo, computerNo)
                .eq(status != null && !status.trim().isEmpty(), OnlineRecord::getStatus, status)
                .orderByDesc(OnlineRecord::getId);

        List<OnlineRecord> list = list(wrapper);
        for (OnlineRecord record : list) {
            fillRunningInfo(record);
        }
        return list;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void settleRunningRecords() {
        List<OnlineRecord> runningRecords = list(new LambdaQueryWrapper<OnlineRecord>()
                .eq(OnlineRecord::getStatus, "进行中"));
        LocalDateTime now = LocalDateTime.now();
        for (OnlineRecord record : runningRecords) {
            try {
                Member member = memberService.getById(record.getMemberId());
                Computer computer = computerService.getById(record.getComputerId());
                if (member == null || computer == null) {
                    continue;
                }
                settleRunningCharge(record, member, computer, now);
            } catch (RuntimeException e) {
                log.warn("自动结算上机记录失败，recordId={}", record.getId(), e);
            }
        }
    }

    private void fillRunningInfo(OnlineRecord record) {
        Member member = memberService.getById(record.getMemberId());
        Computer computer = computerService.getById(record.getComputerId());
        if (member == null || computer == null) {
            return;
        }

        record.setMemberBalance(member.getBalance());
        record.setMemberLevel(member.getMemberLevel());
        record.setDiscountRate(getDiscountRate(member.getMemberLevel()));

        if ("进行中".equals(record.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            long minutes = Duration.between(record.getStartTime(), now).toMinutes();
            long hours = (long) Math.ceil(minutes / 60.0);
            if (hours < 1) {
                hours = 1;
            }
            BigDecimal currentAmount = settleRunningCharge(record, member, computer, now);
            record.setRunningMinutes(minutes < 0 ? 0 : minutes);
            record.setChargeHours(hours);
            record.setCurrentAmount(currentAmount);
            record.setMemberBalance(member.getBalance());
            if (!"进行中".equals(record.getStatus())) {
                record.setBalanceEnough(false);
                record.setWarningMessage("余额不足，系统已自动下机");
                return;
            }
            BigDecimal paidAmount = record.getTotalAmount() == null ? BigDecimal.ZERO : record.getTotalAmount();
            BigDecimal nextAmount = calculateAmount(computer.getPricePerHour(), hours + 1, member);
            BigDecimal pendingAmount = nextAmount.subtract(paidAmount);
            record.setBalanceEnough(pendingAmount.compareTo(BigDecimal.ZERO) <= 0 || member.getBalance().compareTo(pendingAmount) >= 0);
            if (!record.getBalanceEnough()) {
                record.setWarningMessage("当前余额不足以支付下一计费小时，请及时充值");
            }
        } else {
            record.setCurrentAmount(record.getTotalAmount());
            record.setBalanceEnough(true);
        }
    }

    private BigDecimal calculateAmount(BigDecimal pricePerHour, long hours, Member member) {
        return pricePerHour
                .multiply(BigDecimal.valueOf(hours))
                .multiply(getDiscountRate(member.getMemberLevel()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal settleRunningCharge(OnlineRecord record, Member member, Computer computer, LocalDateTime now) {
        long minutes = Duration.between(record.getStartTime(), now).toMinutes();
        long hours = (long) Math.ceil(minutes / 60.0);
        if (hours < 1) {
            hours = 1;
        }

        BigDecimal currentAmount = calculateAmount(computer.getPricePerHour(), hours, member);
        BigDecimal paidAmount = record.getTotalAmount() == null ? BigDecimal.ZERO : record.getTotalAmount();
        BigDecimal pendingAmount = currentAmount.subtract(paidAmount);

        if (pendingAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (member.getBalance().compareTo(pendingAmount) < 0) {
                autoEndForInsufficientBalance(record, computer, now);
                record.setBalanceEnough(false);
                record.setWarningMessage("余额不足，系统已自动下机");
                return paidAmount;
            }
            member.setBalance(member.getBalance().subtract(pendingAmount));
            memberService.updateById(member);

            record.setTotalAmount(currentAmount);
            updateById(record);
        }

        return currentAmount;
    }

    private void autoEndForInsufficientBalance(OnlineRecord record, Computer computer, LocalDateTime endTime) {
        if (!"进行中".equals(record.getStatus())) {
            return;
        }
        record.setEndTime(endTime);
        record.setStatus("已完成");
        updateById(record);

        computer.setStatus("空闲");
        computerService.updateById(computer);
    }

    private BigDecimal getDiscountRate(String memberLevel) {
        if ("钻石会员".equals(memberLevel)) {
            return new BigDecimal("0.80");
        }
        if ("黄金会员".equals(memberLevel)) {
            return new BigDecimal("0.90");
        }
        if ("普通会员".equals(memberLevel)) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ONE;
    }
}
