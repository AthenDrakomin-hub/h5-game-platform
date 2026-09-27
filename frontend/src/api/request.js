import axios from 'axios'
import { showFailToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/wap',
  timeout: 15000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      config.headers['X-Token'] = token
    }
    // 原站要求 X-User-Id header
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      const userId = userInfo?.userId || userInfo?.id
      if (userId) {
        config.headers['X-User-Id'] = userId
      }
    } catch (e) {
      // 忽略解析错误
    }
    return config
  },
  error => Promise.reject(error)
)

/**
 * 兼容多种后端响应结构：
 * 1. { code: 0|200, data, message }  标准
 * 2. { code: 200, result, msg }       变体
 * 3. { status: 'success', data }       变体
 * 4. 无 code 字段，直接是数据           裸数据
 */
function normalizeResponse(res) {
  if (!res || typeof res !== 'object') {
    return { ok: true, data: res }
  }

  // 有 code 字段的标准结构
  if ('code' in res) {
    const ok = res.code === 0 || res.code === 200 || res.code === '0' || res.code === '200'
    const data = res.data !== undefined ? res.data : (res.result !== undefined ? res.result : null)
    const message = res.message || res.msg || ''
    return { ok, data, message, code: res.code }
  }

  // status 字段结构
  if ('status' in res) {
    const ok = res.status === 'success' || res.status === 200 || res.status === 'ok'
    const data = res.data !== undefined ? res.data : (res.result !== undefined ? res.result : null)
    return { ok, data, message: res.message || res.msg || '' }
  }

  // 无 code/status，视为裸数据直接返回
  return { ok: true, data: res }
}

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    const { ok, data, message, code } = normalizeResponse(res)

    if (ok) {
      return data
    }

    // 业务错误
    if (!response.config.skipErrorToast && message) {
      showFailToast(message)
    }

    // 未登录/Token 过期
    if (code === 401 || code === 1001 || code === 1002 || message?.includes('登录') || message?.includes('token') || message?.includes('未登录')) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }

    return Promise.reject({
      code,
      message,
      isBusinessError: true,
      raw: res
    })
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        if (!error.config?.skipErrorToast) {
          showFailToast('登录已过期，请重新登录')
        }
      } else if (status === 403) {
        if (!error.config?.skipErrorToast) {
          showFailToast('没有访问权限')
        }
      } else if (status >= 500) {
        if (!error.config?.skipErrorToast) {
          showFailToast('服务器异常，请稍后重试')
        }
      } else {
        if (!error.config?.skipErrorToast) {
          showFailToast(error.response.data?.message || error.response.data?.msg || '请求失败')
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      if (!error.config?.skipErrorToast) {
        showFailToast('请求超时，请检查网络')
      }
    } else {
      if (!error.config?.skipErrorToast) {
        showFailToast('网络异常，请稍后重试')
      }
    }
    return Promise.reject({ ...error, isNetworkError: true })
  }
)

export default request
