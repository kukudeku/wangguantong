package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.GroupBuyVoucher;
import com.chinasofti.wangguantong.service.GroupBuyVoucherService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/voucher")
public class GroupBuyVoucherController {

    private final GroupBuyVoucherService groupBuyVoucherService;

    public GroupBuyVoucherController(GroupBuyVoucherService groupBuyVoucherService) {
        this.groupBuyVoucherService = groupBuyVoucherService;
    }

    @GetMapping("/list")
    public Result<List<GroupBuyVoucher>> list(@RequestParam(required = false) String voucherCode,
                                               @RequestParam(required = false) String status) {
        List<GroupBuyVoucher> vouchers = groupBuyVoucherService.list(new LambdaQueryWrapper<GroupBuyVoucher>()
                .like(StringUtils.hasText(voucherCode), GroupBuyVoucher::getVoucherCode, voucherCode)
                .eq(StringUtils.hasText(status), GroupBuyVoucher::getStatus, status)
                .orderByDesc(GroupBuyVoucher::getId));
        LocalDateTime now = LocalDateTime.now();
        vouchers.forEach(voucher -> {
            if ("未使用".equals(voucher.getStatus())
                    && voucher.getExpireTime() != null
                    && !voucher.getExpireTime().isAfter(now)) {
                voucher.setStatus("已过期");
            }
        });
        return Result.success(vouchers);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody GroupBuyVoucher voucher) {
        try {
            groupBuyVoucherService.createVoucher(voucher);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/redeem")
    public Result<Void> redeem(@RequestBody Map<String, String> form) {
        try {
            String memberId = form.get("memberId");
            groupBuyVoucherService.redeem(
                    StringUtils.hasText(memberId) ? Long.valueOf(memberId) : null,
                    form.get("voucherCode"));
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.error("用户信息不正确，请重新登录");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/status")
    public Result<Void> changeStatus(@RequestBody Map<String, String> form) {
        try {
            groupBuyVoucherService.changeStatus(Long.valueOf(form.get("id")), form.get("status"));
            return Result.success();
        } catch (NumberFormatException e) {
            return Result.error("团购券信息不正确");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
