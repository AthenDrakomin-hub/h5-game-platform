<template>
  <div class="recharge-page">
    <van-nav-bar title="充值" left-arrow @click-left="$router.back()" />
    <div class="recharge-content">
      <!-- 余额提示 -->
      <div class="balance-tip">
        <span>当前余额：¥{{ userStore.formattedBalance }}</span>
      </div>

      <!-- 充值方式 -->
      <div class="section">
        <div class="section-title">选择充值方式</div>
        <div class="method-list">
          <div
            v-for="method in methods"
            :key="method.id"
            class="method-item"
            :class="{ active: selectedMethod === method.id, disabled: method.status !== 'online' }"
            @click="selectMethod(method)"
          >
            <span class="method-icon">{{ method.icon }}</span>
            <div class="method-info">
              <span class="method-name">{{ method.name }}</span>
              <span class="method-range">{{ method.minAmount }}-{{ method.maxAmount }}元</span>
            </div>
            <van-icon v-if="selectedMethod === method.id" name="checked" color="#e8b860" size="18" />
          </div>
        </div>
      </div>

      <!-- 充值金额 -->
      <div class="section">
        <div class="section-title">充值金额</div>
        <div class="amount-quick">
          <div
            v-for="amt in quickAmounts"
            :key="amt"
            class="amount-chip"
            :class="{ active: amount === amt }"
            @click="amount = amt"
          >
            ¥{{ amt }}
          </div>
        </div>
        <div class="amount-input-wrap">
          <span class="amount-currency">¥</span>
          <input
            v-model.number="amount"
            type="number"
            class="amount-input"
            placeholder="请输入充值金额"
          />
        </div>
        <div class="amount-tip">最低充值 {{ currentMethod?.minAmount || 100 }} 元</div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <van-button
          block
          round
          type="primary"
          class="submit-btn"
          :loading="submitting"
          :disabled="!amount || !selectedMethod"
          @click="onSubmit"
        >
          立即充值 ¥{{ amount || 0 }}
        </van-button>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <van-popup v-model:show="showPayPopup" position="bottom" round :style="{ maxHeight: '80vh' }">
      <div class="pay-popup">
        <div class="pay-header">
          <span class="pay-title">支付订单</span>
          <van-icon name="cross" size="20" @click="showPayPopup = false" />
        </div>
        <div class="pay-amount">
          <span class="pay-label">支付金额</span>
          <span class="pay-value">¥{{ amount }}</span>
        </div>
        <div class="pay-qr" v-if="payInfo?.qrCode">
          <img :src="payInfo.qrCode" alt="支付二维码" class="qr-img" />
          <p class="qr-tip">请使用{{ currentMethod?.name }}扫码支付</p>
        </div>
        <div class="pay-info-text" v-else>
          <p>订单号：{{ payInfo?.orderNo }}</p>
          <p>请在 {{ expireMinutes }} 分钟内完成支付</p>
        </div>
        <div class="pay-actions">
          <van-button block round type="primary" class="pay-confirm-btn" @click="onPaySuccess">
            我已完成支付
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const methods = ref([])
const selectedMethod = ref('')
const amount = ref(null)
const submitting = ref(false)
const showPayPopup = ref(false)
const payInfo = ref(null)
const expireMinutes = ref(30)

const quickAmounts = [100, 500, 1000, 5000, 10000, 50000]

const currentMethod = computed(() => methods.value.find(m => m.id === selectedMethod.value))

function selectMethod(method) {
  if (method.status !== 'online') {
    showFailToast('该方式暂不可用')
    return
  }
  selectedMethod.value = method.id
}

async function loadMethods() {
  try {
    const data = await userApi.getRechargeMethods()
    methods.value = data
    if (data.length) selectedMethod.value = data[0].id
  } catch (e) {
    methods.value = []
  }
}

