package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.mapper.MemberMapper;
import com.chinasofti.wangguantong.service.MemberService;
import org.springframework.stereotype.Service;

/** 用户业务实现，基础增删改查由 MyBatis-Plus 的 {@link ServiceImpl} 提供。 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {
}
