// 本地存储封装
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const REMEMBER_LOGIN_KEY = 'rememberLogin'

export const storage = {
  getToken() {
    return localStorage.getItem(TOKEN_KEY) || ''
  },
  setToken(token) {
    localStorage.setItem(TOKEN_KEY, token)
  },
  removeToken() {
    localStorage.removeItem(TOKEN_KEY)
  },

  getUserInfo() {
    try {
      return JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null')
    } catch {
      return null
    }
  },
  setUserInfo(info) {
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  },
  removeUserInfo() {
    localStorage.removeItem(USER_INFO_KEY)
  },

  getRememberLogin() {
    try {
      return JSON.parse(localStorage.getItem(REMEMBER_LOGIN_KEY) || 'null')
    } catch {
      return null
    }
  },
  setRememberLogin(data) {
    localStorage.setItem(REMEMBER_LOGIN_KEY, JSON.stringify(data))
  },
  removeRememberLogin() {
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
  },

  // sessionStorage
  getSession(key) {
    return sessionStorage.getItem(key)
  },
  setSession(key, value) {
    sessionStorage.setItem(key, value)
  },
  removeSession(key) {
    sessionStorage.removeItem(key)
  },

  clearAll() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(REMEMBER_LOGIN_KEY)
  }
}
