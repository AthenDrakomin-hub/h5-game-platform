package com.h5.service.payment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.h5.entity.Order;
import com.h5.entity.PaymentMethod;
import com.h5.entity.UsdtAddressPool;
import com.h5.mapper.OrderMapper;
import com.h5.mapper.PaymentMethodMapper;
import com.h5.mapper.UsdtAddressPoolMapper;
import com.h5.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * USDT TRC20 自动到账监听服务
 *
 * 原理：
 * 1. 轮询 TronGrid API 获取指定收款地址的 TRC20 USDT 转账记录
 * 2. 匹配 pending 状态的充值订单（按金额 + 时间窗口）
 * 3. 确认到账后自动审核通过：加余额 + 写流水 + 订单状态 success
 *
 * 注意：
 * - 生产环境建议使用 TronGrid API Key 提高限流（免费版 5次/秒）
 * - 金额匹配可能存在多笔同金额订单，按时间窗口（30分钟）+ 金额匹配
 * - 已处理的交易哈希缓存到内存，防止重复处理
 */
@Service
@ConditionalOnProperty(name = "usdt.trc20.enabled", havingValue = "true")
public class UsdtTrc20Service {

    private static final Logger log = LoggerFactory.getLogger(UsdtTrc20Service.class);
    private static final String USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6"; // USDT TRC20 合约地址
    private static final int MATCH_WINDOW_MINUTES = 30; // 订单匹配时间窗口

    @Value("${usdt.trc20.grid-api:https://api.trongrid.io}")
    private String gridApi;

    @Value("${usdt.trc20.api-key:}")
    private String apiKey;

    @Value("${usdt.trc20.poll-interval:5000}")
    private int pollInterval;

    @Value("${usdt.trc20.confirmations:1}")
    private int confirmations;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private UsdtAddressPoolMapper addressPoolMapper;

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    /** 已处理的交易哈希缓存（防止重复） */
    private final Set<String> processedTxHashes = Collections.synchronizedSet(new HashSet<>());
    /** 最后扫描的时间戳 */
    private volatile long lastScanTimestamp = System.currentTimeMillis() / 1000 - 3600; // 初始扫描1小时前

    /**
     * 定时轮询链上交易
     */
    @Scheduled(fixedDelayString = "${usdt.trc20.poll-interval:5000}")
    public void pollTransactions() {
        try {
            scanAndProcess();
        } catch (Exception e) {
            log.error("USDT TRC20 轮询异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 扫描并处理所有启用的 USDT 收款地址
     */
    public void scanAndProcess() {
        // 获取所有启用的 USDT-TRC20 支付方式的收款地址
        // 此处简化：从 payment_methods 表读取，实际可注入 PaymentMethodMapper
        List<String> addresses = getMonitorAddresses();
        if (addresses.isEmpty()) {
            return;
        }

        long currentTimestamp = System.currentTimeMillis() / 1000;

        for (String address : addresses) {
            if (address == null || address.isEmpty() || address.startsWith("TExxx")) {
                continue; // 跳过占位地址
            }
            try {
                processAddress(address, lastScanTimestamp, currentTimestamp);
            } catch (Exception e) {
                log.error("处理地址 {} 失败: {}", address, e.getMessage());
            }
        }

        lastScanTimestamp = currentTimestamp;
    }

    /**
     * 处理单个地址的交易
     */
    @SuppressWarnings("unchecked")
    private void processAddress(String address, long fromTimestamp, long toTimestamp) {
        String url = gridApi + "/v1/accounts/" + address + "/transactions/trc20"
                + "?limit=200&only_to=true&min_timestamp=" + (fromTimestamp * 1000)
                + "&max_timestamp=" + (toTimestamp * 1000);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("TRON-PRO-API-KEY", apiKey);
        }
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, Map.class).getBody();

            if (response == null || !response.containsKey("data")) {
                return;
            }

            List<Map<String, Object>> txList = (List<Map<String, Object>>) response.get("data");
            if (txList == null) return;

            for (Map<String, Object> tx : txList) {
                try {
                    processTransaction(tx, address);
                } catch (Exception e) {
                    log.error("处理交易失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("TronGrid API 请求失败, address={}, error={}", address, e.getMessage());
        }
    }

    /**
     * 处理单笔交易
     */
    private void processTransaction(Map<String, Object> tx, String toAddress) {
        String txHash = (String) tx.get("transaction_id");
        if (txHash == null || processedTxHashes.contains(txHash)) {
            return;
        }

        // 验证是 USDT 合约
        String contract = (String) tx.get("contract_address");
        if (contract == null || !USDT_CONTRACT.equals(contract)) {
            return; // 非 USDT 转账
        }

        // 解析金额（TRC20 USDT 精度 6 位）
        Object valueObj = tx.get("value");
        if (valueObj == null) return;
        BigDecimal amount;
        try {
            amount = new BigDecimal(valueObj.toString())
                    .divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return;
        }

        // 解析时间
        Object blockTsObj = tx.get("block_timestamp");
        LocalDateTime txTime = LocalDateTime.now();
        if (blockTsObj != null) {
            try {
                long ts = Long.parseLong(blockTsObj.toString()) / 1000;
                txTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault());
            } catch (Exception ignored) {}
        }

        // 匹配充值订单
        Order matchedOrder = matchRechargeOrder(amount, txTime, toAddress);
        if (matchedOrder == null) {
            log.info("USDT 到账未匹配到订单, txHash={}, amount={}, address={}", txHash, amount, toAddress);
            processedTxHashes.add(txHash);
            return;
        }

        // 确认到账（自动审核通过）
        confirmRecharge(matchedOrder, txHash);
        processedTxHashes.add(txHash);
        log.info("USDT 自动到账确认, orderNo={}, amount={}, txHash={}",
                matchedOrder.getOrderNo(), amount, txHash);
    }

    /**
     * 匹配 pending 充值订单
     * 条件：type=recharge, status=pending, method=usdt_trc20, 金额匹配, 时间窗口内
     */
    private Order matchRechargeOrder(BigDecimal amount, LocalDateTime txTime, String address) {
        LocalDateTime windowStart = txTime.minusMinutes(MATCH_WINDOW_MINUTES);

        List<Order> candidates = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getType, "recharge")
                        .eq(Order::getStatus, "pending")
                        .eq(Order::getMethod, "usdt_trc20")
                        .ge(Order::getCreateTime, windowStart)
                        .orderByAsc(Order::getCreateTime)
        );

        // 精确金额匹配（取最早的一笔）
        for (Order order : candidates) {
            if (order.getAmount().compareTo(amount) == 0) {
                return order;
            }
        }
        return null;
    }

