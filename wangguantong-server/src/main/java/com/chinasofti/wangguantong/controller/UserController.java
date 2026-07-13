package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.FoodOrder;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.OnlineRecord;
import com.chinasofti.wangguantong.entity.RechargeRecord;
import com.chinasofti.wangguantong.service.FoodOrderService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import com.chinasofti.wangguantong.service.PromotionService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final MemberService memberService;
    private final RechargeRecordService rechargeRecordService;
    private final FoodOrderService foodOrderService;
    private final OnlineRecordService onlineRecordService;
    private final PromotionService promotionService;

    public UserController(MemberService memberService,
                          RechargeRecordService rechargeRecordService,
                          FoodOrderService foodOrderService,
                          OnlineRecordService onlineRecordService,
                          PromotionService promotionService) {
        this.memberService = memberService;
        this.rechargeRecordService = rechargeRecordService;
        this.foodOrderService = foodOrderService;
        this.onlineRecordService = onlineRecordService;
        this.promotionService = promotionService;
    }

    @PostMapping("/login")
    public Result<Member> login(@RequestBody Member loginForm) {
        String idCard = loginForm.getUsername();
        String password = loginForm.getPassword();
        if (!StringUtils.hasText(idCard) || !StringUtils.hasText(password)) {
            return Result.error("身份证号或密码不能为空");
        }
        Member member = memberService.getOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getIdCard, idCard)
                .eq(Member::getPassword, password));

        if (member == null) {
            return Result.error("身份证号或密码错误");
        }
        if (!"正常".equals(member.getStatus())) {
            return Result.error("用户状态不是正常，请联系前台");
        }
        return Result.success(member);
    }

    @PostMapping("/register")
    public Result<Member> register(@RequestBody Member member) {
        try {
            return Result.success(promotionService.register(member));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<Member> changePassword(@RequestBody Map<String, String> form) {
        String memberIdText = form.get("memberId");
        String oldPassword = form.get("oldPassword");
        String newPassword = form.get("newPassword");
        if (!StringUtils.hasText(memberIdText) || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return Result.error("请完整填写原密码和新密码");
        }
        Member member = memberService.getById(Long.valueOf(memberIdText));
        if (member == null) {
            return Result.error("用户不存在");
        }
        if (!oldPassword.equals(member.getPassword())) {
            return Result.error("原密码错误");
        }
        member.setPassword(newPassword);
        memberService.updateById(member);
        return Result.success(member);
    }

    @GetMapping("/balance-detail/{memberId}")
    public Result<List<Map<String, Object>>> balanceDetail(@PathVariable Long memberId) {
        List<Map<String, Object>> list = new ArrayList<>();

        List<RechargeRecord> rechargeList = rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .eq(RechargeRecord::getMemberId, memberId));
        for (RechargeRecord record : rechargeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "充值");
            String rechargeType = StringUtils.hasText(record.getRechargeType()) ? record.getRechargeType() : "会员充值";
            String referenceLabel = rechargeType.contains("邀请") || rechargeType.contains("新人") ? "邀请码" : "券码";
            String reference = StringUtils.hasText(record.getReferenceNo())
                    ? "（" + referenceLabel + "：" + record.getReferenceNo() + "）" : "";
            item.put("description", rechargeType + reference);
            item.put("amount", record.getAmount());
            item.put("createTime", record.getCreateTime());
            list.add(item);
        }

        List<FoodOrder> foodOrders = foodOrderService.list(new LambdaQueryWrapper<FoodOrder>()
                .eq(FoodOrder::getMemberId, memberId)
                .and(wrapper -> wrapper.eq(FoodOrder::getPaymentMethod, "余额支付")
                        .or().isNull(FoodOrder::getPaymentMethod)
                        .or().eq(FoodOrder::getPaymentMethod, "")));
        for (FoodOrder order : foodOrders) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "点餐");
            item.put("description", order.getFoodItemName() + " x " + order.getQuantity() + "（" + order.getStatus() + "）");
            item.put("amount", "已取消".equals(order.getStatus()) ? BigDecimal.ZERO : order.getTotalAmount().negate());
            item.put("createTime", order.getCreateTime());
            list.add(item);
        }

        List<OnlineRecord> onlineRecords = onlineRecordService.list(new LambdaQueryWrapper<OnlineRecord>()
                .eq(OnlineRecord::getMemberId, memberId));
        for (OnlineRecord record : onlineRecords) {
            if (record.getTotalAmount() == null || record.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("type", "上机");
            item.put("description", record.getComputerNo() + ("进行中".equals(record.getStatus()) ? " 上机实时扣费" : " 上机结算"));
            item.put("amount", record.getTotalAmount().negate());
            item.put("createTime", record.getEndTime() == null ? record.getStartTime() : record.getEndTime());
            list.add(item);
        }

        list.sort(Comparator.comparing(item -> (LocalDateTime) item.get("createTime"), Comparator.nullsLast(Comparator.reverseOrder())));
        return Result.success(list);
    }

    @GetMapping("/orders/{memberId}")
    public Result<List<FoodOrder>> orders(@PathVariable Long memberId) {
        List<FoodOrder> orders = foodOrderService.list(new LambdaQueryWrapper<FoodOrder>()
                .eq(FoodOrder::getMemberId, memberId)
                .orderByDesc(FoodOrder::getId));
        foodOrderService.fillPaymentStatuses(orders);
        return Result.success(orders);
    }

    @GetMapping("/online-records/{memberId}")
    public Result<List<OnlineRecord>> onlineRecords(@PathVariable Long memberId) {
        List<OnlineRecord> records = onlineRecordService.listWithRunningInfo(null, null, null);
        records.removeIf(record -> !memberId.equals(record.getMemberId()));
        return Result.success(records);
    }
}
