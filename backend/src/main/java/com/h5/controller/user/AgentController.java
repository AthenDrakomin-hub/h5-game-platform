package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 代理中心 Controller（已分层，业务逻辑在 AgentService）
 */
@RestController
@RequestMapping("/wap/agent")
public class AgentController {

    @Autowired private AgentService agentService;

    /** 代理总览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(agentService.getOverview(UserContext.getUserId()));
    }

    /** 代理统计（今日/本周/本月） */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(agentService.getStats(UserContext.getUserId()));
    }

    /** 团队成员列表 */
    @GetMapping("/members")
    public Result<Map<String, Object>> members(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(agentService.getTeamMembers(UserContext.getUserId(), page, pageSize));
    }

    /** 佣金记录 */
    @GetMapping("/records")
    public Result<Map<String, Object>> records(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(agentService.getCommissionRecords(UserContext.getUserId(), page, pageSize));
    }

    /** 成员统计 */
    @GetMapping("/member-stats")
    public Result<Map<String, Object>> memberStats() {
        return Result.success(agentService.getMemberStats(UserContext.getUserId()));
    }

    /** 推广看板 */
    @GetMapping("/promote-dashboard")
    public Result<Map<String, Object>> promoteDashboard() {
        return Result.success(agentService.getPromoteDashboard(UserContext.getUserId()));
    }

    /** 工作台佣金统计 */
    @GetMapping("/workbench-stats")
    public Result<Map<String, Object>> workbenchStats() {
        return Result.success(agentService.getWorkbenchStats(UserContext.getUserId()));
    }

    /** 返佣比例 */
    @GetMapping("/rebate-ratio")
    public Result<Map<String, Object>> rebateRatio() {
        return Result.success(agentService.getRebateRatio());
    }

    /** 代理等级列表 */
    @GetMapping("/rate-scope")
    public Result<List<Map<String, Object>>> rateScope() {
        return Result.success(agentService.getRateScope());
    }

    /** 亏损救助汇总 */
    @GetMapping("/loss-summary")
    public Result<Map<String, Object>> lossSummary() {
        return Result.success(agentService.getLossSummary(UserContext.getUserId()));
    }

    /** 亏损救助记录 */
    @GetMapping("/loss-records")
    public Result<Map<String, Object>> lossRecords(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(agentService.getLossRecords(UserContext.getUserId(), page, pageSize));
    }

    /** 佣金提现 */
    @PostMapping("/withdraw")
    public Result<Void> withdraw(@RequestBody Map<String, Object> params) {
        BigDecimal amount = params.get("amount") != null
                ? new BigDecimal(params.get("amount").toString()) : BigDecimal.ZERO;
        agentService.withdrawCommission(UserContext.getUserId(), amount);
        return Result.success();
    }

    /** 创建成员（代理代注册） */
    @PostMapping("/create-member")
    public Result<Map<String, Object>> createMember(@RequestBody Map<String, String> params) {
        return Result.success(agentService.createMember(
                UserContext.getUserId(), params.get("username"), params.get("password")));
    }
}
