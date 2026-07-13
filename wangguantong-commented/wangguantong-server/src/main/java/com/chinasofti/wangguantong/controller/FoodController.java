package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.FoodItem;
import com.chinasofti.wangguantong.entity.FoodOrder;
import com.chinasofti.wangguantong.service.FoodItemService;
import com.chinasofti.wangguantong.service.FoodOrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 网吧商品与点餐订单接口。
 *
 * <p>接口分为两组：/item/* 管理商品，/order/* 管理顾客订单。
 * 商品基础增删改查直接使用 MyBatis-Plus；扣余额、退款等组合操作交给 Service 处理。</p>
 */
@RestController
@RequestMapping("/food")
public class FoodController {

    private final FoodItemService foodItemService;
    private final FoodOrderService foodOrderService;

    public FoodController(FoodItemService foodItemService, FoodOrderService foodOrderService) {
        this.foodItemService = foodItemService;
        this.foodOrderService = foodOrderService;
    }

    /** 后台查询商品，支持名称、分类、状态组合筛选。 */
    @GetMapping("/item/list")
    public Result<List<FoodItem>> itemList(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FoodItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), FoodItem::getName, name)
                .eq(StringUtils.hasText(category), FoodItem::getCategory, category)
                .eq(StringUtils.hasText(status), FoodItem::getStatus, status)
                .orderByDesc(FoodItem::getId);
        return Result.success(foodItemService.list(wrapper));
    }

    /** 用户端只需要看到“上架”商品，因此提供一个更简单的专用接口。 */
    @GetMapping("/item/available")
    public Result<List<FoodItem>> availableItems() {
        return Result.success(foodItemService.list(new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getStatus, "上架")
                .orderByAsc(FoodItem::getId)));
    }

    /** 新增商品，并为未填写的状态和分类设置默认值。 */
    @PostMapping("/item/add")
    public Result<Void> addItem(@RequestBody FoodItem item) {
        if (!StringUtils.hasText(item.getName())) {
            return Result.error("商品名称不能为空");
        }
        if (item.getPrice() == null) {
            return Result.error("商品价格不能为空");
        }
        // 主键交给数据库生成，防止前端误传 id 覆盖旧数据。
        item.setId(null);
        if (!StringUtils.hasText(item.getStatus())) {
            item.setStatus("上架");
        }
        if (!StringUtils.hasText(item.getCategory())) {
            item.setCategory("其他");
        }
        foodItemService.save(item);
        return Result.success();
    }

    /** 修改商品；分类为空时统一归到“其他”。 */
    @PutMapping("/item/update")
    public Result<Void> updateItem(@RequestBody FoodItem item) {
        if (item.getId() == null || foodItemService.getById(item.getId()) == null) {
            return Result.error("商品不存在");
        }
        if (!StringUtils.hasText(item.getCategory())) {
            item.setCategory("其他");
        }
        foodItemService.updateById(item);
        return Result.success();
    }

    /** 删除商品基础资料。历史订单已保存商品名称和价格，不依赖当前商品对象。 */
    @DeleteMapping("/item/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        if (foodItemService.getById(id) == null) {
            return Result.error("商品不存在");
        }
        foodItemService.removeById(id);
        return Result.success();
    }

    /**
     * 创建点餐订单。Service 会检查商品状态、计算总价，并在会员点餐时扣减余额。
     */
    @PostMapping("/order/add")
    public Result<Void> addOrder(@RequestBody FoodOrder order) {
        try {
            foodOrderService.createOrder(order);
            return Result.success();
        // Service 主动抛出的 RuntimeException 中保存了适合直接提示用户的业务信息。
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 取消订单。会员订单取消后，Service 会把已扣金额退回。 */
    @PostMapping("/order/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        try {
            foodOrderService.cancelOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 标记订单已完成，表示商品已经制作或交付。 */
    @PostMapping("/order/complete/{id}")
    public Result<Void> completeOrder(@PathVariable Long id) {
        try {
            foodOrderService.completeOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 后台查询订单，可按顾客姓名和订单状态筛选。 */
    @GetMapping("/order/list")
    public Result<List<FoodOrder>> orderList(@RequestParam(required = false) String customerName,
                                             @RequestParam(required = false) String status) {
        LambdaQueryWrapper<FoodOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(customerName), FoodOrder::getCustomerName, customerName)
                .eq(StringUtils.hasText(status), FoodOrder::getStatus, status)
                .orderByDesc(FoodOrder::getId);
        return Result.success(foodOrderService.list(wrapper));
    }
}
