package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin/game")
public class AdminGameController {

    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private DrawResultMapper drawResultMapper;
    @Autowired private CasinoProviderMapper providerMapper;
    @Autowired private CasinoGameMapper casinoGameMapper;

    // ===== 彩票游戏 =====
    @GetMapping("/lottery/list")
    public Result<Map<String, Object>> lotteryList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        Page<Lottery> pageResult = lotteryMapper.selectPage(new Page<>(page, pageSize), new LambdaQueryWrapper<Lottery>().orderByAsc(Lottery::getSort));
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @PostMapping("/lottery/save")
    public Result<Void> saveLottery(@RequestBody Lottery lottery) {
        if (lottery.getId() != null) {
            lotteryMapper.updateById(lottery);
        } else {
            lotteryMapper.insert(lottery);
        }
        return Result.success();
    }

    @PostMapping("/lottery/delete/{id}")
    public Result<Void> deleteLottery(@PathVariable Long id) {
        lotteryMapper.deleteById(id);
        return Result.success();
    }

    // ===== 开奖记录 =====
    @GetMapping("/draw/list")
    public Result<Map<String, Object>> drawList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String lotteryCode) {
        LambdaQueryWrapper<DrawResult> wrapper = new LambdaQueryWrapper<DrawResult>().orderByDesc(DrawResult::getPeriod);
        if (lotteryCode != null && !lotteryCode.isEmpty()) wrapper.eq(DrawResult::getLotteryCode, lotteryCode);
        Page<DrawResult> pageResult = drawResultMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @PostMapping("/draw/save")
    public Result<Void> saveDraw(@RequestBody DrawResult draw) {
        if (draw.getId() != null) {
            drawResultMapper.updateById(draw);
        } else {
            drawResultMapper.insert(draw);
        }
        return Result.success();
    }

    // ===== 娱乐城平台 =====
    @GetMapping("/casino/provider/list")
    public Result<List<CasinoProvider>> providerList() {
        return Result.success(providerMapper.selectList(new LambdaQueryWrapper<CasinoProvider>().orderByAsc(CasinoProvider::getSort)));
    }

    @PostMapping("/casino/provider/save")
    public Result<Void> saveProvider(@RequestBody CasinoProvider provider) {
        if (provider.getId() != null) providerMapper.updateById(provider);
        else providerMapper.insert(provider);
        return Result.success();
    }

    // ===== 娱乐城游戏 =====
    @GetMapping("/casino/game/list")
    public Result<Map<String, Object>> casinoGameList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String providerCode) {
        LambdaQueryWrapper<CasinoGame> wrapper = new LambdaQueryWrapper<CasinoGame>().orderByDesc(CasinoGame::getSort);
        if (providerCode != null && !providerCode.isEmpty()) wrapper.eq(CasinoGame::getProviderCode, providerCode);
        Page<CasinoGame> pageResult = casinoGameMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @PostMapping("/casino/game/save")
    public Result<Void> saveCasinoGame(@RequestBody CasinoGame game) {
        if (game.getId() != null) casinoGameMapper.updateById(game);
        else casinoGameMapper.insert(game);
        return Result.success();
    }

    @PostMapping("/casino/game/delete/{id}")
    public Result<Void> deleteCasinoGame(@PathVariable Long id) {
        casinoGameMapper.deleteById(id);
        return Result.success();
    }
}
