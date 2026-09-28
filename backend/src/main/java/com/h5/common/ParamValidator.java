package com.h5.common;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 参数校验工具类
 * 统一处理Controller中@RequestBody Map参数的校验，避免NPE和NumberFormatException
 */
public class ParamValidator {

    /** 从Map中获取BigDecimal，无效值抛BusinessException */
    public static BigDecimal requireBigDecimal(Map<String, Object> params, String key, String fieldName) {
        Object val = params.get(key);
        if (val == null) {
            throw new BusinessException(400, fieldName + "不能为空");
        }
        try {
            BigDecimal result = new BigDecimal(val.toString());
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, fieldName + "必须大于0");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }

    /** 从Map中获取BigDecimal（可空，空则返回默认值） */
    public static BigDecimal optionalBigDecimal(Map<String, Object> params, String key, BigDecimal defaultValue) {
        Object val = params.get(key);
        if (val == null || val.toString().isEmpty()) return defaultValue;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 从Map中获取String，空值抛BusinessException */
    public static String requireString(Map<String, Object> params, String key, String fieldName) {
        Object val = params.get(key);
        if (val == null || val.toString().isEmpty()) {
            throw new BusinessException(400, fieldName + "不能为空");
        }
        return val.toString();
    }

    /** 从Map中获取String（可空） */
    public static String optionalString(Map<String, Object> params, String key, String defaultValue) {
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    /** 从Map中获取int，无效值抛BusinessException */
    public static int requireInt(Map<String, Object> params, String key, String fieldName, int min) {
        Object val = params.get(key);
        if (val == null) {
            throw new BusinessException(400, fieldName + "不能为空");
        }
        try {
            int result = Integer.parseInt(val.toString());
            if (result < min) {
                throw new BusinessException(400, fieldName + "不能小于" + min);
            }
            return result;
        } catch (NumberFormatException e) {
            throw new BusinessException(400, fieldName + "格式错误");
        }
    }

    /** 从Map中获取int（可空，空则返回默认值） */
    public static int optionalInt(Map<String, Object> params, String key, int defaultValue) {
        Object val = params.get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
