import { config } from '../config';

/** 主菜单 Inline Keyboard */
export function mainMenuKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '🚀 立即进入游戏', web_app: { url: config.miniAppUrl } }],
        [
          { text: '💰 充值', callback_data: 'wallet_recharge' },
          { text: '📤 提现', callback_data: 'wallet_withdraw' },
          { text: '📊 明细', callback_data: 'wallet_transactions' },
        ],
        [
          { text: '🏎️ 极速赛车', callback_data: 'game_racing' },
          { text: '✈️ 幸运飞艇', callback_data: 'game_airship' },
          { text: '🎰 娱乐城', callback_data: 'game_casino' },
        ],
        [
          { text: '📅 每日签到', callback_data: 'promo_signin' },
          { text: '🎁 活动中心', callback_data: 'promo_list' },
          { text: '👥 邀请返利', callback_data: 'promo_invite' },
        ],
        [
          { text: '💬 在线客服', callback_data: 'support' },
          { text: '📖 游戏规则', callback_data: 'rules' },
          { text: '⚙️ 设置', callback_data: 'settings' },
        ],
      ],
    },
  };
}

/** 钱包菜单 */
export function walletKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '💰 充值', callback_data: 'wallet_recharge' }],
        [{ text: '📤 提现', callback_data: 'wallet_withdraw' }],
        [{ text: '📊 交易明细', callback_data: 'wallet_transactions' }],
        [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
      ],
    },
  };
}

/** 充值方式选择 */
export function rechargeMethodKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '🏦 银行卡转账', callback_data: 'recharge_bank' }],
        [{ text: '💳 USDT (TRC20)', callback_data: 'recharge_usdt' }],
        [{ text: '📱 在线支付', callback_data: 'recharge_online' }],
        [{ text: '🔙 返回', callback_data: 'wallet_menu' }],
      ],
    },
  };
}

/** 游戏快捷入口 */
export function gameKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '🏎️ 极速赛车', callback_data: 'game_racing' }],
        [{ text: '✈️ 幸运飞艇', callback_data: 'game_airship' }],
        [{ text: '🎯 时时彩', callback_data: 'game_lottery' }],
        [{ text: '🎰 娱乐城', callback_data: 'game_casino' }],
        [{ text: '🚀 进入游戏大厅', web_app: { url: config.miniAppUrl } }],
        [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
      ],
    },
  };
}

/** 活动菜单 */
export function promoKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '📅 每日签到', callback_data: 'promo_signin' }],
        [{ text: '🎁 优惠活动', callback_data: 'promo_list' }],
        [{ text: '👥 邀请返利', callback_data: 'promo_invite' }],
        [{ text: '👑 VIP 特权', callback_data: 'promo_vip' }],
        [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
      ],
    },
  };
}

/** 客服菜单 */
export function supportKeyboard() {
  return {
    reply_markup: {
      inline_keyboard: [
        [{ text: '💰 充值问题', callback_data: 'faq_recharge' }],
        [{ text: '📤 提现问题', callback_data: 'faq_withdraw' }],
        [{ text: '🎮 游戏问题', callback_data: 'faq_game' }],
        [{ text: '👤 账号问题', callback_data: 'faq_account' }],
        [{ text: '🧑‍💼 转人工客服', callback_data: 'support_human' }],
        [{ text: '🔙 返回主菜单', callback_data: 'main_menu' }],
      ],
    },
  };
}

/** 返回按钮 */
export function backKeyboard(callbackData: string = 'main_menu') {
  return {
    reply_markup: {
      inline_keyboard: [[{ text: '🔙 返回', callback_data: callbackData }]],
    },
  };
}
