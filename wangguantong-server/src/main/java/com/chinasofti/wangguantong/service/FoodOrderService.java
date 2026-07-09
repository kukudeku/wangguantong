package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.FoodOrder;

public interface FoodOrderService extends IService<FoodOrder> {

    void createOrder(FoodOrder foodOrder);

    void cancelOrder(Long id);

    void completeOrder(Long id);
}
