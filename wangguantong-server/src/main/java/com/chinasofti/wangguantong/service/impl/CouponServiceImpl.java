package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chinasofti.wangguantong.entity.CouponTemplate;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.MemberSignIn;
import com.chinasofti.wangguantong.entity.SignInRewardRule;
import com.chinasofti.wangguantong.entity.UserCoupon;
import com.chinasofti.wangguantong.mapper.CouponTemplateMapper;
import com.chinasofti.wangguantong.mapper.MemberSignInMapper;
import com.chinasofti.wangguantong.mapper.SignInRewardRuleMapper;
import com.chinasofti.wangguantong.mapper.UserCouponMapper;
import com.chinasofti.wangguantong.service.CouponService;
import com.chinasofti.wangguantong.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponTemplateMapper templateMapper;
    private final SignInRewardRuleMapper ruleMapper;
    private final MemberSignInMapper signInMapper;
    private final UserCouponMapper userCouponMapper;
    private final MemberService memberService;

    public CouponServiceImpl(CouponTemplateMapper templateMapper,
                             SignInRewardRuleMapper ruleMapper,
                             MemberSignInMapper signInMapper,
                             UserCouponMapper userCouponMapper,
                             MemberService memberService) {
        this.templateMapper = templateMapper;
        this.ruleMapper = ruleMapper;
        this.signInMapper = signInMapper;
        this.userCouponMapper = userCouponMapper;
        this.memberService = memberService;
    }

    @Override
    public List<CouponTemplate> listTemplates(String name, String status) {
        return templateMapper.selectList(new LambdaQueryWrapper<CouponTemplate>()
                .like(StringUtils.hasText(name), CouponTemplate::getName, name)
                .eq(StringUtils.hasText(status), CouponTemplate::getStatus, status)
                .orderByDesc(CouponTemplate::getId));
    }

    @Override
    public void saveTemplate(CouponTemplate template) {
        if (template == null || !StringUtils.hasText(template.getName())) {
            throw new RuntimeException("优惠券名称不能为空");
        }
        BigDecimal minSpend = template.getMinSpend() == null ? BigDecimal.ZERO : template.getMinSpend();
        if (minSpend.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("使用门槛不能小于 0");
        }
        if (template.getDiscountAmount() == null || template.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("优惠金额必须大于 0");
        }
        if (template.getDiscountAmount().compareTo(minSpend) > 0 && minSpend.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("优惠金额不能大于使用门槛");
        }
        if (template.getValidDays() == null || template.getValidDays() <= 0) {
            throw new RuntimeException("有效天数必须大于 0");
        }
        template.setName(template.getName().trim());
        template.setMinSpend(minSpend);
        if (!StringUtils.hasText(template.getStatus())) {
            template.setStatus("启用");
        }
        if (template.getId() == null) {
            template.setCreateTime(LocalDateTime.now());
            templateMapper.insert(template);
        } else if (templateMapper.selectById(template.getId()) == null) {
            throw new RuntimeException("优惠券模板不存在");
        } else {
            templateMapper.updateById(template);
        }
    }

    @Override
    public void changeTemplateStatus(Long id, String status) {
        if (!("启用".equals(status) || "停用".equals(status))) {
            throw new RuntimeException("优惠券状态不正确");
        }
        CouponTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("优惠券模板不存在");
        }
        template.setStatus(status);
        templateMapper.updateById(template);
    }

    @Override
    public List<SignInRewardRule> listRules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<SignInRewardRule>()
                .orderByAsc(SignInRewardRule::getConsecutiveDays));
    }

    @Override
    public void saveRule(SignInRewardRule rule) {
        if (rule == null || rule.getConsecutiveDays() == null || rule.getConsecutiveDays() <= 0) {
            throw new RuntimeException("连续签到天数必须大于 0");
        }
        CouponTemplate template = templateMapper.selectById(rule.getCouponTemplateId());
        if (template == null || !"启用".equals(template.getStatus())) {
            throw new RuntimeException("请选择已启用的优惠券模板");
        }
        Long duplicate = ruleMapper.selectCount(new LambdaQueryWrapper<SignInRewardRule>()
                .eq(SignInRewardRule::getConsecutiveDays, rule.getConsecutiveDays())
                .ne(rule.getId() != null, SignInRewardRule::getId, rule.getId()));
        if (duplicate > 0) {
            throw new RuntimeException("该签到天数已经配置奖励");
        }
        rule.setCouponName(template.getName());
        if (rule.getId() == null) {
            rule.setCreateTime(LocalDateTime.now());
            ruleMapper.insert(rule);
        } else {
            ruleMapper.updateById(rule);
        }
    }

    @Override
    public void deleteRule(Long id) {
        ruleMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getSignInStatus(Long memberId) {
        requireMember(memberId);
        LocalDate today = LocalDate.now();
        MemberSignIn latest = signInMapper.selectOne(new LambdaQueryWrapper<MemberSignIn>()
                .eq(MemberSignIn::getMemberId, memberId)
                .orderByDesc(MemberSignIn::getSignDate)
                .last("LIMIT 1"));
        boolean signedToday = latest != null && today.equals(latest.getSignDate());
        int consecutiveDays = 0;
        if (latest != null && (signedToday || today.minusDays(1).equals(latest.getSignDate()))) {
            consecutiveDays = latest.getConsecutiveDays();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("signedToday", signedToday);
        result.put("consecutiveDays", consecutiveDays);
        result.put("rules", listRules());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signIn(Long memberId) {
        requireMember(memberId);
        LocalDate today = LocalDate.now();
        Long todayCount = signInMapper.selectCount(new LambdaQueryWrapper<MemberSignIn>()
                .eq(MemberSignIn::getMemberId, memberId)
                .eq(MemberSignIn::getSignDate, today));
        if (todayCount > 0) {
            throw new RuntimeException("今天已经签到，请明天再来");
        }
        MemberSignIn latest = signInMapper.selectOne(new LambdaQueryWrapper<MemberSignIn>()
                .eq(MemberSignIn::getMemberId, memberId)
                .orderByDesc(MemberSignIn::getSignDate)
                .last("LIMIT 1"));
        int days = latest != null && today.minusDays(1).equals(latest.getSignDate())
                ? latest.getConsecutiveDays() + 1 : 1;

        MemberSignIn signIn = new MemberSignIn();
        signIn.setMemberId(memberId);
        signIn.setSignDate(today);
        signIn.setConsecutiveDays(days);
        signIn.setCreateTime(LocalDateTime.now());
        signInMapper.insert(signIn);

        SignInRewardRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<SignInRewardRule>()
                .eq(SignInRewardRule::getConsecutiveDays, days)
                .last("LIMIT 1"));
        UserCoupon reward = null;
        if (rule != null) {
            CouponTemplate template = templateMapper.selectById(rule.getCouponTemplateId());
            if (template != null && "启用".equals(template.getStatus())) {
                LocalDateTime now = LocalDateTime.now();
                reward = new UserCoupon();
                reward.setMemberId(memberId);
                reward.setTemplateId(template.getId());
                reward.setCouponName(template.getName());
                reward.setMinSpend(template.getMinSpend());
                reward.setDiscountAmount(template.getDiscountAmount());
                reward.setStatus("可使用");
                reward.setSourceType("签到奖励");
                reward.setSourceRefId(signIn.getId());
                reward.setReceiveTime(now);
                reward.setExpireTime(now.plusDays(template.getValidDays()));
                userCouponMapper.insert(reward);
                signIn.setRewardRuleId(rule.getId());
                signIn.setRewardCouponId(reward.getId());
                signInMapper.updateById(signIn);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("consecutiveDays", days);
        result.put("coupon", reward);
        result.put("message", reward == null ? "签到成功" : "签到成功，已获得" + reward.getCouponName());
        return result;
    }

    @Override
    public List<UserCoupon> listUserCoupons(Long memberId, String status) {
        requireMember(memberId);
        LocalDateTime now = LocalDateTime.now();
        List<UserCoupon> available = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getMemberId, memberId)
                .eq(UserCoupon::getStatus, "可使用")
                .le(UserCoupon::getExpireTime, now));
        for (UserCoupon coupon : available) {
            coupon.setStatus("已过期");
            userCouponMapper.updateById(coupon);
        }
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getMemberId, memberId)
                .eq(StringUtils.hasText(status), UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getId));
    }

    @Override
    public UserCoupon useCoupon(Long couponId, Long memberId, BigDecimal orderAmount, String batchNo) {
        if (couponId == null) {
            return null;
        }
        UserCoupon coupon = userCouponMapper.selectById(couponId);
        if (coupon == null || !memberId.equals(coupon.getMemberId())) {
            throw new RuntimeException("优惠券不存在");
        }
        if (!"可使用".equals(coupon.getStatus())) {
            throw new RuntimeException("优惠券已使用或已过期");
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getExpireTime() != null && !coupon.getExpireTime().isAfter(now)) {
            coupon.setStatus("已过期");
            userCouponMapper.updateById(coupon);
            throw new RuntimeException("优惠券已过期");
        }
        if (orderAmount.compareTo(coupon.getMinSpend()) < 0) {
            throw new RuntimeException("订单金额未达到优惠券使用门槛");
        }
        int updated = userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getId, couponId)
                .eq(UserCoupon::getStatus, "可使用")
                .set(UserCoupon::getStatus, "已使用")
                .set(UserCoupon::getUseTime, now)
                .set(UserCoupon::getOrderBatchNo, batchNo));
        if (updated == 0) {
            throw new RuntimeException("优惠券已被使用，请勿重复提交");
        }
        return coupon;
    }

    @Override
    public void restoreCoupon(String batchNo) {
        if (!StringUtils.hasText(batchNo)) return;
        userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                .eq(UserCoupon::getOrderBatchNo, batchNo)
                .eq(UserCoupon::getStatus, "已使用")
                .set(UserCoupon::getStatus, "可使用")
                .set(UserCoupon::getUseTime, null)
                .set(UserCoupon::getOrderBatchNo, null));
    }

    private Member requireMember(Long memberId) {
        Member member = memberId == null ? null : memberService.getById(memberId);
        if (member == null) throw new RuntimeException("用户不存在");
        if (!"正常".equals(member.getStatus())) throw new RuntimeException("用户状态异常");
        return member;
    }
}
