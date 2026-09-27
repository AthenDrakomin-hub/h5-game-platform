import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { showSuccessToast, showFailToast } from 'vant'
import { isTelegram, getTelegramUser, getInitData } from '@/utils/telegram'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    // 兼容两种结构：
    // 1. 真实后端：{ token, userId, id, username, nickname, balance, ... }
    // 2. 旧结构/mock：{ token, userInfo: { ... } }
    if (data.userInfo) {
      userInfo.value = data.userInfo
    } else {
      const { token: _, ...rest } = data
      userInfo.value = rest
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function login({ username, password }) {
    const data = await authApi.login({ username, password })
    setAuth(data)
    return data
  }

  async function register({ username, password, nickname }) {
    return await authApi.register({ username, password, nickname })
  }

  async function trialLogin() {
    const data = await authApi.trialLogin()
    setAuth(data)
    return data
  }

  /**
   * Telegram Mini App 自动登录
   * 用 initData 中的用户信息直接创建会话（mock 环境下本地生成 token）
   * 生产环境应将 initData 发送到后端验证后换取 token
   */
  async function telegramLogin() {
    if (!isTelegram) return null
    const tgUser = getTelegramUser()
    if (!tgUser) return null
    try {
      // mock 环境：直接用 Telegram 用户信息生成会话
      // 生产环境：await authApi.telegramLogin({ initData: getInitData() })
      const mockToken = 'tg_' + btoa(`${tgUser.id}:${Date.now()}`).replace(/=/g, '')
      const data = {
        token: mockToken,
        userInfo: {
          id: tgUser.id,
          username: tgUser.username,
          nickname: tgUser.nickname,
          avatar: tgUser.avatar,
          loginType: 'telegram',
          vipLevel: 1,
          balance: 10000,
          createTime: new Date().toISOString()
        }
      }
      setAuth(data)
      return data
    } catch (e) {
      showFailToast('Telegram 登录失败')
      return null
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('rememberLogin')
    showSuccessToast('已退出登录')
  }

  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    trialLogin,
    telegramLogin,
    logout,
    updateUserInfo,
    setAuth
  }
})
