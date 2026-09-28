<template>
  <div class="user-center">
    <!-- 顶部导航 -->
    <div class="top-bar">
      <span class="top-title">我的</span>
      <div class="top-actions">
        <van-icon name="setting-o" size="20" color="#d4a84b" @click="goSettings" />
        <van-icon name="chat-o" size="20" color="#d4a84b" @click="goPage('/user/message')" />
      </div>
    </div>

    <!-- 用户资料 + 资产合并卡片 -->
    <div class="profile-assets-card">
      <!-- 用户资料行 -->
      <div class="user-profile-row" @click="goLoginIfNeeded">
        <div class="avatar-wrap">
          <div v-if="authStore.isLoggedIn" class="avatar">
            <span class="avatar-level">{{ vipLevelName }}</span>
          </div>
          <div v-else class="avatar avatar-guest">
            <van-icon name="user-o" size="24" color="#666" />
          </div>
        </div>
        <div class="user-info">
          <template v-if="authStore.isLoggedIn">
            <div class="username">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</div>
            <div class="user-sub">{{ joinDateText }}</div>
          </template>
          <template v-else>
            <div class="username">点击登录</div>
            <div class="user-sub">登录后享受更多服务</div>
          </template>
        </div>
        <div class="profile-right">
          <div class="msg-icon-wrap" @click.stop="goPage('/user/message')">
            <van-icon name="envelop-o" size="18" color="#d4a84b" />
            <span v-if="unreadCount > 0" class="msg-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
          <van-icon name="arrow" size="14" color="#666" />
        </div>
      </div>

      <!-- 资产行 -->
      <div class="assets-row">
        <div class="assets-left">
          <div class="assets-label">
            总资产
            <van-icon name="eye-o" size="14" color="#888" class="eye-icon" @click="toggleBalance" />
          </div>
          <div class="assets-amount">
            <span class="currency">¥</span>
            <span class="amount">{{ formattedBalance }}</span>
          </div>
          <div class="yesterday-profit" @click="goPage('/user/profit-loss-report')">
            昨日收益 <span class="profit-value">+¥{{ yesterdayProfit }}</span>
            <van-icon name="arrow" size="10" color="#888" />
          </div>
        </div>
        <div class="assets-actions">
          <div class="action-btn recharge" @click="goRecharge">
            <van-icon name="add-o" size="16" />
            <span>充值</span>
          </div>
          <div class="action-btn withdraw" @click="goWithdraw">
            <van-icon name="down" size="16" />
            <span>提现</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 两个大卡片：VIP特权 + 代理中心 -->
    <div class="big-cards-row">
      <div class="big-card vip-card" @click="goPage('/user/vip')">
        <div class="big-card-icon">
          <van-icon name="vip-card-o" size="32" color="#d4a84b" />
        </div>
        <div class="big-card-info">
          <div class="big-card-title">VIP特权</div>
          <div class="big-card-sub">解锁尊享特权</div>
        </div>
      </div>
      <div class="big-card agent-card" @click="goPage('/user/agent')">
        <div class="big-card-icon">
          <van-icon name="friends-o" size="32" color="#d4a84b" />
        </div>
        <div class="big-card-info">
          <div class="big-card-title">代理中心</div>
          <div class="big-card-sub">邀请好友赚佣金</div>
        </div>
      </div>
    </div>

    <!-- 快捷菜单 4个图标 -->
    <div class="quick-menu-block">
      <div class="quick-icons-row">
        <div class="quick-icon-item" @click="goPage('/user/transfer')">
          <div class="quick-icon-wrap">
            <van-icon name="exchange" size="26" color="#d4a84b" />
          </div>
          <span class="quick-icon-label">转账</span>
        </div>
        <div class="quick-icon-item" @click="goPage('/user/bet-records')">
          <div class="quick-icon-wrap">
            <van-icon name="chart-trending-o" size="26" color="#d4a84b" />
          </div>
          <span class="quick-icon-label">投注记录</span>
        </div>
        <div class="quick-icon-item" @click="goPage('/user/transaction')">
          <div class="quick-icon-wrap">
            <van-icon name="balance-list-o" size="26" color="#d4a84b" />
          </div>
          <span class="quick-icon-label">账变记录</span>
        </div>
        <div class="quick-icon-item" @click="goPage('/user/yuebao')">
          <div class="quick-icon-wrap">
            <van-icon name="gold-coin-o" size="26" color="#d4a84b" />
          </div>
          <span class="quick-icon-label">余额宝</span>
        </div>
      </div>
    </div>

    <!-- 菜单列表 -->
    <div class="menu-list-block">
      <div class="menu-list-item" @click="goPage('/user/welfare')">
        <div class="menu-icon-wrap">
          <van-icon name="gift-o" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">福利中心</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/rebate')">
        <div class="menu-icon-wrap">
          <van-icon name="clock-o" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">实时返水</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/orders?type=recharge')">
        <div class="menu-icon-wrap">
          <van-icon name="credit-pay" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">充值记录</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/orders?type=withdraw')">
        <div class="menu-icon-wrap">
          <van-icon name="bank-card" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">提现记录</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/switch-record')">
        <div class="menu-icon-wrap">
          <van-icon name="swap" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">转换记录</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/profit-loss-report')">
        <div class="menu-icon-wrap">
          <van-icon name="chart-colume" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">盈亏记录</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goAppDownload">
        <div class="menu-icon-wrap">
          <van-icon name="down" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">APP下载</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
      <div class="menu-list-item" @click="goPage('/user/feedback')">
        <div class="menu-icon-wrap">
          <van-icon name="edit" size="22" color="#d4a84b" />
        </div>
        <span class="menu-text">有奖反馈</span>
        <van-icon name="arrow" size="14" color="#555" />
      </div>
    </div>

    <!-- 退出登录 -->
    <div v-if="authStore.isLoggedIn" class="logout-section">
      <div class="logout-btn" @click="onLogout">退出登录</div>
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
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const userStore = useUserStore()

