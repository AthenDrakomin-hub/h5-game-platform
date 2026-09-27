package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.entity.Promotion;
import com.h5.mapper.PromotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin/promo")
public class AdminPromoController {

    @Autowired private PromotionMapper promotionMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<Promotion>().orderByDesc(Promotion::getSort);
        if (category != null && !category.isEmpty()) wrapper.eq(Promotion::getCategory, category);
        Page<Promotion> pageResult = promotionMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @GetMapping("/detail/{id}")
    public Result<Promotion> detail(@PathVariable Long id) {
        return Result.success(promotionMapper.selectById(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody Promotion promo) {
        if (promo.getId() != null) promotionMapper.updateById(promo);
        else promotionMapper.insert(promo);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        promotionMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/toggle-status")
    public Result<Void> toggleStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        Promotion promo = promotionMapper.selectById(id);
        if (promo != null) {
            promo.setStatus(status);
            promotionMapper.updateById(promo);
        }
        return Result.success();
    }
}
