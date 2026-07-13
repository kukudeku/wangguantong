package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.ReservationRecord;

public interface ReservationRecordService extends IService<ReservationRecord> {

    void addReservation(Long memberId, Long computerId, String reserveTimeText);

    void cancelReservation(Long id);

    void cancelReservationForMember(Long id, Long memberId);

    void startFromReservation(Long id);

    void startFromReservationForMember(Long id, Long memberId);

    void expireReservations();
}
