package com.chinasofti.wangguantong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chinasofti.wangguantong.entity.OnlineRecord;

import java.util.List;

/**
 * 上机记录业务接口。
 *
 * <p>这里封装上机、下机、实时计费和查询展示数据等核心业务。</p>
 */
public interface OnlineRecordService extends IService<OnlineRecord> {

    /**
     * 为指定会员办理上机。
     * 首个计费小时会立即扣款，成功后电脑状态变为“使用中”。
     */
    void startOnline(Long memberId, Long computerId);

    /** 结束指定的进行中记录，并释放对应电脑。 */
    void endOnline(Long recordId);

    /**
     * 按条件查询记录，同时为进行中的记录补充实时分钟数、实时消费和余额提醒。
     */
    List<OnlineRecord> listWithRunningInfo(String memberName, String computerNo, String status);
}
