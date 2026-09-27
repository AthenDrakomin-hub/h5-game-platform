import { homeHandlers } from './home'
import { authHandlers } from './auth'
import { lotteryHandlers } from './lottery'
import { casinoHandlers } from './casino'
import { promoHandlers } from './promo'
import { userHandlers } from './user'

export const handlers = [
  ...homeHandlers,
  ...authHandlers,
  ...lotteryHandlers,
  ...casinoHandlers,
  ...promoHandlers,
  ...userHandlers
]
