import request from './request'

export const authApi = {
  login(data) {
    return request.post('/wap/auth/login', data, { skipErrorToast: true })
  },
  register(data) {
    return request.post('/wap/auth/register', data, { skipErrorToast: true })
  },
  trialLogin() {
    return request.post('/wap/auth/trial-login', null, { skipErrorToast: true })
  },
  /**
   * Telegram Mini App 登录
   * 将 WebApp initData 发送到后端验证签名并换取 JWT
   */
  telegramLogin(initData) {
    return request.post('/wap/auth/telegram', { initData }, { skipErrorToast: true })
  },
  phoneLogin(data) {
    return request.post('/wap/auth/phone-login', data, { skipErrorToast: true })
  },
  sendSmsCode(phone) {
    return request.post('/wap/sms/send-code', { phone })
  },
  logout() {
    return Promise.resolve({ success: true })
  }
}
