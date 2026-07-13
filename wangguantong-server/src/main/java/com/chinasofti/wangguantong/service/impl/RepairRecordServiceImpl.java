package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.RepairRecord;
import com.chinasofti.wangguantong.mapper.RepairRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.RepairRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class RepairRecordServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord>
        implements RepairRecordService {

    private final ComputerService computerService;
    private final MemberService memberService;

    public RepairRecordServiceImpl(ComputerService computerService, MemberService memberService) {
        this.computerService = computerService;
        this.memberService = memberService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void report(Long computerId, Long memberId, String problemDescription) {
        if (computerId == null) {
            throw new RuntimeException("请选择报修电脑");
        }
        if (!StringUtils.hasText(problemDescription)) {
            throw new RuntimeException("请填写故障说明");
        }
        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        long activeCount = count(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getComputerId, computerId)
                .in(RepairRecord::getStatus, Arrays.asList("待处理", "处理中")));
        if (activeCount > 0) {
            throw new RuntimeException("该电脑已有未完成的报修记录");
        }

        RepairRecord record = new RepairRecord();
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        if (memberId != null) {
            Member member = memberService.getById(memberId);
            if (member == null) {
                throw new RuntimeException("用户不存在");
            }
            record.setMemberId(member.getId());
            record.setMemberName(member.getName());
        } else {
            record.setMemberName("管理员");
        }
        record.setProblemDescription(problemDescription.trim());
        record.setStatus("待处理");
        record.setCreateTime(LocalDateTime.now());
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long recordId, String status, String processRemark) {
        RepairRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("报修记录不存在");
        }
        Computer computer = computerService.getById(record.getComputerId());
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if ("处理中".equals(status)) {
            if (!"待处理".equals(record.getStatus())) {
                throw new RuntimeException("只有待处理记录可以开始维修");
            }
            if (!"空闲".equals(computer.getStatus()) && !"维修中".equals(computer.getStatus())) {
                throw new RuntimeException("电脑当前正在使用或已预约，请先释放机位");
            }
            computer.setStatus("维修中");
            computerService.updateById(computer);
            record.setProcessTime(now);
        } else if ("已完成".equals(status)) {
            if (!"处理中".equals(record.getStatus())) {
                throw new RuntimeException("只有处理中的记录可以完成维修");
            }
            if ("维修中".equals(computer.getStatus())) {
                computer.setStatus("空闲");
                computerService.updateById(computer);
            }
            record.setFinishTime(now);
        } else if ("已取消".equals(status)) {
            if ("已完成".equals(record.getStatus()) || "已取消".equals(record.getStatus())) {
                throw new RuntimeException("该报修记录已经结束");
            }
            if ("处理中".equals(record.getStatus()) && "维修中".equals(computer.getStatus())) {
                computer.setStatus("空闲");
                computerService.updateById(computer);
            }
            record.setFinishTime(now);
        } else {
            throw new RuntimeException("不支持的处理状态");
        }
        record.setStatus(status);
        record.setProcessRemark(StringUtils.hasText(processRemark) ? processRemark.trim() : null);
        updateById(record);
    }
}
