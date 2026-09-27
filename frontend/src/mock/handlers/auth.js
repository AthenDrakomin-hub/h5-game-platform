import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })
const fail = (message, code = 1001) => HttpResponse.json({ code, data: null, message })

let userCounter = 1000

export const authHandlers = [
  http.post('/wap/auth/login', async ({ request }) => {
    const body = await request.json()
    const { username, password } = body

    // 演示账号：demo / 123456
    if (username === 'demo' && password === '123456') {
      return ok({
        token: 'mock-token-' + Date.now(),
        userInfo: {
          id: 1,
          username: 'demo',
          nickname: '演示用户',
          avatar: '',
          balance: 10000.00,
          vipLevel: 2,
          phone: '138****8888',
          createTime: '2026-01-01 00:00:00'
        }
      })
    }

    // 任意非空账号密码也可登录（方便演示）
    if (username && password && password.length >= 6) {
      userCounter++
      return ok({
        token: 'mock-token-' + Date.now(),
        userInfo: {
          id: userCounter,
          username,
          nickname: username,
          avatar: '',
          balance: 5000.00,
          vipLevel: 1,
          phone: '',
          createTime: new Date().toISOString()
        }
      })
    }

    return fail('用户名或密码错误')
  }),

  http.post('/wap/auth/register', async ({ request }) => {
    const body = await request.json()
    const { username, password, nickname } = body

    if (!username || username.length < 4) {
      return fail('用户名至少4位')
    }
    if (!password || password.length < 6) {
      return fail('密码至少6位')
    }

    userCounter++
    return ok({
      userId: userCounter,
      username,
      nickname: nickname || username,
      message: '注册成功'
    })
  }),

  http.post('/wap/auth/trial', () => {
    userCounter++
    return ok({
      token: 'trial-token-' + Date.now(),
      userInfo: {
        id: userCounter,
        username: 'guest_' + userCounter,
        nickname: '试玩用户' + userCounter,
        avatar: '',
        balance: 2000.00,
        vipLevel: 0,
        isTrial: true,
        createTime: new Date().toISOString()
      }
    })
  }),

  http.post('/wap/auth/logout', () => {
    return ok({ message: '退出成功' })
  }),

  http.post('/wap/auth/sms-code', async ({ request }) => {
    const body = await request.json()
    if (!body.phone) return fail('请输入手机号')
    return ok({ message: '验证码已发送', code: '123456' })
  }),

  http.post('/wap/auth/reset-password', async ({ request }) => {
    const body = await request.json()
    if (!body.phone || !body.code || !body.password) {
      return fail('参数不完整')
    }
    return ok({ message: '密码重置成功' })
  })
]
