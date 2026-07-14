package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.Member;

public interface MemberService extends IService<Member> {

    Member saveNewMember(Member member);

    Member ensureInviteCode(Member member);
}
