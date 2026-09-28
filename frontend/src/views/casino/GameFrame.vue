<template>
  <div class="game-frame-page">
    <van-nav-bar :title="gameTitle" left-arrow @click-left="goBack">
      <template #right>
        <van-icon name="replay" size="18" @click="reloadFrame" />
      </template>
    </van-nav-bar>
    <div class="frame-container">
      <div v-if="loading" class="frame-loading">
        <van-loading size="32px" color="#f0d080" vertical>
          游戏加载中...
        </van-loading>
      </div>
      <iframe
        v-show="!loading"
        ref="gameFrame"
        :src="gameUrl"
        class="game-iframe"
        frameborder="0"
        @load="onFrameLoad"
      ></iframe>
      <div v-if="loadError" class="frame-error">
        <van-icon name="warning-o" size="48" color="#ee0a24" />
        <p>游戏加载失败，请稍后重试</p>
        <van-button size="small" type="primary" @click="reloadFrame">重新加载</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { casinoApi } from '@/api/casino'

const route = useRoute()
const router = useRouter()

const gameTitle = ref(route.query.title || '游戏')
const gameUrl = ref('')
const loading = ref(true)
const loadError = ref(false)
const gameFrame = ref(null)

async function loadGame() {
  loading.value = true
  loadError.value = false
  try {
    const platform = route.query.platform || route.query.venue || 'pg'
    const gameId = route.query.gameId || route.query.gameCode || ''
    const data = await casinoApi.enterGame(platform, gameId)
    // 兼容多种返回结构
    gameUrl.value = data?.gameUrl || data?.url || data?.game_url || data?.enterUrl || 'about:blank'
    if (!gameUrl.value || gameUrl.value === 'about:blank') {
      loadError.value = true
    }
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function onFrameLoad() {
  loading.value = false
}

function reloadFrame() {
  if (gameFrame.value) {
    loading.value = true
    gameFrame.value.src = gameUrl.value
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadGame()
})
</script>

<style scoped>
.game-frame-page {
  height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: #f2e0b8 !important;
}
.frame-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}
.game-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
.frame-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  gap: 12px;
}
.frame-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  gap: 16px;
}
.frame-error p {
  color: #8a7a5a;
  font-size: 14px;
  margin: 0;
}
</style>
