package com.h5.controller.casino;

import com.h5.common.Result;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/transfer")
public class TransferController {

    @GetMapping("/balances")
    public Result<List<Map<String, Object>>> balances() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("platform", "main", "name", "主钱包", "balance", new BigDecimal("0.00")));
        return Result.success(list);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }
}
