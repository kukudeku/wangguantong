package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.dto.FoodOrderBatchRequest;
import com.chinasofti.wangguantong.dto.PaymentCreateResponse;
import com.chinasofti.wangguantong.entity.FoodOrder;

import java.util.List;

public interface FoodOrderService extends IService<FoodOrder> {

    PaymentCreateResponse createOrder(FoodOrder foodOrder);

    PaymentCreateResponse createBatchOrder(FoodOrderBatchRequest request);

    void cancelOrder(Long id);

    void completeOrder(Long id);

    void fillPaymentStatuses(List<FoodOrder> orders);
}
