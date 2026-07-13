package com.chinasofti.wangguantong.service;

import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.PromotionRule;

import java.util.Map;

public interface PromotionService {

    Member register(Member member);

    Member ensureInviteCode(Member member);

    Map<String, Object> getUserOverview(Long memberId);

    Map<String, Object> getAdminOverview();

    PromotionRule updateRule(PromotionRule rule);
}
