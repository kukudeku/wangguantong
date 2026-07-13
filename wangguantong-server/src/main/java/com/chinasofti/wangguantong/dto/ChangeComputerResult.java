package com.chinasofti.wangguantong.dto;

import java.math.BigDecimal;

public class ChangeComputerResult {

    private boolean changed;
    private BigDecimal extraAmount;
    private String sourceComputerNo;
    private String targetComputerNo;
    private String message;

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public BigDecimal getExtraAmount() {
        return extraAmount;
    }

    public void setExtraAmount(BigDecimal extraAmount) {
        this.extraAmount = extraAmount;
    }

    public String getSourceComputerNo() {
        return sourceComputerNo;
    }

    public void setSourceComputerNo(String sourceComputerNo) {
        this.sourceComputerNo = sourceComputerNo;
    }

    public String getTargetComputerNo() {
        return targetComputerNo;
    }

    public void setTargetComputerNo(String targetComputerNo) {
        this.targetComputerNo = targetComputerNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
