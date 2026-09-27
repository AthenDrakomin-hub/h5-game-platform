import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })
const fail = (message, code = 1001) => HttpResponse.json({ code, data: null, message })

let balance = 10000.00

export const userHandlers = [
  // 余额与资料
  http.get('/wap/user/balance', () => {
    return ok({ balance, vipLevel: 2, frozenAmount: 0, totalRecharge: 50000, totalWithdraw: 30000 })
  }),

  http.get('/wap/user/profile', () => {
    return ok({
      id: 1,
      username: 'demo',
      nickname: '演示用户',
      avatar: '',
      phone: '138****8888',
      email: '',
      vipLevel: 2,
      vipName: '白银会员',
      isRealName: false,
      hasFundPassword: false,
      registerTime: '2026-01-01 00:00:00',
      lastLoginTime: new Date().toISOString(),
      lastLoginIp: '192.168.1.1'
    })
  }),

  http.put('/wap/user/profile', async ({ request }) => {
    const body = await request.json()
    return ok({ ...body, updated: true })
  }),

  // 充值
  http.get('/wap/user/recharge/methods', () => {
    return ok([
      { id: 'usdt', name: 'USDT充值', icon: '💰', status: 'online', minAmount: 100, maxAmount: 50000 },
      { id: 'bank', name: '银行卡转账', icon: '🏦', status: 'online', minAmount: 100, maxAmount: 100000 },
      { id: 'alipay', name: '支付宝', icon: '💙', status: 'online', minAmount: 50, maxAmount: 20000 },
      { id: 'wechat', name: '微信支付', icon: '💚', status: 'online', minAmount: 50, maxAmount: 20000 },
      { id: 'digital_cny', name: '数字人民币', icon: '💴', status: 'online', minAmount: 100, maxAmount: 50000 },
      { id: 'card_secret', name: '卡密充值', icon: '🎫', status: 'online', minAmount: 10, maxAmount: 10000 }
    ])
  }),

  http.post('/wap/user/recharge/order', async ({ request }) => {
    const body = await request.json()
    const orderNo = 'RECHARGE' + Date.now()
    balance += parseFloat(body.amount || 0)
    return ok({
      orderNo,
      amount: body.amount,
      method: body.method,
      status: 'pending',
      payUrl: `https://example.com/pay/${orderNo}`,
      qrCode: `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=mock_pay_${orderNo}`,
      expireTime: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
      createdAt: new Date().toISOString()
    })
  }),

  http.get('/wap/user/recharge/order/:orderNo', ({ params }) => {
    return ok({
      orderNo: params.orderNo,
      amount: 1000,
      method: 'usdt',
      status: 'success',
      confirmedAt: new Date().toISOString(),
      createdAt: new Date(Date.now() - 300000).toISOString()
    })
  }),

  // 提现
  http.get('/wap/user/withdraw/config', () => {
    return ok({
      minAmount: 100,
      maxAmount: 50000,
      feeRate: 0.01,
      dailyLimit: 100000,
      todayWithdrawn: 5000,
      requireFundPassword: true
    })
  }),

  http.post('/wap/user/withdraw', async ({ request }) => {
    const body = await request.json()
    const orderNo = 'WITHDRAW' + Date.now()
    balance -= parseFloat(body.amount || 0)
    return ok({
      orderNo,
      amount: body.amount,
      fee: body.amount * 0.01,
      actualAmount: body.amount * 0.99,
      method: body.method,
      status: 'pending',
      createdAt: new Date().toISOString(),
      estimatedTime: '10-30分钟'
    })
  }),

  // 银行卡
  http.get('/wap/user/bank-cards', () => {
    return ok([
      { id: 1, bankName: '中国工商银行', cardNumber: '6222 **** **** 8888', holderName: '张*', isDefault: true, status: 'active' },
      { id: 2, bankName: '中国建设银行', cardNumber: '6217 **** **** 6666', holderName: '张*', isDefault: false, status: 'active' }
    ])
  }),

  http.post('/wap/user/bank-cards', async ({ request }) => {
    const body = await request.json()
    return ok({ id: Date.now(), ...body, status: 'active' })
  }),

  http.delete('/wap/user/bank-cards/:id', () => {
    return ok({ deleted: true })
  }),

  // 钱包地址
  http.get('/wap/user/wallet-addresses', ({ request }) => {
    const url = new URL(request.url)
    const type = url.searchParams.get('type') || 'usdt'
    return ok([
      { id: 1, type, network: 'TRC20', address: 'T' + 'x'.repeat(32), label: '常用钱包', isDefault: true, status: 'active' },
      { id: 2, type, network: 'ERC20', address: '0x' + 'x'.repeat(40), label: '以太坊钱包', isDefault: false, status: 'active' }
    ])
  }),

  http.post('/wap/user/wallet-addresses', async ({ request }) => {
    const body = await request.json()
    return ok({ id: Date.now(), ...body, status: 'active' })
  }),

  http.delete('/wap/user/wallet-addresses/:id', () => {
    return ok({ deleted: true })
  }),

  // 订单/记录
  http.get('/wap/user/orders', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => {
      const type = i % 2 === 0 ? 'recharge' : 'withdraw'
      return {
        id: (page - 1) * pageSize + i,
        orderNo: (type === 'recharge' ? 'RECHARGE' : 'WITHDRAW') + (Date.now() - i * 1000),
        type,
        amount: type === 'recharge' ? 1000 + i * 100 : 500 + i * 50,
        method: type === 'recharge' ? 'USDT' : '银行卡',
        status: i % 3 === 0 ? 'success' : i % 3 === 1 ? 'pending' : 'failed',
        createdAt: new Date(Date.now() - i * 3600000).toISOString()
      }
    })
    return ok({ list, total: 100, page, pageSize })
  }),

  http.get('/wap/user/bet-records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      betId: 'BET' + (Date.now() - i * 1000),
      gameName: ['北京PK10', '重庆时时彩', '加拿大28'][i % 3],
      issue: '20260927-0' + (10 - (i % 10)),
      playType: ['冠军', '大小', '和值'][i % 3],
      betContent: ['5号', '大', '13'][i % 3],
      amount: 10 + i,
      odds: 9.8,
      status: i % 3 === 0 ? 'won' : i % 3 === 1 ? 'lost' : 'pending',
      winAmount: i % 3 === 0 ? (10 + i) * 9.8 : 0,
      createdAt: new Date(Date.now() - i * 180000).toISOString()
    }))
    return ok({ list, total: 200, page, pageSize })
  }),

  http.get('/wap/user/transactions', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const types = ['recharge', 'withdraw', 'bet', 'win', 'rebate', 'adjustment']
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      type: types[i % types.length],
      typeName: ['充值', '提现', '投注', '中奖', '返水', '调整'][i % types.length],
      amount: (i % 2 === 0 ? 1 : -1) * (50 + i * 10),
      balanceAfter: 10000 - i * 100,
      remark: '账变备注' + i,
      createdAt: new Date(Date.now() - i * 600000).toISOString()
    }))
    return ok({ list, total: 500, page, pageSize })
  }),

  http.get('/wap/user/game-records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      gameName: ['AG百家乐', 'PG老虎机', '捕鱼王'][i % 3],
      platform: ['AG', 'PG', 'FS'][i % 3],
      roundId: 'ROUND' + (Date.now() - i * 1000),
      betAmount: 50 + i * 10,
      payout: i % 2 === 0 ? 100 + i * 10 : 0,
      profit: i % 2 === 0 ? 50 : -50 - i * 10,
      status: 'settled',
      playTime: new Date(Date.now() - i * 600000).toISOString()
    }))
    return ok({ list, total: 150, page, pageSize })
  }),

  // 消息
  http.get('/wap/user/messages', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      title: ['充值到账通知', '中奖通知', '系统公告', '活动提醒'][i % 4],
      content: ['您的充值1000元已到账', '恭喜您中奖98元', '系统将于今晚维护', '新活动已上线'][i % 4],
      type: ['recharge', 'win', 'system', 'promo'][i % 4],
      isRead: i > 5,
      createdAt: new Date(Date.now() - i * 3600000).toISOString()
    }))
    return ok({ list, total: 30, page, pageSize, unreadCount: 6 })
  }),

  http.put('/wap/user/messages/:id/read', () => {
    return ok({ read: true })
  }),

  http.get('/wap/user/announcements', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      title: '系统公告第' + (i + 1) + '期',
      content: '这是一条系统公告内容，用于演示。',
      createdAt: new Date(Date.now() - i * 86400000).toISOString()
    }))
    return ok({ list, total: 10, page, pageSize })
  }),

  // 设置
  http.post('/wap/user/change-password', async ({ request }) => {
    const body = await request.json()
    if (!body.oldPassword || !body.newPassword) {
      return fail('参数不完整')
    }
    return ok({ message: '密码修改成功' })
  }),

  http.post('/wap/user/fund-password', async ({ request }) => {
    const body = await request.json()
    return ok({ message: '资金密码设置成功' })
  }),

  http.post('/wap/user/bind-phone', async ({ request }) => {
    const body = await request.json()
    return ok({ phone: body.phone, message: '手机绑定成功' })
  }),

  // VIP
  http.get('/wap/user/vip', () => {
    return ok({
      currentLevel: 2,
      currentName: '白银会员',
      nextLevel: 3,
      nextName: '黄金会员',
      progress: 65,
      amountToNext: 3500,
      benefits: [
        { level: 1, name: '普通会员', rebate: '0.5%', dailyWithdraw: 50000 },
        { level: 2, name: '白银会员', rebate: '0.8%', dailyWithdraw: 100000 },
        { level: 3, name: '黄金会员', rebate: '1.0%', dailyWithdraw: 200000 },
        { level: 4, name: '铂金会员', rebate: '1.2%', dailyWithdraw: 500000 },
        { level: 5, name: '钻石会员', rebate: '1.5%', dailyWithdraw: 1000000 }
      ]
    })
  }),

  // 代理
  http.get('/wap/user/agent', () => {
    return ok({
      isAgent: true,
      agentCode: 'AGENT001',
      inviteUrl: 'https://example.com/register/AGENT001',
      totalMembers: 128,
      activeMembers: 45,
      totalCommission: 12580.50,
      todayCommission: 356.80,
      pendingCommission: 1200.00
    })
  }),

  http.get('/wap/user/agent/records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      memberName: 'user_' + (100 + i),
      memberLevel: i % 3 + 1,
      betAmount: 1000 + i * 100,
      commission: 10 + i,
      status: i % 2 === 0 ? 'settled' : 'pending',
      createdAt: new Date(Date.now() - i * 86400000).toISOString()
    }))
    return ok({ list, total: 500, page, pageSize })
  }),

  // 礼金/福利
  http.get('/wap/user/welfare', () => {
    return ok([
      { id: 1, name: '每日签到', type: 'daily', amount: 8, status: 'available', icon: '📅' },
      { id: 2, name: '注册礼金', type: 'register', amount: 88, status: 'claimed', icon: '🎁' },
      { id: 3, name: '首充赠送', type: 'first_deposit', amount: 100, status: 'claimed', icon: '💰' },
      { id: 4, name: '生日礼金', type: 'birthday', amount: 188, status: 'locked', icon: '🎂' },
      { id: 5, name: '每周救援', type: 'rescue', amount: 500, status: 'available', icon: '🛟' }
    ])
  }),

  http.post('/wap/user/welfare/:id/claim', ({ params }) => {
    return ok({ welfareId: params.id, claimed: true, amount: 88 })
  }),

  http.get('/wap/user/welfare/records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      welfareName: ['每日签到', '注册礼金', '首充赠送'][i % 3],
      amount: 8 + i * 10,
      status: 'claimed',
      claimedAt: new Date(Date.now() - i * 86400000).toISOString()
    }))
    return ok({ list, total: 30, page, pageSize })
  }),

  // 反馈
  http.post('/wap/user/feedback', async ({ request }) => {
    const body = await request.json()
    return ok({ id: Date.now(), ...body, status: 'submitted' })
  })
]
