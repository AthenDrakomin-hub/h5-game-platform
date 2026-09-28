package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 盈亏报表 Controller（已分层，业务逻辑在 ReportService）
 */
@RestController
@RequestMapping("/wap/report")
public class ProfitReportController {

    @Autowired private ReportService reportService;

    /** 盈亏统计 */
    @GetMapping("/profit")
    public Result<Map<String, Object>> profit(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getProfitReport(UserContext.getUserId(), startDate, endDate));
    }
}
