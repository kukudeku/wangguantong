package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.entity.InvitationRecord;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.PromotionRule;
import com.chinasofti.wangguantong.mapper.InvitationRecordMapper;
import com.chinasofti.wangguantong.mapper.PromotionRuleMapper;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.PromotionService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionServiceImpl implements PromotionService {

    private static final BigDecimal DEFAULT_INVITER_REWARD = new BigDecimal("10.00");
    private static final BigDecimal DEFAULT_INVITEE_REWARD = new BigDecimal("5.00");

    private final PromotionRuleMapper ruleMapper;
    private final InvitationRecordMapper invitationRecordMapper;
    private final MemberService memberService;
    private final RechargeRecordService rechargeRecordService;

    public PromotionServiceImpl(PromotionRuleMapper ruleMapper,
                                InvitationRecordMapper invitationRecordMapper,
                                MemberService memberService,
                                RechargeRecordService rechargeRecordService) {
        this.ruleMapper = ruleMapper;
        this.invitationRecordMapper = invitationRecordMapper;
        this.memberService = memberService;
        this.rechargeRecordService = rechargeRecordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member register(Member member) {
        validateRegistration(member);

        String invitationCode = normalizeInviteCode(member.getInvitationCode());
        PromotionRule rule = null;
        Member inviter = null;
        if (StringUtils.hasText(invitationCode)) {
            rule = getOrCreateRule();
            if (!"启用".equals(rule.getStatus())) {
                throw new RuntimeException("推广活动暂未开启");
            }
            inviter = memberService.getOne(new LambdaQueryWrapper<Member>()
                    .eq(Member::getInviteCode, invitationCode)
                    .last("LIMIT 1"));
            if (inviter == null) {
                throw new RuntimeException("邀请码不存在，请检查后重试");
            }
            if (!"正常".equals(inviter.getStatus())) {
                throw new RuntimeException("该邀请码暂不可用");
            }
        }

        member.setId(null);
        member.setUsername(member.getIdCard().trim());
        member.setIdCard(member.getIdCard().trim());
        member.setPhone(member.getPhone().trim());
        member.setName(member.getName().trim());
        member.setBalance(BigDecimal.ZERO);
        member.setUserType("会员");
        member.setMemberLevel("普通会员");
        member.setStatus("正常");
        member.setInviterMemberId(inviter == null ? null : inviter.getId());
        member.setCreateTime(LocalDateTime.now());
        memberService.saveNewMember(member);

        if (inviter != null) {
            grantInvitationReward(inviter, member, invitationCode, rule);
        }
        return memberService.getById(member.getId());
    }

    @Override
    public Map<String, Object> getUserOverview(Long memberId) {
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        memberService.ensureInviteCode(member);
        PromotionRule rule = getOrCreateRule();
        List<InvitationRecord> records = listRecords(memberId);

        Map<String, Object> result = new HashMap<>();
        result.put("inviteCode", member.getInviteCode());
        result.put("inviterReward", rule.getInviterReward());
        result.put("inviteeReward", rule.getInviteeReward());
        result.put("activityStatus", rule.getStatus());
        result.put("invitedCount", records.size());
        result.put("totalReward", records.stream()
                .map(InvitationRecord::getInviterReward)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("records", records);
        return result;
    }

    @Override
    public Map<String, Object> getAdminOverview() {
        PromotionRule rule = getOrCreateRule();
        List<InvitationRecord> records = invitationRecordMapper.selectList(
                new LambdaQueryWrapper<InvitationRecord>().orderByDesc(InvitationRecord::getId));
        BigDecimal totalReward = records.stream()
                .map(record -> safeAmount(record.getInviterReward()).add(safeAmount(record.getInviteeReward())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("rule", rule);
        result.put("successCount", records.size());
        result.put("totalReward", totalReward);
        result.put("records", records);
        return result;
    }

    @Override
    public PromotionRule updateRule(PromotionRule form) {
        if (form == null || form.getInviterReward() == null || form.getInviterReward().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("邀请人奖励不能小于 0");
        }
        if (form.getInviteeReward() == null || form.getInviteeReward().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("新用户奖励不能小于 0");
        }
        if (!("启用".equals(form.getStatus()) || "停用".equals(form.getStatus()))) {
            throw new RuntimeException("活动状态不正确");
        }
        PromotionRule rule = getOrCreateRule();
        rule.setInviterReward(form.getInviterReward());
        rule.setInviteeReward(form.getInviteeReward());
        rule.setStatus(form.getStatus());
        rule.setUpdateTime(LocalDateTime.now());
        ruleMapper.updateById(rule);
        return rule;
    }

    private void validateRegistration(Member member) {
        if (member == null || !StringUtils.hasText(member.getName())) {
            throw new RuntimeException("姓名不能为空");
        }
        if (!StringUtils.hasText(member.getIdCard())) {
            throw new RuntimeException("身份证号不能为空");
        }
        if (!StringUtils.hasText(member.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!StringUtils.hasText(member.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        long idCardCount = memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getIdCard, member.getIdCard().trim()));
        if (idCardCount > 0) {
            throw new RuntimeException("该身份证号已注册");
        }
        long phoneCount = memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, member.getPhone().trim()));
        if (phoneCount > 0) {
            throw new RuntimeException("该手机号已注册");
        }
    }

    private void grantInvitationReward(Member inviter,
                                       Member invitee,
                                       String invitationCode,
                                       PromotionRule rule) {
        Long rewardedCount = invitationRecordMapper.selectCount(new LambdaQueryWrapper<InvitationRecord>()
                .eq(InvitationRecord::getInviteeMemberId, invitee.getId()));
        if (rewardedCount > 0) {
            throw new RuntimeException("该用户已经领取过邀请奖励");
        }
        BigDecimal inviterReward = safeAmount(rule.getInviterReward());
        BigDecimal inviteeReward = safeAmount(rule.getInviteeReward());
        if (inviterReward.compareTo(BigDecimal.ZERO) > 0) {
            rechargeRecordService.recharge(inviter.getId(), inviterReward, "邀请好友奖励", invitationCode);
        }
        if (inviteeReward.compareTo(BigDecimal.ZERO) > 0) {
            rechargeRecordService.recharge(invitee.getId(), inviteeReward, "新人注册奖励", invitationCode);
        }

        InvitationRecord record = new InvitationRecord();
        record.setInviteCode(invitationCode);
        record.setInviterMemberId(inviter.getId());
        record.setInviterMemberName(inviter.getName());
        record.setInviteeMemberId(invitee.getId());
        record.setInviteeMemberName(invitee.getName());
        record.setInviterReward(inviterReward);
        record.setInviteeReward(inviteeReward);
        record.setStatus("已奖励");
        record.setCreateTime(LocalDateTime.now());
        invitationRecordMapper.insert(record);
    }

    private PromotionRule getOrCreateRule() {
        PromotionRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<PromotionRule>()
                .orderByAsc(PromotionRule::getId)
                .last("LIMIT 1"));
        if (rule != null) {
            return rule;
        }
        PromotionRule defaultRule = new PromotionRule();
        defaultRule.setInviterReward(DEFAULT_INVITER_REWARD);
        defaultRule.setInviteeReward(DEFAULT_INVITEE_REWARD);
        defaultRule.setStatus("启用");
        defaultRule.setUpdateTime(LocalDateTime.now());
        ruleMapper.insert(defaultRule);
        return defaultRule;
    }

    private List<InvitationRecord> listRecords(Long memberId) {
        return invitationRecordMapper.selectList(new LambdaQueryWrapper<InvitationRecord>()
                .eq(InvitationRecord::getInviterMemberId, memberId)
                .orderByDesc(InvitationRecord::getId));
    }

    private String normalizeInviteCode(String inviteCode) {
        return StringUtils.hasText(inviteCode) ? inviteCode.trim().toUpperCase() : null;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
