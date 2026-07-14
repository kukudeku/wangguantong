package com.chinasofti.wangguantong.service.impl;

import com.chinasofti.wangguantong.entity.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberServiceImplTest {

    @Test
    void shouldInsertMemberWithTemporaryInviteCodeBeforeCreatingFinalCode() {
        FakeMemberService memberService = new FakeMemberService();

        Member member = new Member();
        Member result = memberService.saveNewMember(member);

        assertNotNull(memberService.inviteCodeAtInsert);
        assertTrue(memberService.inviteCodeAtInsert.startsWith("TMP"));
        assertEquals(20, memberService.inviteCodeAtInsert.length());
        assertEquals("WG000012", result.getInviteCode());
        assertTrue(memberService.updated);
    }

    private static class FakeMemberService extends MemberServiceImpl {

        private String inviteCodeAtInsert;
        private boolean updated;

        @Override
        public boolean save(Member member) {
            inviteCodeAtInsert = member.getInviteCode();
            member.setId(12L);
            return true;
        }

        @Override
        public boolean updateById(Member member) {
            updated = true;
            return true;
        }
    }
}
