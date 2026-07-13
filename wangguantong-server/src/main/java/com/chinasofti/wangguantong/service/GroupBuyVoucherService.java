package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.GroupBuyVoucher;

public interface GroupBuyVoucherService extends IService<GroupBuyVoucher> {

    void createVoucher(GroupBuyVoucher voucher);

    void redeem(Long memberId, String voucherCode);

    void changeStatus(Long voucherId, String status);
}
