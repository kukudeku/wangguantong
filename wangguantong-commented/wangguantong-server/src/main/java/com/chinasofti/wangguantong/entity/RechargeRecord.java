package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录实体，对应 recharge_record 表。
 * 每次余额增加都应写入一条记录，方便后台统计和用户查看余额明细。
 */
@TableName("recharge_record")
public class RechargeRecord {

    /** 充值记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被充值的会员 id。 */
    private Long memberId;

    /** 充值时保存的会员姓名。 */
    private String memberName;

    /** 本次充值金额，必须大于 0。 */
    private BigDecimal amount;

    /** 充值发生时间。 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
