import request from './request'

export const promoApi = {
  getList(params = {}) {
    return request.get('/wap/promotion/list', { params })
  },
  getFloatActivities() {
    return request.get('/wap/activity-float/list')
  },
  getFloatEvent() {
    return request.get('/wap/activity-float/event')
  },
  getRewardList(params = {}) {
    return request.get('/wap/reward/list', { params })
  },
  getRewardSummary() {
    return request.get('/wap/reward/summary')
  },
  claimAllRewards() {
    return request.post('/wap/reward/claim-all')
  },
  getRechargeRewardPreview(data) {
    return request.post('/wap/recharge-reward/preview', data)
  },
  getRechargeRewardSummary() {
    return request.get('/wap/recharge-reward/summary')
  },
  getRegisterBonusSummary() {
    return request.get('/wap/register-bonus/summary')
  },
  getTaskList() {
    return request.get('/wap/task/list')
  },
  signin() {
    return request.post('/wap/task/signin')
  },
  getSigninInfo() {
    return request.get('/wap/task/signin/info')
  },
  claimAllTasks() {
    return request.post('/wap/task/claim-all')
  },
  getLuckyWheelInfo() {
    return request.get('/wap/lucky-wheel/info')
  },
  drawLuckyWheel() {
    return request.post('/wap/lucky-wheel/draw')
  },
  getLossRescueSummary() {
    return request.get('/wap/loss-rescue/summary')
  },
  getInviteInfo() {
    return request.get('/wap/invite/info')
  }
}

// ===== 兼容旧方法名 + 数据结构 adapter =====
promoApi.getList = async (params = {}) => {
  const raw = await request.get('/wap/promotion/list', { params })
  const list = Array.isArray(raw) ? raw : (raw?.list || raw?.records || [])
  return {
    list: list.map(p => ({
      id: p.id,
      title: p.title || '',
      image: p.coverImage || '',
      coverImage: p.coverImage || '',
      tag: p.tag || p.typeName || '',
      type: p.type || '',
      typeName: p.typeName || '',
      description: p.description || p.content || ''
    })),
    total: list.length
  }
}
promoApi.getDetail = (id) => Promise.resolve({ id, title: '活动详情', content: '', image: '' })
promoApi.getCategories = () => Promise.resolve([
  { id: 'all', name: '全部' },
  { id: 'deposit', name: '充值优惠' },
  { id: 'rebate', name: '返水优惠' },
  { id: 'bonus', name: '礼金活动' }
])
promoApi.claim = () => promoApi.claimAllRewards()
promoApi.getRecords = (params) => promoApi.getRewardList(params)
