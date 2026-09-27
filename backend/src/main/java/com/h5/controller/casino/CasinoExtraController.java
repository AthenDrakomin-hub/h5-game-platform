package com.h5.controller.casino;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.CasinoGame;
import com.h5.entity.CasinoProvider;
import com.h5.entity.User;
import com.h5.mapper.CasinoGameMapper;
import com.h5.mapper.CasinoProviderMapper;
import com.h5.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 娱乐城额外接口 Controller（真实数据实现）
 */
@RestController
@RequestMapping("/wap/game")
public class CasinoExtraController {

    @Autowired private CasinoGameMapper gameMapper;
    @Autowired private CasinoProviderMapper providerMapper;
    @Autowired private UserMapper userMapper;

    /** 全部游戏（分页） */
    @GetMapping("/all-games")
    public Result<Map<String, Object>> allGames(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        Page<CasinoGame> pageResult = gameMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<CasinoGame>()
                        .eq(CasinoGame::getStatus, 1)
                        .orderByAsc(CasinoGame::getSort)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 大奖游戏 */
    @GetMapping("/big-prize-games")
    public Result<List<CasinoGame>> bigPrizeGames() {
        List<CasinoGame> list = gameMapper.selectList(
                new LambdaQueryWrapper<CasinoGame>()
                        .eq(CasinoGame::getStatus, 1)
                        .eq(CasinoGame::getIsHot, 1)
                        .orderByAsc(CasinoGame::getSort)
                        .last("LIMIT 20")
        );
        return Result.success(list);
    }

    /** 平台余额 */
    @GetMapping("/platform-balance/{platform}")
    public Result<Map<String, Object>> platformBalance(@PathVariable String platform) {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("platform", platform);
        data.put("balance", user != null && user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
        return Result.success(data);
    }

    /** KM平台列表 */
    @GetMapping("/km-platforms")
    public Result<List<CasinoProvider>> kmPlatforms() {
        List<CasinoProvider> list = providerMapper.selectList(
                new LambdaQueryWrapper<CasinoProvider>()
                        .eq(CasinoProvider::getStatus, 1)
                        .orderByAsc(CasinoProvider::getSort)
        );
        return Result.success(list);
    }
}
