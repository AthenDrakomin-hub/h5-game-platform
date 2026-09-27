package com.h5.controller.lottery;

import com.h5.common.Result;
import com.h5.entity.DrawResult;
import com.h5.entity.Lottery;
import com.h5.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 开奖信息 Controller（委托 LotteryService）
 */
@RestController
@RequestMapping("/wap/draw")
public class DrawController {

    @Autowired private LotteryService lotteryService;

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
            Lottery lottery = lotteryService.getLotteryByCode(code);
            if (lottery == null) continue;

            DrawResult lastDraw = lotteryService.getLatestDraw(code);

            Map<String, Object> info = new HashMap<>();
            info.put("lotteryCode", code);
            info.put("lotteryName", lottery.getName());
            info.put("drawInterval", lottery.getDrawInterval());
            info.put("closeTime", lottery.getCloseTime());
            info.put("openTime", lottery.getDrawInterval() - lottery.getCloseTime());

            if (lastDraw != null) {
                info.put("lastPeriod", lastDraw.getPeriod());
                info.put("lastNumbers", lastDraw.getNumbers());
                info.put("lastDrawTime", lastDraw.getDrawTime() != null ? lastDraw.getDrawTime().toString() : null);
                try {
                    long nextPeriod = Long.parseLong(lastDraw.getPeriod()) + 1;
                    info.put("currentPeriod", String.valueOf(nextPeriod));
                } catch (NumberFormatException e) {
                    info.put("currentPeriod", lastDraw.getPeriod());
                }
                info.put("closeCountdown", lottery.getCloseTime());
                info.put("drawCountdown", lottery.getDrawInterval());
            } else {
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

    /** 生成模拟开奖号码（无开奖记录时兜底） */
    private String generateMockNumbers(String code) {
        Random random = new Random();
        switch (code) {
            case "jsdd":
                return random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10);
            case "jspk10":
                List<Integer> nums = new ArrayList<>();
                for (int i = 1; i <= 10; i++) nums.add(i);
                Collections.shuffle(nums);
                return nums.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
            case "jsssc":
                return random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10);
            default:
                return random.nextInt(10) + "," + random.nextInt(10) + "," + random.nextInt(10);
        }
    }
}
