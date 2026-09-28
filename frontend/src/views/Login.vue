<template>
  <div :class="['login-page', { 'login-popup-mode': popupMode }]">
    <!-- 头部 -->
    <div :class="['header-bg', { 'header-popup': popupMode }]">
      <div v-if="!popupMode" class="header-close" @click="goBack">
        <van-icon name="cross" size="18" color="#fff" />
      </div>
      <div v-if="popupMode" class="header-content header-popup-title">
        <span class="popup-site-name">{{ siteName }}</span>
      </div>
      <div v-else class="header-content">
        <div class="logo-area">
          <img src="/assets/logo.png" alt="logo" class="header-logo-img" />
        </div>
        <p class="site-slogan">{{ slogan }}</p>
      </div>

      <!-- Tab 切换 -->
      <div :class="['auth-tabs', { 'auth-tabs-popup': popupMode }]">
        <div class="auth-tab" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">登录</div>
        <div class="tab-divider">|</div>
        <div class="auth-tab" :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">注册</div>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="main-content">
      <!-- 登录表单 -->
      <transition name="form-fade" mode="out-in">
        <div v-if="activeTab === 'login'" key="login" class="form-fade">
          <van-form class="login-form" @submit="onLoginSubmit">
            <div class="input-container">
              <div class="field-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="#9CA3AF" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <van-field
                v-model="loginForm.username"
                name="username"
                placeholder="请输入账号"
                class="pill-field"
                :border="false"
              />
            </div>

            <div class="input-container">
              <div class="field-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="#9CA3AF" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <van-field
                v-model="loginForm.password"
                type="password"
                name="password"
                placeholder="请输入密码"
                class="pill-field"
                :border="false"
              />
              <div class="eye-btn" @click="showPassword = !showPassword">
                <van-icon :name="showPassword ? 'eye-o' : 'closed-eye'" size="20" color="#C0C4CC" />
              </div>
            </div>

            <div class="remember-row" @click="rememberMe = !rememberMe">
              <div class="checkbox" :class="{ checked: rememberMe }">
                <svg v-if="rememberMe" viewBox="0 0 24 24" width="12" height="12" stroke="#fff" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <span>记住账号</span>
            </div>

            <van-button
              round
              block
              type="primary"
              class="action-btn primary-btn"
              :loading="loginLoading"
              native-type="submit"
            >
              登 录
            </van-button>

            <div class="footer-links">
              <span class="footer-link" @click="goChat">联系客服</span>
              <span class="footer-link" @click="onTrialLogin" data-auth-free-action="trial">免费试玩</span>
              <span class="footer-link forgot-link" @click="onForgotPassword">忘记密码</span>
            </div>

            <div v-if="popupMode" class="popup-secondary-btns">
              <div class="popup-secondary-btn" @click="goDownload">下载APP领豪礼</div>
              <div class="popup-secondary-btn" @click="goChat">联系客服</div>
            </div>
          </van-form>
        </div>

        <!-- 注册表单 -->
        <div v-else key="register" class="form-fade">
          <van-form class="login-form" @submit="onRegisterSubmit">
            <div class="input-container">
              <div class="field-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="#9CA3AF" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <van-field
                v-model="registerForm.username"
                name="username"
                placeholder="请输入用户名 (4-20位字母数字)"
                class="pill-field"
                :border="false"
              />
            </div>

            <div class="input-container">
              <div class="field-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="#9CA3AF" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <van-field
                v-model="registerForm.password"
                type="password"
                name="password"
                placeholder="请输入密码 (6-20位)"
                class="pill-field"
                :border="false"
              />
            </div>

            <div class="input-container">
              <div class="field-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" stroke="#9CA3AF" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <van-field
                v-model="registerForm.rePassword"
                type="password"
                name="rePassword"
                placeholder="请再次输入密码"
                class="pill-field"
                :border="false"
              />
            </div>

            <div class="remember-row" @click="agreeProtocol = !agreeProtocol">
              <div class="checkbox" :class="{ checked: agreeProtocol }">
                <svg v-if="agreeProtocol" viewBox="0 0 24 24" width="12" height="12" stroke="#fff" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <span>我已满18岁，已阅读且同意 <span class="protocol-link" @click.stop="showAgreement = true">《用户协议》</span></span>
            </div>

            <van-button
              round
              block
              type="primary"
              class="action-btn register-btn-solid"
              :loading="registerLoading"
              :disabled="!agreeProtocol"
              native-type="submit"
            >
              注 册
            </van-button>

            <div class="footer-links">
              <span class="footer-link" @click="goChat">联系客服</span>
              <span class="footer-link" @click="onTrialLogin" data-auth-free-action="trial">免费试玩</span>
            </div>

            <div v-if="popupMode" class="popup-secondary-btns">
              <div class="popup-secondary-btn" @click="goDownload">下载APP领豪礼</div>
              <div class="popup-secondary-btn" @click="goChat">联系客服</div>
            </div>
          </van-form>
        </div>
      </transition>
    </div>

    <!-- 用户协议弹窗 -->
    <van-popup v-model:show="showAgreement" position="center" round :style="{ width: '90%', maxHeight: '70vh' }">
      <div class="agreement-popup-content">
        <div class="modal-header">
          <h2 class="modal-title">用户协议</h2>
          <van-icon name="cross" class="modal-close-btn" @click="showAgreement = false" />
        </div>
        <div class="modal-content">
          <ol class="agreement-list">
            <li>用户在使用本应用前，必须阅读并理解相关规则，一旦进入本网站进行使用，即被视为已接受所有规则。</li>
            <li>用户有责任确保账户及登录资料的保密性，以会员账户及密码进行的任何操作均将被视为有效。</li>
            <li>本平台保留随时修改本协议、规则或隐私政策的权利，修改后的内容自指定日期起生效。</li>
            <li>用户必须根据其所在国家的法律达到法定年龄才能使用本应用。</li>
            <li>本平台仅供技术学习演示使用，不涉及任何真实交易。</li>
          </ol>
        </div>
        <div class="modal-footer">
          <van-button round block type="primary" class="modal-confirm-btn" @click="agreeProtocol = true; showAgreement = false">已阅读并同意</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 滑块验证码 -->
    <van-popup v-model:show="showCaptcha" position="center" round :style="{ width: '92%', maxWidth: '360px' }" :close-on-click-overlay="false">
      <PuzzleCaptcha
        :show="showCaptcha"
        @success="onCaptchaSuccess"
        @fail="onCaptchaFail"
        @close="showCaptcha = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useNoticeStore } from '@/stores/notice'
