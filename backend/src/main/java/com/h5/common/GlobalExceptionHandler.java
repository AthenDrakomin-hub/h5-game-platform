package com.h5.common;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * 生产环境不暴露内部异常信息，统一返回友好提示
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：直接返回业务提示 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.error(400, msg);
    }

    /** 参数非法 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    /** JSON解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(400, "请求体格式错误");
    }

    /** 乐观锁冲突：并发扣余额时版本号不匹配 */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public Result<?> handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        log.warn("乐观锁冲突: {}", e.getMessage());
        return Result.error(409, "操作繁忙，请稍后重试");
    }

    /** MyBatis-Plus异常 */
    @ExceptionHandler(MybatisPlusException.class)
    public Result<?> handleMybatisPlusException(MybatisPlusException e) {
        log.error("MyBatis-Plus异常: {}", e.getMessage(), e);
        return Result.error(500, "数据操作失败");
    }

    /** 兜底：所有未捕获异常，生产环境不暴露内部信息 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("未捕获异常: {}", e.getMessage(), e);
        return Result.error(500, "服务器繁忙，请稍后重试");
    }
}
