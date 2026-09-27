package com.h5.controller.lottery;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.entity.DrawResult;
import com.h5.entity.Lottery;
import com.h5.mapper.DrawResultMapper;
import com.h5.mapper.LotteryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/wap/draw")
public class DrawController {

    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private DrawResultMapper drawResultMapper;

    /**
     * 批量获取开奖信息
     * GET /api/wap/draw/info/batch?codes=jsdd,jspk10
     */
    @GetMapping("/info/batch")
    public Result<Map<String, Object>> batch(@RequestParam String codes) {
        String[] codeArray = codes.split(",");
        Map<String, Object> result = new HashMap<>();

        for (String code : codeArray) {
            code = code.trim();
            Lottery lottery = lotteryMapper.selectOne(
                    new LambdaQueryWrapper<Lottery>().eq(Lottery::getCode, code)
            );
            if (lottery == null) continue;

            // 获取最近一期开奖
            DrawResult lastDraw = drawResultMapper.selectOne(
                    new LambdaQueryWrapper<DrawResult>()
                            .eq(DrawResult::getLotteryCode, code)
                            .eq(DrawResult::getStatus, 1)
                            .orderByDesc(DrawResult::getDrawTime)
                            .last("LIMIT 1")
            );

            Map<String, Object> info = new HashMap<>();
            info.put("lotteryCode", code);
            info.put("lotteryName", lottery.getName());
            info.put("drawInterval", lottery.getDrawInterval());
            info.put("closeTime", lottery.getCloseTime());
            info.put("openTime", lottery.getDrawInterval() - lottery.getCloseTime());

            if (lastDraw != null) {
                info.put("lastPeriod", lastDraw.getPeriod());
                info.put("lastNumbers", lastDraw.getNumbers());
                info.put("lastDrawTime", lastDraw.getDrawTime().toString());
                // 计算当前期号（上期+1）
                long nextPeriod = Long.parseLong(lastDraw.getPeriod()) + 1;
                info.put("currentPeriod", String.valueOf(nextPeriod));
                // 倒计时（模拟：封盘倒计时 = closeTime）
                info.put("closeCountdown", lottery.getCloseTime());
                info.put("drawCountdown", lottery.getDrawInterval());
            } else {
                // 无开奖记录时生成模拟数据
                String period = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                info.put("lastPeriod", period);
                info.put("currentPeriod", String.valueOf(Long.parseLong(period) + 1));
                info.put("lastNumbers", generateMockNumbers(code));
                info.put("closeCountdown", lottery.getCloseTime());
                info.put("drawCountdown", lottery.getDrawInterval());
            }

            result.put(code, info);
        }
        return Result.success(result);
    }

    /**
     * 生成模拟开奖号码
     */
    private String generateMockNumbers(String code) {
        Random random = new Random();
        switch (code) {
            case "jsdd": // PC28: 3个数字 0-9
                return (random.nextInt(10)) + "," + (random.nextInt(10)) + "," + (random.nextInt(10));
            case "jspk10": // 赛车: 10个数字 1-10
                List<Integer> nums = new ArrayList<>();
                for (int i = 1; i <= 10; i++) nums.add(i);
                Collections.shuffle(nums);
                return nums.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
            case "jsssc": // 时时彩: 5个数字 0-9
                return random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10);
            default:
                return random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10);
        }
    }
}
