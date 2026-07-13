package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.ReservationRecord;
import com.chinasofti.wangguantong.mapper.ReservationRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import com.chinasofti.wangguantong.service.ReservationRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 电脑预约业务实现。
 *
 * <p>预约成功后，预约记录状态为“已预约”，电脑状态为“预约锁定”。
 * 用户上机后预约记录改成“已上机”；取消或超时后电脑重新变成“空闲”。</p>
 */
@Service
public class ReservationRecordServiceImpl extends ServiceImpl<ReservationRecordMapper, ReservationRecord> implements ReservationRecordService {

    /** 查询预约人资料。 */
    private final MemberService memberService;
    /** 查询和修改电脑占用状态。 */
    private final ComputerService computerService;
    /** 预约到店上机时复用普通上机流程，避免出现两套计费规则。 */
    private final OnlineRecordService onlineRecordService;

    /** Spring 自动注入本类需要的三个业务对象。 */
    public ReservationRecordServiceImpl(MemberService memberService,
                                        ComputerService computerService,
                                        OnlineRecordService onlineRecordService) {
        this.memberService = memberService;
        this.computerService = computerService;
        this.onlineRecordService = onlineRecordService;
    }

    @Override
    // 预约记录和电脑锁定必须同步成功，所以放在同一个事务中。
    @Transactional(rollbackFor = Exception.class)
    public void addReservation(Long memberId, Long computerId, String reserveTimeText) {
        // 第一步：验证预约人存在，并且账号可正常使用。
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (!"正常".equals(member.getStatus())) {
            throw new RuntimeException("会员状态不是正常，不能预约");
        }

        // 第二步：只有空闲电脑可以预约，防止一个机位被多人占用。
        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"空闲".equals(computer.getStatus())) {
            throw new RuntimeException("只有空闲电脑可以预约锁定");
        }

        // 第三步：把前端字符串转换为 Java 时间，并限制在当前时间到未来一小时之间。
        LocalDateTime reserveTime = parseReserveTime(reserveTimeText);
        LocalDateTime now = LocalDateTime.now();
        if (reserveTime.isBefore(now)) {
            throw new RuntimeException("预约时间不能早于当前时间");
        }
        if (reserveTime.isAfter(now.plusHours(1))) {
            throw new RuntimeException("用户预约只能预约最近 1 小时内的时间");
        }

        // 第四步：保存一条预约快照，姓名和电脑编号便于历史列表直接展示。
        ReservationRecord record = new ReservationRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        record.setReserveTime(reserveTime);
        record.setStatus("已预约");
        record.setCreateTime(now);
        save(record);

        // 第五步：锁定电脑，座位图会根据这个状态禁止其他用户操作。
        computer.setStatus("预约锁定");
        computerService.updateById(computer);
    }

    @Override
    // 取消预约和释放电脑是一个整体操作。
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long id) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"已预约".equals(record.getStatus())) {
            throw new RuntimeException("只有已预约记录可以取消");
        }

        // 保留预约记录，仅修改状态，便于以后查询历史。
        record.setStatus("已取消");
        updateById(record);

        // 只释放仍处于“预约锁定”的电脑，避免误改已经维修或使用中的电脑。
        Computer computer = computerService.getById(record.getComputerId());
        if (computer != null && "预约锁定".equals(computer.getStatus())) {
            computer.setStatus("空闲");
            computerService.updateById(computer);
        }
    }

    @Override
    // 预约记录、电脑状态和新上机记录需要保持一致，因此开启事务。
    @Transactional(rollbackFor = Exception.class)
    public void startFromReservation(Long id) {
        ReservationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!"已预约".equals(record.getStatus())) {
            throw new RuntimeException("只有已预约记录可以上机");
        }
        // 用户点击上机时再次判断是否超时，避免恰好还没等到定时任务执行的时间窗口。
        if (record.getReserveTime() != null && record.getReserveTime().isBefore(LocalDateTime.now())) {
            record.setStatus("已取消");
            updateById(record);
            Computer reservedComputer = computerService.getById(record.getComputerId());
            if (reservedComputer != null && "预约锁定".equals(reservedComputer.getStatus())) {
                reservedComputer.setStatus("空闲");
                computerService.updateById(reservedComputer);
            }
            throw new RuntimeException("预约已超时，系统已自动取消");
        }

        Computer computer = computerService.getById(record.getComputerId());
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"预约锁定".equals(computer.getStatus())) {
            throw new RuntimeException("电脑不是预约锁定状态");
        }

        // 普通上机逻辑只接收“空闲”电脑，因此先临时释放，再复用统一的校验和扣费流程。
        // 如果后续上机失败，事务会回滚，电脑仍保持预约锁定，不会丢失预约状态。
        computer.setStatus("空闲");
        computerService.updateById(computer);
        onlineRecordService.startOnline(record.getMemberId(), record.getComputerId());

        record.setStatus("已上机");
        updateById(record);
    }

    @Override
    // fixedDelay=60000 表示上一次执行结束后等待 60 秒，再执行下一次。
    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void expireReservations() {
        // 一次性查询所有“时间已到但状态仍是已预约”的记录。
        LocalDateTime now = LocalDateTime.now();
        List<ReservationRecord> expiredRecords = list(new LambdaQueryWrapper<ReservationRecord>()
                .eq(ReservationRecord::getStatus, "已预约")
                .lt(ReservationRecord::getReserveTime, now));
        // 逐条取消预约，并把仍被该预约锁定的电脑释放。
        for (ReservationRecord record : expiredRecords) {
            record.setStatus("已取消");
            updateById(record);

            Computer computer = computerService.getById(record.getComputerId());
            if (computer != null && "预约锁定".equals(computer.getStatus())) {
                computer.setStatus("空闲");
                computerService.updateById(computer);
            }
        }
    }

    private LocalDateTime parseReserveTime(String reserveTimeText) {
        // 后端统一要求 yyyy-MM-dd HH:mm:ss，例如 2026-07-10 15:30:00。
        if (reserveTimeText == null || reserveTimeText.trim().isEmpty()) {
            throw new RuntimeException("请选择预约时间");
        }
        return LocalDateTime.parse(reserveTimeText, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
