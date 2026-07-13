package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.ReservationRecord;

/** 电脑预约业务接口，负责预约锁定、取消、预约上机和超时清理。 */
public interface ReservationRecordService extends IService<ReservationRecord> {

    /** 新增一条一小时范围内的预约，并锁定电脑。 */
    void addReservation(Long memberId, Long computerId, String reserveTimeText);

    /** 取消有效预约并释放电脑。 */
    void cancelReservation(Long id);

    /** 使用预约记录办理上机。 */
    void startFromReservation(Long id);

    /** 定时取消已经超过预约时间、但仍未上机的预约。 */
    void expireReservations();
}
