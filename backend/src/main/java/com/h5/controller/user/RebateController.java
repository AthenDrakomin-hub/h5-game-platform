package com.h5.controller.user;

import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 返水 Controller（已分层，业务逻辑在 AgentService）
 */
@RestController
@RequestMapping("/wap/rebate")
public class RebateController {

    @Autowired private AgentService agentService;

    /** 返水总览 */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(agentService.getRebateOverview(UserContext.getUserId()));
    }

    /** 返水阶梯 */
    @GetMapping("/ladders")
    public Result<List<Map<String, Object>>> ladders() {
        return Result.success(agentService.getRebateLadders());
    }

    /** 厂商返水记录 */
    @GetMapping("/vendor-records")
    public Result<Map<String, Object>> vendorRecords(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(agentService.getRebateRecords(UserContext.getUserId(), page, pageSize));
    }

    /** 返水用户列表 */
    @GetMapping("/user-list")
    public Result<Map<String, Object>> userList(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(agentService.getRebateRecords(UserContext.getUserId(), page, pageSize));
    }

    /** 领取全部返水 */
    @PostMapping("/claim-all")
    public Result<Void> claimAll() {
        agentService.claimAllRebate(UserContext.getUserId());
        return Result.success();
    }
}
