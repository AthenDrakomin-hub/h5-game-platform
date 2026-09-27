package com.h5.controller.user;

import com.h5.common.Result;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/report")
public class ProfitReportController {

    @GetMapping("/profit")
    public Result<Map<String, Object>> profit(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalRecharge", new BigDecimal("0.00"));
        data.put("totalWithdraw", new BigDecimal("0.00"));
        data.put("totalBet", new BigDecimal("0.00"));
        data.put("totalWin", new BigDecimal("0.00"));
        data.put("netProfit", new BigDecimal("0.00"));
        data.put("rechargeCount", 0);
        data.put("withdrawCount", 0);
        data.put("betCount", 0);
        List<Map<String, Object>> daily = new ArrayList<>();
        data.put("daily", daily);
        return Result.success(data);
    }
}
