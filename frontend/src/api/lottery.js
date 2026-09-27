import request from './request'

export const lotteryApi = {
  getCategories() {
    return request.get('/wap/home/categories')
  },
  getLotteryData() {
    return request.get('/wap/home-data/lottery')
  },
  getDrawInfo(codes) {
    return request.get('/wap/draw/info/batch', { params: { codes: codes.join(',') } })
  },
  getTrendDragon(code, params = {}) {
    return request.get('/wap/trend/dragon', { params: { code, ...params } })
  },
  getTrendMiss(code, params = {}) {
    return request.get('/wap/trend/miss', { params: { code, ...params } })
  },
  submitBet(data) {
    return request.post('/wap/bet/place', data)
  },
  getPendingBets() {
    return request.get('/wap/bet/pending')
  },
  getBetList(params = {}) {
    return request.get('/wap/bet/list', { params })
  },
  getThirdBetList(params = {}) {
    return request.get('/wap/third-bet/list', { params })
  }
}

// ===== 兼容旧方法名 + 数据结构 adapter =====
lotteryApi.getCategories = async () => {
  const raw = await request.get('/wap/home/categories')
  return (raw || []).filter(c => c.scope === 'lottery' || !c.scope).map(c => ({
    id: c.id,
    name: c.name,
    code: c.code,
    icon: c.icon || c.iconUrl || '',
    badge: c.badge || '',
    status: c.status
  }))
}
lotteryApi.getGameList = async () => {
  const raw = await request.get('/wap/home/games')
  return (raw || []).map(g => ({
    id: g.id,
    name: g.name,
    code: g.code,
    icon: g.icon || '',
    iconType: g.iconType,
    bgColor: g.bgColor,
    description: g.description || '',
    drawInterval: g.drawInterval,
    drawTime: g.drawTime,
    status: g.status
  }))
}
lotteryApi.getCurrentIssue = (code) => lotteryApi.getDrawInfo([code])
lotteryApi.getGameInfo = (code) => lotteryApi.getDrawInfo([code])
lotteryApi.getHistoryResults = (code) => lotteryApi.getDrawInfo([code])
lotteryApi.getTrendData = (code, type = 'dragon') =>
  type === 'miss' ? lotteryApi.getTrendMiss(code) : lotteryApi.getTrendDragon(code)
lotteryApi.getPlayConfig = () => Promise.resolve({})
lotteryApi.getBetRecords = (params) => lotteryApi.getBetList(params)
