package com.h5.controller.user;

import com.h5.common.Result;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/rebate")
public class RebateController {

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRebate", new BigDecimal("0.00"));
        data.put("availableRebate", new BigDecimal("0.00"));
        data.put("claimedRebate", new BigDecimal("0.00"));
        data.put("todayRebate", new BigDecimal("0.00"));
        data.put("weekRebate", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/ladders")
    public Result<List<Map<String, Object>>> ladders() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.005, "minAmount", 0, "maxAmount", 10000));
        list.add(Map.of("level", 2, "ratio", 0.008, "minAmount", 10000, "maxAmount", 50000));
        list.add(Map.of("level", 3, "ratio", 0.01, "minAmount", 50000, "maxAmount", 200000));
        return Result.success(list);
    }

    @GetMapping("/vendor-records")
    public Result<Map<String, Object>> vendorRecords(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/user-list")
    public Result<Map<String, Object>> userList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @PostMapping("/claim-all")
    public Result<Void> claimAll() {
        return Result.success();
    }
}
