package com.chinasofti.wangguantong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/** 电脑机位实体，对应 computer 表。 */
@TableName("computer")
public class Computer {

    /** 数据库自增主键，接口内部关联电脑时优先使用 id。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 给用户看的电脑编号，例如 A001、B002。 */
    private String computerNo;

    /** 电脑所在区域，例如“电竞一区”。 */
    private String area;

    /** 每小时基础单价。金额使用 BigDecimal，避免 double 的小数精度误差。 */
    private BigDecimal pricePerHour;

    /** 机位状态：空闲、使用中、预约锁定、维修中。 */
    private String status;

    /** 管理员填写的补充说明。 */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComputerNo() {
        return computerNo;
    }

    public void setComputerNo(String computerNo) {
        this.computerNo = computerNo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
