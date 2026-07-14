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
    public void report(Long computerId, Long memberId, String serviceType, String serviceLocation,
                       String problemDescription) {
        String normalizedType = StringUtils.hasText(serviceType) ? serviceType.trim() : "故障报修";
        if (!"故障报修".equals(normalizedType) && !"呼叫网管".equals(normalizedType)) {
            throw new RuntimeException("不支持的服务类型");
        }
        if ("故障报修".equals(normalizedType) && computerId == null) {
            throw new RuntimeException("请选择报修电脑");
        }
        if (!StringUtils.hasText(problemDescription)) {
            throw new RuntimeException("请填写故障说明");
        }
        Computer computer = null;
        if (computerId != null) {
            computer = computerService.getById(computerId);
            if (computer == null) {
                throw new RuntimeException("电脑不存在");
            }
        }
        if ("故障报修".equals(normalizedType)) {
            long activeCount = count(new LambdaQueryWrapper<RepairRecord>()
                    .eq(RepairRecord::getComputerId, computerId)
                    .eq(RepairRecord::getServiceType, "故障报修")
                    .in(RepairRecord::getStatus, Arrays.asList("待处理", "处理中")));
            if (activeCount > 0) {
                throw new RuntimeException("该电脑已有未完成的报修记录");
            }
        } else if (!StringUtils.hasText(serviceLocation) && computer == null) {
            throw new RuntimeException("请填写所在位置");
        }

        RepairRecord record = new RepairRecord();
        if (computer != null) {
            record.setComputerId(computer.getId());
            record.setComputerNo(computer.getComputerNo());
        }
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
        record.setServiceType(normalizedType);
        record.setServiceLocation(StringUtils.hasText(serviceLocation)
                ? serviceLocation.trim() : computer == null ? null : computer.getComputerNo());
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
        boolean repairRequest = !"呼叫网管".equals(record.getServiceType());
        Computer computer = record.getComputerId() == null ? null : computerService.getById(record.getComputerId());
        if (repairRequest && computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if ("处理中".equals(status)) {
            if (!"待处理".equals(record.getStatus())) {
                throw new RuntimeException("只有待处理工单可以受理");
            }
            if (repairRequest) {
                if (!"空闲".equals(computer.getStatus()) && !"维修中".equals(computer.getStatus())) {
                    throw new RuntimeException("电脑当前正在使用或已预约，请先释放机位");
                }
                computer.setStatus("维修中");
                computerService.updateById(computer);
            }
            record.setProcessTime(now);
        } else if ("已完成".equals(status)) {
            if (!"处理中".equals(record.getStatus())) {
                throw new RuntimeException("只有处理中工单可以完成");
            }
            if (repairRequest && "维修中".equals(computer.getStatus())) {
                computer.setStatus("空闲");
                computerService.updateById(computer);
            }
            record.setFinishTime(now);
        } else if ("已取消".equals(status)) {
            if ("已完成".equals(record.getStatus()) || "已取消".equals(record.getStatus())) {
                throw new RuntimeException("该服务工单已经结束");
            }
            if (repairRequest && "处理中".equals(record.getStatus()) && "维修中".equals(computer.getStatus())) {
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
