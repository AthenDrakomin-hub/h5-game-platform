<template>
  <div class="withdraw-page">
    <van-nav-bar title="提现" left-arrow @click-left="$router.back()" />
    <div class="withdraw-content">
      <!-- 余额 -->
      <div class="balance-card">
        <div class="balance-info">
          <span class="balance-label">可提现余额</span>
          <span class="balance-value">¥{{ userStore.formattedBalance }}</span>
        </div>
        <div class="balance-actions">
          <span class="all-btn" @click="amount = maxWithdraw">全部提现</span>
        </div>
      </div>

      <!-- 提现方式 -->
      <div class="section">
        <div class="section-title">提现方式</div>
        <div class="method-tabs">
          <div
            class="method-tab"
            :class="{ active: withdrawType === 'bank' }"
            @click="withdrawType = 'bank'"
          >
            🏦 银行卡
          </div>
          <div
            class="method-tab"
            :class="{ active: withdrawType === 'usdt' }"
            @click="withdrawType = 'usdt'"
          >
            💰 USDT
          </div>
        </div>
      </div>

      <!-- 收款账户 -->
      <div class="section">
        <div class="section-title">
          收款账户
          <span class="add-link" @click="goAddAccount">+ 添加</span>
        </div>
        <div v-if="accounts.length" class="account-list">
          <div
            v-for="acc in accounts"
            :key="acc.id"
            class="account-item"
            :class="{ active: selectedAccount === acc.id }"
            @click="selectedAccount = acc.id"
          >
            <span class="account-name">{{ acc.bankName || acc.network }}</span>
            <span class="account-number">{{ acc.cardNumber || acc.address }}</span>
            <van-icon v-if="selectedAccount === acc.id" name="checked" color="#e8b860" />
          </div>
        </div>
        <div v-else class="empty-account">
          <van-empty description="暂无收款账户" />
        </div>
      </div>

      <!-- 提现金额 -->
      <div class="section">
        <div class="section-title">提现金额</div>
        <div class="amount-input-wrap">
          <span class="amount-currency">¥</span>
          <input
            v-model.number="amount"
            type="number"
            class="amount-input"
            placeholder="请输入提现金额"
          />
        </div>
        <div class="amount-info">
          <span>手续费：¥{{ fee }}</span>
          <span>到账金额：¥{{ actualAmount }}</span>
        </div>
        <div class="amount-tip">
          最低提现 {{ config?.minAmount || 100 }} 元，每日限额 {{ config?.dailyLimit || 100000 }} 元
        </div>
      </div>

      <!-- 资金密码 -->
      <div class="section" v-if="config?.requireFundPassword">
        <div class="section-title">资金密码</div>
        <div class="pwd-input-wrap">
          <input
            v-model="fundPassword"
            type="password"
            class="pwd-input"
            placeholder="请输入资金密码"
            maxlength="6"
          />
        </div>
      </div>

      <!-- 提交 -->
      <div class="submit-section">
        <van-button
          block
          round
          type="primary"
          class="submit-btn"
          :loading="submitting"
          :disabled="!amount || !selectedAccount"
          @click="onSubmit"
        >
          确认提现
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const withdrawType = ref('bank')
const accounts = ref([])
const selectedAccount = ref(null)
const amount = ref(null)
const fundPassword = ref('')
const config = ref(null)
const submitting = ref(false)

const fee = computed(() => {
  if (!amount.value) return '0.00'
  return (amount.value * (config.value?.feeRate || 0.01)).toFixed(2)
})

const actualAmount = computed(() => {
  if (!amount.value) return '0.00'
  return (amount.value - parseFloat(fee.value)).toFixed(2)
})

const maxWithdraw = computed(() => {
  return Math.min(userStore.balance, config.value?.dailyLimit || 100000)
})

async function loadConfig() {
  try {
    config.value = await userApi.getWithdrawConfig()
  } catch (e) {
    config.value = null
  }
}

async function loadAccounts() {
  try {
    if (withdrawType.value === 'bank') {
      accounts.value = await userApi.getBankCards()
    } else {
      accounts.value = await userApi.getWalletAddresses('usdt')
    }
    if (accounts.value.length && !selectedAccount.value) {
      selectedAccount.value = accounts.value[0].id
    }
  } catch (e) {
    accounts.value = []
  }
}

function goAddAccount() {
  if (withdrawType.value === 'bank') {
    router.push('/user/bank-cards/form')
  } else {
    router.push('/user/wallet-addresses/form')
  }
}

async function onSubmit() {
  if (!amount.value || amount.value <= 0) {
    showFailToast('请输入提现金额')
    return
  }
  if (amount.value < (config.value?.minAmount || 100)) {
    showFailToast(`最低提现 ${config.value?.minAmount || 100} 元`)
    return
  }
  if (amount.value > userStore.balance) {
    showFailToast('余额不足')
    return
  }
  if (!selectedAccount.value) {
    showFailToast('请选择收款账户')
    return
  }

  try {
    await showConfirmDialog({
      title: '确认提现',
      message: `提现金额 ¥${amount.value}，手续费 ¥${fee.value}，实际到账 ¥${actualAmount}`,
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    await userApi.createWithdraw({
      type: withdrawType.value,
      accountId: selectedAccount.value,
      amount: amount.value,
      fundPassword: fundPassword.value
    })
    showSuccessToast('提现申请已提交')
    userStore.fetchBalance()
    router.push('/user/orders')
  } catch (e) {
    showFailToast(e.message || '提现失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadConfig()
  loadAccounts()
  userStore.fetchBalance()
})
</script>

<style scoped>
.withdraw-page {
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
.withdraw-content {
  padding: 12px;
}
.balance-card {
  background: linear-gradient(135deg, #2a2a3e, #1a1a2e);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #3a3a4e;
}
.balance-label {
  display: block;
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}
.balance-value {
  font-size: 22px;
  font-weight: 700;
  color: #e8b860;
}
.all-btn {
  font-size: 13px;
  color: #e8b860;
  border: 1px solid #e8b860;
  padding: 4px 12px;
  border-radius: 12px;
  cursor: pointer;
}
.section {
  margin-bottom: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #f0f0f0;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.add-link {
  font-size: 12px;
  color: #e8b860;
  font-weight: normal;
  cursor: pointer;
}
.method-tabs {
  display: flex;
  gap: 10px;
}
.method-tab {
  flex: 1;
  background: #1a1a1a;
  border: 2px solid transparent;
  border-radius: 10px;
  padding: 14px;
  text-align: center;
  font-size: 14px;
  color: #aaa;
  cursor: pointer;
}
.method-tab.active {
  border-color: #e8b860;
  color: #e8b860;
  background: rgba(232, 184, 96, 0.05);
}
.account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.account-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #1a1a1a;
  border-radius: 10px;
  padding: 14px;
  border: 2px solid transparent;
  cursor: pointer;
}
.account-item.active {
  border-color: #e8b860;
}
.account-name {
  font-size: 14px;
  color: #e0e0e0;
  font-weight: 500;
  min-width: 80px;
}
.account-number {
  flex: 1;
  font-size: 13px;
  color: #888;
}
.empty-account {
  padding: 20px;
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
.amount-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #888;
}
.amount-tip {
  font-size: 11px;
  color: #666;
  margin-top: 6px;
}
.pwd-input-wrap {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid #2a2a2a;
}
.pwd-input {
  width: 100%;
  height: 48px;
  background: transparent;
  border: none;
  outline: none;
  font-size: 16px;
  color: #f0f0f0;
  letter-spacing: 8px;
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
</style>