const showBalance = ref(true)
const unreadCount = ref(0)
const yesterdayProfit = ref('0.00')

const formattedBalance = computed(() => {
  if (!showBalance.value) return '****'
  return userStore.formattedBalance
})

const vipLevelName = computed(() => {
  const level = userStore.vipLevel || authStore.userInfo?.vipLevel || 0
  const names = ['普通', '青铜', '白银', '黄金', '铂金', '钻石', '王者']
  return names[level] || '普通'
})

const joinDateText = computed(() => {
  const createTime = authStore.userInfo?.createTime
  if (!createTime) return '今日加入'
  const date = new Date(createTime)
  const today = new Date()
  if (date.toDateString() === today.toDateString()) return '今日加入'
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()} 加入`
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

function goAppDownload() {
  showToast('APP下载功能开发中')
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
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
  padding-bottom: 70px;
}

/* 顶部导航 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  position: relative;
  padding: 0 16px;
}
.top-title {
  font-size: 17px;
  font-weight: 700;
  color: #d4a84b;
  letter-spacing: 2px;
}
.top-actions {
  position: absolute;
  right: 16px;
  display: flex;
  gap: 18px;
}

/* 资料+资产合并卡片 */
.profile-assets-card {
  margin: 8px 12px 12px;
  background: linear-gradient(135deg, #2a1f10 0%, #1a130a 100%);
  border-radius: 16px;
  border: 1px solid rgba(212, 168, 75, 0.15);
  overflow: hidden;
}

/* 用户资料行 */
.user-profile-row {
  display: flex;
  align-items: center;
  padding: 18px 16px 14px;
  gap: 12px;
}
.avatar-wrap {
  flex-shrink: 0;
}
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #d4a84b, #a07828);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(212, 168, 75, 0.4);
}
.avatar-level {
  font-size: 13px;
  font-weight: 700;
  color: #1a130a;
}
.avatar-guest {
  background: #2a2a2a;
  border-color: #444;
}
.user-info {
  flex: 1;
}
.username {
  font-size: 16px;
  font-weight: 600;
  color: #f0e6d0;
  margin-bottom: 3px;
}
.user-sub {
  font-size: 11px;
  color: #8a7a5a;
}
.profile-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-icon-wrap {
  position: relative;
  padding: 4px;
}
.msg-badge {
  position: absolute;
  top: 0;
  right: -2px;
  background: #ee0a24;
  color: #fff;
  font-size: 9px;
  min-width: 14px;
  height: 14px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
}

/* 资产行 */
.assets-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 18px;
}
.assets-left {
  flex: 1;
}
.assets-label {
  font-size: 12px;
  color: #8a7a5a;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}
.eye-icon {
  cursor: pointer;
}
.assets-amount {
  display: flex;
  align-items: baseline;
  gap: 2px;
}
.currency {
  font-size: 14px;
  color: #d4a84b;
  font-weight: 600;
}
.amount {
  font-size: 26px;
  font-weight: 700;
  color: #f0e6d0;
  font-variant-numeric: tabular-nums;
}
.yesterday-profit {
  font-size: 11px;
  color: #8a7a5a;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 3px;
}
.profit-value {
  color: #52c41a;
  font-weight: 600;
}
.assets-actions {
  display: flex;
  gap: 10px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 9px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.action-btn.recharge {
  background: linear-gradient(135deg, #d4a84b, #a07828);
  color: #1a130a;
}
.action-btn.withdraw {
  background: linear-gradient(135deg, #d4a84b, #a07828);
  color: #1a130a;
}

/* 两个大卡片 */
.big-cards-row {
  display: flex;
  gap: 10px;
  padding: 0 12px 12px;
}
.big-card {
  flex: 1;
  background: linear-gradient(135deg, #2a1f10 0%, #1a130a 100%);
  border-radius: 14px;
  border: 1px solid rgba(212, 168, 75, 0.12);
  padding: 16px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.big-card-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(212, 168, 75, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}
.big-card-info {
  flex: 1;
}
.big-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #f0e6d0;
  margin-bottom: 2px;
}
.big-card-sub {
  font-size: 11px;
  color: #8a7a5a;
}

/* 快捷菜单 */
.quick-menu-block {
  margin: 0 12px 12px;
  background: linear-gradient(135deg, #2a1f10 0%, #1a130a 100%);
  border-radius: 14px;
  border: 1px solid rgba(212, 168, 75, 0.12);
  padding: 16px 0;
}
.quick-icons-row {
  display: flex;
  justify-content: space-around;
}
.quick-icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.quick-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(212, 168, 75, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}
.quick-icon-label {
  font-size: 11px;
  color: #b0a080;
}

/* 菜单列表 */
.menu-list-block {
  margin: 0 12px 12px;
  background: linear-gradient(135deg, #2a1f10 0%, #1a130a 100%);
  border-radius: 14px;
  border: 1px solid rgba(212, 168, 75, 0.12);
  overflow: hidden;
}
.menu-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(212, 168, 75, 0.06);
  cursor: pointer;
}
.menu-list-item:last-child {
  border-bottom: none;
}
.menu-icon-wrap {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.menu-text {
  flex: 1;
  font-size: 14px;
  color: #d0c4a8;
}

/* 退出登录 */
.logout-section {
  padding: 8px 12px 16px;
}
.logout-btn {
  background: transparent;
  border: 1px solid rgba(238, 10, 36, 0.5);
  color: #ee0a24;
  text-align: center;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.center-footer {
  text-align: center;
  padding: 16px;
}
.center-footer p {
  font-size: 10px;
  color: #4a4030;
  margin: 0;
}
</style>
