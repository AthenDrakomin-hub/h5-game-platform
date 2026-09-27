/**
 * Telegram Mini App 适配层
 * 封装 Telegram WebApp SDK，提供统一的初始化、用户、主题、按钮、反馈、分享能力
 * 通过 VITE_TELEGRAM_ENABLED 环境变量开关
 */

const TG = typeof window !== 'undefined' ? window.Telegram?.WebApp : null

export const isTelegram = !!TG

/** 初始化 Telegram WebApp，必须在应用启动时调用 */
export function initTelegram() {
  if (!TG) return false
  try {
    TG.ready()
    TG.expand()
    // 禁用垂直滑动关闭（防止误触）
    if (TG.disableVerticalSwipes) TG.disableVerticalSwipes()
    // 设置头部颜色
    if (TG.setHeaderColor) TG.setHeaderColor('secondary_bg_color')
    return true
  } catch (e) {
    console.warn('[Telegram] init failed:', e)
    return false
  }
}

/** 获取 Telegram 用户信息（从 initData 解析） */
export function getTelegramUser() {
  if (!TG || !TG.initDataUnsafe?.user) return null
  const u = TG.initDataUnsafe.user
  return {
    id: u.id,
    username: u.username || `tg_${u.id}`,
    nickname: u.first_name ? (u.last_name ? `${u.first_name} ${u.last_name}` : u.first_name) : u.username || `TG${u.id}`,
    avatar: u.photo_url || '',
    languageCode: u.language_code || 'zh-cn',
    isPremium: !!u.is_premium
  }
}

/** 获取 initData（用于后端验证） */
export function getInitData() {
  return TG?.initData || ''
}

/** 获取主题参数 */
export function getThemeParams() {
  if (!TG) return null
  return TG.themeParams || {}
}

/** 监听主题变化 */
export function onThemeChange(callback) {
  if (!TG) return () => {}
  const handler = () => callback(getThemeParams())
  TG.onEvent('themeChanged', handler)
  return () => TG.offEvent('themeChanged', handler)
}

/** 应用 Telegram 主题到 CSS 变量（与现有深色金调做兼容合并） */
export function applyTelegramTheme(params) {
  if (!params) return
  const root = document.documentElement
  // Telegram 主题色映射到 Vant 变量
  // 仅覆盖背景/文字类，保留品牌金调主色
  const map = {
    bg_color: '--tg-bg-color',
    text_color: '--tg-text-color',
    hint_color: '--tg-hint-color',
    link_color: '--tg-link-color',
    button_color: '--tg-button-color',
    button_text_color: '--tg-button-text-color',
    secondary_bg_color: '--tg-secondary-bg-color'
  }
  Object.entries(map).forEach(([tgKey, cssVar]) => {
    if (params[tgKey]) {
      root.style.setProperty(cssVar, params[tgKey])
    }
  })
  // 标记 Telegram 环境
  root.classList.add('telegram-env')
}

/* ===== BackButton ===== */
export function showBackButton(onClick) {
  if (!TG?.BackButton) return
  TG.BackButton.show()
  TG.BackButton.onClick(onClick)
}

export function hideBackButton() {
  if (!TG?.BackButton) return
  TG.BackButton.hide()
  TG.BackButton.offClick()
}

/* ===== MainButton ===== */
export function showMainButton(text, onClick, options = {}) {
  if (!TG?.MainButton) return
  TG.MainButton.setText(text)
  if (options.color) TG.MainButton.setParams({ color: options.color })
  TG.MainButton.show()
  TG.MainButton.onClick(onClick)
}

export function hideMainButton() {
  if (!TG?.MainButton) return
  TG.MainButton.hide()
  TG.MainButton.offClick()
}

export function setMainButtonLoading(loading) {
  if (!TG?.MainButton) return
  if (loading) TG.MainButton.showProgress()
  else TG.MainButton.hideProgress()
}

/* ===== HapticFeedback 触觉反馈 ===== */
export const haptic = {
  impact(style = 'medium') {
    // style: light | medium | heavy | rigid | soft
    TG?.HapticFeedback?.impactOccurred?.(style)
  },
  success() {
    TG?.HapticFeedback?.notificationOccurred?.('success')
  },
  warning() {
    TG?.HapticFeedback?.notificationOccurred?.('warning')
  },
  error() {
    TG?.HapticFeedback?.notificationOccurred?.('error')
  },
  selection() {
    TG?.HapticFeedback?.selectionChanged?.()
  }
}

/* ===== 分享 ===== */
export function shareToTelegram(text, url = '') {
  if (!TG) {
    // 非 Telegram 环境，用 Web Share API 或复制
    if (navigator.share) {
      navigator.share({ title: text, text, url }).catch(() => {})
    }
    return
  }
  const shareUrl = url || window.location.href
  TG.openTelegramLink(`https://t.me/share/url?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(text)}`)
}

/* ===== 关闭 Mini App ===== */
export function closeApp() {
  TG?.close?.()
}

/* ===== 版本信息 ===== */
export function getTgVersion() {
  return TG?.version || ''
}

export function getPlatform() {
  return TG?.platform || ''
}
