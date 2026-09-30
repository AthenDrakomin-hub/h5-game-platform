<template>
  <div class="settings-page">
    <van-nav-bar title="设置" left-arrow @click-left="$router.back()" />
    <div class="settings-content">
      <!-- 账号信息 -->
      <div class="settings-section">
        <div class="settings-item" @click="showProfile = true">
          <span class="item-label">个人资料</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="settings-item" @click="goChangePassword">
          <span class="item-label">修改登录密码</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="settings-item" @click="goFundPassword">
          <span class="item-label">资金密码</span>
          <span class="item-value">{{ hasFundPassword ? '已设置' : '未设置' }}</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="settings-item" @click="goBindPhone">
          <span class="item-label">绑定手机</span>
          <span class="item-value">{{ phoneText }}</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
      </div>

      <!-- 安全设置 -->
      <div class="settings-section">
        <div class="settings-item">
          <span class="item-label">消息通知</span>
          <van-switch v-model="notificationEnabled" size="20" />
        </div>
        <div class="settings-item">
          <span class="item-label">清除缓存</span>
          <span class="item-value">{{ cacheSize }}</span>
          <van-icon name="arrow" size="14" color="#555" @click="clearCache" />
        </div>
      </div>

      <!-- 关于 -->
      <div class="settings-section">
        <div class="settings-item" @click="showAbout = true">
          <span class="item-label">关于我们</span>
          <van-icon name="arrow" size="14" color="#555" />
        </div>
        <div class="settings-item">
          <span class="item-label">版本号</span>
          <span class="item-value">v1.0.0</span>
        </div>
      </div>

      <!-- 退出 -->
      <div v-if="authStore.isLoggedIn" class="logout-section">
        <van-button block class="logout-btn" @click="onLogout">退出登录</van-button>
      </div>
    </div>

    <!-- 个人资料弹窗 -->
    <van-popup v-model:show="showProfile" position="center" round :style="{ width: '90%' }">
      <div class="profile-popup">
        <div class="popup-header">
          <span class="popup-title">个人资料</span>
          <van-icon name="cross" size="18" @click="showProfile = false" />
        </div>
        <div class="profile-info">
          <div class="profile-row"><span>用户名</span><span>{{ authStore.userInfo?.username }}</span></div>
          <div class="profile-row"><span>昵称</span><span>{{ authStore.userInfo?.nickname }}</span></div>
          <div class="profile-row"><span>VIP等级</span><span>VIP{{ userStore.vipLevel }}</span></div>
          <div class="profile-row"><span>注册时间</span><span>{{ authStore.userInfo?.createTime ? formatDate(authStore.userInfo.createTime) : '--' }}</span></div>
        </div>
      </div>
    </van-popup>

    <!-- 关于弹窗 -->
    <van-popup v-model:show="showAbout" position="center" round :style="{ width: '90%' }">
      <div class="about-popup">
        <div class="popup-header">
          <span class="popup-title">关于我们</span>
          <van-icon name="cross" size="18" @click="showAbout = false" />
        </div>
        <div class="about-content">
          <p>本应用为技术学习演示项目</p>
          <p>所有数据均为模拟数据</p>
          <p>不涉及任何真实交易</p>
          <p class="version">Version 1.0.0</p>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import { showSuccessToast, showConfirmDialog, showFailToast } from 'vant'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const userStore = useUserStore()

const showProfile = ref(false)
const showAbout = ref(false)
const notificationEnabled = ref(true)
const cacheSize = ref('12.5MB')

const hasFundPassword = computed(() => authStore.userInfo?.hasFundPassword || false)
const phoneText = computed(() => authStore.userInfo?.phone || '未绑定')

function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

function goChangePassword() {
  showFailToast('修改密码功能开发中')
}

function goFundPassword() {
  showFailToast('资金密码功能开发中')
}

function goBindPhone() {
  showFailToast('绑定手机功能开发中')
}

function clearCache() {
  showConfirmDialog({
    title: '清除缓存',
    message: '确定要清除缓存吗？',
    confirmButtonText: '清除',
    cancelButtonText: '取消'
  }).then(() => {
    cacheSize.value = '0KB'
    showSuccessToast('缓存已清除')
  }).catch(() => {})
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

onMounted(() => {
  if (authStore.isLoggedIn) {
    userStore.fetchBalance()
  }
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: var(--gold-1) !important;
}
.settings-content {
  padding: 12px;
}
.settings-section {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 14px;
  margin-bottom: 12px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.12);
}
.settings-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  cursor: pointer;
}
.settings-item:last-child {
  border-bottom: none;
}
.item-label {
  flex: 1;
  font-size: 14px;
  color: var(--gold-1);
}
.item-value {
  font-size: 13px;
  color: var(--text-hint);
  margin-right: 8px;
}
:deep(.van-switch) {
  background: rgba(255,255,255,0.15);
}
:deep(.van-switch--on) {
  background: linear-gradient(180deg, #f0d080, #d4a84b);
}
.logout-section {
  padding: 16px 0;
}
.logout-btn {
  background: transparent !important;
  border: 1px solid rgba(238,10,36,0.6) !important;
  color: #ee0a24 !important;
  height: 44px !important;
  border-radius: 10px !important;
}
.profile-popup, .about-popup {
  padding: 20px;
  background: linear-gradient(180deg, var(--app-bg-2), var(--app-bg));
}
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.popup-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--gold-1);
}
.profile-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.profile-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}
.profile-row span:first-child {
  color: var(--text-hint);
}
.profile-row span:last-child {
  color: var(--gold-1);
}
.about-content {
  text-align: center;
  padding: 10px 0;
}
.about-content p {
  font-size: 13px;
  color: #aaa;
  margin: 8px 0;
}
.about-content .version {
  margin-top: 20px;
  color: var(--text-hint);
  font-size: 12px;
}
</style>
