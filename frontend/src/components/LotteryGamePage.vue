<template>
  <div class="lottery-game-page">
    <van-nav-bar :title="gameName" left-arrow @click-left="$router.back()">
      <template #right>
        <span class="nav-balance" @click="goRecharge">余额: {{ formattedBalance }}</span>
      </template>
    </van-nav-bar>

    <!-- 期号与倒计时 -->
    <div class="issue-bar">
      <div class="issue-info">
        <span class="issue-label">当前期号</span>
        <span class="issue-num">{{ currentIssue?.issue || '--' }}</span>
      </div>
      <div class="countdown">
        <van-icon name="clock-o" color="#f0d080" />
        <span class="countdown-text">{{ formatCountdown(countdown) }}</span>
      </div>
    </div>

    <div class="game-content">
      <!-- 玩法Tab -->
      <van-tabs v-model:active="activePlayType" color="#f0d080" sticky offset-top="44px" class="play-tabs">
        <van-tab v-for="play in playTypes" :key="play.id" :title="play.name" :name="play.id">
          <div class="play-content">
            <!-- 选号区 -->
            <div class="betting-panel">
              <div class="panel-title">选择号码</div>
              <div class="number-grid" :class="category">
                <div
                  v-for="n in numberOptions"
                  :key="n"
                  class="number-ball"
                  :class="{ selected: selectedNumbers.includes(n) }"
                  @click="toggleNumber(n)"
                >
                  {{ n }}
                </div>
              </div>
            </div>

            <!-- 走势图 -->
            <LotteryTrendPanel :game-code="gameCode" :category="category" />

            <!-- 历史开奖 -->
            <div class="history-panel">
              <div class="panel-title">最近开奖</div>
              <div class="history-list">
                <div v-for="(item, idx) in historyList.slice(0, 5)" :key="idx" class="history-item">
                  <span class="history-issue">{{ item.issue }}</span>
                  <div class="history-numbers">
                    <span v-for="(num, ni) in getNumberArray(item.numbers)" :key="ni" class="history-ball">
                      {{ num }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-tab>
      </van-tabs>
    </div>

    <!-- 底部投注栏 -->
    <div class="bet-action-bar">
      <div class="bet-info">
        <div class="bet-count">已选 <span class="text-gold">{{ selectedNumbers.length }}</span> 注</div>
        <div class="bet-amount">
          <span>金额</span>
          <van-stepper v-model="betAmount" min="1" max="100000" step="1" integer button-size="24" />
        </div>
      </div>
      <van-button type="primary" class="bet-btn" :disabled="selectedNumbers.length === 0" @click="onBet">
        立即投注
      </van-button>
    </div>

    <!-- 滑块验证码 -->
    <van-popup v-model:show="showCaptcha" position="center" round :style="{ width: '92%', maxWidth: '360px' }">
      <PuzzleCaptcha :show="showCaptcha" @success="onCaptchaSuccess" @fail="showCaptcha = false" @close="showCaptcha = false" />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { lotteryApi } from '@/api/lottery'
import LotteryTrendPanel from '@/components/LotteryTrendPanel.vue'
import PuzzleCaptcha from '@/components/PuzzleCaptcha.vue'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const props = defineProps({
  category: { type: String, required: true } // pk10 / ssc / lhc / pc28
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const userStore = useUserStore()

const gameCode = computed(() => route.params.code || '')
const gameName = ref('彩票游戏')
const currentIssue = ref(null)
const countdown = ref(180)
const playTypes = ref([])
const activePlayType = ref('')
const selectedNumbers = ref([])
const betAmount = ref(10)
const historyList = ref([])
const showCaptcha = ref(false)
let timer = null

const formattedBalance = computed(() => userStore.formattedBalance)

const numberOptions = computed(() => {
  switch (props.category) {
    case 'pk10': return Array.from({ length: 10 }, (_, i) => i + 1)
    case 'ssc': return Array.from({ length: 10 }, (_, i) => i)
    case 'pc28': return Array.from({ length: 10 }, (_, i) => i)
    case 'lhc': return Array.from({ length: 49 }, (_, i) => i + 1)
    default: return Array.from({ length: 10 }, (_, i) => i + 1)
  }
})

function getNumberArray(numbers) {
  if (Array.isArray(numbers)) return numbers
  if (typeof numbers === 'string') return numbers.split(',').map(n => n.trim()).filter(Boolean)
  if (numbers?.red) return [...numbers.red, `+${numbers.blue}`]
  return []
}

function formatCountdown(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function toggleNumber(n) {
  const idx = selectedNumbers.value.indexOf(n)
  if (idx > -1) {
    selectedNumbers.value.splice(idx, 1)
  } else {
    selectedNumbers.value.push(n)
  }
}

// 彩种 code 映射（页面 category → 真实 lotteryCode）
const CODE_MAP = {
  pk10: 'jspk10',
  ssc: 'jsssc',
  pc28: 'jsdd',
  lhc: 'happy8lhc'
}

async function loadGameInfo() {
  try {
    const realCode = CODE_MAP[props.category] || gameCode.value
    const drawData = await lotteryApi.getDrawInfo([realCode]).catch(() => ({}))
    const info = drawData?.[realCode] || {}

    if (info?.lotteryName) gameName.value = info.lotteryName

    // 转换为页面期望的结构
    currentIssue.value = {
      issue: info.currentPeriod || '--',
      lastIssue: info.lastPeriod || '--',
      lastNumbers: info.lastNumbers || '',
      countDown: info.closeCountdown || info.drawCountdown || 180
    }
    countdown.value = info.closeCountdown || info.drawCountdown || 180

    // 历史开奖（用最近一期 + 模拟历史，真实历史需单独接口）
    if (info.lastPeriod && info.lastNumbers) {
      historyList.value = [{
        issue: info.lastPeriod,
        numbers: info.lastNumbers
      }]
    }

    // 玩法配置（静态，按彩种生成）
    playTypes.value = getDefaultPlayTypes(props.category)
    if (playTypes.value.length) activePlayType.value = playTypes.value[0].id
  } catch (e) {
    // 静默
  }
}

function getDefaultPlayTypes(category) {
  const map = {
    pk10: [{ id: 'digit', name: '冠亚和' }, { id: 'top1', name: '第一名' }],
    ssc: [{ id: 'digit', name: '五星直选' }, { id: 'sum', name: '总和' }],
    pc28: [{ id: 'sum', name: '和值' }, { id: 'bigsmall', name: '大小' }, { id: 'parity', name: '单双' }],
    lhc: [{ id: 'special', name: '特码' }, { id: 'zodiac', name: '生肖' }]
  }
  return map[category] || [{ id: 'default', name: '默认玩法' }]
}

function startCountdown() {
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      countdown.value = 180
      loadGameInfo()
    }
  }, 1000)
}

function onBet() {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', route.fullPath)
    return
  }
  if (selectedNumbers.value.length === 0) {
    showFailToast('请先选择号码')
    return
  }
  const totalAmount = selectedNumbers.value.length * betAmount.value
  showConfirmDialog({
    title: '确认投注',
    message: `共 ${selectedNumbers.value.length} 注，每注 ${betAmount.value} 元，合计 ${totalAmount} 元`,
    confirmButtonText: '确认投注',
    cancelButtonText: '取消'
  }).then(() => {
    showCaptcha.value = true
  }).catch(() => {})
}

