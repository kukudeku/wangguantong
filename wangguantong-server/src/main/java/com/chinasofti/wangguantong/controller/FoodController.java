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

@RestController
@RequestMapping("/food")
public class FoodController {

    private final FoodItemService foodItemService;
    private final FoodOrderService foodOrderService;

    public FoodController(FoodItemService foodItemService, FoodOrderService foodOrderService) {
        this.foodItemService = foodItemService;
        this.foodOrderService = foodOrderService;
    }

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

    @GetMapping("/item/available")
    public Result<List<FoodItem>> availableItems() {
        return Result.success(foodItemService.list(new LambdaQueryWrapper<FoodItem>()
                .eq(FoodItem::getStatus, "上架")
                .orderByAsc(FoodItem::getId)));
    }

    @PostMapping("/item/add")
    public Result<Void> addItem(@RequestBody FoodItem item) {
        if (!StringUtils.hasText(item.getName())) {
            return Result.error("商品名称不能为空");
        }
        if (item.getPrice() == null) {
            return Result.error("商品价格不能为空");
        }
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

    @DeleteMapping("/item/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        if (foodItemService.getById(id) == null) {
            return Result.error("商品不存在");
        }
        foodItemService.removeById(id);
        return Result.success();
    }

    @PostMapping("/order/add")
    public Result<Void> addOrder(@RequestBody FoodOrder order) {
        try {
            foodOrderService.createOrder(order);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/order/cancel/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        try {
            foodOrderService.cancelOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/order/complete/{id}")
    public Result<Void> completeOrder(@PathVariable Long id) {
        try {
            foodOrderService.completeOrder(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

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
