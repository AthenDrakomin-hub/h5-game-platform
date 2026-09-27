package com.h5.controller.casino;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.CasinoGame;
import com.h5.entity.User;
import com.h5.mapper.CasinoGameMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wap")
public class CasinoEnterController {

    @Autowired private CasinoGameMapper gameMapper;
    @Autowired private UserMapper userMapper;

    /**
     * 进入娱乐城游戏
     * GET /api/wap/{platform}/enter?gameCode=xxx
     * 例如: /api/wap/pg/enter?gameCode=xxx
     */
    @GetMapping("/{platform}/enter")
    public Result<Map<String, Object>> enter(
            @PathVariable String platform,
            @RequestParam(required = false) String gameCode,
            @RequestParam(required = false) Long gameId) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");

        CasinoGame game = null;
        if (gameId != null) {
            game = gameMapper.selectById(gameId);
        } else if (gameCode != null && !gameCode.isEmpty()) {
            game = gameMapper.selectOne(
                    new LambdaQueryWrapper<CasinoGame>()
                            .eq(CasinoGame::getGameCode, gameCode)
                            .eq(CasinoGame::getProviderCode, platform)
            );
        }

        if (game == null) {
            // 返回模拟游戏 URL
            Map<String, Object> data = new HashMap<>();
            data.put("gameUrl", "https://demo." + platform + ".com/game?token=demo_" + userId);
            data.put("platform", platform);
            data.put("gameCode", gameCode);
            return Result.success(data);
        }

        // 生成游戏进入 URL（模板替换 token）
        String enterUrl = game.getEnterUrl();
        if (enterUrl == null || enterUrl.isEmpty()) {
            enterUrl = "https://demo." + platform + ".com/game/" + game.getGameCode() + "?token=user_" + userId;
        } else {
            enterUrl = enterUrl.replace("{token}", "user_" + userId)
                    .replace("{userId}", String.valueOf(userId))
                    .replace("{gameCode}", game.getGameCode());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("gameUrl", enterUrl);
        data.put("platform", platform);
        data.put("gameCode", game.getGameCode());
        data.put("gameName", game.getName());
        return Result.success(data);
    }

    /**
     * 娱乐城转账（转入/转出）
     * POST /api/wap/casino/transfer
     */
    @PostMapping("/casino/transfer")
    public Result<Map<String, Object>> transfer(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");

        String type = params.get("type") != null ? params.get("type").toString() : "in"; // in转入 out转出
        String platform = params.get("platform") != null ? params.get("platform").toString() : "";
        java.math.BigDecimal amount = new java.math.BigDecimal(params.get("amount") != null ? params.get("amount").toString() : "0");

        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) throw new BusinessException("转账金额必须大于0");

        if ("in".equals(type)) {
            if (amount.compareTo(user.getBalance()) > 0) throw new BusinessException("余额不足");
            user.setBalance(user.getBalance().subtract(amount));
        } else {
            user.setBalance(user.getBalance().add(amount));
        }
        userMapper.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("platform", platform);
        data.put("amount", amount);
        data.put("balance", user.getBalance());
        return Result.success(data);
    }
}
