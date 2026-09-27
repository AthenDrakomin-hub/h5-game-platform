package com.h5.service.payment;

import com.h5.entity.Order;
import java.util.Map;

/**
 * 三方支付网关接口（可插拔）
 *
 * 实现类示例：
 *   - EasyPayGateway（易支付）
 *   - CodePayGateway（码支付）
 *   - UsdtTrc20Gateway（USDT TRC20，链上监听）
 *
 * 支付流程：
 * 1. createPayment: 创建三方支付订单，返回支付链接/二维码/参数
 * 2. 用户完成支付
 * 3. handleCallback: 三方平台异步回调通知，验签后确认到账
 * 4. queryPayment: 主动查询支付状态（兜底）
 */
public interface PaymentGateway {

    /**
     * 网关编码（与 payment_methods.code 对应）
     */
    String getCode();

    /**
     * 是否启用
     */
    boolean isEnabled();

    /**
     * 创建支付订单
     *
     * @param order 平台订单
     * @return 支付参数（支付链接/二维码URL/表单参数等）
     */
    Map<String, Object> createPayment(Order order);

    /**
     * 处理支付回调
     *
     * @param params 回调参数
     * @return 回调处理结果（success/fail + 订单号）
     */
    CallbackResult handleCallback(Map<String, String> params);

    /**
     * 主动查询支付状态
     *
     * @param orderNo 平台订单号
     * @return 支付状态（pending/success/failed）
     */
    String queryPayment(String orderNo);

    /**
     * 回调处理结果
     */
    class CallbackResult {
        private boolean success;
        private String orderNo;
        private String message;

        public static CallbackResult ok(String orderNo) {
            CallbackResult r = new CallbackResult();
            r.success = true;
            r.orderNo = orderNo;
            return r;
        }

        public static CallbackResult fail(String message) {
            CallbackResult r = new CallbackResult();
            r.success = false;
            r.message = message;
            return r;
        }

        public boolean isSuccess() { return success; }
        public String getOrderNo() { return orderNo; }
        public String getMessage() { return message; }
    }
}
