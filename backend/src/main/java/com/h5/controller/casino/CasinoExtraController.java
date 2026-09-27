package com.h5.controller.casino;

import com.h5.common.Result;
import com.h5.common.UserContext;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/game")
public class CasinoExtraController {

    @GetMapping("/all-games")
    public Result<Map<String, Object>> allGames(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/big-prize-games")
    public Result<List<Map<String, Object>>> bigPrizeGames() {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/platform-balance/{platform}")
    public Result<Map<String, Object>> platformBalance(@PathVariable String platform) {
        Map<String, Object> data = new HashMap<>();
        data.put("platform", platform);
        data.put("balance", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/km-platforms")
    public Result<List<Map<String, Object>>> kmPlatforms() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "name", "KM电子", "code", "km", "status", 1));
        return Result.success(list);
    }
}
