package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/wap/agent")
public class AgentController {

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", "INV" + UserContext.getUserId());
        data.put("inviteLink", "https://h5.lexiangim.com/register?invite=INV" + UserContext.getUserId());
        data.put("totalMembers", 0);
        data.put("activeMembers", 0);
        data.put("totalRecharge", new BigDecimal("0.00"));
        data.put("totalCommission", new BigDecimal("0.00"));
        data.put("availableCommission", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayMembers", 0);
        data.put("todayRecharge", new BigDecimal("0.00"));
        data.put("todayCommission", new BigDecimal("0.00"));
        data.put("weekMembers", 0);
        data.put("weekRecharge", new BigDecimal("0.00"));
        data.put("monthMembers", 0);
        data.put("monthRecharge", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/members")
    public Result<Map<String, Object>> members(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @GetMapping("/member-stats")
    public Result<Map<String, Object>> memberStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", 0);
        data.put("active", 0);
        data.put("inactive", 0);
        data.put("todayNew", 0);
        return Result.success(data);
    }

    @GetMapping("/promote-dashboard")
    public Result<Map<String, Object>> promoteDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("inviteCode", "INV" + UserContext.getUserId());
        data.put("inviteLink", "https://h5.lexiangim.com/register?invite=INV" + UserContext.getUserId());
        data.put("qrCode", "/uploads/qrcode/invite_" + UserContext.getUserId() + ".png");
        data.put("totalClicks", 0);
        data.put("totalRegisters", 0);
        data.put("conversionRate", "0%");
        return Result.success(data);
    }

    @GetMapping("/workbench-stats")
    public Result<Map<String, Object>> workbenchStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayCommission", new BigDecimal("0.00"));
        data.put("weekCommission", new BigDecimal("0.00"));
        data.put("monthCommission", new BigDecimal("0.00"));
        data.put("totalCommission", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/rebate-ratio")
    public Result<Map<String, Object>> rebateRatio() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentRatio", 0.01);
        data.put("level", 1);
        data.put("nextLevelRatio", 0.02);
        data.put("nextLevelRecharge", 10000);
        return Result.success(data);
    }

    @GetMapping("/rate-scope")
    public Result<List<Map<String, Object>>> rateScope() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("level", 1, "ratio", 0.01, "minRecharge", 0));
        list.add(Map.of("level", 2, "ratio", 0.02, "minRecharge", 10000));
        list.add(Map.of("level", 3, "ratio", 0.03, "minRecharge", 50000));
        return Result.success(list);
    }

    @GetMapping("/loss-summary")
    public Result<Map<String, Object>> lossSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalLoss", new BigDecimal("0.00"));
        data.put("availableRescue", new BigDecimal("0.00"));
        data.put("claimedRescue", new BigDecimal("0.00"));
        return Result.success(data);
    }

    @GetMapping("/loss-records")
    public Result<Map<String, Object>> lossRecords(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        return Result.success(data);
    }

    @PostMapping("/withdraw")
    public Result<Void> withdraw(@RequestBody Map<String, Object> params) {
        return Result.success();
    }

    @PostMapping("/create-member")
    public Result<Map<String, Object>> createMember(@RequestBody Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", params.get("username"));
        data.put("password", params.get("password"));
        return Result.success(data);
    }
}
