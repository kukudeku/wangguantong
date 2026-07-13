package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.service.MemberService;
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

/**
 * 后台会员资料管理接口。
 *
 * <p>会员既包含后台维护的资料，也承担用户端登录账号的作用。
 * 当前系统把身份证号同时作为 username，方便初学者理解。</p>
 */
@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /** 按姓名、身份证号、手机号模糊查询会员。 */
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

    /** 根据主键查询单个会员，常用于充值或查看详情前确认会员存在。 */
    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        return Result.success(member);
    }

    /**
     * 后台新增会员。余额、状态、注册时间等关键字段由后端统一初始化，
     * 不信任前端传入的默认值。
     */
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
        // 用户端实际使用身份证号登录，因此同步保存到 username 字段。
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
        member.setCreateTime(LocalDateTime.now());
        memberService.save(member);
        return Result.success();
    }

    /**
     * 修改会员资料。前端若不填写新密码，就不会覆盖原密码。
     */
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

    /** 删除会员基础资料。课堂项目未做历史记录外键约束，正式系统应改为逻辑删除。 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (memberService.getById(id) == null) {
            return Result.error("会员不存在");
        }
        memberService.removeById(id);
        return Result.success();
    }
}
