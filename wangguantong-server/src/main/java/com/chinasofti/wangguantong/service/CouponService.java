package com.chinasofti.wangguantong.service;

import com.chinasofti.wangguantong.entity.CouponTemplate;
import com.chinasofti.wangguantong.entity.SignInRewardRule;
import com.chinasofti.wangguantong.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {
    List<CouponTemplate> listTemplates(String name, String status);
    void saveTemplate(CouponTemplate template);
    void changeTemplateStatus(Long id, String status);
    List<SignInRewardRule> listRules();
    void saveRule(SignInRewardRule rule);
    void deleteRule(Long id);
    Map<String, Object> getSignInStatus(Long memberId);
    Map<String, Object> signIn(Long memberId);
    List<UserCoupon> listUserCoupons(Long memberId, String status);
    UserCoupon useCoupon(Long couponId, Long memberId, BigDecimal orderAmount, String batchNo);
    void restoreCoupon(String batchNo);
}