async function onCaptchaSuccess() {
  showCaptcha.value = false
  try {
    const totalAmount = selectedNumbers.value.length * betAmount.value
    await lotteryApi.submitBet({
      gameCode: gameCode.value,
      issue: currentIssue.value?.issue,
      playType: activePlayType.value,
      numbers: selectedNumbers.value,
      amount: betAmount.value,
      totalAmount
    })
    showSuccessToast('投注成功')
    selectedNumbers.value = []
    userStore.fetchBalance()
  } catch (e) {
    showFailToast(e.message || '投注失败')
  }
}

function goRecharge() {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', '/user/recharge')
    return
  }
  router.push('/user/recharge')
}

onMounted(() => {
  loadGameInfo()
  startCountdown()
  if (authStore.isLoggedIn) {
    userStore.fetchBalance()
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.lottery-game-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  padding-bottom: 70px;
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f2e0b8 !important;
}
.nav-balance {
  font-size: 13px;
  color: #f0d080;
  cursor: pointer;
}
.issue-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: rgba(0,0,0,0.2);
  border-bottom: 1px solid #222;
}
.issue-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.issue-label {
  font-size: 12px;
  color: #8a7a5a;
}
.issue-num {
  font-size: 15px;
  color: #f0d080;
  font-weight: 600;
}
.countdown {
  display: flex;
  align-items: center;
  gap: 4px;
}
.countdown-text {
  font-size: 14px;
  color: #f0d080;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.game-content {
  padding: 0 0 12px;
}
:deep(.play-tabs .van-tabs__nav) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.play-tabs .van-tab) {
  color: #8a7a5a;
}
:deep(.play-tabs .van-tab--active) {
  color: #f0d080;
}
.play-content {
  padding: 12px;
}
.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #f2e0b8;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
.panel-title::before {
  content: '';
  width: 3px;
  height: 12px;
  background: #f0d080;
  border-radius: 2px;
  margin-right: 6px;
}
.betting-panel {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}
.number-grid {
  display: grid;
  gap: 8px;
}
.number-grid.pk10, .number-grid.ssc, .number-grid.pc28 {
  grid-template-columns: repeat(5, 1fr);
}
.number-grid.lhc {
  grid-template-columns: repeat(7, 1fr);
}
.number-ball {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 50%;
  font-size: 14px;
  color: #d0c4a8;
  cursor: pointer;
  transition: all 0.15s;
  border: 2px solid transparent;
}
.number-ball.selected {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  color: #3a2610;
  font-weight: 700;
  border-color: #fff;
  transform: scale(1.05);
}
.number-ball:active {
  transform: scale(0.95);
}
.history-panel {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 12px;
}
.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.history-issue {
  font-size: 11px;
  color: #6a5a40;
  min-width: 80px;
}
.history-numbers {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.history-ball {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 50%;
  font-size: 10px;
  color: #f0d080;
}
.bet-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-top: 1px solid rgba(255,255,255,0.1);
  z-index: 100;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
}
.bet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.bet-count {
  font-size: 12px;
  color: #8a7a5a;
}
.bet-amount {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #8a7a5a;
}
:deep(.bet-amount .van-stepper) {
  background: rgba(255,255,255,0.06);
  border-radius: 12px;
}
:deep(.bet-amount .van-stepper__minus), :deep(.bet-amount .van-stepper__plus) {
  background: rgba(255,255,255,0.1);
  color: #f0d080;
}
:deep(.bet-amount .van-stepper__input) {
  color: #f2e0b8;
  background: transparent;
}
.bet-btn {
  flex-shrink: 0;
  width: 120px;
  height: 42px !important;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
}
.bet-btn:disabled {
  opacity: 0.4;
}
</style>
