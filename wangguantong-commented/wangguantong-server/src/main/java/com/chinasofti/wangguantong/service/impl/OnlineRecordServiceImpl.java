package com.chinasofti.wangguantong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chinasofti.wangguantong.entity.Computer;
import com.chinasofti.wangguantong.entity.Member;
import com.chinasofti.wangguantong.entity.OnlineRecord;
import com.chinasofti.wangguantong.mapper.OnlineRecordMapper;
import com.chinasofti.wangguantong.service.ComputerService;
import com.chinasofti.wangguantong.service.MemberService;
import com.chinasofti.wangguantong.service.OnlineRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 上机与实时计费的核心业务实现。
 *
 * <p>本项目按“计费小时”扣费：开始上机立即扣第 1 小时；进入第 2、3……小时后，
 * 定时任务继续补扣新增小时的费用。若余额无法支付下一小时，系统自动结束上机并释放电脑。</p>
 *
 * <p>上机记录和会员余额、电脑状态彼此关联，因此修改这些数据的方法使用事务，
 * 防止只扣了钱却没有生成记录，或记录结束后电脑仍显示使用中。</p>
 */
@Service
public class OnlineRecordServiceImpl extends ServiceImpl<OnlineRecordMapper, OnlineRecord> implements OnlineRecordService {

    /** 定时计费遇到单条异常时记录日志，便于后台排查，同时不影响其他上机记录。 */
    private static final Logger log = LoggerFactory.getLogger(OnlineRecordServiceImpl.class);

    /** 读取会员状态、等级和余额，并在计费时更新余额。 */
    private final MemberService memberService;
    /** 读取电脑价格和状态，并在上下机时改变占用状态。 */
    private final ComputerService computerService;

    /** Spring 通过构造器注入两个依赖对象。 */
    public OnlineRecordServiceImpl(MemberService memberService, ComputerService computerService) {
        this.memberService = memberService;
        this.computerService = computerService;
    }

    @Override
    // rollbackFor=Exception 表示发生任何异常都回滚本方法内的数据库修改。
    @Transactional(rollbackFor = Exception.class)
    public void startOnline(Long memberId, Long computerId) {
        // 参数校验放在最前面，让前端能收到明确提示，而不是数据库异常。
        if (memberId == null) {
            throw new RuntimeException("请选择会员");
        }
        if (computerId == null) {
            throw new RuntimeException("请选择电脑");
        }

        // 验证会员存在、账号正常，并且同一会员没有另一条进行中的上机记录。
        Member member = memberService.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        if (!"正常".equals(member.getStatus())) {
            throw new RuntimeException("会员状态不是正常，不能上机");
        }
        long runningCount = count(new LambdaQueryWrapper<OnlineRecord>()
                .eq(OnlineRecord::getMemberId, memberId)
                .eq(OnlineRecord::getStatus, "进行中"));
        if (runningCount > 0) {
            throw new RuntimeException("该会员已有正在进行的上机记录");
        }

        // 只有空闲机位可以开始上机，使用中、预约锁定和维修中都不能操作。
        Computer computer = computerService.getById(computerId);
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }
        if (!"空闲".equals(computer.getStatus())) {
            throw new RuntimeException("电脑不是空闲状态，不能上机");
        }

        // 上机前先计算折扣后的首小时费用。余额不足时不创建任何上机数据。
        BigDecimal firstAmount = calculateAmount(computer.getPricePerHour(), 1, member);
        if (member.getBalance().compareTo(firstAmount) < 0) {
            throw new RuntimeException("会员余额不足，请先充值");
        }

        // 立即扣除首小时费用，实现“实时扣费”，而不是下机时一次性结算。
        member.setBalance(member.getBalance().subtract(firstAmount));
        memberService.updateById(member);

        // 保存上机记录。totalAmount 表示截至目前已经实际扣除的累计金额。
        OnlineRecord record = new OnlineRecord();
        record.setMemberId(member.getId());
        record.setMemberName(member.getName());
        record.setComputerId(computer.getId());
        record.setComputerNo(computer.getComputerNo());
        record.setStartTime(LocalDateTime.now());
        record.setTotalAmount(firstAmount);
        record.setStatus("进行中");
        save(record);

