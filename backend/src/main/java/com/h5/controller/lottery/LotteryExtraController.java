package com.h5.controller.lottery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Bet;
import com.h5.entity.DrawResult;
import com.h5.entity.Lottery;
import com.h5.mapper.BetMapper;
import com.h5.mapper.DrawResultMapper;
import com.h5.mapper.LotteryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 彩票额外接口 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/lottery")
public class LotteryExtraController {

    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private DrawResultMapper drawResultMapper;
    @Autowired private BetMapper betMapper;

    /** 彩票数据（分类+彩种） */
    @GetMapping("/data")
    public Result<Map<String, Object>> lotteryData() {
        List<Lottery> lotteries = lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>()
                        .eq(Lottery::getStatus, 1)
                        .orderByAsc(Lottery::getSort)
        );

        Map<String, List<Lottery>> grouped = lotteries.stream()
                .collect(Collectors.groupingBy(l -> l.getCategoryCode() != null ? l.getCategoryCode() : "other"));

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, List<Lottery>> entry : grouped.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", entry.getKey());
            cat.put("name", entry.getValue().get(0).getCategoryName());
            cat.put("games", entry.getValue());
            categories.add(cat);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("games", lotteries);
        return Result.success(data);
    }

    /** 龙虎走势 */
    @GetMapping("/trend/dragon/{code}")
    public Result<Map<String, Object>> trendDragon(@PathVariable String code,
                                                     @RequestParam(defaultValue = "30") int limit) {
        List<DrawResult> draws = drawResultMapper.selectList(
                new LambdaQueryWrapper<DrawResult>()
                        .eq(DrawResult::getLotteryCode, code)
                        .eq(DrawResult::getStatus, 1)
                        .orderByDesc(DrawResult::getDrawTime)
                        .last("LIMIT " + limit)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (DrawResult d : draws) {
            String nums = d.getNumbers() != null ? d.getNumbers() : "";
            String[] parts = nums.split(",");
            String result = "draw";
            if (parts.length >= 2) {
                try {
                    int first = Integer.parseInt(parts[0].trim());
                    int last = Integer.parseInt(parts[parts.length - 1].trim());
                    if (first > last) result = "dragon";
                    else if (first < last) result = "tiger";
                } catch (NumberFormatException ignored) {}
            }
            Map<String, Object> item = new HashMap<>();
            item.put("period", d.getPeriod());
            item.put("numbers", nums);
            item.put("result", result);
            item.put("drawTime", d.getDrawTime());
            list.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("list", list);
        data.put("total", list.size());
        return Result.success(data);
    }

    /** 遗漏走势 */
    @GetMapping("/trend/miss/{code}")
    public Result<Map<String, Object>> trendMiss(@PathVariable String code,
                                                    @RequestParam(defaultValue = "30") int limit) {
        List<DrawResult> draws = drawResultMapper.selectList(
                new LambdaQueryWrapper<DrawResult>()
                        .eq(DrawResult::getLotteryCode, code)
                        .eq(DrawResult::getStatus, 1)
                        .orderByDesc(DrawResult::getDrawTime)
                        .last("LIMIT " + limit)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        for (DrawResult d : draws) {
            Map<String, Object> item = new HashMap<>();
            item.put("period", d.getPeriod());
            item.put("numbers", d.getNumbers());
            item.put("drawTime", d.getDrawTime());
            list.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("list", list);
        data.put("total", list.size());
        return Result.success(data);
    }

    /** 待结算投注 */
    @GetMapping("/pending-bets")
    public Result<List<Bet>> pendingBets() {
        Long userId = UserContext.getUserId();
        List<Bet> list = betMapper.selectList(
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getUserId, userId)
                        .eq(Bet::getStatus, "pending")
                        .orderByDesc(Bet::getCreateTime)
                        .last("LIMIT 50")
        );
        return Result.success(list);
    }

    /** 投注记录 */
    @GetMapping("/bet-list")
    public Result<Map<String, Object>> betList(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        Page<Bet> pageResult = betMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Bet>()
                        .eq(Bet::getUserId, userId)
                        .orderByDesc(Bet::getCreateTime)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 第三方投注记录（娱乐城） */
    @GetMapping("/third-bet-list")
    public Result<Map<String, Object>> thirdBetList(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }
}
