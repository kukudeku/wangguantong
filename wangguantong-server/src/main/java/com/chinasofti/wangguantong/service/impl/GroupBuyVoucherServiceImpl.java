package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.GroupBuyVoucher;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.mapper.GroupBuyVoucherMapper;
import com.chinasofti.wangguantong.service.GroupBuyVoucherService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.RechargeRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class GroupBuyVoucherServiceImpl extends ServiceImpl<GroupBuyVoucherMapper, GroupBuyVoucher>
        implements GroupBuyVoucherService {

    private final MemberService memberService;
    private final RechargeRecordService rechargeRecordService;

    public GroupBuyVoucherServiceImpl(MemberService memberService,
                                      RechargeRecordService rechargeRecordService) {
        this.memberService = memberService;
        this.rechargeRecordService = rechargeRecordService;
    }

    @Override
    public void createVoucher(GroupBuyVoucher voucher) {
        if (voucher == null || !StringUtils.hasText(voucher.getVoucherCode())) {
            throw new RuntimeException("团购券码不能为空");
        }
        if (voucher.getAmount() == null || voucher.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于 0");
        }
        String voucherCode = voucher.getVoucherCode().trim();
        long count = count(new LambdaQueryWrapper<GroupBuyVoucher>()
                .eq(GroupBuyVoucher::getVoucherCode, voucherCode));
        if (count > 0) {
            throw new RuntimeException("该团购券码已存在");
        }
        if (voucher.getExpireTime() != null && !voucher.getExpireTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("有效期必须晚于当前时间");
        }
        voucher.setId(null);
        voucher.setVoucherCode(voucherCode);
        voucher.setStatus("未使用");
        voucher.setUsedMemberId(null);
        voucher.setUsedMemberName(null);
        voucher.setUsedTime(null);
        voucher.setCreateTime(LocalDateTime.now());
        save(voucher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeem(Long memberId, String voucherCode) {
        if (memberId == null) {
            throw new RuntimeException("用户信息不存在，请重新登录");
        }
        if (!StringUtils.hasText(voucherCode)) {
            throw new RuntimeException("请输入团购券码");
        }
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (!"正常".equals(member.getStatus())) {
            throw new RuntimeException("用户状态异常，不能核销团购券");
        }

        String normalizedCode = voucherCode.trim();
        GroupBuyVoucher voucher = getOne(new LambdaQueryWrapper<GroupBuyVoucher>()
                .eq(GroupBuyVoucher::getVoucherCode, normalizedCode));
        if (voucher == null) {
            throw new RuntimeException("后台未录入该团购券，无法充值");
        }
        if ("已使用".equals(voucher.getStatus())) {
            throw new RuntimeException("该团购券已核销，不能重复充值");
        }
        if ("已禁用".equals(voucher.getStatus())) {
            throw new RuntimeException("该团购券已被后台禁用");
        }
        if (!"未使用".equals(voucher.getStatus())) {
            throw new RuntimeException("该团购券状态异常，无法充值");
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getExpireTime() != null && !voucher.getExpireTime().isAfter(now)) {
            throw new RuntimeException("该团购券已过期");
        }

        boolean claimed = update(new LambdaUpdateWrapper<GroupBuyVoucher>()
                .eq(GroupBuyVoucher::getId, voucher.getId())
                .eq(GroupBuyVoucher::getStatus, "未使用")
                .set(GroupBuyVoucher::getStatus, "已使用")
                .set(GroupBuyVoucher::getUsedMemberId, member.getId())
                .set(GroupBuyVoucher::getUsedMemberName, member.getName())
                .set(GroupBuyVoucher::getUsedTime, now));
        if (!claimed) {
            throw new RuntimeException("团购券已被核销，请勿重复提交");
        }
        rechargeRecordService.recharge(member.getId(), voucher.getAmount(), "团购券充值", normalizedCode);
    }

    @Override
    public void changeStatus(Long voucherId, String status) {
        if (voucherId == null || !("未使用".equals(status) || "已禁用".equals(status))) {
            throw new RuntimeException("团购券状态不正确");
        }
        GroupBuyVoucher voucher = getById(voucherId);
        if (voucher == null) {
            throw new RuntimeException("团购券不存在");
        }
        if ("已使用".equals(voucher.getStatus())) {
            throw new RuntimeException("已使用的团购券不能修改状态");
        }
        voucher.setStatus(status);
        updateById(voucher);
    }
}
