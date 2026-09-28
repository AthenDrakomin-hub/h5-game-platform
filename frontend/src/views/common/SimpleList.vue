<template>
  <div class="list-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="list-content" v-loading="loading">
      <div v-for="item in items" :key="item.id" class="list-item">
        <div class="item-left">
          <img v-if="item.icon && item.icon.startsWith('/')" :src="item.icon" class="item-icon-img" />
          <span v-else class="item-icon">{{ item.icon || '📋' }}</span>
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-desc">{{ item.desc }}</span>
          </div>
        </div>
        <span v-if="item.amount" class="item-amount" :class="item.type">{{ item.amount }}</span>
      </div>
      <van-empty v-if="!loading && items.length === 0" :description="emptyText" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const loading = ref(false)
const items = ref([])

const title = computed(() => route.meta?.title || '列表')
const emptyText = computed(() => '暂无' + (route.meta?.title || '') + '记录')

// 根据路由path映射列表类型
const listType = computed(() => {
  const path = route.path
  if (path.includes('access')) return 'access'
  if (path.includes('gift')) return 'gift'
  if (path.includes('recent')) return 'recent'
  if (path.includes('favorites') || path.includes('favorite')) return 'favorite'
  return 'recent'
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/wap/user/simple-list',
      method: 'get',
      params: { type: listType.value, page: 1, pageSize: 20 }
    })
    items.value = res.data?.list || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.list-page { min-height: 100vh; background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%); }
:deep(.van-nav-bar) { background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6)); }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: #f0f0f0 !important; }
.list-content { padding: 12px; }
.list-item { display: flex; align-items: center; justify-content: space-between; padding: 14px; background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6)); border-radius: 10px; margin-bottom: 8px; }
.item-left { display: flex; align-items: center; gap: 12px; }
.item-icon { font-size: 28px; }
.item-icon-img { width: 36px; height: 36px; border-radius: 8px; object-fit: cover; }
.item-info { display: flex; flex-direction: column; }
.item-name { color: #f2e0b8; font-size: 14px; font-weight: 600; }
.item-desc { color: #8a7a5a; font-size: 12px; margin-top: 2px; }
.item-amount { font-size: 16px; font-weight: 700; }
.item-amount.plus { color: #52c41a; }
.item-amount.minus { color: #ff4d4f; }
.item-amount.neutral { color: #f0d080; }
</style>
