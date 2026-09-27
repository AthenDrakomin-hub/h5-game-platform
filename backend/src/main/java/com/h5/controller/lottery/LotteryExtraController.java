package com.h5.controller.lottery;

import com.h5.common.Result;
import com.h5.common.UserContext;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/wap/lottery")
public class LotteryExtraController {

    @GetMapping("/data")
    public Result<Map<String, Object>> lotteryData() {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", new ArrayList<>());
        data.put("games", new ArrayList<>());
        return Result.success(data);
    }

    @GetMapping("/trend/dragon/{code}")
    public Result<Map<String, Object>> trendDragon(@PathVariable String code, @RequestParam(defaultValue = "30") int limit) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/trend/miss/{code}")
    public Result<Map<String, Object>> trendMiss(@PathVariable String code, @RequestParam(defaultValue = "30") int limit) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/pending-bets")
    public Result<List<Map<String, Object>>> pendingBets() {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/bet-list")
    public Result<Map<String, Object>> betList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/third-bet-list")
    public Result<Map<String, Object>> thirdBetList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }
}
