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
            <van-icon v-if="selectedAccount === acc.id" name="checked" color="#f0d080" />
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

      <!-- 提现说明 -->
      <div class="withdraw-notes">
        <div class="notes-title">温馨提示</div>
        <div class="notes-item">1. 提现申请提交后，USDT通常5-30分钟到账，银行卡1-24小时到账</div>
        <div class="notes-item">2. 手续费：USDT TRC20免手续费，银行卡按金额1%收取（最低5元）</div>
        <div class="notes-item">3. 每日提现次数不限，单笔最低100元，最高50000元</div>
        <div class="notes-item">4. 为保障资金安全，大额提现可能需要人工审核</div>
        <div class="notes-item">5. 提现到账时间受区块链网络或银行处理速度影响</div>
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
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  padding-bottom: 100px;
}
:deep(.van-nav-bar) {
  background: transparent;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f2e0b8 !important;
}
.withdraw-content {
  padding: 12px;
  padding-bottom: 90px;
}
.balance-card {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.balance-label {
  display: block;
  font-size: 12px;
  color: #8a7a5a;
  margin-bottom: 4px;
}
.balance-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
}
.all-btn {
  font-size: 13px;
  color: #f0d080;
  border: 1px solid #d4a84b;
  padding: 4px 14px;
  border-radius: 12px;
  cursor: pointer;
}
.section {
  margin-bottom: 16px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #f2e0b8;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.add-link {
  font-size: 12px;
  color: #f0d080;
  font-weight: normal;
  cursor: pointer;
}
.method-tabs {
  display: flex;
  gap: 10px;
}
.method-tab {
  flex: 1;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 10px;
  padding: 14px;
  text-align: center;
  font-size: 14px;
  color: #8a7a5a;
  cursor: pointer;
}
.method-tab.active {
  border-color: #d4a84b;
  color: #f0d080;
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
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 10px;
  padding: 14px;
  border: 1px solid rgba(255,255,255,0.12);
  cursor: pointer;
}
.account-item.active {
  border-color: #d4a84b;
}
.account-name {
  font-size: 14px;
  color: #f0e6d0;
  font-weight: 500;
  min-width: 80px;
}
.account-number {
  flex: 1;
  font-size: 13px;
  color: #8a7a5a;
}
.empty-account {
  padding: 20px;
}
.amount-input-wrap {
  display: flex;
  align-items: center;
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
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
  color: #555;
  font-size: 14px;
}
.amount-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #8a7a5a;
}
.amount-tip {
  font-size: 11px;
  color: #8a7a5a;
  margin-top: 6px;
}
.pwd-input-wrap {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid rgba(255,255,255,0.12);
}
.pwd-input {
  width: 100%;
  height: 48px;
  background: transparent;
  border: none;
  outline: none;
  font-size: 16px;
  color: #ffffff;
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
  background: linear-gradient(180deg, rgba(13,10,6,0.9), #0d0a06);
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
/* 提现说明 */
.withdraw-notes {
  background: linear-gradient(rgba(255,255,255,0.06) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.6), rgba(11,10,8,0.4));
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.08);
  padding: 14px;
  margin-top: 16px;
}
.notes-title {
  font-size: 13px;
  font-weight: 600;
  color: #f2e0b8;
  margin-bottom: 8px;
}
.notes-item {
  font-size: 11px;
  color: #8a7a5a;
  line-height: 1.8;
}
</style>
