package com.chinasofti.wangguantong.controller;

import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.CouponTemplate;
import com.chinasofti.wangguantong.entity.SignInRewardRule;
import com.chinasofti.wangguantong.entity.UserCoupon;
import com.chinasofti.wangguantong.service.CouponService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/template/list")
    public Result<List<CouponTemplate>> templateList(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String status) {
        return Result.success(couponService.listTemplates(name, status));
    }

    @PostMapping("/template/save")
    public Result<Void> saveTemplate(@RequestBody CouponTemplate template) {
        try {
            couponService.saveTemplate(template);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/template/status")
    public Result<Void> templateStatus(@RequestBody Map<String, String> form) {
        try {
            couponService.changeTemplateStatus(Long.valueOf(form.get("id")), form.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/rule/list")
    public Result<List<SignInRewardRule>> ruleList() {
        return Result.success(couponService.listRules());
    }

    @PostMapping("/rule/save")
    public Result<Void> saveRule(@RequestBody SignInRewardRule rule) {
        try {
            couponService.saveRule(rule);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/rule/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        couponService.deleteRule(id);
        return Result.success();
    }

    @GetMapping("/sign-in/status/{memberId}")
    public Result<Map<String, Object>> signInStatus(@PathVariable Long memberId) {
        try {
            return Result.success(couponService.getSignInStatus(memberId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/sign-in/{memberId}")
    public Result<Map<String, Object>> signIn(@PathVariable Long memberId) {
        try {
            return Result.success(couponService.signIn(memberId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{memberId}")
    public Result<List<UserCoupon>> userCoupons(@PathVariable Long memberId,
                                                 @RequestParam(required = false) String status) {
        try {
            return Result.success(couponService.listUserCoupons(memberId, status));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
