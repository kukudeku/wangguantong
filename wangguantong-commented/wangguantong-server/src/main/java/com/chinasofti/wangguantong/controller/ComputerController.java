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

/**
 * 电脑机位管理接口，负责机位的查询、新增、修改和删除。
 *
 * <p>电脑状态是业务流程的重要依据：只有“空闲”电脑才能上机或预约，
 * “使用中”的电脑不能被删除。</p>
 */
@RestController
@RequestMapping("/computer")
public class ComputerController {

    private final ComputerService computerService;

    public ComputerController(ComputerService computerService) {
        this.computerService = computerService;
    }

    /**
     * 查询电脑列表。area 使用模糊查询，status 使用精确查询；参数为空时不加该条件。
     */
    @GetMapping("/list")
    public Result<List<Computer>> list(@RequestParam(required = false) String area,
                                       @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Computer> wrapper = new LambdaQueryWrapper<>();
        // MyBatis-Plus 的条件参数为 false 时，会自动忽略对应查询条件。
        wrapper.like(StringUtils.hasText(area), Computer::getArea, area)
                .eq(StringUtils.hasText(status), Computer::getStatus, status)
                .orderByDesc(Computer::getId);
        return Result.success(computerService.list(wrapper));
    }

    /** 查询所有空闲电脑，供后台办理上机时选择。 */
    @GetMapping("/free")
    public Result<List<Computer>> freeList() {
        return Result.success(computerService.list(new LambdaQueryWrapper<Computer>()
                .eq(Computer::getStatus, "空闲")
                .orderByAsc(Computer::getComputerNo)));
    }

    /**
     * 新增电脑。Controller 先检查必填字段，再补充默认状态。
     */
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
        // 新增时不接受前端传来的 id，让数据库自增生成主键。
        computer.setId(null);
        if (!StringUtils.hasText(computer.getStatus())) {
            computer.setStatus("空闲");
        }
        computerService.save(computer);
        return Result.success();
    }

    /** 修改已有电脑；先查一次可避免更新不存在的数据。 */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Computer computer) {
        if (computer.getId() == null || computerService.getById(computer.getId()) == null) {
            return Result.error("电脑不存在");
        }
        computerService.updateById(computer);
        return Result.success();
    }

    /**
     * 删除电脑。使用中的电脑与上机记录有关，直接删除会造成数据状态不一致。
     */
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
