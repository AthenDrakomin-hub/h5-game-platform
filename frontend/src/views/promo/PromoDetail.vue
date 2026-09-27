<template>
  <div class="promo-detail-page">
    <van-nav-bar title="活动详情" left-arrow @click-left="$router.back()" />
    <div v-if="loading" class="loading-wrap">
      <van-loading color="#e8b860">加载中...</van-loading>
    </div>
    <div v-else-if="promo" class="promo-detail-content">
      <img :src="promo.image" :alt="promo.title" class="detail-banner" />
      <div class="detail-body">
        <h1 class="detail-title">{{ promo.title }}</h1>
        <div class="detail-meta">
          <span class="meta-item">
            <van-icon name="clock-o" size="12" />
            {{ promo.startDate }} 至 {{ promo.endDate }}
          </span>
          <span class="meta-tag">{{ getCategoryName(promo.category) }}</span>
        </div>
        <div class="detail-divider"></div>
        <div class="detail-content-html" v-html="promo.content"></div>
        <div class="detail-rules" v-if="promo.rules?.length">
          <h3 class="rules-title">活动规则</h3>
          <ol class="rules-list">
            <li v-for="(rule, idx) in promo.rules" :key="idx">{{ rule }}</li>
          </ol>
        </div>
      </div>
      <div class="detail-footer">
        <van-button block type="primary" class="claim-btn" @click="onClaim">
          立即参与
        </van-button>
      </div>
    </div>
    <div v-else class="empty-state">
      <van-empty description="活动不存在或已结束" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { promoApi } from '@/api/promo'
import { showSuccessToast, showFailToast } from 'vant'

const route = useRoute()
const authStore = useAuthStore()
const appStore = useAppStore()

const promo = ref(null)
const loading = ref(true)

const categoryMap = {
  newbie: '新人专享',
  daily: '每日活动',
  deposit: '充值优惠',
  vip: 'VIP专享',
  invite: '邀请奖励',
  rescue: '救援金'
}

function getCategoryName(cat) {
  return categoryMap[cat] || '优惠活动'
}

async function loadDetail() {
  loading.value = true
  try {
    const data = await promoApi.getDetail(route.params.id)
    promo.value = data
  } catch (e) {
    promo.value = null
  } finally {
    loading.value = false
  }
}

async function onClaim() {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', route.fullPath)
    return
  }
  try {
    await promoApi.claim(route.params.id)
    showSuccessToast('参与成功')
  } catch (e) {
    showFailToast(e.message || '参与失败')
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.promo-detail-page {
  min-height: 100vh;
  background: #0d0d0d;
  padding-bottom: 80px;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
.loading-wrap {
  padding: 60px;
  text-align: center;
}
.detail-banner {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.detail-body {
  padding: 16px;
}
.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: #f0f0f0;
  margin: 0 0 10px 0;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #888;
}
.meta-tag {
  font-size: 11px;
  color: #e8b860;
  border: 1px solid #e8b860;
  padding: 2px 8px;
  border-radius: 10px;
}
.detail-divider {
  height: 1px;
  background: #2a2a2a;
  margin-bottom: 16px;
}
.detail-content-html {
  font-size: 14px;
  color: #ccc;
  line-height: 1.8;
}
.detail-content-html :deep(p) {
  margin: 0 0 10px 0;
}
.detail-content-html :deep(strong) {
  color: #e8b860;
}
.detail-content-html :deep(ol) {
  padding-left: 20px;
}
.detail-content-html :deep(li) {
  margin-bottom: 6px;
}
.detail-rules {
  margin-top: 20px;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 16px;
}
.rules-title {
  font-size: 15px;
  font-weight: 600;
  color: #f0f0f0;
  margin: 0 0 12px 0;
}
.rules-list {
  padding-left: 20px;
  margin: 0;
}
.rules-list li {
  font-size: 13px;
  color: #999;
  line-height: 1.8;
  margin-bottom: 6px;
}
.detail-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  padding: 12px 16px;
  background: #1a1a1a;
  border-top: 1px solid #333;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
.claim-btn {
  background: linear-gradient(135deg, #e8b860, #c99a3e) !important;
  border: none !important;
  color: #1a1a1a !important;
  font-weight: 600 !important;
  height: 44px !important;
  border-radius: 22px !important;
}
.empty-state {
  padding: 60px 20px;
}
:deep(.van-empty__description) {
  color: #666;
}
</style>
