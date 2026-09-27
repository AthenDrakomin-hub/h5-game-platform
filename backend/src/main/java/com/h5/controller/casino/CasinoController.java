package com.h5.controller.casino;

import com.h5.common.Result;
import com.h5.entity.CasinoGame;
import com.h5.entity.CasinoProvider;
import com.h5.service.CasinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 娱乐城 Controller（委托 CasinoService）
 */
@RestController
@RequestMapping("/wap/game")
public class CasinoController {

    @Autowired private CasinoService casinoService;

    /** 娱乐城平台列表 */
    @GetMapping("/casino-providers")
    public Result<List<CasinoProvider>> providers() {
        return Result.success(casinoService.getProviders());
    }

    /** 娱乐城游戏列表（分页） */
    @GetMapping("/casino-games")
    public Result<Map<String, Object>> games(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(casinoService.getGames(providerCode, category, null, page, pageSize));
    }

    /** 热门游戏 */
    @GetMapping("/hot-games")
    public Result<List<CasinoGame>> hotGames() {
        return Result.success(casinoService.getGames(null, null, "hot", 1, 10).get("list") instanceof List
                ? (List<CasinoGame>) casinoService.getGames(null, null, "hot", 1, 10).get("list")
                : List.of());
    }

    /** 最新游戏 */
    @GetMapping("/new-games")
    public Result<List<CasinoGame>> newGames() {
        Map<String, Object> result = casinoService.getGames(null, null, "new", 1, 10);
        return Result.success((List<CasinoGame>) result.get("list"));
    }
}
