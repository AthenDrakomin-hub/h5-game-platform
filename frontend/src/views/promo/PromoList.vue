<template>
  <div class="promo-list-page">
    <van-nav-bar title="优惠活动" left-arrow @click-left="$router.back()" />

    <!-- 分类Tab -->
    <van-tabs v-model:active="activeCategory" color="#f0d080" class="promo-tabs">
      <van-tab v-for="cat in categories" :key="cat.id" :title="cat.name" :name="cat.id" />
    </van-tabs>

    <div class="promo-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="promo-cards">
        <div
          v-for="promo in filteredPromos"
          :key="promo.id"
          class="promo-card"
          @click="goDetail(promo.id)"
        >
          <img :src="promo.image" :alt="promo.title" class="promo-banner" />
          <div class="promo-info">
            <div class="promo-title-row">
              <span class="promo-title">{{ promo.title }}</span>
              <span class="promo-status" :class="promo.status">{{ promo.status === 'active' ? '进行中' : '已结束' }}</span>
            </div>
            <p class="promo-desc">{{ promo.description }}</p>
            <div class="promo-footer">
              <span class="promo-date">{{ promo.startDate }} 至 {{ promo.endDate }}</span>
              <span class="promo-action">立即参与 ›</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && filteredPromos.length === 0" class="empty-state">
        <img src="/empty-state.svg" alt="empty" class="empty-img" />
        <p>暂无活动</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { promoApi } from '@/api/promo'

const router = useRouter()

const activeCategory = ref('all')
const promos = ref([])
const loading = ref(false)

const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'newbie', name: '新人专享' },
  { id: 'daily', name: '每日活动' },
  { id: 'deposit', name: '充值优惠' },
  { id: 'vip', name: 'VIP专享' },
  { id: 'invite', name: '邀请奖励' }
])

const filteredPromos = computed(() => {
  if (activeCategory.value === 'all') return promos.value
  return promos.value.filter(p => p.category === activeCategory.value)
})

async function loadPromos() {
  loading.value = true
  try {
    const data = await promoApi.getList()
    promos.value = data.list?.length ? data.list : getDefaultPromos()
  } catch (e) {
    promos.value = getDefaultPromos()
  } finally {
    loading.value = false
  }
}

function getDefaultPromos() {
  return [
    { id: 1, title: '新人首存100%加成', description: '首次充值享100%奖励，最高5000元！', image: '/assets/banners/banner1.png', category: 'newbie', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31' },
    { id: 2, title: '每日返利 流水越高越多', description: '每日根据投注流水返还0.5%-2%', image: '/assets/banners/banner2.png', category: 'daily', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31' },
    { id: 3, title: '充值500送100', description: '单笔充值满500元额外送100元彩金', image: '/assets/banners/banner3.png', category: 'deposit', status: 'active', startDate: '2026-09-15', endDate: '2026-10-15' },
    { id: 4, title: 'VIP专属周薪', description: 'VIP3以上每周领取专属薪资奖励', image: '/assets/banners/banner4.png', category: 'vip', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31' },
    { id: 5, title: '邀请好友得红包', description: '邀请好友注册充值，双方各得50元', image: '/assets/banners/banner5.png', category: 'invite', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31' },
  ]
}

function goDetail(id) {
  router.push(`/promo/detail/${id}`)
}

onMounted(() => {
  loadPromos()
})
</script>

<style scoped>
.promo-list-page {
  min-height: 100vh;
  background: var(--app-bg);
  padding-bottom: 60px;
}
:deep(.van-nav-bar) {
  background: transparent !important;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
:deep(.promo-tabs .van-tabs__nav) {
  background: transparent;
}
:deep(.promo-tabs .van-tab) {
  color: var(--text-hint);
}
:deep(.promo-tabs .van-tab--active) {
  color: #f0d080;
}
:deep(.promo-tabs .van-tabs__line) {
  background: linear-gradient(90deg, #f0d080, #d4a84b);
}
.promo-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.promo-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.promo-card {
  background: var(--card-bg);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(255,255,255,0.12);
}
.promo-card:active {
  transform: scale(0.99);
  border-color: rgba(212,168,75,0.4);
}
.promo-banner {
  width: 100%;
  height: 140px;
  object-fit: cover;
}
.promo-info {
  padding: 12px 14px;
}
.promo-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.promo-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gold-1);
}
.promo-status {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(240,208,128,0.15);
  color: #f0d080;
}
.promo-status.ended {
  background: rgba(128, 128, 128, 0.15);
  color: var(--text-hint);
}
.promo-desc {
  font-size: 12px;
  color: #a09070;
  margin: 0 0 10px 0;
  line-height: 1.5;
}
.promo-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.promo-date {
  font-size: 11px;
  color: #6a5a40;
}
.promo-action {
  font-size: 12px;
  color: #f0d080;
  font-weight: 500;
}
.empty-state {
  padding: 60px 20px;
  text-align: center;
}
.empty-img {
  width: 160px;
  height: auto;
  margin-bottom: 12px;
}
.empty-state p {
  color: #6a5a40;
  font-size: 14px;
}
</style>
