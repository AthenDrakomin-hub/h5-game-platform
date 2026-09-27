import request from './request'

export const userApi = {
  getInfo() {
    return request.get('/wap/user/info', { skipAuthToast: true })
  },
  getAvatars() {
    return request.get('/wap/user/avatars')
  },
  updateAvatar(avatar) {
    return request.post('/wap/user/avatar', { avatar })
  },
  getDeviceInfo() {
    return request.get('/wap/user/device-info')
  },
  getRechargeMethods() {
    return request.get('/wap/payment-methods/recharge-methods')
  },
  submitRecharge(data) {
    return request.post('/wap/recharge/submit', data)
  },
  getRechargeList(params = {}) {
    return request.get('/wap/recharge/list', { params })
  },
  declarePaid(orderNo) {
    return request.post('/wap/recharge/declare-paid', { orderNo })
  },
  manualRechargeOrder(data) {
    return request.post('/wap/recharge/manual-order', data)
  },
  createUsdtOrder(data) {
    return request.post('/wap/recharge/usdt/create-order', data)
  },
  createOrder(channel, data) {
    return request.post(`/wap/${channel}/create-order`, data)
  },
  queryOrder(channel, orderNo) {
    return request.get(`/wap/${channel}/query-order`, { params: { orderNo } })
  },
  getCardSecretMeta() {
    return request.get('/wap/card-secret/meta')
  },
  redeemCardSecret(code) {
    return request.post('/wap/card-secret/redeem', { code })
  },
  getWithdrawMethods() {
    return request.get('/wap/payment-methods/withdraw-methods')
  },
  submitWithdraw(data) {
    return request.post('/wap/withdraw/submit', data)
  },
  getWithdrawList(params = {}) {
    return request.get('/wap/withdraw/list', { params })
  },
  getUsdtRate() {
    return request.get('/wap/rate/usdt-cny')
  },
  getFreeWithdrawInfo() {
    return request.get('/wap/free-withdraw/info')
  },
  submitFreeWithdraw(data) {
    return request.post('/wap/free-withdraw/submit', data)
  },
  getWalletList(params = {}) {
    return request.get('/wap/user-wallet/list', { params })
  },
  saveWallet(data) {
    return request.post('/wap/user-wallet/save', data)
  },
  deleteWallet(id) {
    return request.post('/wap/user-wallet/delete', { id })
  },
  toggleWallet(id) {
    return request.post('/wap/user-wallet/toggle', { id })
  },
  getWalletMethodList() {
    return request.get('/wap/wallet-method/list')
  },
  getWalletMethodTypeList() {
    return request.get('/wap/wallet-method-type/list')
  },
  getTransactionList(params = {}) {
    return request.get('/wap/transaction/list', { params })
  },
  getProfitReport(params = {}) {
    return request.get('/wap/profit-report', { params })
  },
  getAllMessages(params = {}) {
    return request.get('/wap/message/all/list', { params })
  },
  getNotificationMessages(params = {}) {
    return request.get('/wap/message/notification/list', { params })
  },
  getFinanceMessages(params = {}) {
    return request.get('/wap/message/finance/list', { params })
  },
  getPromoMessages(params = {}) {
    return request.get('/wap/message/promo/list', { params })
  },
  readAllMessages() {
    return request.post('/wap/message/read-all')
  },
  deleteReadMessages() {
    return request.post('/wap/message/delete-read')
  },
  getPrivateMessages(params = {}) {
    return request.get('/wap/private-message/list', { params })
  },
  changePassword(data) {
    return request.post('/wap/settings/password', data)
  },
  setWithdrawPassword(data) {
    return request.post('/wap/settings/withdraw-password', data)
  },
  bindPhone(data) {
    return request.post('/wap/settings/phone', data)
  },
  bindEmail(data) {
    return request.post('/wap/settings/email', data)
  },
  setBirthday(data) {
    return request.post('/wap/settings/birthday', data)
  },
  getBindStatus() {
    return request.get('/wap/info-change/bindStatus')
  },
  getVipInfo() {
    return request.get('/wap/vip/info')
  },
  getVipLevels() {
    return request.get('/wap/vip/levels')
  },
  getVipContents() {
    return request.get('/wap/vip/contents')
  },
  getAgentOverview() {
    return request.get('/wap/agent/overview')
  },
  getAgentStats() {
    return request.get('/wap/agent/stats')
  },
  getAgentMembers(params = {}) {
    return request.get('/wap/agent/members', { params })
  },
  getAgentRecords(params = {}) {
    return request.get('/wap/agent/records', { params })
  },
  getAgentMemberStats(params = {}) {
    return request.get('/wap/agent/member-stats', { params })
  },
  getAgentPromoteDashboard() {
    return request.get('/wap/agent/promote-dashboard')
  },
  getAgentWorkbenchStats() {
    return request.get('/wap/agent/workbench-stats')
  },
  getAgentRebateRatio() {
    return request.get('/wap/agent/rebate-ratio')
  },
  getAgentRateScope() {
    return request.get('/wap/agent/rate-scope')
  },
  createAgentMember(data) {
    return request.post('/wap/agent/create-member', data)
  },
  agentWithdraw(data) {
    return request.post('/wap/agent/withdraw', data)
  },
  getAgentLossSummary() {
    return request.get('/wap/agent-loss/summary')
  },
  getAgentLossRecords(params = {}) {
    return request.get('/wap/agent-loss/records', { params })
  },
  getRebateOverview() {
    return request.get('/wap/rebate/v/overview')
  },
  getRebateLadders() {
    return request.get('/wap/rebate/v/ladders')
  },
  getRebateVendorRecords(params = {}) {
    return request.get('/wap/rebate/v/vendor-records', { params })
  },
  claimAllRebate() {
    return request.post('/wap/rebate/v/claim-all')
  },
  getUserRebateList(params = {}) {
    return request.get('/wap/user/rebate/list', { params })
  },
  getBetRewardSummary() {
    return request.get('/wap/bet-reward/summary')
  },
  claimAvailableBetReward() {
    return request.post('/wap/bet-reward/claim-available')
  },
  claimAllBetReward() {
    return request.post('/wap/bet-reward/claim-all')
  },
  getYuebaoConfig() {
    return request.get('/wap/yuebao/config')
  },
  getYuebaoInfo() {
    return request.get('/wap/yuebao/info')
  },
  getYuebaoRecords(params = {}) {
    return request.get('/wap/yuebao/records', { params })
  },
  yuebaoTransferIn(data) {
    return request.post('/wap/yuebao/transfer-in', data)
  },
  yuebaoTransferOut(data) {
    return request.post('/wap/yuebao/transfer-out', data)
  },
  claimAllYuebao() {
    return request.post('/wap/yuebao/claim-all')
  },
  submitFeedback(data) {
    return request.post('/wap/feedback/submit', data)
  },
  getMyFeedback(params = {}) {
    return request.get('/wap/feedback/my-list', { params })
  },
  getFeedbackReplies(id) {
    return request.get('/wap/feedback/replies', { params: { id } })
  },
  uploadImage(formData) {
    return request.post('/wap/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// ===== 兼容旧方法名 + 真实数据结构 adapter =====
const _orig = userApi

userApi.getBalance = async () => {
  const info = await _orig.getInfo()
  return { balance: info?.balance ?? 0, vipLevel: info?.vipLevel ?? 1 }
}
userApi.getProfile = () => _orig.getInfo()

// 充值方式：合并 cnyMethods + cryptoMethods → 页面期望的扁平数组
userApi.getRechargeMethods = async () => {
  const raw = await _orig.getRechargeMethods()
  const cny = (raw?.cnyMethods || []).map(m => ({
    id: m.id,
    name: m.name,
    icon: m.currency === 'cny' ? '💴' : '💰',
    currency: m.currency,
    channel: m.channel,
    code: m.code,
    minAmount: 100,
    maxAmount: 50000,
    status: m.status === 1 ? 'online' : 'offline',
    qrcodeImage: m.qrcodeImage || '',
    payeeName: m.payeeName || '',
    payeeAccount: m.payeeAccount || '',
    tutorial: m.tutorial || '',
    countdownMinutes: m.countdownMinutes || 30,
    raw: m
  }))
  const crypto = (raw?.cryptoMethods || []).map(m => ({
    id: m.id,
    name: m.name,
    icon: '₮',
    currency: m.currency,
    channel: m.channel,
    code: m.code,
    minAmount: 100,
    maxAmount: 500000,
    status: m.status === 1 ? 'online' : 'offline',
    countdownMinutes: m.countdownMinutes || 15,
    raw: m
  }))
  return [...cny, ...crypto]
}
userApi.createRechargeOrder = (data) => _orig.submitRecharge(data)

// 提现方式适配
userApi.getWithdrawConfig = async () => {
  const raw = await _orig.getWithdrawMethods()
  return {
    minAmount: 100,
    dailyLimit: 100000,
    feeRate: 0.01,
    requireFundPassword: true,
    methods: raw || []
  }
}
userApi.createWithdraw = (data) => _orig.submitWithdraw(data)

// 银行卡/钱包适配
userApi.getBankCards = async () => {
  const raw = await _orig.getWalletList({ type: 'bank' })
  const list = raw?.list || raw || []
  return list.map(w => ({
    id: w.id,
    bankName: w.bankName || w.name || '银行卡',
    cardNumber: w.cardNumber || w.account || w.address || '',
    holderName: w.holderName || w.realName || '',
    isDefault: w.isDefault || w.isDefault === 1
  }))
}
userApi.deleteBankCard = (id) => _orig.deleteWallet(id)
userApi.getWalletAddresses = (type = 'usdt') => _orig.getWalletList({ type })

// 订单/交易记录适配
userApi.getOrders = async (params) => {
  const raw = await _orig.getTransactionList(params)
  const list = raw?.list || raw?.records || []
  return {
    list: list.map(t => ({
      id: t.id,
      orderNo: t.orderNo || t.serialNo || t.id,
      type: t.type === 'recharge' || t.amount > 0 ? 'recharge' : 'withdraw',
      amount: Math.abs(t.amount || t.changeAmount || 0),
      method: t.method || t.channel || t.remark || '',
      status: t.status === 1 || t.status === 'success' ? 'success' : (t.status === 0 ? 'pending' : 'failed'),
      createdAt: t.createdAt || t.createTime || ''
    })),
    total: raw?.total || list.length,
    page: raw?.current || 1,
    pages: raw?.pages || 1
  }
}

// 消息列表适配
userApi.getMessages = async (params) => {
  const raw = await _orig.getAllMessages(params)
  const list = raw?.list || raw?.records || []
  return {
    list: list.map(m => ({
      id: m.id,
      title: m.title || '',
      content: m.content || '',
      type: m.type || 'system',
      isRead: m.isRead === 1 || m.isRead === true,
      createdAt: m.createdAt || ''
    })),
    total: raw?.total || list.length,
    unreadCounts: raw?.unreadCounts || {}
  }
}
userApi.readMessage = () => _orig.readAllMessages()

userApi.setFundPassword = (data) => _orig.setWithdrawPassword(data)

// VIP 信息适配
userApi.getVipInfo = async () => {
  const [info, levels] = await Promise.all([_orig.getVipInfo(), _orig.getVipLevels().catch(() => [])])
  const levelList = Array.isArray(levels) ? levels : (levels?.list || [])
  return {
    currentLevel: info?.currentLevel ?? 0,
    currentName: info?.currentLevelName || `VIP${info?.currentLevel ?? 0}`,
    nextName: info?.nextLevelName || '',
    progress: info?.progressPercent ?? 0,
    amountToNext: info?.upgradeNeedAmount ?? info?.upgradeRechargeNeedAmount ?? 0,
    benefits: levelList.map(l => ({
      level: l.id ?? 0,
      name: l.levelName || l.levelTitle || '',
      rebate: l.rebateSlot || l.rebateLottery || '0%',
      dailyWithdraw: l.withdrawLimitRmb || 0
    }))
  }
}