    /**
     * 确认充值到账（加余额 + 写流水 + 更新订单）
     */
    private void confirmRecharge(Order order, String txHash) {
        // 更新订单状态
        order.setStatus("success");
        order.setAuditTime(LocalDateTime.now());
        order.setRemark("USDT自动到账, txHash=" + txHash);
        orderMapper.updateById(order);

        // 加余额 + 写流水
        balanceService.addBalance(
                order.getUserId(),
                order.getAmount(),
                "recharge",
                order.getId(),
                order.getOrderNo(),
                "USDT-TRC20 自动到账"
        );
    }

    /**
     * 获取需要监控的收款地址列表
     * 优先从 usdt_address_pool 地址池读取，其次从 payment_methods 读取，最后从订单反推
     */
    private List<String> getMonitorAddresses() {
        List<String> addresses = new ArrayList<>();

        // 1. 从地址池读取所有启用地址
        List<UsdtAddressPool> pool = addressPoolMapper.selectList(
                new LambdaQueryWrapper<UsdtAddressPool>()
                        .eq(UsdtAddressPool::getStatus, 1)
        );
        for (UsdtAddressPool addr : pool) {
            if (addr.getAddress() != null && !addr.getAddress().isEmpty()
                    && !addresses.contains(addr.getAddress())) {
                addresses.add(addr.getAddress());
            }
        }

        // 2. 从支付方式表读取 USDT 收款地址
        if (addresses.isEmpty()) {
            List<PaymentMethod> methods = paymentMethodMapper.selectList(
                    new LambdaQueryWrapper<PaymentMethod>()
                            .eq(PaymentMethod::getType, "usdt")
                            .eq(PaymentMethod::getStatus, 1)
            );
            for (PaymentMethod pm : methods) {
                if (pm.getPayAccount() != null && !pm.getPayAccount().isEmpty()
                        && !addresses.contains(pm.getPayAccount())) {
                    addresses.add(pm.getPayAccount());
                }
            }
        }

        // 3. 兜底：从最近的 usdt_trc20 订单中提取收款地址
        if (addresses.isEmpty()) {
            List<Order> recentOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getMethod, "usdt_trc20")
                            .isNotNull(Order::getPayAccount)
                            .orderByDesc(Order::getCreateTime)
                            .last("LIMIT 10")
            );
            for (Order order : recentOrders) {
                if (order.getPayAccount() != null && !order.getPayAccount().isEmpty()
                        && !addresses.contains(order.getPayAccount())) {
                    addresses.add(order.getPayAccount());
                }
            }
        }
        return addresses;
    }

    /**
     * 从地址池轮询分配一个收款地址（轮换策略）
     * 用于创建USDT充值订单时分配地址
     */
    public String allocateAddress() {
        List<UsdtAddressPool> pool = addressPoolMapper.selectList(
                new LambdaQueryWrapper<UsdtAddressPool>()
                        .eq(UsdtAddressPool::getStatus, 1)
                        .orderByAsc(UsdtAddressPool::getSort)
        );
        if (pool.isEmpty()) return null;
        // 简单轮换：取使用次数最少的地址
        UsdtAddressPool selected = pool.get(0);
        int minUsage = Integer.MAX_VALUE;
        for (UsdtAddressPool addr : pool) {
            int usage = addr.getUsageCount() != null ? addr.getUsageCount() : 0;
            if (usage < minUsage) {
                minUsage = usage;
                selected = addr;
            }
        }
        // 更新使用次数
        selected.setUsageCount((selected.getUsageCount() != null ? selected.getUsageCount() : 0) + 1);
        addressPoolMapper.updateById(selected);
        return selected.getAddress();
    }
}
