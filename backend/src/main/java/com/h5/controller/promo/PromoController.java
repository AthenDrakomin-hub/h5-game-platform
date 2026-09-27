package com.h5.controller.promo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.entity.Promotion;
import com.h5.mapper.PromotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wap/promotion")
public class PromoController {

    @Autowired
    private PromotionMapper promotionMapper;

    /**
     * 活动分类
     * GET /api/wap/promotion/categories
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        List<Promotion> all = promotionMapper.selectList(
                new LambdaQueryWrapper<Promotion>().eq(Promotion::getStatus, 1)
        );
        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (Promotion p : all) {
            if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                categoryMap.putIfAbsent(p.getCategory(), p.getCategoryName());
            }
        }
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("code", entry.getKey());
            cat.put("name", entry.getValue());
            categories.add(cat);
        }
        return Result.success(categories);
    }

    /**
     * 活动列表（分页）
     * GET /api/wap/promotion/list?page=1&pageSize=10&category=newbie
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {

        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getStatus, 1)
                .orderByAsc(Promotion::getSort);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Promotion::getCategory, category);
        }

        Page<Promotion> pageResult = promotionMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 活动详情
     * GET /api/wap/promotion/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<Promotion> detail(@PathVariable Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null || promotion.getStatus() == 0) {
            throw new BusinessException("活动不存在或已下架");
        }
        return Result.success(promotion);
    }
}
