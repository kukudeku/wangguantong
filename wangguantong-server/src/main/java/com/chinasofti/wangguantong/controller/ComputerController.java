package com.chinasofti.wangguantong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chinasofti.wangguantong.common.Result;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.service.ComputerService;
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
@RequestMapping("/computer")
public class ComputerController {

    private final ComputerService computerService;

    public ComputerController(ComputerService computerService) {
        this.computerService = computerService;
    }

    @GetMapping("/list")
    public Result<List<Computer>> list(@RequestParam(required = false) String area,
                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Computer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(area), Computer::getArea, area)
                .eq(StringUtils.hasText(status), Computer::getStatus, status)
                .orderByDesc(Computer::getId);
        return Result.success(computerService.list(wrapper));
    }

    @GetMapping("/free")
    public Result<List<Computer>> freeList() {
        return Result.success(computerService.list(new LambdaQueryWrapper<Computer>()
                .eq(Computer::getStatus, "空闲")
                .orderByAsc(Computer::getComputerNo)));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Computer computer) {
        if (!StringUtils.hasText(computer.getComputerNo())) {
            return Result.error("电脑编号不能为空");
        }
        if (!StringUtils.hasText(computer.getArea())) {
            return Result.error("区域不能为空");
        }
        if (computer.getPricePerHour() == null) {
            return Result.error("每小时单价不能为空");
        }
        computer.setId(null);
        if (!StringUtils.hasText(computer.getStatus())) {
            computer.setStatus("空闲");
        }
        computerService.save(computer);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Computer computer) {
        if (computer.getId() == null || computerService.getById(computer.getId()) == null) {
            return Result.error("电脑不存在");
        }
        computerService.updateById(computer);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Computer computer = computerService.getById(id);
        if (computer == null) {
            return Result.error("电脑不存在");
        }
        if ("使用中".equals(computer.getStatus())) {
            return Result.error("使用中的电脑不能删除");
        }
        computerService.removeById(id);
        return Result.success();
    }
}
