<template>
  <div class="user-center">
    <!-- 顶部用户信息 -->
    <div class="user-header">
      <div class="user-bg"></div>
      <div class="user-info-row">
        <div class="avatar-wrap" @click="goLoginIfNeeded">
          <div v-if="authStore.isLoggedIn" class="avatar">
            {{ (authStore.userInfo?.nickname || 'U')[0].toUpperCase() }}
          </div>
          <div v-else class="avatar avatar-guest">
            <van-icon name="user-o" size="28" color="#666" />
          </div>
        </div>
        <div class="user-detail" @click="goLoginIfNeeded">
          <template v-if="authStore.isLoggedIn">
            <div class="username">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</div>
            <div class="user-tags">
              <span class="vip-tag">VIP{{ userStore.vipLevel }}</span>
              <span v-if="authStore.userInfo?.isTrial" class="trial-tag">试玩</span>
            </div>
          </template>
          <template v-else>
            <div class="username">点击登录</div>
            <div class="user-sub">登录后享受更多服务</div>
          </template>
        </div>
        <div class="header-actions">
          <van-icon name="setting-o" size="22" color="#ccc" @click="goSettings" />
        </div>
      </div>

      <!-- 余额卡片 -->
      <div class="balance-card">
        <div class="balance-left">
          <span class="balance-label">账户余额</span>
          <div class="balance-amount">
            <span class="currency">¥</span>
            <span class="amount">{{ formattedBalance }}</span>
            <van-icon name="eye-o" size="16" color="#888" class="eye-icon" @click="toggleBalance" />
          </div>
        </div>
        <div class="balance-actions">
          <div class="balance-btn recharge" @click="goRecharge">充值</div>
          <div class="balance-btn withdraw" @click="goWithdraw">提现</div>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-grid">
        <div class="menu-item" @click="goPage('/user/orders')">
          <van-icon name="orders-o" size="22" color="#e8b860" />
          <span>我的订单</span>
        </div>
        <div class="menu-item" @click="goPage('/user/bet-records')">
          <van-icon name="chart-trending-o" size="22" color="#e8b860" />
          <span>投注记录</span>
        </div>
        <div class="menu-item" @click="goPage('/user/transaction')">
          <van-icon name="balance-list-o" size="22" color="#e8b860" />
          <span>账变记录</span>
        </div>
        <div class="menu-item" @click="goPage('/user/game-records')">
          <van-icon name="gold-coin-o" size="22" color="#e8b860" />
          <span>游戏记录</span>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div class="menu-list">
        <div class="menu-list-item" @click="goPage('/user/vip')">
          <van-icon name="vip-card-o" size="20" color="#e8b860" />
          <span class="menu-text">VIP特权</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/welfare')">
          <van-icon name="gift-o" size="20" color="#e8b860" />
          <span class="menu-text">礼金中心</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/agent')">
          <van-icon name="friends-o" size="20" color="#e8b860" />
          <span class="menu-text">代理中心</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/promote-earn')">
          <van-icon name="share-o" size="20" color="#e8b860" />
          <span class="menu-text">推广赚钱</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/message')">
          <van-icon name="chat-o" size="20" color="#e8b860" />
          <span class="menu-text">消息中心</span>
          <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/bank-cards')">
          <van-icon name="credit-pay" size="20" color="#e8b860" />
          <span class="menu-text">银行卡管理</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/wallet-addresses')">
          <van-icon name="wallet-o" size="20" color="#e8b860" />
          <span class="menu-text">钱包地址</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goPage('/user/feedback')">
          <van-icon name="edit" size="20" color="#e8b860" />
          <span class="menu-text">有奖反馈</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="menu-list-item" @click="goChat">
          <van-icon name="service-o" size="20" color="#e8b860" />
          <span class="menu-text">在线客服</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div v-if="authStore.isLoggedIn" class="logout-section">
      <van-button block round class="logout-btn" @click="onLogout">退出登录</van-button>
    </div>

    <div class="center-footer">
      <p>本平台仅供技术学习演示使用</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const userStore = useUserStore()

const showBalance = ref(true)
const unreadCount = ref(0)

