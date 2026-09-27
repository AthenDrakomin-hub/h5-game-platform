package com.h5.controller.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.entity.*;
import com.h5.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wap/home")
public class HomeController {

    @Autowired private SiteConfigMapper siteConfigMapper;
    @Autowired private BannerMapper bannerMapper;
    @Autowired private NoticeMapper noticeMapper;
    @Autowired private LotteryMapper lotteryMapper;
    @Autowired private CasinoProviderMapper casinoProviderMapper;

    /**
     * 站点配置
     * GET /api/wap/home/config
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        List<SiteConfig> configs = siteConfigMapper.selectList(null);
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(SiteConfig::getConfigKey, SiteConfig::getConfigValue, (a, b) -> a));

        Map<String, Object> data = new HashMap<>();
        data.put("siteName", configMap.getOrDefault("site_name", "H5 Game"));
        data.put("customerServiceUrl", configMap.getOrDefault("customer_service_url", ""));
        data.put("telegramGroupUrl", configMap.getOrDefault("telegram_group_url", ""));
        data.put("rechargeMinAmount", configMap.getOrDefault("recharge_min_amount", "100"));
        data.put("rechargeMaxAmount", configMap.getOrDefault("recharge_max_amount", "50000"));
        data.put("withdrawMinAmount", configMap.getOrDefault("withdraw_min_amount", "100"));
        data.put("withdrawFeeRate", configMap.getOrDefault("withdraw_fee_rate", "0.01"));
        data.put("icp", "");
        data.put("copyright", "© 2024 H5 Game");
        return Result.success(data);
    }

    /**
     * Banner 列表
     * GET /api/wap/home/banners
     */
    @GetMapping("/banners")
    public Result<List<Banner>> banners(@RequestParam(required = false, defaultValue = "home") String position) {
        List<Banner> list = bannerMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .eq(Banner::getPosition, position)
                        .orderByAsc(Banner::getSort)
        );
        return Result.success(list);
    }

    /**
     * 公告列表
     * GET /api/wap/home/announcements
     */
    @GetMapping("/announcements")
    public Result<List<Notice>> announcements() {
        List<Notice> list = noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByAsc(Notice::getSort)
                        .last("LIMIT 10")
        );
        return Result.success(list);
    }

    /**
     * 彩票分类
     * GET /api/wap/home/categories
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        List<Lottery> lotteries = lotteryMapper.selectList(
                new LambdaQueryWrapper<Lottery>()
                        .eq(Lottery::getStatus, 1)
                        .orderByAsc(Lottery::getSort)
        );
        // 按 categoryCode 分组
        Map<String, List<Lottery>> grouped = lotteries.stream()
                .collect(Collectors.groupingBy(Lottery::getCategoryCode));

        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, List<Lottery>> entry : grouped.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", entry.getKey());
            cat.put("name", entry.getValue().get(0).getCategoryName());
            cat.put("games", entry.getValue());
            categories.add(cat);
        }
        return Result.success(categories);
    }

    /**
     * 彩票游戏列表
     * GET /api/wap/home/games
     */
    @GetMapping("/games")
    public Result<List<Lottery>> games(@RequestParam(required = false) String categoryCode) {
        LambdaQueryWrapper<Lottery> wrapper = new LambdaQueryWrapper<Lottery>()
                .eq(Lottery::getStatus, 1)
                .orderByAsc(Lottery::getSort);
        if (categoryCode != null && !categoryCode.isEmpty()) {
            wrapper.eq(Lottery::getCategoryCode, categoryCode);
        }
        List<Lottery> list = lotteryMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 快捷入口
     * GET /api/wap/home/quick-entries
     */
    @GetMapping("/quick-entries")
    public Result<List<Map<String, Object>>> quickEntries() {
        List<Map<String, Object>> entries = new ArrayList<>();
        entries.add(Map.of("id", 1, "name", "充值", "icon", "/uploads/icons/recharge.png", "link", "/user/recharge"));
        entries.add(Map.of("id", 2, "name", "提现", "icon", "/uploads/icons/withdraw.png", "link", "/user/withdraw"));
        entries.add(Map.of("id", 3, "name", "优惠活动", "icon", "/uploads/icons/promo.png", "link", "/promo"));
        entries.add(Map.of("id", 4, "name", "VIP中心", "icon", "/uploads/icons/vip.png", "link", "/user/vip"));
        entries.add(Map.of("id", 5, "name", "在线客服", "icon", "/uploads/icons/service.png", "link", "/chat"));
        entries.add(Map.of("id", 6, "name", "邀请好友", "icon", "/uploads/icons/invite.png", "link", "/user/invite"));
        entries.add(Map.of("id", 7, "name", "游戏记录", "icon", "/uploads/icons/record.png", "link", "/user/bet-record"));
        entries.add(Map.of("id", 8, "name", "更多", "icon", "/uploads/icons/more.png", "link", "/user"));
        return Result.success(entries);
    }
}
