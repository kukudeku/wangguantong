package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.PromotionService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final PromotionService promotionService;

    public MemberController(MemberService memberService, PromotionService promotionService) {
        this.memberService = memberService;
        this.promotionService = promotionService;
    }

    @GetMapping("/list")
    public Result<List<Member>> list(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String idCard,
                                     @RequestParam(required = false) String phone) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Member::getName, name)
                .like(StringUtils.hasText(idCard), Member::getIdCard, idCard)
                .like(StringUtils.hasText(phone), Member::getPhone, phone)
                .orderByDesc(Member::getId);
        return Result.success(memberService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        return Result.success(member);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Member member) {
        if (!StringUtils.hasText(member.getName())) {
            return Result.error("会员姓名不能为空");
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
        member.setUsername(member.getIdCard());
        member.setId(null);
        member.setBalance(BigDecimal.ZERO);
        if (!StringUtils.hasText(member.getUserType())) {
            member.setUserType("会员");
        }
        if (!StringUtils.hasText(member.getMemberLevel())) {
            member.setMemberLevel("普通会员");
        }
        member.setStatus("正常");
        member.setInviteCode(null);
        member.setInviterMemberId(null);
        member.setCreateTime(LocalDateTime.now());
        memberService.save(member);
        promotionService.ensureInviteCode(member);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Member member) {
        if (member.getId() == null || memberService.getById(member.getId()) == null) {
            return Result.error("会员不存在");
        }
        if (!StringUtils.hasText(member.getIdCard())) {
            return Result.error("身份证号不能为空");
        }
        member.setUsername(member.getIdCard());
        memberService.updateById(member);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (memberService.getById(id) == null) {
            return Result.error("会员不存在");
        }
        memberService.removeById(id);
        return Result.success();
    }
}
