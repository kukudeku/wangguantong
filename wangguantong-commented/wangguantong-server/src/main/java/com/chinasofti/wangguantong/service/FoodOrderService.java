package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.FoodOrder;

/**
 * 点餐订单业务接口。
 *
 * <p>订单不仅是简单的数据保存，还涉及商品状态校验、余额扣除、取消退款和订单状态流转，
 * 因此这些操作被集中放在 Service 层。</p>
 */
public interface FoodOrderService extends IService<FoodOrder> {

    /** 创建订单；会员订单会同时扣除余额。 */
    void createOrder(FoodOrder foodOrder);

    /** 取消未完成订单；会员已支付的金额会退回余额。 */
    void cancelOrder(Long id);

    /** 将“已下单”的订单标记为“已完成”。 */
    void completeOrder(Long id);
}
