package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.BusinessException;
import com.h5.common.Result;
import com.h5.entity.PaymentMethod;
import com.h5.mapper.PaymentMethodMapper;
import com.h5.service.AdminLogService;
import com.h5.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理端 - 支付方式管理
 */
@RestController
@RequestMapping("/admin/payment")
public class AdminPaymentController {

    @Autowired private PaymentMethodMapper paymentMethodMapper;
    @Autowired private AdminLogService adminLogService;

    /**
     * 支付方式列表
     * GET /api/admin/payment/list
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<PaymentMethod>()
                .orderByAsc(PaymentMethod::getSort);
        if (type != null && !type.isEmpty()) wrapper.eq(PaymentMethod::getType, type);
        if (status != null) wrapper.eq(PaymentMethod::getStatus, status);

        Page<PaymentMethod> pageResult = paymentMethodMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 全部支付方式（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<PaymentMethod>> all() {
        return Result.success(paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>().orderByAsc(PaymentMethod::getSort)
        ));
    }

    /**
     * 支付方式详情
     */
    @GetMapping("/detail/{id}")
    public Result<PaymentMethod> detail(@PathVariable Long id) {
        PaymentMethod pm = paymentMethodMapper.selectById(id);
        if (pm == null) throw new BusinessException("支付方式不存在");
        return Result.success(pm);
    }

    /**
     * 新增支付方式
     */
    @PostMapping("/create")
    @Transactional
    public Result<Void> create(@RequestBody PaymentMethod paymentMethod, HttpServletRequest request) {
        if (paymentMethod.getName() == null || paymentMethod.getCode() == null) {
            throw new BusinessException("名称和编码不能为空");
        }
        // 检查编码唯一性
        Long count = paymentMethodMapper.selectCount(
                new LambdaQueryWrapper<PaymentMethod>().eq(PaymentMethod::getCode, paymentMethod.getCode())
        );
        if (count > 0) throw new BusinessException("支付方式编码已存在");

        if (paymentMethod.getStatus() == null) paymentMethod.setStatus(1);
        if (paymentMethod.getSort() == null) paymentMethod.setSort(0);
        paymentMethodMapper.insert(paymentMethod);

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "create_payment_method", "payment_method", paymentMethod.getId(),
                null, "{\"code\":\"" + paymentMethod.getCode() + "\",\"name\":\"" + paymentMethod.getName() + "\"}",
                request);
        return Result.success();
    }

    /**
     * 更新支付方式
     */
    @PostMapping("/update")
    @Transactional
    public Result<Void> update(@RequestBody PaymentMethod paymentMethod, HttpServletRequest request) {
        if (paymentMethod.getId() == null) throw new BusinessException("ID不能为空");
        PaymentMethod existing = paymentMethodMapper.selectById(paymentMethod.getId());
        if (existing == null) throw new BusinessException("支付方式不存在");

        paymentMethodMapper.updateById(paymentMethod);

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "update_payment_method", "payment_method", paymentMethod.getId(),
                "{\"status\":" + existing.getStatus() + "}",
                "{\"status\":" + paymentMethod.getStatus() + "}",
                request);
        return Result.success();
    }

    /**
     * 删除支付方式（逻辑删除）
     */
    @PostMapping("/delete/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PaymentMethod existing = paymentMethodMapper.selectById(id);
        if (existing == null) throw new BusinessException("支付方式不存在");
        paymentMethodMapper.deleteById(id);

        adminLogService.log(UserContext.getUserId(), UserContext.getUsername(),
                "delete_payment_method", "payment_method", id,
                "{\"code\":\"" + existing.getCode() + "\"}", null, request);
        return Result.success();
    }

    /**
     * 切换启用/禁用状态
     */
    @PostMapping("/toggle/{id}")
    @Transactional
    public Result<Void> toggle(@PathVariable Long id, HttpServletRequest request) {
        PaymentMethod pm = paymentMethodMapper.selectById(id);
        if (pm == null) throw new BusinessException("支付方式不存在");
        pm.setStatus(pm.getStatus() == 1 ? 0 : 1);
        paymentMethodMapper.updateById(pm);
        return Result.success();
    }
}
