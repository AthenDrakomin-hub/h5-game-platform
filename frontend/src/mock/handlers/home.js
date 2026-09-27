import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })
const fail = (message, code = 1001) => HttpResponse.json({ code, data: null, message })

export const homeHandlers = [
  http.get('/wap/home/config', () => {
    return ok({
      siteName: 'H5 Clone Demo',
      logo: '/images/logo.png',
      slogan: '官方直营 · 信誉首选',
      customerService: {
        type: 'chat',
        url: '/chat'
      },
      downloadUrl: '/download-app',
      announcement: {
        id: 1,
        title: '系统公告',
        content: '欢迎使用 H5 Clone Demo，本平台仅供技术学习演示使用。',
        show: true
      },
      vipLevels: [
        { level: 1, name: '普通会员', minAmount: 0 },
        { level: 2, name: '白银会员', minAmount: 1000 },
        { level: 3, name: '黄金会员', minAmount: 10000 },
        { level: 4, name: '铂金会员', minAmount: 50000 },
        { level: 5, name: '钻石会员', minAmount: 100000 }
      ]
    })
  }),

  http.get('/wap/home/banners', () => {
    return ok([
      { id: 1, image: 'https://picsum.photos/seed/banner1/800/360', link: '/promo/detail/1', title: '新人专享' },
      { id: 2, image: 'https://picsum.photos/seed/banner2/800/360', link: '/promo/detail/2', title: '每日签到' },
      { id: 3, image: 'https://picsum.photos/seed/banner3/800/360', link: '/casino', title: '娱乐城狂欢' }
    ])
  }),

  http.get('/wap/home/quick-nav', () => {
    return ok([
      { id: 1, name: '充值', icon: 'wallet', path: '/user/recharge', requiresAuth: true },
      { id: 2, name: '提现', icon: 'balance-list', path: '/user/withdraw', requiresAuth: true },
      { id: 3, name: '优惠', icon: 'gift', path: '/promo' },
      { id: 4, name: '客服', icon: 'service', path: '/chat' },
      { id: 5, name: '签到', icon: 'success', path: '/user/welfare', requiresAuth: true },
      { id: 6, name: '推广', icon: 'share', path: '/user/promote-earn', requiresAuth: true },
      { id: 7, name: 'VIP', icon: 'vip', path: '/user/vip', requiresAuth: true },
      { id: 8, name: '消息', icon: 'chat', path: '/user/message', requiresAuth: true }
    ])
  }),

  http.get('/wap/home/game-entries', () => {
    return ok([
      { id: 1, name: 'PK10', icon: '🏎️', path: '/pk10/bjpk10', category: 'pk10' },
      { id: 2, name: '六合彩', icon: '🎱', path: '/lhc/hk6', category: 'lhc' },
      { id: 3, name: '时时彩', icon: '🎰', path: '/ssc/cqssc', category: 'ssc' },
      { id: 4, name: '加拿大28', icon: '🎲', path: '/pc28/pc28', category: 'pc28' },
      { id: 5, name: '娱乐城', icon: '🎡', path: '/casino', category: 'casino' },
      { id: 6, name: '开奖结果', icon: '📋', path: '/results', category: 'results' }
    ])
  }),

  http.get('/wap/home/notice', () => {
    return ok({
      id: 1,
      title: '系统维护通知',
      content: '本平台将于每周三凌晨 02:00-04:00 进行系统维护，期间部分功能可能暂时不可用。',
      show: true,
      createdAt: '2026-09-20 10:00:00'
    })
  }),

  http.get('/wap/home/announcements', () => {
    return ok({
      list: [
        { id: 1, title: '关于系统升级的公告', createdAt: '2026-09-25' },
        { id: 2, title: '新游戏上线通知', createdAt: '2026-09-22' },
        { id: 3, title: '中秋节活动公告', createdAt: '2026-09-15' }
      ],
      total: 3
    })
  })
]
