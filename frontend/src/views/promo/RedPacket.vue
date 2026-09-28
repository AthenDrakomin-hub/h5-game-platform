<template>
  <div class="redpacket-page">
    <van-nav-bar title="抢红包" left-arrow @click-left="$router.back()" />
    <div class="redpacket-content">
      <div class="redpacket-header">
        <div class="redpacket-icon">🧧</div>
        <h2>每日抢红包</h2>
        <p>每天 {{ startTime }} - {{ endTime }} 准时开抢</p>
      </div>

      <div class="redpacket-info">
        <div class="info-item">
          <span class="label">今日奖池</span>
          <span class="value">¥{{ prizePool }}</span>
        </div>
        <div class="info-item">
          <span class="label">已抢人数</span>
          <span class="value">{{ joinedCount }}</span>
        </div>
        <div class="info-item">
          <span class="label">我的次数</span>
          <span class="value">{{ myCount }}/{{ maxCount }}</span>
        </div>
      </div>

      <van-button round block type="primary" class="grab-btn" :disabled="!canGrab" @click="grabRedPacket">
        {{ canGrab ? '立即抢红包' : '今日已抢完' }}
      </van-button>

      <div class="record-section">
        <h3>抢包记录</h3>
        <div v-for="r in records" :key="r.id" class="record-item">
          <span class="record-user">{{ r.username }}</span>
          <span class="record-amount">¥{{ r.amount }}</span>
          <span class="record-time">{{ r.time }}</span>
        </div>
        <van-empty v-if="records.length === 0" description="暂无抢包记录" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import request from '@/api/request'

const startTime = ref('10:00')
const endTime = ref('22:00')
const prizePool = ref('888.00')
const joinedCount = ref(128)
const myCount = ref(0)
const maxCount = ref(3)
const records = ref([])
const loading = ref(false)

const canGrab = computed(() => myCount.value < maxCount.value)

const loadInfo = async () => {
  try {
    const res = await request({ url: '/wap/red-packet/info', method: 'get' })
    const d = res.data || {}
    startTime.value = d.startTime || '10:00'
    endTime.value = d.endTime || '22:00'
    prizePool.value = d.prizePool || '888.00'
    maxCount.value = d.maxCount || 3
    myCount.value = d.myCount || 0
  } catch (e) { /* 降级用默认值 */ }
}

const loadRecords = async () => {
  try {
    const res = await request({ url: '/wap/red-packet/records', method: 'get', params: { page: 1, pageSize: 20 } })
    records.value = res.data?.list || []
  } catch (e) { records.value = [] }
}

const grabRedPacket = async () => {
  if (loading.value || !canGrab.value) return
  loading.value = true
  try {
    const res = await request({ url: '/wap/red-packet/grab', method: 'post' })
    const amount = res.data?.amount || '0.00'
    records.value.unshift({
      id: Date.now(),
      username: '我',
      amount: amount,
      time: new Date().toLocaleString()
    })
    myCount.value++
    showToast(`抢到 ¥${amount}`)
  } catch (e) {
    showToast(e.response?.data?.message || '抢包失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadInfo(); loadRecords() })
</script>

<style scoped>
.redpacket-page { min-height: 100vh; background: linear-gradient(180deg, #8b0000, #0d0d0d); }
:deep(.van-nav-bar) { background: transparent; }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: #fff !important; }
.redpacket-content { padding: 20px; }
.redpacket-header { text-align: center; padding: 30px 0; }
.redpacket-icon { font-size: 80px; margin-bottom: 16px; }
.redpacket-header h2 { color: #ffd700; margin: 0 0 8px; }
.redpacket-header p { color: #ffcccc; margin: 0; }
.redpacket-info { background: rgba(255,255,255,0.1); border-radius: 12px; padding: 20px; margin-bottom: 24px; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; }
.info-item .label { color: #ffcccc; }
.info-item .value { color: #ffd700; font-weight: 700; }
.grab-btn { background: linear-gradient(135deg, #ffd700, #ff8c00) !important; border: none !important; color: #8b0000 !important; font-weight: 700; height: 48px; font-size: 16px; }
.record-section { margin-top: 30px; }
.record-section h3 { color: #ffd700; margin-bottom: 12px; }
.record-item { display: flex; justify-content: space-between; padding: 10px; background: rgba(255,255,255,0.05); border-radius: 8px; margin-bottom: 8px; }
.record-user { color: #ccc; }
.record-amount { color: #ffd700; font-weight: 600; }
.record-time { color: #666; font-size: 12px; }
</style>
