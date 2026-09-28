<template>
  <div class="orders-page">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.back()" />
    <van-tabs v-model:active="activeTab" color="#f0d080" class="order-tabs">
      <van-tab title="全部" name="all" />
      <van-tab title="充值" name="recharge" />
      <van-tab title="提现" name="withdraw" />
    </van-tabs>
    <div class="orders-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="order-list">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
        >
          <div class="order-header">
            <span class="order-type" :class="order.type">{{ order.type === 'recharge' ? '充值' : '提现' }}</span>
            <span class="order-status" :class="order.status">{{ statusText(order.status) }}</span>
          </div>
          <div class="order-body">
            <div class="order-row">
              <span class="order-label">订单号</span>
              <span class="order-value">{{ order.orderNo }}</span>
            </div>
            <div class="order-row">
              <span class="order-label">金额</span>
              <span class="order-amount" :class="order.type">
                {{ order.type === 'recharge' ? '+' : '-' }}¥{{ order.amount }}
              </span>
            </div>
            <div class="order-row">
              <span class="order-label">方式</span>
              <span class="order-value">{{ order.method }}</span>
            </div>
            <div class="order-row">
              <span class="order-label">时间</span>
              <span class="order-value">{{ formatTime(order.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && filteredOrders.length === 0" class="empty-state">
        <van-empty description="暂无订单记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.type === activeTab.value)
})

function statusText(status) {
  const map = { success: '成功', pending: '处理中', failed: '失败' }
  return map[status] || status
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await userApi.getOrders({ page: 1, pageSize: 50 })
    orders.value = data.list || []
  } catch (e) {
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f2e0b8 !important;
}
:deep(.order-tabs .van-tabs__nav) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.order-tabs .van-tab) {
  color: #8a7a5a;
}
:deep(.order-tabs .van-tab--active) {
  color: #f0d080;
}
.orders-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.order-card {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.12);
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.order-type {
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}
.order-type.recharge {
  background: rgba(7, 193, 96, 0.15);
  color: #07c160;
}
.order-type.withdraw {
  background: rgba(255, 151, 106, 0.15);
  color: #ff976a;
}
.order-status {
  font-size: 12px;
}
.order-status.success { color: #07c160; }
.order-status.pending { color: #ff976a; }
.order-status.failed { color: #ee0a24; }
.order-body {
  padding: 12px 14px;
}
.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.order-row:last-child {
  margin-bottom: 0;
}
.order-label {
  font-size: 12px;
  color: #8a7a5a;
}
.order-value {
  font-size: 12px;
  color: #ccc;
}
.order-amount {
  font-size: 16px;
  font-weight: 700;
}
.order-amount.recharge { color: #07c160; }
.order-amount.withdraw { color: #ff976a; }
.empty-state {
  padding: 40px 20px;
}
:deep(.van-empty__description) {
  color: #6a5a40;
}
</style>
