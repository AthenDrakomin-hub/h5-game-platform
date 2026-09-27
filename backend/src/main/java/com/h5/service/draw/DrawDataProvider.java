package com.h5.service.draw;

import com.h5.entity.DrawResult;
import java.util.List;

/**
 * 开奖数据提供者接口（可插拔）
 * 实现类：MarkSix6Provider（第三方API）、RandomDrawProvider（内置随机）
 */
public interface DrawDataProvider {

    /**
     * 提供者名称
     */
    String getName();

    /**
     * 是否启用
     */
    boolean isEnabled();

    /**
     * 获取指定彩种最新开奖结果
     *
     * @param lotteryCode 彩种代码 (jspk10/jsssc/jsdd/happy8lhc/...)
     * @return 最新开奖结果，获取失败返回 null
     */
    DrawResult getLatestDraw(String lotteryCode);

    /**
     * 批量获取多个彩种最新开奖
     *
     * @param lotteryCodes 彩种代码列表
     * @return 成功获取到的开奖结果列表
     */
    List<DrawResult> getLatestDraws(List<String> lotteryCodes);

    /**
     * 获取指定彩种的历史开奖记录
     *
     * @param lotteryCode 彩种代码
     * @param limit       条数
     * @return 历史开奖列表（按时间倒序）
     */
    List<DrawResult> getHistoryDraws(String lotteryCode, int limit);
}
