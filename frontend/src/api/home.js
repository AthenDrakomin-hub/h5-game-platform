import request from './request'

export const homeApi = {
  getConfig() {
    return request.get('/wap/home/config', { skipErrorToast: true })
  },
  getBanners() {
    return request.get('/wap/home-data/banners/mine')
  },
  getLotteryData() {
    return request.get('/wap/home-data/lottery')
  },
  getCategories() {
    return request.get('/wap/home/categories')
  },
  getGames() {
    return request.get('/wap/home/games')
  },
  getPopupMessages() {
    return request.get('/wap/message/popup/list')
  },
  getAnnouncements(params = {}) {
    return request.get('/wap/message/announcement/list', { params })
  },
  getUnreadCount() {
    return request.get('/wap/message/unread/count')
  }
}

// ===== 兼容旧方法名 + 数据结构 adapter =====
homeApi.getQuickNav = () => Promise.resolve([
  { id: 1, name: '彩票', icon: '🎰', path: '/lottery' },
  { id: 2, name: '娱乐城', icon: '🎲', path: '/casino' },
  { id: 3, name: '优惠', icon: '🎁', path: '/promo' },
  { id: 4, name: '充值', icon: '💰', path: '/user/recharge' },
  { id: 5, name: '提现', icon: '💵', path: '/user/withdraw' },
  { id: 6, name: '客服', icon: '💬', path: '/chat' },
  { id: 7, name: 'VIP', icon: '👑', path: '/user/vip' },
  { id: 8, name: '下载', icon: '📱', path: '/sponsor' }
])
homeApi.getGameEntries = async () => {
  const games = await homeApi.getGames()
  return (games || []).map(g => ({
    id: g.id,
    name: g.name,
    code: g.code,
    icon: g.icon || '',
    iconType: g.iconType,
    bgColor: g.bgColor,
    description: g.description || '',
    status: g.status
  }))
}
homeApi.getNotice = () => homeApi.getPopupMessages()
homeApi.getAnnouncements = (params) => request.get('/wap/message/announcement/list', { params })
