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

/**
 * 充值业务实现。
 *
 * <p>一次充值会修改两张表：用户表余额增加、充值记录表新增明细。</p>
 */
@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord> implements RechargeRecordService {

    /** 用于查询用户和更新余额。 */
    private final MemberService memberService;

    /** 通过构造器注入依赖，便于保证该字段始终有值。 */
    public RechargeRecordServiceImpl(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    // 使用事务保证“余额增加”和“充值记录保存”不会只成功一半。
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long memberId, BigDecimal amount) {
        // 金额使用 BigDecimal，避免用 double 计算人民币时出现小数精度误差。
        if (memberId == null) {
            throw new RuntimeException("请选择会员");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于 0");
        }

        // 必须从数据库读取用户当前余额，不能直接使用前端提交的余额。
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

        // 保存充值明细，供用户端和后台查询每一次余额变化。
        RechargeRecord record = new RechargeRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setAmount(amount);
        record.setCreateTime(LocalDateTime.now());
        save(record);
    }
}