import { homeApi } from '@/api/home'
import { storage } from '@/utils/storage'
import PuzzleCaptcha from '@/components/PuzzleCaptcha.vue'
import { showSuccessToast, showFailToast } from 'vant'

const props = defineProps({
  popupMode: { type: Boolean, default: false },
  defaultTab: { type: String, default: 'login' }
})

const emit = defineEmits(['close'])

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const authStore = useAuthStore()
const noticeStore = useNoticeStore()

const siteName = ref('H5 Clone')
const slogan = ref('官方直营 · 信誉首选')
const activeTab = ref(props.defaultTab || 'login')
const showPassword = ref(false)
const rememberMe = ref(false)
const agreeProtocol = ref(false)
const showAgreement = ref(false)
const showCaptcha = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)
const pendingAction = ref(null) // 'login' | 'register'

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', rePassword: '' })

async function loadConfig() {
  try {
    const data = await homeApi.getConfig()
    if (data?.siteName) siteName.value = data.siteName
    if (data?.slogan) slogan.value = data.slogan
  } catch (e) {
    // 静默
  }
}

function restoreRemembered() {
  const remembered = storage.getRememberLogin()
  if (remembered) {
    loginForm.username = remembered.username || ''
    loginForm.password = remembered.password || ''
    rememberMe.value = true
  }
}

function onLoginSubmit() {
  if (!loginForm.username.trim()) {
    showFailToast('请输入账号')
    return
  }
  if (!loginForm.password) {
    showFailToast('请输入密码')
    return
  }
  pendingAction.value = 'login'
  showCaptcha.value = true
}

function onRegisterSubmit() {
  const username = registerForm.username.trim().toLowerCase()
  if (!username) {
    showFailToast('请输入用户名')
    return
  }
  if (username.length < 4 || username.length > 20) {
    showFailToast('用户名需4-20位')
    return
  }
  if (!/^[a-z0-9]+$/.test(username) || !/[a-z]/.test(username) || !/[0-9]/.test(username)) {
    showFailToast('用户名须同时含小写字母和数字')
    return
  }
  if (!registerForm.password || registerForm.password.length < 6) {
    showFailToast('密码至少6位')
    return
  }
  if (registerForm.password !== registerForm.rePassword) {
    showFailToast('两次密码不一致')
    return
  }
  if (!agreeProtocol.value) {
    showFailToast('请先同意用户协议')
    return
  }
  pendingAction.value = 'register'
  showCaptcha.value = true
}

async function onCaptchaSuccess() {
  showCaptcha.value = false
  if (pendingAction.value === 'login') {
    await doLogin()
  } else if (pendingAction.value === 'register') {
    await doRegister()
  }
  pendingAction.value = null
}

function onCaptchaFail() {
  // 验证码失败会自动重置，不关闭弹窗
}

async function doLogin() {
  loginLoading.value = true
  try {
    await authStore.login({
      username: loginForm.username.trim().toLowerCase(),
      password: loginForm.password
    })

    if (rememberMe.value) {
      storage.setRememberLogin({ username: loginForm.username, password: loginForm.password })
    } else {
      storage.removeRememberLogin()
    }

    noticeStore.resetNotice()
    showSuccessToast('登录成功')

    if (props.popupMode) {
      emit('close')
      const redirect = appStore.getAndClearAuthRedirect()
      if (redirect) router.push(redirect)
    } else {
      router.push('/')
    }
  } catch (e) {
    if (e.isNetworkError) {
      showFailToast('登录失败，请检查网络')
    } else {
      showFailToast(e.message || '用户名或密码错误')
    }
  } finally {
    loginLoading.value = false
  }
}

