package com.h5.controller.home;

import com.h5.common.Result;
import com.h5.entity.Banner;
import com.h5.entity.Lottery;
import com.h5.entity.Notice;
import com.h5.service.HomeService;
import com.h5.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页 Controller（委托 HomeService / LotteryService）
 */
@RestController
@RequestMapping("/wap/home")
public class HomeController {

    @Autowired private HomeService homeService;
    @Autowired private LotteryService lotteryService;

    /** 站点配置 */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success(homeService.getSiteConfig());
    }

    /** Banner 列表 */
    @GetMapping("/banners")
    public Result<List<Banner>> banners(@RequestParam(required = false, defaultValue = "home") String position) {
        return Result.success(homeService.getBanners(position));
    }

    /** 公告列表 */
    @GetMapping("/announcements")
    public Result<List<Notice>> announcements() {
        return Result.success(homeService.getAnnouncements());
    }

    /** 彩票分类（含彩种） */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(homeService.getCategories());
    }

    /** 彩票游戏列表 */
    @GetMapping("/games")
    public Result<List<Lottery>> games(@RequestParam(required = false) String categoryCode) {
        if (categoryCode != null && !categoryCode.isEmpty()) {
            return Result.success(lotteryService.getLotteriesByCategory(categoryCode));
        }
        return Result.success(lotteryService.getAllLotteries());
    }

    /** 快捷入口 */
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
