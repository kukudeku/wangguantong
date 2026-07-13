package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.PromotionRule;
import com.chinasofti.wangguantong.service.PromotionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/overview/{memberId}")
    public Result<Map<String, Object>> userOverview(@PathVariable Long memberId) {
        try {
            return Result.success(promotionService.getUserOverview(memberId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/overview")
    public Result<Map<String, Object>> adminOverview() {
        return Result.success(promotionService.getAdminOverview());
    }

    @PutMapping("/rule")
    public Result<PromotionRule> updateRule(@RequestBody PromotionRule rule) {
        try {
            return Result.success(promotionService.updateRule(rule));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
