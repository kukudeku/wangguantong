package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/** 网吧点餐商品实体，对应 food_item 表。 */
@TableName("food_item")
public class FoodItem {

    /** 商品主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称，例如“可乐”。 */
    private String name;

    /** 商品分类，例如饮料、餐食、零食、其他。 */
    private String category;

    /** 商品当前售价。订单创建时会复制一份价格到订单中。 */
    private BigDecimal price;

    /** 上架状态：上架或下架。用户端只查询上架商品。 */
    private String status;

    /** 商品描述，例如“冰镇饮料”。 */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
