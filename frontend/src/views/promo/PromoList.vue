<template>
  <div class="promo-list-page">
    <van-nav-bar title="优惠活动" left-arrow @click-left="$router.back()" />

    <!-- 分类Tab -->
    <van-tabs v-model:active="activeCategory" color="#e8b860" class="promo-tabs">
      <van-tab v-for="cat in categories" :key="cat.id" :title="cat.name" :name="cat.id" />
    </van-tabs>

    <div class="promo-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#e8b860">加载中...</van-loading>
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
        <div class="empty-icon">🎁</div>
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
    promos.value = data.list || []
  } catch (e) {
    promos.value = []
  } finally {
    loading.value = false
  }
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
  background: #0d0d0d;
  padding-bottom: 60px;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
:deep(.promo-tabs .van-tabs__nav) {
  background: #1a1a1a;
}
:deep(.promo-tabs .van-tab) {
  color: #888;
}
:deep(.promo-tabs .van-tab--active) {
  color: #e8b860;
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
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #2a2a2a;
}
.promo-card:active {
  transform: scale(0.99);
}
.promo-banner {
  width: 100%;
  height: 140px;
  object-fit: cover;
}
.promo-info {
  padding: 12px;
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
  color: #f0f0f0;
}
.promo-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(7, 193, 96, 0.15);
  color: #07c160;
}
.promo-status.ended {
  background: rgba(128, 128, 128, 0.15);
  color: #888;
}
.promo-desc {
  font-size: 12px;
  color: #888;
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
  color: #666;
}
.promo-action {
  font-size: 12px;
  color: #e8b860;
  font-weight: 500;
}
.empty-state {
  padding: 60px 20px;
  text-align: center;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}
.empty-state p {
  color: #666;
  font-size: 14px;
}
</style>