async function doRegister() {
  registerLoading.value = true
  try {
    await authStore.register({
      username: registerForm.username.trim().toLowerCase(),
      password: registerForm.password,
      nickname: registerForm.username.trim().toLowerCase()
    })
    showSuccessToast('注册成功')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
  } catch (e) {
    showFailToast(e.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

async function onTrialLogin() {
  try {
    await authStore.trialLogin()
    noticeStore.resetNotice()
    showSuccessToast('试玩成功，已获得2000试玩金')
    if (props.popupMode) {
      emit('close')
    } else {
      router.push('/')
    }
  } catch (e) {
    showFailToast(e.message || '试玩失败，请稍后重试')
  }
}

function onForgotPassword() {
  showFailToast('请联系客服找回密码')
}

function goBack() {
  if (props.popupMode) {
    emit('close')
  } else {
    router.back()
  }
}

function goChat() {
  if (props.popupMode) emit('close')
  router.push('/chat')
}

function goDownload() {
  showFailToast('APP下载功能开发中')
}

onMounted(() => {
  loadConfig()
  restoreRemembered()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 30%, #0d0a06 100%);
}
.login-popup-mode {
  min-height: auto;
  padding-bottom: 20px;
  background: linear-gradient(180deg, #1a1208 0%, #0d0a06 100%);
}
.header-bg {
  background: linear-gradient(180deg, rgba(42,31,16,0.6) 0%, transparent 100%);
  padding: 20px 20px 0;
  position: relative;
}
.header-popup {
  padding: 16px 16px 0;
}
.header-close {
  position: absolute;
  top: 16px;
  left: 16px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-content {
  text-align: center;
  padding: 10px 0 16px;
}
.header-popup-title {
  padding: 4px 0 12px;
}
.logo-area {
  margin-bottom: 8px;
}
.header-logo-img {
  height: 44px;
  object-fit: contain;
}
.header-logo-full {
  font-size: 48px;
}
.popup-site-name {
  font-size: 18px;
  font-weight: 600;
  color: #f0d080;
}
.site-slogan {
  font-size: 13px;
  color: #8a7a5a;
  margin: 0;
}
.auth-tabs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 8px 0 16px;
}
.auth-tabs-popup {
  padding: 4px 0 12px;
}
.auth-tab {
  font-size: 16px;
  color: #808080;
  cursor: pointer;
  padding: 4px 0;
  position: relative;
  transition: all 0.2s;
}
.auth-tab.active {
  color: #f0d080;
  font-size: 18px;
  font-weight: 600;
}
.tab-divider {
  color: rgba(255,255,255,0.15);
  font-size: 14px;
}
.main-content {
  padding: 0 24px;
}
.login-form {
  margin-top: 8px;
}
.input-container {
  display: flex;
  align-items: center;
  background: linear-gradient(rgba(255,255,255,0.08) 0%, rgba(255,255,255,0) 26%),
              linear-gradient(145deg, rgba(31,26,21,0.7), rgba(11,10,8,0.5));
  border-radius: 12px;
  margin-bottom: 14px;
  padding: 0 16px;
  border: 1px solid rgba(255,255,255,0.1);
}
.field-icon {
  display: flex;
  align-items: center;
  margin-right: 8px;
}
.pill-field {
  flex: 1;
  background: transparent !important;
  --van-field-input-text-color: #f0e8d8;
  --van-field-placeholder-text-color: #6a5a40;
}
.pill-field :deep(.van-field__control) {
  min-height: 44px;
}
.eye-btn {
  padding: 8px;
}
.remember-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 4px 16px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
}
.checkbox {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1.5px solid #555;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.checkbox.checked {
  background: linear-gradient(135deg, #f0d080, #d4a84b);
  border-color: #f0d080;
}
.protocol-link {
  color: #f0d080;
}
.action-btn {
  margin-top: 8px !important;
  font-weight: 600 !important;
  height: 46px !important;
}
.primary-btn {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  border-radius: 10px !important;
}
.register-btn-solid {
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  border-radius: 10px !important;
}
.register-btn-solid:disabled {
  opacity: 0.5;
}
.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}
.footer-link {
  font-size: 13px;
  color: #888;
  cursor: pointer;
}
.forgot-link {
  color: #f0d080;
}
.popup-secondary-btns {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #222;
}
.popup-secondary-btn {
  font-size: 12px;
  color: #888;
  cursor: pointer;
}
.agreement-popup-content {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #f2e0b8;
  margin: 0;
}
.modal-close-btn {
  font-size: 20px;
  color: #888;
  cursor: pointer;
}
.modal-content {
  margin-bottom: 20px;
}
.agreement-list {
  padding-left: 20px;
  margin: 0;
}
.agreement-list li {
  font-size: 13px;
  color: #aaa;
  line-height: 1.8;
  margin-bottom: 8px;
}
.modal-confirm-btn {
  background: linear-gradient(135deg, #f0d080, #d4a84b) !important;
  border: none !important;
  color: #1a1a1a !important;
}
</style>
