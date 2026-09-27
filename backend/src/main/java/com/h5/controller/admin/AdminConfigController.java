package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.common.Result;
import com.h5.entity.Banner;
import com.h5.entity.Notice;
import com.h5.entity.SiteConfig;
import com.h5.mapper.BannerMapper;
import com.h5.mapper.NoticeMapper;
import com.h5.mapper.SiteConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin/config")
public class AdminConfigController {

    @Autowired private SiteConfigMapper siteConfigMapper;
    @Autowired private BannerMapper bannerMapper;
    @Autowired private NoticeMapper noticeMapper;

    @GetMapping("/site")
    public Result<List<SiteConfig>> siteConfig() {
        return Result.success(siteConfigMapper.selectList(new LambdaQueryWrapper<SiteConfig>()));
    }

    @PostMapping("/site/save")
    public Result<Void> saveSiteConfig(@RequestBody SiteConfig config) {
        if (config.getId() != null) siteConfigMapper.updateById(config);
        else siteConfigMapper.insert(config);
        return Result.success();
    }

    @GetMapping("/banner/list")
    public Result<List<Banner>> bannerList() {
        return Result.success(bannerMapper.selectList(new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort)));
    }

    @PostMapping("/banner/save")
    public Result<Void> saveBanner(@RequestBody Banner banner) {
        if (banner.getId() != null) bannerMapper.updateById(banner);
        else bannerMapper.insert(banner);
        return Result.success();
    }

    @PostMapping("/banner/delete/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/notice/list")
    public Result<List<Notice>> noticeList() {
        return Result.success(noticeMapper.selectList(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getSort)));
    }

    @PostMapping("/notice/save")
    public Result<Void> saveNotice(@RequestBody Notice notice) {
        if (notice.getId() != null) noticeMapper.updateById(notice);
        else noticeMapper.insert(notice);
        return Result.success();
    }

    @PostMapping("/notice/delete/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }
}
