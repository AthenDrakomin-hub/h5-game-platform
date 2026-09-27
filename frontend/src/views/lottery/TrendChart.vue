<template>
  <div class="trend-page">
    <van-nav-bar title="走势图" left-arrow @click-left="$router.back()" />
    <div class="trend-content">
      <van-tabs v-model:active="activeCode" @change="onTabChange" sticky>
        <van-tab v-for="lot in lotteries" :key="lot.code" :title="lot.name" :name="lot.code" />
      </van-tabs>

      <div class="trend-stats">
        <div class="stat-item">
          <span class="stat-label">龙</span>
          <span class="stat-value dragon">{{ dragonCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">虎</span>
          <span class="stat-value tiger">{{ tigerCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">大</span>
          <span class="stat-value big">{{ bigCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">小</span>
          <span class="stat-value small">{{ smallCount }}</span>
        </div>
      </div>

      <div class="trend-list" v-loading="loading">
        <div v-for="item in trendList" :key="item.period" class="trend-item">
          <span class="period">{{ item.period }}</span>
          <span class="numbers">{{ item.numbers }}</span>
          <span class="tags">
            <van-tag :type="item.dragonTiger === 'dragon' ? 'danger' : item.dragonTiger === 'tiger' ? 'primary' : 'default'" size="mini">{{ item.dragonTiger === 'dragon' ? '龙' : item.dragonTiger === 'tiger' ? '虎' : '和' }}</van-tag>
            <van-tag :type="item.bigSmall === 'big' ? 'warning' : 'success'" size="mini">{{ item.bigSmall === 'big' ? '大' : '小' }}</van-tag>
          </span>
        </div>
        <van-empty v-if="trendList.length === 0" description="暂无走势数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const activeCode = ref('jsdd')
const loading = ref(false)
const trendList = ref([])

const lotteries = ref([
  { code: 'jsdd', name: 'PC28' },
  { code: 'jspk10', name: 'PK10' },
  { code: 'jsssc', name: '时时彩' },
  { code: 'happy8lhc', name: '六合彩' }
])

const dragonCount = computed(() => trendList.value.filter(i => i.dragonTiger === 'dragon').length)
const tigerCount = computed(() => trendList.value.filter(i => i.dragonTiger === 'tiger').length)
const bigCount = computed(() => trendList.value.filter(i => i.bigSmall === 'big').length)
const smallCount = computed(() => trendList.value.filter(i => i.bigSmall === 'small').length)

const loadTrend = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/wap/lottery/trend/dragon/' + activeCode.value, method: 'get', params: { limit: 30 } })
    trendList.value = res.data?.list || []
  } catch (e) {
    trendList.value = []
  } finally {
    loading.value = false
  }
}

const onTabChange = () => loadTrend()
onMounted(() => loadTrend())
</script>

<style scoped>
.trend-page { min-height: 100vh; background: #0d0d0d; }
:deep(.van-nav-bar), :deep(.van-tabs) { background: #1a1a1a; }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-tab) { color: #f0f0f0 !important; }
:deep(.van-tab--active) { color: #e8b860 !important; }
.trend-content { padding-bottom: 20px; }
.trend-stats { display: flex; justify-content: space-around; padding: 16px; background: #1a1a1a; margin-bottom: 8px; }
.stat-item { text-align: center; }
.stat-label { display: block; font-size: 12px; color: #888; margin-bottom: 4px; }
.stat-value { font-size: 20px; font-weight: 700; }
.stat-value.dragon { color: #ff4d4f; }
.stat-value.tiger { color: #1890ff; }
.stat-value.big { color: #faad14; }
.stat-value.small { color: #52c41a; }
.trend-list { padding: 0 12px; }
.trend-item { display: flex; align-items: center; padding: 12px; background: #1a1a1a; border-radius: 8px; margin-bottom: 8px; }
.period { width: 100px; font-size: 12px; color: #888; }
.numbers { flex: 1; font-size: 14px; color: #e8b860; font-weight: 600; letter-spacing: 2px; }
.tags { display: flex; gap: 4px; }
</style>
