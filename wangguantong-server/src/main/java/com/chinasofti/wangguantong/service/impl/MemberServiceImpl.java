package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.mapper.MemberMapper;
import com.chinasofti.wangguantong.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private static final String PENDING_INVITE_CODE_PREFIX = "TMP";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member saveNewMember(Member member) {
        // 数据库生成会员 ID 前，先写入唯一临时值，兼容 invite_code 非空约束。
        member.setInviteCode(createPendingInviteCode());
        if (!save(member) || member.getId() == null) {
            throw new RuntimeException("会员保存失败");
        }
        member.setInviteCode(createInviteCode(member.getId()));
        if (!updateById(member)) {
            throw new RuntimeException("会员邀请码生成失败");
        }
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member ensureInviteCode(Member member) {
        if (member == null || member.getId() == null) {
            throw new RuntimeException("用户信息不正确");
        }
        if (!StringUtils.hasText(member.getInviteCode())
                || member.getInviteCode().startsWith(PENDING_INVITE_CODE_PREFIX)) {
            member.setInviteCode(createInviteCode(member.getId()));
            if (!updateById(member)) {
                throw new RuntimeException("会员邀请码生成失败");
            }
        }
        return member;
    }

    private String createPendingInviteCode() {
        String randomText = UUID.randomUUID().toString().replace("-", "").substring(0, 17).toUpperCase();
        return PENDING_INVITE_CODE_PREFIX + randomText;
    }

    private String createInviteCode(Long memberId) {
        return "WG" + String.format("%06d", memberId);
    }
}
