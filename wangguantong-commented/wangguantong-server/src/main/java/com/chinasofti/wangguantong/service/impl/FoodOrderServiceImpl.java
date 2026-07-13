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

/**
 * 点餐订单业务实现。
 *
 * <p>主要业务状态流转为：已下单 -> 已完成，或者 已下单 -> 已取消。
 * 会员下单会立即扣余额；取消时会把该订单金额退回。</p>
 */
@Service
public class FoodOrderServiceImpl extends ServiceImpl<FoodOrderMapper, FoodOrder> implements FoodOrderService {

    /** 用于读取商品的最新名称、价格和上下架状态。 */
    private final FoodItemService foodItemService;
    /** 用于校验会员并修改会员余额。 */
    private final MemberService memberService;

    /** 构造器注入：Spring 启动时自动把两个 Service 对象传进来。 */
    public FoodOrderServiceImpl(FoodItemService foodItemService, MemberService memberService) {
        this.foodItemService = foodItemService;
        this.memberService = memberService;
    }

    @Override
    // 订单保存和余额扣除必须一起成功；任何一步抛出异常，事务会回滚前面的数据库修改。
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(FoodOrder foodOrder) {
        // 先检查前端必须提交的关键参数，避免后续出现空指针或无效订单。
        if (foodOrder.getFoodItemId() == null) {
            throw new RuntimeException("请选择商品");
        }
        if (foodOrder.getQuantity() == null || foodOrder.getQuantity() <= 0) {
            throw new RuntimeException("数量必须大于 0");
        }

        // 价格不能相信前端传值，必须按商品编号从数据库重新读取真实商品。
        FoodItem item = foodItemService.getById(foodOrder.getFoodItemId());
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"上架".equals(item.getStatus())) {
            throw new RuntimeException("商品已下架，不能下单");
        }

        // 把下单当时的商品名称和价格保存到订单。以后即使商品改价，历史订单金额也不会变化。
        foodOrder.setFoodItemName(item.getName());
        foodOrder.setPrice(item.getPrice());
        foodOrder.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(foodOrder.getQuantity())));
        foodOrder.setStatus("已下单");
        foodOrder.setCreateTime(LocalDateTime.now());

        if ("会员".equals(foodOrder.getCustomerType())) {
            // 会员订单从账户余额支付，所以要验证会员存在且余额充足。
            Member member = memberService.getById(foodOrder.getMemberId());
            if (member == null) {
                throw new RuntimeException("会员不存在");
            }
            if (member.getBalance().compareTo(foodOrder.getTotalAmount()) < 0) {
                throw new RuntimeException("会员余额不足，请先充值");
            }
            // 下单时立即扣款，不等待后台点击“完成”。
            member.setBalance(member.getBalance().subtract(foodOrder.getTotalAmount()));
            memberService.updateById(member);
            foodOrder.setMemberName(member.getName());
            foodOrder.setCustomerName(member.getName());
        } else {
            // 非会员请求统一按散客保存，姓名为空时使用默认名称，避免数据库中出现难识别的空值。
            foodOrder.setCustomerType("散客");
            if (foodOrder.getCustomerName() == null || foodOrder.getCustomerName().trim().isEmpty()) {
                foodOrder.setCustomerName("散客");
            }
        }

        // ServiceImpl.save 会调用 FoodOrderMapper 执行 INSERT。
        save(foodOrder);
    }

    @Override
    public void cancelOrder(Long id) {
        // 只有存在且尚未结束的订单才允许取消。
        FoodOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("已取消".equals(order.getStatus())) {
            throw new RuntimeException("订单已经取消");
        }
        if ("已完成".equals(order.getStatus())) {
            throw new RuntimeException("订单已完成，不能取消");
        }
        // 会员订单在创建时已经扣款，因此取消时需要按原订单金额退款。
        if ("会员".equals(order.getCustomerType()) && order.getMemberId() != null) {
            Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                member.setBalance(member.getBalance().add(order.getTotalAmount()));
                memberService.updateById(member);
            }
        }
        // 状态改为“已取消”后，后台列表和用户订单记录都会显示最终结果。
        order.setStatus("已取消");
        updateById(order);
    }

    @Override
    public void completeOrder(Long id) {
        // “完成”表示商品已经交付给顾客，不再允许取消和退款。
        FoodOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"已下单".equals(order.getStatus())) {
            throw new RuntimeException("只有已下单订单可以完成");
        }
        order.setStatus("已完成");
        updateById(order);
    }
}
