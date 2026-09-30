<template>
  <div class="simple-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="simple-content" v-loading="loading">
      <div class="content-card" v-html="content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const loading = ref(false)
const content = ref('<h3>加载中...</h3>')
const title = computed(() => route.meta?.title || '详情')

const loadDetail = async () => {
  const id = route.params.id
  if (!id) {
    content.value = '<h3>帮助中心</h3><p>如需更多帮助，请联系在线客服。</p>'
    return
  }
  loading.value = true
  try {
    const res = await request({ url: `/wap/help/detail/${id}`, method: 'get' })
    const faq = res.data
    if (faq) {
      content.value = `<h3>${faq.question}</h3><p>${faq.answer}</p>`
    } else {
      content.value = '<h3>帮助中心</h3><p>内容不存在或已删除。</p>'
    }
  } catch (e) {
    content.value = '<h3>帮助中心</h3><p>加载失败，请稍后重试。</p>'
  } finally {
    loading.value = false
  }
}

onMounted(() => loadDetail())
</script>

<style scoped>
.simple-page { min-height: 100vh; background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%); }
:deep(.van-nav-bar) { background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6)); }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: var(--gold-1) !important; }
.simple-content { padding: 16px; }
.content-card { background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6)); border-radius: 12px; padding: 20px; color: var(--gold-1); line-height: 1.8; }
.content-card :deep(h3) { color: #f0d080; margin-top: 0; }
.content-card :deep(strong) { color: #f0d080; }
</style>