const formattedBalance = computed(() => {
  if (!showBalance.value) return '****'
  return userStore.formattedBalance
})

function toggleBalance() {
  showBalance.value = !showBalance.value
}

function goLoginIfNeeded() {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', '/user/center')
  }
}

function goPage(path) {
  if (!authStore.isLoggedIn) {
    appStore.openAuthPopup('login', path)
    return
  }
  router.push(path)
}

function goRecharge() {
  goPage('/user/recharge')
}

function goWithdraw() {
  goPage('/user/withdraw')
}

function goSettings() {
  goPage('/user/settings')
}

function goChat() {
  router.push('/chat')
}

async function onLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出当前账号吗？',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
    authStore.logout()
  } catch (e) {
    // 取消
  }
}

async function loadData() {
  if (authStore.isLoggedIn) {
    userStore.fetchBalance()
    try {
      const data = await userApi.getMessages({ page: 1, pageSize: 1 })
      unreadCount.value = data.unreadCount || 0
    } catch (e) {
      // 静默
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background: #0d0d0d;
  padding-bottom: 60px;
}
.user-header {
  position: relative;
  padding-bottom: 70px;
}
.user-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(180deg, #1a1a2e 0%, #0d0d0d 100%);
}
.user-info-row {
  position: relative;
  display: flex;
  align-items: center;
  padding: 20px 16px 16px;
  gap: 12px;
}
.avatar-wrap {
  flex-shrink: 0;
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8b860, #c99a3e);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
}
.avatar-guest {
  background: #2a2a2a;
}
.user-detail {
  flex: 1;
}
.username {
  font-size: 18px;
  font-weight: 600;
  color: #f0f0f0;
  margin-bottom: 4px;
}
.user-sub {
  font-size: 12px;
  color: #888;
}
.user-tags {
  display: flex;
  gap: 6px;
}
.vip-tag {
  font-size: 10px;
  background: linear-gradient(135deg, #e8b860, #c99a3e);
  color: #1a1a1a;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 700;
}
.trial-tag {
  font-size: 10px;
  background: rgba(7, 193, 96, 0.2);
  color: #07c160;
  padding: 2px 6px;
  border-radius: 4px;
}
.header-actions {
  flex-shrink: 0;
}
.balance-card {
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  background: linear-gradient(135deg, #2a2a3e, #1a1a2e);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #3a3a4e;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}
.balance-left {
  flex: 1;
}
.balance-label {
  font-size: 12px;
  color: #888;
  display: block;
  margin-bottom: 4px;
}
.balance-amount {
  display: flex;
  align-items: center;
  gap: 4px;
}
.currency {
  font-size: 14px;
  color: #e8b860;
}
.amount {
  font-size: 24px;
  font-weight: 700;
  color: #e8b860;
  font-variant-numeric: tabular-nums;
}
.eye-icon {
  margin-left: 6px;
  cursor: pointer;
}
.balance-actions {
  display: flex;
  gap: 8px;
}
.balance-btn {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.balance-btn.recharge {
  background: linear-gradient(135deg, #e8b860, #c99a3e);
  color: #1a1a1a;
}
.balance-btn.withdraw {
  background: transparent;
  border: 1px solid #e8b860;
  color: #e8b860;
}
.menu-section {
  padding: 16px;
}
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 16px 8px;
}
.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.menu-item span {
  font-size: 11px;
  color: #aaa;
}
.menu-list {
  background: #1a1a1a;
  border-radius: 12px;
  overflow: hidden;
}
.menu-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #222;
  cursor: pointer;
}
.menu-list-item:last-child {
  border-bottom: none;
}
.menu-text {
  flex: 1;
  font-size: 14px;
  color: #d0d0d0;
}
.badge {
  background: #ee0a24;
  color: #fff;
  font-size: 10px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}
.logout-section {
  padding: 0 16px 16px;
}
.logout-btn {
  background: transparent !important;
  border: 1px solid #ee0a24 !important;
  color: #ee0a24 !important;
  height: 44px !important;
}
.center-footer {
  text-align: center;
  padding: 20px;
}
.center-footer p {
  font-size: 11px;
  color: #444;
  margin: 0;
}
</style>
