package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.Lottery;
import com.h5.entity.DrawResult;
import com.h5.mapper.LotteryMapper;
import com.h5.mapper.DrawResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 彩票服务（彩种配置 + 开奖查询）
 */
@Service
public class LotteryService {

    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private DrawResultMapper drawResultMapper;

    /**
     * 获取所有启用彩种
     */
    public List<Lottery> getAllLotteries() {
        return lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>()
                        .eq(Lottery::getStatus, 1)
                        .orderByAsc(Lottery::getSort)
        );
    }

    /**
     * 按分类获取彩种
     */
    public List<Lottery> getLotteriesByCategory(String categoryCode) {
        return lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>()
                        .eq(Lottery::getStatus, 1)
                        .eq(Lottery::getCategoryCode, categoryCode)
                        .orderByAsc(Lottery::getSort)
        );
    }

    /**
     * 获取彩种详情
     */
    public Lottery getLotteryByCode(String code) {
        return lotteryMapper.selectOne(
                new LambdaQueryWrapper<Lottery>().eq(Lottery::getCode, code)
        );
    }

    /**
     * 获取最新开奖结果
     */
    public DrawResult getLatestDraw(String lotteryCode) {
        return drawResultMapper.selectOne(
                new LambdaQueryWrapper<DrawResult>()
                        .eq(DrawResult::getLotteryCode, lotteryCode)
                        .eq(DrawResult::getStatus, 1)
                        .orderByDesc(DrawResult::getDrawTime)
                        .last("LIMIT 1")
        );
    }

    /**
     * 批量获取多个彩种最新开奖
     */
    public Map<String, DrawResult> getLatestDraws(List<String> lotteryCodes) {
        Map<String, DrawResult> result = new HashMap<>();
        for (String code : lotteryCodes) {
            DrawResult draw = getLatestDraw(code);
            if (draw != null) {
                result.put(code, draw);
            }
        }
        return result;
    }

    /**
     * 获取历史开奖记录
     */
    public Map<String, Object> getHistoryDraws(String lotteryCode, int page, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DrawResult> pageResult =
                drawResultMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize),
                        new LambdaQueryWrapper<DrawResult>()
                                .eq(DrawResult::getLotteryCode, lotteryCode)
                                .eq(DrawResult::getStatus, 1)
                                .orderByDesc(DrawResult::getDrawTime)
                );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 获取所有分类
     */
    public List<Map<String, Object>> getCategories() {
        List<Lottery> all = getAllLotteries();
        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (Lottery l : all) {
            if (l.getCategoryCode() != null && !categoryMap.containsKey(l.getCategoryCode())) {
                categoryMap.put(l.getCategoryCode(), l.getCategoryName());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", entry.getKey());
            cat.put("name", entry.getValue());
            result.add(cat);
        }
        return result;
    }
}
