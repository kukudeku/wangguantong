package com.chinasofti.wangguantong.service.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentServiceImplTest {

    @Test
    void shouldFormatWechatExpireTimeWithoutFractionalSeconds() {
        LocalDateTime expireTime = LocalDateTime.of(2026, 7, 14, 2, 42, 52, 688_941_100);

        String result = PaymentServiceImpl.formatWechatExpireTime(
                expireTime,
                ZoneId.of("Asia/Shanghai")
        );

        assertEquals("2026-07-14T02:42:52+08:00", result);
    }
}
