<template>
  <div class="chat-page">
    <van-nav-bar title="在线客服" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="more-o" size="20" />
      </template>
    </van-nav-bar>

    <div class="chat-content" ref="chatContent">
      <!-- 系统提示 -->
      <div class="system-tip">
        <span>客服小助手已接入，为您提供7x24小时服务</span>
      </div>

      <!-- 消息列表 -->
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="message-row"
        :class="msg.role"
      >
        <div v-if="msg.role === 'agent'" class="avatar agent-avatar">
          <van-icon name="service" size="20" color="#fff" />
        </div>
        <div class="message-bubble" :class="msg.role">
          <p>{{ msg.content }}</p>
          <span class="message-time">{{ msg.time }}</span>
        </div>
        <div v-if="msg.role === 'user'" class="avatar user-avatar">
          {{ userInitial }}
        </div>
      </div>

      <!-- 正在输入 -->
      <div v-if="agentTyping" class="message-row agent">
        <div class="avatar agent-avatar">
          <van-icon name="service" size="20" color="#fff" />
        </div>
        <div class="message-bubble agent typing">
          <span class="typing-dots">
            <span></span><span></span><span></span>
          </span>
        </div>
      </div>
    </div>

    <!-- 快捷问题 -->
    <div class="quick-questions" v-if="messages.length <= 2">
      <span class="quick-title">常见问题</span>
      <div class="quick-list">
        <span
          v-for="q in quickQuestions"
          :key="q"
          class="quick-item"
          @click="sendQuickQuestion(q)"
        >
          {{ q }}
        </span>
      </div>
    </div>

    <!-- 输入栏 -->
    <div class="chat-input-bar">
      <input
        v-model="inputText"
        class="chat-input"
        placeholder="请输入您的问题..."
        @keyup.enter="sendMessage"
      />
      <van-button
        type="primary"
        size="small"
        class="send-btn"
        :disabled="!inputText.trim()"
        @click="sendMessage"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const chatContent = ref(null)
const inputText = ref('')
const agentTyping = ref(false)

const userInitial = computed(() => {
  return (authStore.userInfo?.nickname || 'U')[0].toUpperCase()
})

const messages = ref([
  {
    role: 'agent',
    content: '您好！欢迎咨询，请问有什么可以帮您？',
    time: dayjs().format('HH:mm')
  }
])

const quickQuestions = [
  '如何充值？',
  '如何提现？',
  '忘记密码怎么办？',
  '优惠活动有哪些？'
]

const autoReplies = {
  '如何充值？': '充值流程：1. 进入个人中心 → 点击充值 2. 选择充值方式（支持USDT/银行卡/支付宝/微信等）3. 输入金额并提交 4. 完成支付后自动到账。如有问题请提供订单号。',
  '如何提现？': '提现流程：1. 进入个人中心 → 点击提现 2. 选择提现方式（银行卡/USDT）3. 绑定收款账户 4. 输入提现金额和资金密码 5. 提交后10-30分钟内到账。每日有提现限额。',
  '忘记密码怎么办？': '如忘记登录密码，请联系在线客服提供注册手机号和身份信息，我们将协助您重置密码。为保障账户安全，密码重置需要验证身份。',
  '优惠活动有哪些？': '当前活动包括：1. 新人注册送88元彩金 2. 首充100%赠送 3. 每日签到领红包 4. VIP专属返水 5. 邀请好友得佣金。详情请查看"优惠活动"页面。'
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContent.value) {
      chatContent.value.scrollTop = chatContent.value.scrollHeight
    }
  })
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return

  messages.value.push({
    role: 'user',
    content: text,
    time: dayjs().format('HH:mm')
  })
  inputText.value = ''
  scrollToBottom()

  // 模拟客服回复
  agentTyping.value = true
  setTimeout(() => {
    agentTyping.value = false
    const reply = autoReplies[text] || '感谢您的咨询，您的问题已记录，客服人员将尽快为您处理。如需人工服务，请稍候。'
    messages.value.push({
      role: 'agent',
      content: reply,
      time: dayjs().format('HH:mm')
    })
    scrollToBottom()
  }, 1000 + Math.random() * 1000)
}

function sendQuickQuestion(q) {
  inputText.value = q
  sendMessage()
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-page {
  height: 100vh;
  background: var(--app-bg);
  display: flex;
  flex-direction: column;
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: var(--gold-1) !important;
}
.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
}
.system-tip {
  text-align: center;
  margin-bottom: 16px;
}
.system-tip span {
  font-size: 11px;
  color: #6a5a40;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
  padding: 4px 12px;
  border-radius: 10px;
}
.message-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: flex-start;
}
.message-row.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
}
.agent-avatar {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
}
.user-avatar {
  background: rgba(255,255,255,0.1);
  color: #f0d080;
}
.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
}
.message-bubble.agent {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
  border: 1px solid rgba(255,255,255,0.12);
  border-top-left-radius: 4px;
}
.message-bubble.user {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%);
  border-top-right-radius: 4px;
}
.message-bubble p {
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
  word-break: break-word;
}
.message-bubble.agent p {
  color: var(--gold-1);
}
.message-bubble.user p {
  color: #3a2610;
}
.message-time {
  display: block;
  font-size: 10px;
  margin-top: 4px;
  opacity: 0.6;
}
.message-bubble.typing {
  padding: 14px;
}
.typing-dots {
  display: flex;
  gap: 4px;
}
.typing-dots span {
  width: 6px;
  height: 6px;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  animation: typing 1.4s infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}
.quick-questions {
  padding: 8px 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
}
.quick-title {
  font-size: 11px;
  color: #6a5a40;
  display: block;
  margin-bottom: 8px;
}
.quick-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.quick-item {
  font-size: 12px;
  color: #f0d080;
  background: rgba(240, 208, 128, 0.1);
  border: 1px solid rgba(240, 208, 128, 0.3);
  padding: 6px 12px;
  border-radius: 14px;
  cursor: pointer;
}
.chat-input-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg);
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
}
.chat-input {
  flex: 1;
  height: 38px;
  background: rgba(255,255,255,0.06);
  border: none;
  border-radius: 19px;
  padding: 0 16px;
  font-size: 14px;
  color: var(--gold-1);
  outline: none;
}
.chat-input::placeholder {
  color: #6a5a40;
}
.send-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  border-radius: 16px !important;
  height: 38px !important;
}
.send-btn:disabled {
  opacity: 0.4;
}
</style>
