package com.h5.controller.casino;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.entity.CasinoGame;
import com.h5.entity.CasinoProvider;
import com.h5.mapper.CasinoGameMapper;
import com.h5.mapper.CasinoProviderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wap/game")
public class CasinoController {

    @Autowired private CasinoProviderMapper providerMapper;
    @Autowired private CasinoGameMapper gameMapper;

    /**
     * 娱乐城平台列表
     * GET /api/wap/game/casino-providers
     */
    @GetMapping("/casino-providers")
    public Result<List<CasinoProvider>> providers() {
        List<CasinoProvider> list = providerMapper.selectList(
                new LambdaQueryWrapper<CasinoProvider>()
                        .eq(CasinoProvider::getStatus, 1)
                        .orderByAsc(CasinoProvider::getSort)
        );
        return Result.success(list);
    }

    /**
     * 娱乐城游戏列表（分页）
     * GET /api/wap/game/casino-games?page=1&pageSize=20&providerCode=pg
     */
    @GetMapping("/casino-games")
    public Result<Map<String, Object>> games(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<CasinoGame> wrapper = new LambdaQueryWrapper<CasinoGame>()
                .eq(CasinoGame::getStatus, 1)
                .orderByAsc(CasinoGame::getSort);
        if (providerCode != null && !providerCode.isEmpty()) {
            wrapper.eq(CasinoGame::getProviderCode, providerCode);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(CasinoGame::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(CasinoGame::getName, keyword);
        }

        Page<CasinoGame> pageResult = gameMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("totalPages", pageResult.getPages());
        return Result.success(data);
    }

    /**
     * 热门游戏
     * GET /api/wap/game/hot-games
     */
    @GetMapping("/hot-games")
    public Result<List<CasinoGame>> hotGames() {
        List<CasinoGame> list = gameMapper.selectList(
                new LambdaQueryWrapper<CasinoGame>()
                        .eq(CasinoGame::getStatus, 1)
                        .eq(CasinoGame::getIsHot, 1)
                        .orderByAsc(CasinoGame::getSort)
                        .last("LIMIT 10")
        );
        return Result.success(list);
    }

    /**
     * 最新游戏
     * GET /api/wap/game/new-games
     */
    @GetMapping("/new-games")
    public Result<List<CasinoGame>> newGames() {
        List<CasinoGame> list = gameMapper.selectList(
                new LambdaQueryWrapper<CasinoGame>()
                        .eq(CasinoGame::getStatus, 1)
                        .eq(CasinoGame::getIsNew, 1)
                        .orderByDesc(CasinoGame::getId)
                        .last("LIMIT 10")
        );
        return Result.success(list);
    }
}