async function onSubmit() {
  if (!amount.value || amount.value <= 0) {
    showFailToast('请输入充值金额')
    return
  }
  if (currentMethod.value && amount.value < currentMethod.value.minAmount) {
    showFailToast(`最低充值 ${currentMethod.value.minAmount} 元`)
    return
  }
  submitting.value = true
  try {
    const data = await userApi.createRechargeOrder({
      method: selectedMethod.value,
      amount: amount.value
    })
    payInfo.value = data
    showPayPopup.value = true
  } catch (e) {
    showFailToast(e.message || '创建订单失败')
  } finally {
    submitting.value = false
  }
}

function onPaySuccess() {
  showPayPopup.value = false
  showSuccessToast('充值成功')
  userStore.fetchBalance()
  router.push('/user/orders')
}

onMounted(() => {
  loadMethods()
  userStore.fetchBalance()
})
</script>

<style scoped>
.recharge-page {
  min-height: 100vh;
  background: #0d0d0d;
  padding-bottom: 100px;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
.recharge-content {
  padding: 12px;
}
.balance-tip {
  background: #1a1a1a;
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #e8b860;
}
.section {
  margin-bottom: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #f0f0f0;
  margin-bottom: 10px;
  padding-left: 4px;
}
.method-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.method-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 14px;
  border: 2px solid transparent;
  cursor: pointer;
}
.method-item.active {
  border-color: #e8b860;
  background: rgba(232, 184, 96, 0.05);
}
.method-item.disabled {
  opacity: 0.4;
}
.method-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #222;
  border-radius: 8px;
  flex-shrink: 0;
}
.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.method-name {
  font-size: 14px;
  color: #e0e0e0;
  font-weight: 500;
}
.method-range {
  font-size: 11px;
  color: #666;
}
.amount-quick {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}
.amount-chip {
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  font-size: 14px;
  color: #ccc;
  cursor: pointer;
}
.amount-chip.active {
  border-color: #e8b860;
  color: #e8b860;
  background: rgba(232, 184, 96, 0.05);
}
.amount-input-wrap {
  display: flex;
  align-items: center;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid #2a2a2a;
}
.amount-currency {
  font-size: 18px;
  color: #e8b860;
  margin-right: 8px;
}
.amount-input {
  flex: 1;
  height: 48px;
  background: transparent;
  border: none;
  outline: none;
  font-size: 18px;
  color: #f0f0f0;
}
.amount-input::placeholder {
  color: #555;
  font-size: 14px;
}
.amount-tip {
  font-size: 11px;
  color: #666;
  margin-top: 8px;
  padding-left: 4px;
}
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  padding: 12px 16px;
  background: #1a1a1a;
  border-top: 1px solid #333;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
.submit-btn {
  background: linear-gradient(135deg, #e8b860, #c99a3e) !important;
  border: none !important;
  color: #1a1a1a !important;
  font-weight: 600 !important;
  height: 46px !important;
}
.submit-btn:disabled {
  opacity: 0.4;
}
.pay-popup {
  padding: 20px;
}
.pay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.pay-title {
  font-size: 18px;
  font-weight: 600;
  color: #f0f0f0;
}
.pay-amount {
  text-align: center;
  margin-bottom: 20px;
}
.pay-label {
  display: block;
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
}
.pay-value {
  font-size: 36px;
  font-weight: 700;
  color: #e8b860;
}
.pay-qr {
  text-align: center;
  margin-bottom: 20px;
}
.qr-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
}
.qr-tip {
  font-size: 13px;
  color: #888;
  margin-top: 12px;
}
.pay-info-text {
  text-align: center;
  padding: 20px;
}
.pay-info-text p {
  font-size: 13px;
  color: #aaa;
  margin: 6px 0;
}
.pay-actions {
  margin-top: 16px;
}
.pay-confirm-btn {
  background: linear-gradient(135deg, #e8b860, #c99a3e) !important;
  border: none !important;
  color: #1a1a1a !important;
  font-weight: 600 !important;
  height: 46px !important;
}
</style>