        // 最后占用电脑，用户端座位图会立即把它显示为“使用中”。
        computer.setStatus("使用中");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endOnline(Long recordId) {
        // 下机只能操作“进行中”的记录，历史记录不能重复结算。
        OnlineRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("上机记录不存在");
        }
        if (!"进行中".equals(record.getStatus())) {
            throw new RuntimeException("该记录不是进行中状态");
        }

        Member member = memberService.getById(record.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        Computer computer = computerService.getById(record.getComputerId());
        if (computer == null) {
            throw new RuntimeException("电脑不存在");
        }

        // 下机前再执行一次实时结算，补扣定时任务尚未处理的费用。
        LocalDateTime endTime = LocalDateTime.now();
        settleRunningCharge(record, member, computer, endTime);
        // settleRunningCharge 可能因余额不足已经自动下机，此时不应再次修改状态。
        if (!"进行中".equals(record.getStatus())) {
            return;
        }

        // 正常下机：记录结束时间并释放电脑。
        record.setEndTime(endTime);
        record.setStatus("已完成");
        updateById(record);

        computer.setStatus("空闲");
        computerService.updateById(computer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OnlineRecord> listWithRunningInfo(String memberName, String computerNo, String status) {
        // 条件为空时不拼接对应 SQL；有值时使用模糊查询或精确状态查询。
        LambdaQueryWrapper<OnlineRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(memberName != null && !memberName.trim().isEmpty(), OnlineRecord::getMemberName, memberName)
                .like(computerNo != null && !computerNo.trim().isEmpty(), OnlineRecord::getComputerNo, computerNo)
                .eq(status != null && !status.trim().isEmpty(), OnlineRecord::getStatus, status)
                .orderByDesc(OnlineRecord::getId);

        List<OnlineRecord> list = list(wrapper);
        // 数据库只保存固定字段；实时分钟、当前消费等展示字段在返回前动态计算。
        for (OnlineRecord record : list) {
            fillRunningInfo(record);
        }
        return list;
    }

    /**
     * 每分钟检查全部进行中的上机记录并补扣费用。
     * fixedDelay=60000 表示一次执行完成后间隔 60 秒再执行下一轮。
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void settleRunningRecords() {
        // 只扫描当前上机记录，历史记录不再参与计费。
        List<OnlineRecord> runningRecords = list(new LambdaQueryWrapper<OnlineRecord>()
                .eq(OnlineRecord::getStatus, "进行中"));
        LocalDateTime now = LocalDateTime.now();
        for (OnlineRecord record : runningRecords) {
            try {
                Member member = memberService.getById(record.getMemberId());
                Computer computer = computerService.getById(record.getComputerId());
                // 数据不完整时跳过这一条，避免空指针中断整轮定时任务。
                if (member == null || computer == null) {
                    continue;
                }
                settleRunningCharge(record, member, computer, now);
            } catch (RuntimeException e) {
                // 单个会员结算失败只写日志，其他会员仍继续结算。
                log.warn("自动结算上机记录失败，recordId={}", record.getId(), e);
            }
        }
    }

    /** 为一条数据库记录补充用户端和后台表格需要的实时字段。 */
    private void fillRunningInfo(OnlineRecord record) {
        Member member = memberService.getById(record.getMemberId());
        Computer computer = computerService.getById(record.getComputerId());
        if (member == null || computer == null) {
            return;
        }

        // 以下字段使用 @TableField(exist=false)，只返回给前端，不写入 online_record 表。
        record.setMemberBalance(member.getBalance());
        record.setMemberLevel(member.getMemberLevel());
        record.setDiscountRate(getDiscountRate(member.getMemberLevel()));

        if ("进行中".equals(record.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            // Duration 计算从开始时间到现在经过的完整分钟数。
            long minutes = Duration.between(record.getStartTime(), now).toMinutes();
            // Math.ceil 向上取整：1~60 分钟按 1 小时，61~120 分钟按 2 小时。
            long hours = (long) Math.ceil(minutes / 60.0);
            if (hours < 1) {
                hours = 1;
            }
            // 查询列表时也顺便结算，保证页面刚打开时显示的是最新余额和消费。
            BigDecimal currentAmount = settleRunningCharge(record, member, computer, now);
            record.setRunningMinutes(minutes < 0 ? 0 : minutes);
            record.setChargeHours(hours);
            record.setCurrentAmount(currentAmount);
            record.setMemberBalance(member.getBalance());
            // 结算过程中若余额不足，记录已经自动完成，此处返回明确的页面提示。
            if (!"进行中".equals(record.getStatus())) {
                record.setBalanceEnough(false);
                record.setWarningMessage("余额不足，系统已自动下机");
                return;
            }
            // 预先计算进入下一小时还需扣多少钱，用于提前显示余额不足提醒。
            BigDecimal paidAmount = record.getTotalAmount() == null ? BigDecimal.ZERO : record.getTotalAmount();
            BigDecimal nextAmount = calculateAmount(computer.getPricePerHour(), hours + 1, member);
            BigDecimal pendingAmount = nextAmount.subtract(paidAmount);
            record.setBalanceEnough(pendingAmount.compareTo(BigDecimal.ZERO) <= 0 || member.getBalance().compareTo(pendingAmount) >= 0);
            if (!record.getBalanceEnough()) {
                record.setWarningMessage("当前余额不足以支付下一计费小时，请及时充值");
            }
        } else {
            // 已完成记录不再动态计费，当前消费就是最终累计金额。
            record.setCurrentAmount(record.getTotalAmount());
            record.setBalanceEnough(true);
        }
    }

    /**
     * 计算指定小时数的累计费用：小时单价 × 计费小时 × 会员折扣。
     * setScale(2) 把金额统一保留两位小数，HALF_UP 是常见的四舍五入规则。
     */
    private BigDecimal calculateAmount(BigDecimal pricePerHour, long hours, Member member) {
        return pricePerHour
                .multiply(BigDecimal.valueOf(hours))
                .multiply(getDiscountRate(member.getMemberLevel()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 把一条进行中的记录结算到指定时间。
     *
     * @return 按当前时长计算出的累计消费；若余额不足，则返回已经成功支付的金额
     */
    private BigDecimal settleRunningCharge(OnlineRecord record, Member member, Computer computer, LocalDateTime now) {
        // 先根据时长确定当前应该处于第几个计费小时。
        long minutes = Duration.between(record.getStartTime(), now).toMinutes();
        long hours = (long) Math.ceil(minutes / 60.0);
        if (hours < 1) {
            hours = 1;
        }

        // currentAmount 是“理论累计费用”，paidAmount 是“数据库中已经扣过的累计费用”。
        BigDecimal currentAmount = calculateAmount(computer.getPricePerHour(), hours, member);
        BigDecimal paidAmount = record.getTotalAmount() == null ? BigDecimal.ZERO : record.getTotalAmount();
        // 两者相减只扣新增部分，避免定时任务每分钟重复扣首小时费用。
        BigDecimal pendingAmount = currentAmount.subtract(paidAmount);

        if (pendingAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 余额不够支付新增计费小时：不允许产生欠费，直接自动下机。
            if (member.getBalance().compareTo(pendingAmount) < 0) {
                autoEndForInsufficientBalance(record, computer, now);
                record.setBalanceEnough(false);
                record.setWarningMessage("余额不足，系统已自动下机");
                return paidAmount;
            }
            // 余额充足时只扣差额，并同步更新记录中的累计已付金额。
            member.setBalance(member.getBalance().subtract(pendingAmount));
            memberService.updateById(member);

            record.setTotalAmount(currentAmount);
            updateById(record);
        }

        return currentAmount;
    }

    /** 余额不足时结束记录并释放机位，防止用户继续产生无法支付的费用。 */
    private void autoEndForInsufficientBalance(OnlineRecord record, Computer computer, LocalDateTime endTime) {
        // 幂等保护：若记录已结束，再次调用不会重复修改。
        if (!"进行中".equals(record.getStatus())) {
            return;
        }
        record.setEndTime(endTime);
        record.setStatus("已完成");
        updateById(record);

        computer.setStatus("空闲");
        computerService.updateById(computer);
    }

    /**
     * 把会员等级转换成计费乘数。
     * 例如黄金会员返回 0.90，表示原价乘以 90%，即九折。
     */
    private BigDecimal getDiscountRate(String memberLevel) {
        if ("钻石会员".equals(memberLevel)) {
            return new BigDecimal("0.80");
        }
        if ("黄金会员".equals(memberLevel)) {
            return new BigDecimal("0.90");
        }
        if ("普通会员".equals(memberLevel)) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ONE;
    }
}
