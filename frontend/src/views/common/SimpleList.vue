<template>
  <div class="list-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="list-content">
      <div v-for="item in items" :key="item.id" class="list-item">
        <div class="item-left">
          <span class="item-icon">{{ item.icon }}</span>
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-desc">{{ item.desc }}</span>
          </div>
        </div>
        <span class="item-amount" :class="item.type">{{ item.amount }}</span>
      </div>
      <van-empty v-if="items.length === 0" :description="emptyText" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta?.title || '列表')
const emptyText = computed(() => '暂无' + (route.meta?.title || '') + '记录')

const items = ref([])
</script>

<style scoped>
.list-page { min-height: 100vh; background: #0d0d0d; }
:deep(.van-nav-bar) { background: #1a1a1a; }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: #f0f0f0 !important; }
.list-content { padding: 12px; }
.list-item { display: flex; align-items: center; justify-content: space-between; padding: 14px; background: #1a1a1a; border-radius: 10px; margin-bottom: 8px; }
.item-left { display: flex; align-items: center; gap: 12px; }
.item-icon { font-size: 28px; }
.item-info { display: flex; flex-direction: column; }
.item-name { color: #f0f0f0; font-size: 14px; font-weight: 600; }
.item-desc { color: #888; font-size: 12px; margin-top: 2px; }
.item-amount { font-size: 16px; font-weight: 700; }
.item-amount.plus { color: #52c41a; }
.item-amount.minus { color: #ff4d4f; }
.item-amount.neutral { color: #e8b860; }
</style>
