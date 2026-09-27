package com.h5.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 首页服务（站点配置/Banner/公告/彩种分类）
 */
@Service
public class HomeService {

    @Autowired private SiteConfigMapper siteConfigMapper;
    @Autowired private BannerMapper bannerMapper;
    @Autowired private NoticeMapper noticeMapper;
    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private CasinoProviderMapper casinoProviderMapper;

    @Value("${site.name:H5 Game}")
    private String siteName;

    @Value("${site.domain:}")
    private String siteDomain;

    /**
     * 站点配置（KV + 基础信息）
     */
    public Map<String, Object> getSiteConfig() {
        Map<String, Object> data = new HashMap<>();
        data.put("siteName", siteName);
        data.put("domain", siteDomain);

        // 从 site_config 表读取所有配置
        List<SiteConfig> configs = siteConfigMapper.selectList(null);
        Map<String, String> configMap = new HashMap<>();
        for (SiteConfig c : configs) {
            configMap.put(c.getConfigKey(), c.getConfigValue());
        }
        data.put("configs", configMap);

        // 常用配置提取
        data.put("customerServiceUrl", configMap.getOrDefault("customer_service_url", ""));
        data.put("telegramGroupUrl", configMap.getOrDefault("telegram_group_url", ""));
        data.put("rechargeMinAmount", configMap.getOrDefault("recharge_min_amount", "100"));
        data.put("rechargeMaxAmount", configMap.getOrDefault("recharge_max_amount", "50000"));
        data.put("withdrawMinAmount", configMap.getOrDefault("withdraw_min_amount", "100"));
        data.put("withdrawFeeRate", configMap.getOrDefault("withdraw_fee_rate", "0.01"));
        data.put("trialBalance", configMap.getOrDefault("trial_balance", "2000"));

        return data;
    }

    /**
     * Banner 列表
     */
    public List<Banner> getBanners(String position) {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort);
        if (position != null && !position.isEmpty()) {
            wrapper.eq(Banner::getPosition, position);
        }
        return bannerMapper.selectList(wrapper);
    }

    /**
     * 公告列表
     */
    public List<Notice> getAnnouncements() {
        return noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByAsc(Notice::getSort)
        );
    }

    /**
     * 彩票分类（含彩种）
     */
    public List<Map<String, Object>> getCategories() {
        List<Lottery> all = lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>().eq(Lottery::getStatus, 1).orderByAsc(Lottery::getSort)
        );
        Map<String, List<Lottery>> grouped = new LinkedHashMap<>();
        Map<String, String> catNames = new HashMap<>();
        for (Lottery l : all) {
            String cat = l.getCategoryCode() != null ? l.getCategoryCode() : "other";
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(l);
            catNames.put(cat, l.getCategoryName());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Lottery>> entry : grouped.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", entry.getKey());
            cat.put("name", catNames.get(entry.getKey()));
            cat.put("lotteries", entry.getValue());
            result.add(cat);
        }
        return result;
    }

    /**
     * 娱乐城平台
     */
    public List<CasinoProvider> getCasinoProviders() {
        return casinoProviderMapper.selectList(
                new LambdaQueryWrapper<CasinoProvider>()
                        .eq(CasinoProvider::getStatus, 1)
                        .orderByAsc(CasinoProvider::getSort)
        );
    }
}
