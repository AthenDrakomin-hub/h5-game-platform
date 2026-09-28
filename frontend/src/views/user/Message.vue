<template>
  <div class="message-page">
    <van-nav-bar title="消息中心" left-arrow @click-left="$router.back()" />
    <div class="message-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ unread: !msg.isRead }"
          @click="readMessage(msg)"
        >
          <div class="msg-icon" :class="msg.type">
            {{ typeIcon(msg.type) }}
          </div>
          <div class="msg-content">
            <div class="msg-title-row">
              <span class="msg-title">{{ msg.title }}</span>
              <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <p class="msg-desc">{{ msg.content }}</p>
          </div>
          <span v-if="!msg.isRead" class="unread-dot"></span>
        </div>
      </div>
      <div v-if="!loading && messages.length === 0" class="empty-state">
        <van-empty description="暂无消息" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const messages = ref([])
const loading = ref(false)

function typeIcon(type) {
  const map = { recharge: '💰', win: '🎉', system: '📢', promo: '🎁' }
  return map[type] || '💬'
}

function formatTime(time) {
  return dayjs(time).format('MM-DD HH:mm')
}

async function readMessage(msg) {
  if (!msg.isRead) {
    try {
      await userApi.readMessage(msg.id)
      msg.isRead = true
    } catch (e) {
      // 静默
    }
  }
}

async function loadMessages() {
  loading.value = true
  try {
    const data = await userApi.getMessages({ page: 1, pageSize: 50 })
    messages.value = data.list || []
  } catch (e) {
    messages.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
.message-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.message-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  cursor: pointer;
  position: relative;
}
.message-item.unread {
  border-color: rgba(232, 184, 96, 0.3);
  background: rgba(232, 184, 96, 0.03);
}
.msg-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(255,255,255,0.06);
  flex-shrink: 0;
}
.msg-content {
  flex: 1;
  min-width: 0;
}
.msg-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.msg-title {
  font-size: 14px;
  font-weight: 600;
  color: #d0c4a8;
}
.msg-time {
  font-size: 11px;
  color: #6a5a40;
  flex-shrink: 0;
  margin-left: 8px;
}
.msg-desc {
  font-size: 12px;
  color: #8a7a5a;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.unread-dot {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 8px;
  height: 8px;
  background: #ee0a24;
  border-radius: 50%;
}
.empty-state {
  padding: 40px 20px;
}
:deep(.van-empty__description) {
  color: #6a5a40;
}
</style>
