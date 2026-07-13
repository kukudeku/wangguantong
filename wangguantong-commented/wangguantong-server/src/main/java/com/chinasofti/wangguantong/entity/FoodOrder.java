package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 点餐订单实体，对应 food_order 表。
 *
 * <p>订单会保存下单时的商品名称和单价。即使管理员以后修改或删除商品，
 * 历史订单仍能显示当时的真实内容。</p>
 */
@TableName("food_order")
public class FoodOrder {

    /** 订单主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员 id；散客订单可以为空。 */
    private Long memberId;

    /** 下单时保存的会员姓名。 */
    private String memberName;

    /** 顾客类型：会员或散客。 */
    private String customerType;

    /** 订单显示的顾客名称。 */
    private String customerName;

    /** 关联的商品 id。 */
    private Long foodItemId;

    /** 下单时的商品名称快照。 */
    private String foodItemName;

    /** 下单时的商品单价快照。 */
    private BigDecimal price;

    /** 购买数量，必须大于 0。 */
    private Integer quantity;

    /** 订单总金额，计算公式为 price × quantity。 */
    private BigDecimal totalAmount;

    /** 订单状态：已下单、已完成、已取消。 */
    private String status;

    /** 下单时间。 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(Long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodItemName() {
        return foodItemName;
    }

    public void setFoodItemName(String foodItemName) {
        this.foodItemName = foodItemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
