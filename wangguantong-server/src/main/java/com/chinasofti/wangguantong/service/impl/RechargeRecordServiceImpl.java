package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import com.chinasofti.wangguantong.mapper.RechargeRecordMapper;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeRecordService {

    private final MemberService memberService;

    public RechargeRecordServiceImpl(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long memberId, BigDecimal amount) {
        recharge(memberId, amount, "余额充值", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long memberId, BigDecimal amount, String rechargeType, String referenceNo) {
        if (memberId == null) {
            throw new RuntimeException("请选择会员");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于 0");
        }

        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 增加会员余额。
        member.setBalance(member.getBalance().add(amount));
        // 散客充值后自动成为普通会员。
        if ("散客".equals(member.getUserType())) {
            member.setUserType("会员");
            member.setMemberLevel("普通会员");
        }
        memberService.updateById(member);

        // 保存充值记录。
        RechargeRecord record = new RechargeRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setAmount(amount);
        record.setRechargeType(rechargeType);
        record.setReferenceNo(referenceNo);
        record.setCreateTime(LocalDateTime.now());
        save(record);
    }
}
