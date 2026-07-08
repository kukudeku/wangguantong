package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.FoodItem;
import com.chinasofti.wangguantong.mapper.FoodItemMapper;
import com.chinasofti.wangguantong.service.FoodItemService;
import org.springframework.stereotype.Service;

@Service
public class FoodItemServiceImpl extends ServiceImpl<FoodItemMapper, FoodItem> implements FoodItemService {
}
