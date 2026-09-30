<template>
  <div class="vip-page">
    <van-nav-bar title="VIP特权" left-arrow @click-left="$router.back()" />
    <div class="vip-content">
      <!-- 当前等级卡片 -->
      <div class="vip-card-current">
        <div class="vip-crown"><GoldIcon name="crown" :size="32" /></div>
        <div class="vip-level-info">
          <span class="vip-level-name">{{ vipInfo?.currentName || '普通会员' }}</span>
          <span class="vip-level-num">VIP{{ vipInfo?.currentLevel || 1 }}</span>
        </div>
        <div class="vip-progress">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: (vipInfo?.progress || 0) + '%' }"></div>
          </div>
          <span class="progress-text">距离{{ vipInfo?.nextName || '下一等级' }}还差 ¥{{ vipInfo?.amountToNext || 0 }}</span>
        </div>
      </div>

      <!-- 等级列表 -->
      <div class="vip-levels">
        <div class="section-title">等级特权</div>
        <div
          v-for="level in vipInfo?.benefits || []"
          :key="level.level"
          class="level-item"
          :class="{ current: level.level === vipInfo?.currentLevel }"
        >
          <div class="level-header">
            <span class="level-badge">VIP{{ level.level }}</span>
            <span class="level-name">{{ level.name }}</span>
            <span v-if="level.level === vipInfo?.currentLevel" class="current-tag">当前</span>
          </div>
          <div class="level-benefits">
            <div class="benefit-item">
              <span class="benefit-label">返水比例</span>
              <span class="benefit-value text-gold">{{ level.rebate }}</span>
            </div>
            <div class="benefit-item">
              <span class="benefit-label">每日提现</span>
              <span class="benefit-value">¥{{ formatAmount(level.dailyWithdraw) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import GoldIcon from '@/components/GoldIcon.vue'

const vipInfo = ref(null)

function formatAmount(amount) {
  if (amount >= 10000) return (amount / 10000) + '万'
  return amount.toLocaleString()
}

async function loadVip() {
  try {
    vipInfo.value = await userApi.getVipInfo()
  } catch (e) {
    vipInfo.value = null
  }
}

onMounted(() => {
  loadVip()
})
</script>

<style scoped>
.vip-page {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
.vip-content {
  padding: 12px;
}
.vip-card-current {
  background: linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border: 1px solid #d4a84b;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}
.vip-crown {
  font-size: 40px;
  margin-bottom: 8px;
}
.vip-level-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}
.vip-level-name {
  font-size: 20px;
  font-weight: 700;
  color: #f0d080;
}
.vip-level-num {
  font-size: 14px;
  color: #d4a84b;
  background: rgba(240, 208, 128, 0.15);
  padding: 2px 8px;
  border-radius: 4px;
}
.vip-progress {
  margin-top: 8px;
}
.progress-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f0d080, #d4a84b);
  border-radius: 3px;
  transition: width 0.3s;
}
.progress-text {
  font-size: 11px;
  color: var(--text-hint);
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gold-1);
  margin-bottom: 12px;
  padding-left: 4px;
}
.vip-levels {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.level-item {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
}
.level-item.current {
  border-color: #f0d080;
  background: rgba(240, 208, 128, 0.05);
}
.level-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.level-badge {
  font-size: 12px;
  font-weight: 700;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  color: #3a2610;
  padding: 2px 8px;
  border-radius: 4px;
}
.level-name {
  font-size: 14px;
  color: var(--gold-1);
  font-weight: 500;
  flex: 1;
}
.current-tag {
  font-size: 10px;
  color: #f0d080;
  border: 1px solid #f0d080;
  padding: 1px 6px;
  border-radius: 3px;
}
.level-benefits {
  display: flex;
  gap: 20px;
}
.benefit-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.benefit-label {
  font-size: 11px;
  color: #6a5a40;
}
.benefit-value {
  font-size: 14px;
  color: var(--gold-1);
  font-weight: 600;
}
</style>
