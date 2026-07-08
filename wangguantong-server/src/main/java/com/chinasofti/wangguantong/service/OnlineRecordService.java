package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.OnlineRecord;

import java.util.List;

public interface OnlineRecordService extends IService<OnlineRecord> {

    void startOnline(Long memberId, Long computerId);

    void endOnline(Long recordId);

    List<OnlineRecord> listWithRunningInfo(String memberName, String computerNo, String status);
}
