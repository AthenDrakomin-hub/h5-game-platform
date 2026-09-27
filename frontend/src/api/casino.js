import request from './request'

export const casinoApi = {
  getPlatforms() {
    return request.get('/wap/game/casino-providers')
  },
  getGames(params = {}) {
    return request.get('/wap/game/casino-games', { params })
  },
  getAllGames(params = {}) {
    return request.get('/wap/game/all', { params })
  },
  getBigPrizeGames() {
    return request.get('/wap/game/big-prize-ticker-games')
  },
  enterGame(platform, gameId) {
    return request.post(`/wap/${platform}/enter`, { gameId })
  },
  getPlatformBalance(platform) {
    return request.get(`/wap/${platform}/balance`)
  },
  withdrawFromPlatform(platform, data) {
    return request.post(`/wap/${platform}/withdraw`, data)
  },
  getKmPlatforms() {
    return request.get('/wap/km/platforms')
  },
  getTransferBalances() {
    return request.get('/wap/transfer/balances')
  },
  transferToVenue(data) {
    return request.post('/wap/transfer/to-venue', data)
  },
  transferFromVenue(data) {
    return request.post('/wap/transfer/from-venue', data)
  },
  getTransferList(params = {}) {
    return request.get('/wap/transfer/list', { params })
  }
}

// ===== 兼容旧方法名 + 数据结构 adapter =====
casinoApi.getPlatforms = async () => {
  const raw = await request.get('/wap/game/casino-providers')
  return (raw || []).map((p, i) => ({
    id: i + 1,
    code: p.code,
    name: p.name,
    icon: '🎮',
    status: 'online'
  }))
}
casinoApi.getGames = async (params = {}) => {
  const raw = await request.get('/wap/game/casino-games', { params })
  const list = raw?.list || raw?.records || []
  return {
    list: list.map(g => ({
      id: g.id,
      name: g.name,
      gameCode: g.gameCode,
      icon: g.icon || g.venue?.logo || '',
      venue: g.venue?.name || g.venueCode || '',
      venueCode: g.providerCode || g.venue?.code || '',
      category: g.venueCategory || g.category || '',
      categoryName: g.venueCategoryName || '',
      isHot: g.isHot === 1 || g.isHot === true,
      isHomeHot: g.isHomeHot === 1 || g.isHomeHot === true
    })),
    total: raw?.total || list.length,
    page: raw?.page || 1,
    pages: raw?.pages || 1
  }
}
casinoApi.getGameUrl = (gameId) => casinoApi.enterGame('ag', gameId)
casinoApi.getHotGames = () => casinoApi.getAllGames({ isHot: 1 })
casinoApi.getNewGames = () => casinoApi.getAllGames({ sort: 'new' })
casinoApi.getFavorites = () => casinoApi.getAllGames({ favorite: true })
casinoApi.toggleFavorite = () => Promise.resolve({ success: true })
