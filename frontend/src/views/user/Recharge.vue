<template>
  <div class="recharge-page">
    <van-nav-bar title="充值" left-arrow @click-left="$router.back()" />
    <div class="recharge-content">
      <!-- 余额提示 -->
      <div class="balance-tip">
        <span>当前余额：¥{{ userStore.formattedBalance }}</span>
      </div>

      <!-- 优惠活动横幅 -->
      <div class="promo-banner" @click="$router.push('/promo')">
        <div class="promo-banner-item">
          <div class="promo-banner-icon"><GoldIcon name="gift" :size="20" /></div>
          <div class="promo-banner-text">
            <span class="promo-banner-title">首存奖励</span>
            <span class="promo-banner-desc">首次存款享100%加成</span>
          </div>
        </div>
        <div class="promo-banner-divider"></div>
        <div class="promo-banner-item">
          <div class="promo-banner-icon"><GoldIcon name="coin" :size="20" /></div>
          <div class="promo-banner-text">
            <span class="promo-banner-title">每日返利</span>
            <span class="promo-banner-desc">流水越高返利越多</span>
          </div>
        </div>
        <GoldIcon name="arrow" :size="14" color="var(--text-hint)" class="promo-banner-arrow" />
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
            <div class="method-icon">
              <img v-if="method.icon && (method.icon.startsWith('/') || method.icon.startsWith('http'))" :src="method.icon" :alt="method.name" class="method-icon-img" />
              <span v-else>{{ method.icon || '💰' }}</span>
            </div>
            <div class="method-info">
              <span class="method-name">{{ method.name }}</span>
              <span class="method-range">{{ method.minAmount }}-{{ method.maxAmount }}元</span>
            </div>
            <van-icon v-if="selectedMethod === method.id" name="checked" color="#f0d080" size="18" />
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
          <van-button block type="primary" class="pay-confirm-btn" @click="onPaySuccess">
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
    // 默认支付方式（带真实加密货币图标）
    methods.value = [
      { id: 1, name: 'USDT-TRC20', icon: '/assets/crypto/usdt.svg', minAmount: 100, maxAmount: 50000, status: 'online' },
      { id: 2, name: 'USDT-ERC20', icon: '/assets/crypto/usdt.svg', minAmount: 100, maxAmount: 50000, status: 'online' },
      { id: 3, name: 'BTC', icon: '/assets/crypto/btc.svg', minAmount: 500, maxAmount: 100000, status: 'online' },
      { id: 4, name: 'ETH', icon: '/assets/crypto/eth.svg', minAmount: 300, maxAmount: 80000, status: 'online' },
      { id: 5, name: 'TRX', icon: '/assets/crypto/trx.svg', minAmount: 100, maxAmount: 30000, status: 'online' },
    ]
    selectedMethod.value = 1
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
  background: var(--app-bg);
  padding-bottom: 100px;
}
:deep(.van-nav-bar) {
  background: transparent;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
.recharge-content {
  padding: 12px;
}
.balance-tip {
  background: var(--card-bg);
  border-radius: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  padding: 14px 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--gold-1);
}
/* 优惠活动横幅 */
.promo-banner {
  display: flex;
  align-items: center;
  background: var(--card-bg);
  border-radius: 14px;
  border: 1px solid rgba(212,168,75,0.25);
  padding: 12px 14px;
  margin-bottom: 16px;
}
.promo-banner-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.promo-banner-icon {
  font-size: 22px;
}
.promo-banner-text {
  display: flex;
  flex-direction: column;
}
.promo-banner-title {
  font-size: 13px;
  font-weight: 600;
  color: #f0d080;
}
.promo-banner-desc {
  font-size: 10px;
  color: var(--text-hint);
  margin-top: 2px;
}
.promo-banner-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.1);
  margin: 0 10px;
}
.promo-banner-arrow {
  flex-shrink: 0;
}
.section {
  margin-bottom: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gold-1);
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
  background: var(--card-bg);
  border-radius: 12px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  cursor: pointer;
}
.method-item.active {
  border-color: #d4a84b;
  background: var(--card-bg);
}
.method-item.disabled {
  opacity: 0.4;
}
.method-icon {
  font-size: 24px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.06);
  border-radius: 10px;
  flex-shrink: 0;
}
.method-icon-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}
.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.method-name {
  font-size: 14px;
  color: var(--gold-1);
  font-weight: 500;
}
.method-range {
  font-size: 11px;
  color: var(--text-hint);
}
.amount-quick {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}
.amount-chip {
  background: var(--card-bg);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
  font-size: 14px;
  color: var(--gold-1);
  cursor: pointer;
}
.amount-chip.active {
  border-color: #d4a84b;
  color: #f0d080;
  background: var(--card-bg);
}
.amount-input-wrap {
  display: flex;
  align-items: center;
  background: var(--card-bg);
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid rgba(255,255,255,0.12);
}
.amount-currency {
  font-size: 18px;
  color: #ffffff;
  margin-right: 8px;
  font-weight: 600;
}
.amount-input {
  flex: 1;
  height: 48px;
  background: transparent;
  border: none;
  outline: none;
  font-size: 20px;
  color: #ffffff;
  font-weight: 600;
}
.amount-input::placeholder {
  color: var(--text-hint);
  font-size: 14px;
}
.amount-tip {
  font-size: 11px;
  color: var(--text-hint);
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
  background: linear-gradient(180deg, rgba(13,10,6,0.9), var(--app-bg));
  border-top: 1px solid rgba(255,255,255,0.08);
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
.submit-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  height: 46px !important;
  border-radius: 10px !important;
}
.submit-btn:disabled {
  opacity: 0.4;
}
.pay-popup {
  padding: 20px;
  background: var(--app-bg-2);
  border-radius: 16px 16px 0 0;
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
  color: var(--gold-1);
}
.pay-amount {
  text-align: center;
  margin-bottom: 20px;
}
.pay-label {
  display: block;
  font-size: 13px;
  color: var(--text-hint);
  margin-bottom: 8px;
}
.pay-value {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
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
  color: var(--text-hint);
  margin-top: 12px;
}
.pay-info-text {
  text-align: center;
  padding: 20px;
}
.pay-info-text p {
  font-size: 13px;
  color: var(--text-hint);
  margin: 6px 0;
}
.pay-actions {
  margin-top: 16px;
}
.pay-confirm-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  font-weight: 600 !important;
  height: 46px !important;
  border-radius: 10px !important;
}
</style>
