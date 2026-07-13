package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.dto.FoodOrderBatchRequest;
import com.chinasofti.wangguantong.dto.PaymentCreateResponse;
import com.chinasofti.wangguantong.entity.FoodItem;
import com.chinasofti.wangguantong.entity.FoodOrder;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.UserCoupon;
import com.chinasofti.wangguantong.mapper.FoodOrderMapper;
import com.chinasofti.wangguantong.service.FoodItemService;
import com.chinasofti.wangguantong.service.FoodOrderService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.CouponService;
import com.chinasofti.wangguantong.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FoodOrderServiceImpl extends ServiceImpl<FoodOrderMapper, FoodOrder> implements FoodOrderService {

    private static final String BALANCE_PAYMENT = "余额支付";
    private static final List<String> PAYMENT_METHODS = List.of(BALANCE_PAYMENT, "微信支付", "支付宝支付");

    private final FoodItemService foodItemService;
    private final MemberService memberService;
    private final CouponService couponService;
    private final PaymentService paymentService;

    public FoodOrderServiceImpl(FoodItemService foodItemService,
                                MemberService memberService,
                                CouponService couponService,
                                PaymentService paymentService) {
        this.foodItemService = foodItemService;
        this.memberService = memberService;
        this.couponService = couponService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentCreateResponse createOrder(FoodOrder foodOrder) {
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

        String paymentMethod = normalizePaymentMethod(foodOrder.getPaymentMethod());
        foodOrder.setPaymentMethod(paymentMethod);
        foodOrder.setFoodItemName(item.getName());
        foodOrder.setPrice(item.getPrice());
        BigDecimal grossAmount = item.getPrice().multiply(BigDecimal.valueOf(foodOrder.getQuantity()));
        foodOrder.setBatchNo(createBatchNo());
        foodOrder.setGrossAmount(grossAmount);
        foodOrder.setDiscountAmount(BigDecimal.ZERO);
        foodOrder.setTotalAmount(grossAmount);
        foodOrder.setStatus(BALANCE_PAYMENT.equals(paymentMethod) ? "已下单" : "待支付");
        foodOrder.setCreateTime(LocalDateTime.now());

        if ("会员".equals(foodOrder.getCustomerType())) {
            Member member = memberService.getById(foodOrder.getMemberId());
            if (member == null) {
                throw new RuntimeException("会员不存在");
            }
            if (BALANCE_PAYMENT.equals(paymentMethod)) {
                if (member.getBalance().compareTo(foodOrder.getTotalAmount()) < 0) {
                    throw new RuntimeException("会员余额不足，请先充值");
                }
                member.setBalance(member.getBalance().subtract(foodOrder.getTotalAmount()));
                memberService.updateById(member);
            }
            foodOrder.setMemberName(member.getName());
            foodOrder.setCustomerName(member.getName());
        } else {
            if (BALANCE_PAYMENT.equals(paymentMethod)) {
                throw new RuntimeException("散客不能使用余额支付，请选择微信支付或支付宝支付");
            }
            foodOrder.setCustomerType("散客");
            if (foodOrder.getCustomerName() == null || foodOrder.getCustomerName().trim().isEmpty()) {
                foodOrder.setCustomerName("散客");
            }
        }

        save(foodOrder);
        if (BALANCE_PAYMENT.equals(paymentMethod)) {
            return createPaidResponse(foodOrder.getBatchNo(), paymentMethod, foodOrder.getTotalAmount());
        }
        return paymentService.createPayment(foodOrder.getBatchNo(), foodOrder.getMemberId(),
                paymentMethod, foodOrder.getTotalAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentCreateResponse createBatchOrder(FoodOrderBatchRequest request) {
        if (request == null || request.getMemberId() == null) {
            throw new RuntimeException("用户信息不存在，请重新登录");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("购物车不能为空");
        }
        Member member = memberService.getById(request.getMemberId());
        if (member == null || !"正常".equals(member.getStatus())) {
            throw new RuntimeException("用户不存在或状态异常");
        }

        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());
        List<FoodOrder> orders = new ArrayList<>();
        BigDecimal grossTotal = BigDecimal.ZERO;
        for (FoodOrderBatchRequest.Item requestItem : request.getItems()) {
            if (requestItem.getFoodItemId() == null || requestItem.getQuantity() == null || requestItem.getQuantity() <= 0) {
                throw new RuntimeException("商品或数量不正确");
            }
            FoodItem item = foodItemService.getById(requestItem.getFoodItemId());
            if (item == null || !"上架".equals(item.getStatus())) {
                throw new RuntimeException("购物车中有商品已下架，请重新选择");
            }
            BigDecimal lineGross = item.getPrice().multiply(BigDecimal.valueOf(requestItem.getQuantity()));
            FoodOrder order = new FoodOrder();
            order.setMemberId(member.getId());
            order.setMemberName(member.getName());
            order.setCustomerType("会员");
            order.setCustomerName(member.getName());
            order.setFoodItemId(item.getId());
            order.setFoodItemName(item.getName());
            order.setPrice(item.getPrice());
            order.setQuantity(requestItem.getQuantity());
            order.setGrossAmount(lineGross);
            order.setPaymentMethod(paymentMethod);
            orders.add(order);
            grossTotal = grossTotal.add(lineGross);
        }

        String batchNo = createBatchNo();
        UserCoupon coupon = couponService.useCoupon(request.getUserCouponId(), member.getId(), grossTotal, batchNo);
        BigDecimal discountTotal = coupon == null ? BigDecimal.ZERO : coupon.getDiscountAmount().min(grossTotal);
        BigDecimal payable = grossTotal.subtract(discountTotal);
        if (BALANCE_PAYMENT.equals(paymentMethod)) {
            if (member.getBalance().compareTo(payable) < 0) {
                throw new RuntimeException("会员余额不足，请先充值");
            }
            member.setBalance(member.getBalance().subtract(payable));
            memberService.updateById(member);
        }

        BigDecimal remainingDiscount = discountTotal;
        LocalDateTime now = LocalDateTime.now();
        for (FoodOrder order : orders) {
            BigDecimal lineDiscount = order.getGrossAmount().min(remainingDiscount);
            remainingDiscount = remainingDiscount.subtract(lineDiscount);
            order.setBatchNo(batchNo);
            order.setDiscountAmount(lineDiscount);
            order.setCouponName(lineDiscount.compareTo(BigDecimal.ZERO) > 0 ? coupon.getCouponName() : null);
            order.setTotalAmount(order.getGrossAmount().subtract(lineDiscount));
            order.setStatus(BALANCE_PAYMENT.equals(paymentMethod) || payable.compareTo(BigDecimal.ZERO) == 0
                    ? "已下单" : "待支付");
            order.setCreateTime(now);
            save(order);
        }
        if (BALANCE_PAYMENT.equals(paymentMethod)) {
            return createPaidResponse(batchNo, paymentMethod, payable);
        }
        return paymentService.createPayment(batchNo, member.getId(), paymentMethod, payable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
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
        if ("待支付".equals(order.getStatus())) {
            throw new RuntimeException("订单正在等待支付，超时后会自动关闭");
        }
        if ("退款中".equals(order.getStatus())) {
            throw new RuntimeException("订单正在退款，请稍后查看结果");
        }
        if (!usesBalance(order.getPaymentMethod())) {
            paymentService.refundOrder(order);
            return;
        }
        if ("会员".equals(order.getCustomerType()) && order.getMemberId() != null && usesBalance(order.getPaymentMethod())) {
            Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                member.setBalance(member.getBalance().add(order.getTotalAmount()));
                memberService.updateById(member);
            }
        }
        order.setStatus("已取消");
        updateById(order);
        if (order.getBatchNo() != null) {
            long activeCount = count(new LambdaQueryWrapper<FoodOrder>()
                    .eq(FoodOrder::getBatchNo, order.getBatchNo())
                    .ne(FoodOrder::getStatus, "已取消"));
            if (activeCount == 0) {
                couponService.restoreCoupon(order.getBatchNo());
            }
        }
    }

    @Override
    public void completeOrder(Long id) {
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

    @Override
    public void fillPaymentStatuses(List<FoodOrder> orders) {
        paymentService.fillPaymentStatuses(orders);
    }

    private String createBatchNo() {
        return "FO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String value = StringUtils.hasText(paymentMethod) ? paymentMethod.trim() : BALANCE_PAYMENT;
        if (!PAYMENT_METHODS.contains(value)) {
            throw new RuntimeException("支付方式不正确");
        }
        return value;
    }

    private boolean usesBalance(String paymentMethod) {
        return !StringUtils.hasText(paymentMethod) || BALANCE_PAYMENT.equals(paymentMethod);
    }

    private PaymentCreateResponse createPaidResponse(String batchNo, String paymentMethod, BigDecimal amount) {
        PaymentCreateResponse response = new PaymentCreateResponse();
        response.setOrderBatchNo(batchNo);
        response.setPaymentMethod(paymentMethod);
        response.setAmount(amount);
        response.setStatus("已支付");
        return response;
    }
}
