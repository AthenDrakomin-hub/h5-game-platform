package com.h5.controller.promo;

import com.h5.common.Result;
import com.h5.entity.Promotion;
import com.h5.service.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动 Controller（委托 PromoService）
 */
@RestController
@RequestMapping("/wap/promotion")
public class PromoController {

    @Autowired private PromoService promoService;

    /** 活动分类 */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.success(promoService.getCategories());
    }

    /** 活动列表（分页） */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return Result.success(promoService.getPromotionList(category, page, pageSize));
    }

    /** 活动详情 */
    @GetMapping("/detail/{id}")
    public Result<Promotion> detail(@PathVariable Long id) {
        return Result.success(promoService.getPromotionDetail(id));
    }
}
