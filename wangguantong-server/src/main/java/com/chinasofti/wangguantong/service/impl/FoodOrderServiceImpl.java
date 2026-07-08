package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.FoodItem;
import com.chinasofti.wangguantong.entity.FoodOrder;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.mapper.FoodOrderMapper;
import com.chinasofti.wangguantong.service.FoodItemService;
import com.chinasofti.wangguantong.service.FoodOrderService;
import com.chinasofti.wangguantong.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FoodOrderServiceImpl extends ServiceImpl<FoodOrderMapper, FoodOrder> implements FoodOrderService {

    private final FoodItemService foodItemService;
    private final MemberService memberService;

    public FoodOrderServiceImpl(FoodItemService foodItemService, MemberService memberService) {
        this.foodItemService = foodItemService;
        this.memberService = memberService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(FoodOrder foodOrder) {
        if (foodOrder.getFoodItemId() == null) {
            throw new RuntimeException("请选择商品");
        }
        if (foodOrder.getQuantity() == null || foodOrder.getQuantity() <= 0) {
            throw new RuntimeException("数量必须大于 0");
        }

        FoodItem item = foodItemService.getById(foodOrder.getFoodItemId());
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"上架".equals(item.getStatus())) {
            throw new RuntimeException("商品已下架，不能下单");
        }

        foodOrder.setFoodItemName(item.getName());
        foodOrder.setPrice(item.getPrice());
        foodOrder.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(foodOrder.getQuantity())));
        foodOrder.setStatus("已下单");
        foodOrder.setCreateTime(LocalDateTime.now());

        if ("会员".equals(foodOrder.getCustomerType())) {
            Member member = memberService.getById(foodOrder.getMemberId());
            if (member == null) {
                throw new RuntimeException("会员不存在");
            }
            if (member.getBalance().compareTo(foodOrder.getTotalAmount()) < 0) {
                throw new RuntimeException("会员余额不足，请先充值");
            }
            member.setBalance(member.getBalance().subtract(foodOrder.getTotalAmount()));
            memberService.updateById(member);
            foodOrder.setMemberName(member.getName());
            foodOrder.setCustomerName(member.getName());
        } else {
            foodOrder.setCustomerType("散客");
            if (foodOrder.getCustomerName() == null || foodOrder.getCustomerName().trim().isEmpty()) {
                foodOrder.setCustomerName("散客");
            }
        }

        save(foodOrder);
    }

    @Override
    public void cancelOrder(Long id) {
        FoodOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("已取消".equals(order.getStatus())) {
            throw new RuntimeException("订单已经取消");
        }
        if ("会员".equals(order.getCustomerType()) && order.getMemberId() != null) {
            Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                member.setBalance(member.getBalance().add(order.getTotalAmount()));
                memberService.updateById(member);
            }
        }
        order.setStatus("已取消");
        updateById(order);
    }
}
