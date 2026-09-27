package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.CasinoGame;
import com.h5.entity.CasinoProvider;
import com.h5.mapper.CasinoGameMapper;
import com.h5.mapper.CasinoProviderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 娱乐城服务
 */
@Service
public class CasinoService {

    @Autowired private CasinoProviderMapper providerMapper;
    @Autowired private CasinoGameMapper gameMapper;

    /**
     * 获取所有启用平台
     */
    public List<CasinoProvider> getProviders() {
        return providerMapper.selectList(
                new LambdaQueryWrapper<CasinoProvider>()
                        .eq(CasinoProvider::getStatus, 1)
                        .orderByAsc(CasinoProvider::getSort)
        );
    }

    /**
     * 获取游戏列表（支持平台/分类/热门/最新筛选）
     */
    public Map<String, Object> getGames(String providerCode, String category,
                                          String filter, int page, int pageSize) {
        LambdaQueryWrapper<CasinoGame> wrapper = new LambdaQueryWrapper<CasinoGame>()
                .eq(CasinoGame::getStatus, 1)
                .orderByAsc(CasinoGame::getSort);

        if (providerCode != null && !providerCode.isEmpty()) {
            wrapper.eq(CasinoGame::getProviderCode, providerCode);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(CasinoGame::getCategory, category);
        }
        if ("hot".equals(filter)) {
            wrapper.eq(CasinoGame::getIsHot, 1);
        } else if ("new".equals(filter)) {
            wrapper.eq(CasinoGame::getIsNew, 1);
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CasinoGame> pageResult =
                gameMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    /**
     * 获取游戏详情
     */
    public CasinoGame getGameDetail(Long gameId) {
        return gameMapper.selectById(gameId);
    }

    /**
     * 获取所有分类
     */
    public List<String> getCategories() {
        List<CasinoGame> all = gameMapper.selectList(
                new LambdaQueryWrapper<CasinoGame>().eq(CasinoGame::getStatus, 1)
        );
        return all.stream()
                .map(CasinoGame::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
