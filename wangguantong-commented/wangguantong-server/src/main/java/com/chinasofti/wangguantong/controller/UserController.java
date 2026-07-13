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

/**
 * 顾客自助端专用接口。
 *
 * <p>它负责身份证号登录、注册、修改密码，以及把充值、点餐、上机记录整理成
 * 用户容易理解的消费明细。项目按作业要求没有 JWT，登录态由前端 localStorage 保存。</p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final MemberService memberService;
    private final RechargeRecordService rechargeRecordService;
    private final FoodOrderService foodOrderService;
    private final OnlineRecordService onlineRecordService;

    public UserController(MemberService memberService,
                          RechargeRecordService rechargeRecordService,
                          FoodOrderService foodOrderService,
                          OnlineRecordService onlineRecordService) {
        this.memberService = memberService;
        this.rechargeRecordService = rechargeRecordService;
        this.foodOrderService = foodOrderService;
        this.onlineRecordService = onlineRecordService;
    }

    /**
     * 用户登录。前端字段名仍叫 username，但实际填写的是身份证号。
     */
    @PostMapping("/login")
    public Result<Member> login(@RequestBody Member loginForm) {
        String idCard = loginForm.getUsername();
        String password = loginForm.getPassword();
        if (!StringUtils.hasText(idCard) || !StringUtils.hasText(password)) {
            return Result.error("身份证号或密码不能为空");
        }
        // 同时匹配身份证号和密码；getOne 表示最多期望得到一个账号。
        Member member = memberService.getOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getIdCard, idCard)
                .eq(Member::getPassword, password));

        if (member == null) {
            return Result.error("身份证号或密码错误");
        }
        // 禁用账号即使密码正确也不能进入用户端。
        if (!"正常".equals(member.getStatus())) {
            return Result.error("用户状态不是正常，请联系前台");
        }
        return Result.success(member);
    }

    /**
     * 用户自助注册。身份证号和手机号都必须唯一，新账号默认为普通会员且余额为 0。
     */
    @PostMapping("/register")
    public Result<Member> register(@RequestBody Member member) {
        if (!StringUtils.hasText(member.getName())) {
            return Result.error("姓名不能为空");
        }
        if (!StringUtils.hasText(member.getIdCard())) {
            return Result.error("身份证号不能为空");
        }
        if (!StringUtils.hasText(member.getPhone())) {
            return Result.error("手机号不能为空");
        }
        if (!StringUtils.hasText(member.getPassword())) {
            return Result.error("密码不能为空");
        }
        // 分别检查身份证号、手机号，给用户返回更准确的重复提示。
        long idCardCount = memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getIdCard, member.getIdCard()));
        if (idCardCount > 0) {
            return Result.error("该身份证号已注册");
        }
        long phoneCount = memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhone, member.getPhone()));
        if (phoneCount > 0) {
            return Result.error("该手机号已注册");
        }

        // 这些初始值由后端决定，避免前端伪造会员等级或初始余额。
        member.setId(null);
        member.setUsername(member.getIdCard());
        member.setBalance(BigDecimal.ZERO);
        member.setUserType("会员");
        member.setMemberLevel("普通会员");
        member.setStatus("正常");
        member.setCreateTime(LocalDateTime.now());
        memberService.save(member);
        return Result.success(member);
    }

    /**
     * 用户修改自己的登录密码。先比对原密码，再保存新密码。
     */
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

    /**
     * 生成统一的余额明细。
     *
     * <p>充值、点餐、上机来自三张不同的数据表。此方法把它们转换成相同的
     * type、description、amount、createTime 四个字段，再按时间倒序排列。</p>
     */
    @GetMapping("/balance-detail/{memberId}")
    public Result<List<Map<String, Object>>> balanceDetail(@PathVariable Long memberId) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 1. 充值是收入，所以 amount 保持正数。
        List<RechargeRecord> rechargeList = rechargeRecordService.list(new LambdaQueryWrapper<RechargeRecord>()
                .eq(RechargeRecord::getMemberId, memberId));
        for (RechargeRecord record : rechargeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "充值");
            item.put("description", "会员充值");
            item.put("amount", record.getAmount());
            item.put("createTime", record.getCreateTime());
            list.add(item);
        }

        // 2. 点餐是支出，所以正常订单取负数；已取消订单已退款，明细金额记为 0。
        List<FoodOrder> foodOrders = foodOrderService.list(new LambdaQueryWrapper<FoodOrder>()
                .eq(FoodOrder::getMemberId, memberId));
        for (FoodOrder order : foodOrders) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "点餐");
            item.put("description", order.getFoodItemName() + " x " + order.getQuantity() + "（" + order.getStatus() + "）");
            item.put("amount", "已取消".equals(order.getStatus()) ? BigDecimal.ZERO : order.getTotalAmount().negate());
            item.put("createTime", order.getCreateTime());
            list.add(item);
        }

        // 3. 上机也是支出。没有产生费用的记录不显示，避免明细出现无意义的 0 元行。
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

        // null 时间放在最后，其余记录按时间从新到旧排列。
        list.sort(Comparator.comparing(item -> (LocalDateTime) item.get("createTime"), Comparator.nullsLast(Comparator.reverseOrder())));
        return Result.success(list);
    }

    /** 查询当前会员的点餐订单，最新订单在前。 */
    @GetMapping("/orders/{memberId}")
    public Result<List<FoodOrder>> orders(@PathVariable Long memberId) {
        return Result.success(foodOrderService.list(new LambdaQueryWrapper<FoodOrder>()
                .eq(FoodOrder::getMemberId, memberId)
                .orderByDesc(FoodOrder::getId)));
    }

    /**
     * 查询当前会员的上机记录。先复用 Service 计算实时字段，再过滤出本会员的数据。
     */
    @GetMapping("/online-records/{memberId}")
    public Result<List<OnlineRecord>> onlineRecords(@PathVariable Long memberId) {
        List<OnlineRecord> records = onlineRecordService.listWithRunningInfo(null, null, null);
        records.removeIf(record -> !memberId.equals(record.getMemberId()));
        return Result.success(records);
    }
}
