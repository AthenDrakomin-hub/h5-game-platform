import { http, HttpResponse } from 'msw'

const ok = (data) => HttpResponse.json({ code: 0, data, message: 'success' })

const promos = [
  { id: 1, title: '新人注册送88元彩金', category: 'newbie', image: 'https://picsum.photos/seed/promo1/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: '新用户注册即送88元彩金，可用于所有游戏。' },
  { id: 2, title: '每日签到领红包', category: 'daily', image: 'https://picsum.photos/seed/promo2/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: '每日签到可领取随机红包，连续签到奖励翻倍。' },
  { id: 3, title: '首充100%赠送', category: 'deposit', image: 'https://picsum.photos/seed/promo3/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: '首次充值享受100%赠送，最高赠送888元。' },
  { id: 4, title: 'VIP专属返水', category: 'vip', image: 'https://picsum.photos/seed/promo4/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: 'VIP会员享受高额返水，最高可达1.5%。' },
  { id: 5, title: '邀请好友得佣金', category: 'invite', image: 'https://picsum.photos/seed/promo5/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: '邀请好友注册充值，享受永久佣金分成。' },
  { id: 6, title: '亏损救援金', category: 'rescue', image: 'https://picsum.photos/seed/promo6/800/400', status: 'active', startDate: '2026-09-01', endDate: '2026-12-31', description: '每周亏损达到一定金额可申请救援金。' }
]

export const promoHandlers = [
  http.get('/wap/promo/list', ({ request }) => {
    const url = new URL(request.url)
    const category = url.searchParams.get('category')
    const list = category ? promos.filter(p => p.category === category) : promos
    return ok({ list, total: list.length })
  }),

  http.get('/wap/promo/detail/:id', ({ params }) => {
    const promo = promos.find(p => p.id === parseInt(params.id))
    if (!promo) {
      return HttpResponse.json({ code: 404, data: null, message: '活动不存在' })
    }
    return ok({
      ...promo,
      content: `<p>${promo.description}</p><p><strong>活动规则：</strong></p><ol><li>活动期间内有效</li><li>每位用户仅限参与一次</li><li>最终解释权归平台所有</li></ol>`,
      rules: ['活动期间内有效', '每位用户仅限参与一次', '最终解释权归平台所有']
    })
  }),

  http.get('/wap/promo/categories', () => {
    return ok([
      { id: 'all', name: '全部' },
      { id: 'newbie', name: '新人专享' },
      { id: 'daily', name: '每日活动' },
      { id: 'deposit', name: '充值优惠' },
      { id: 'vip', name: 'VIP专享' },
      { id: 'invite', name: '邀请奖励' }
    ])
  }),

  http.post('/wap/promo/claim/:id', ({ params }) => {
    return ok({ promoId: params.id, claimed: true, amount: 88, message: '领取成功' })
  }),

  http.get('/wap/promo/records', ({ request }) => {
    const url = new URL(request.url)
    const page = parseInt(url.searchParams.get('page') || '1')
    const pageSize = parseInt(url.searchParams.get('pageSize') || '20')
    const list = Array.from({ length: pageSize }, (_, i) => ({
      id: (page - 1) * pageSize + i,
      promoId: i + 1,
      promoTitle: promos[i % promos.length].title,
      amount: 10 + i * 5,
      status: 'claimed',
      claimedAt: new Date(Date.now() - i * 86400000).toISOString()
    }))
    return ok({ list, total: 50, page, pageSize })
  })
]
