package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/** 电脑预约记录实体，对应 reservation_record 表。 */
@TableName("reservation_record")
public class ReservationRecord {

    /** 预约记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约会员 id。 */
    private Long memberId;

    /** 预约时保存的会员姓名。 */
    private String memberName;

    /** 被预约的电脑 id。 */
    private Long computerId;

    /** 预约时保存的电脑编号。 */
    private String computerNo;

    /** 用户选择的预约时间。当前规则只能预约未来 1 小时内。 */
    private LocalDateTime reserveTime;

    /** 预约状态：已预约、已取消、已上机。 */
    private String status;

    /** 创建预约的时间。 */
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

    public Long getComputerId() {
        return computerId;
    }

    public void setComputerId(Long computerId) {
        this.computerId = computerId;
    }

    public String getComputerNo() {
        return computerNo;
    }

    public void setComputerNo(String computerNo) {
        this.computerNo = computerNo;
    }

    public LocalDateTime getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(LocalDateTime reserveTime) {
        this.reserveTime = reserveTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
