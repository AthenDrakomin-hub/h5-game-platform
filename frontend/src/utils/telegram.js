/**
 * Telegram Mini App 适配层
 * 封装 Telegram WebApp SDK，提供统一的初始化、用户、主题、按钮、反馈、分享能力
 * 通过 VITE_TELEGRAM_ENABLED 环境变量开关
 *
 * 版本兼容：所有 6.1+ 才引入的 API（BackButton/setHeaderColor/disableVerticalSwipes 等）
 * 调用前必须通过 isVersionAtLeast('6.1') 检查，低版本静默降级，避免控制台警告刷屏
 */

const TG = typeof window !== 'undefined' ? window.Telegram?.WebApp : null

export const isTelegram = !!TG

/* ===== 版本工具 ===== */

/**
 * 比较版本号，返回当前 TG 版本是否 >= minVersion
 * @param {string} minVersion 如 '6.1'、'7.0'
 */
export function isVersionAtLeast(minVersion) {
  if (!TG?.version) return false
  const cur = String(TG.version).split('.').map(Number)
  const min = String(minVersion).split('.').map(Number)
  for (let i = 0; i < Math.max(cur.length, min.length); i++) {
    const c = cur[i] || 0
    const m = min[i] || 0
    if (c > m) return true
    if (c < m) return false
  }
  return true
}

/** 安全调用：版本不支持时静默跳过 */
function safeCall(minVersion, fn) {
  if (!isVersionAtLeast(minVersion)) return
  try { fn() } catch (e) { /* 静默降级 */ }
}

/* ===== 初始化 ===== */

/** 初始化 Telegram WebApp，必须在应用启动时调用 */
export function initTelegram() {
  if (!TG) return false
  try {
    TG.ready()
    TG.expand()
    // disableVerticalSwipes 需要 6.1+
    safeCall('6.1', () => TG.disableVerticalSwipes())
    // setHeaderColor 需要 6.1+
    safeCall('6.1', () => TG.setHeaderColor('secondary_bg_color'))
    return true
  } catch (e) {
    console.warn('[Telegram] init failed:', e)
    return false
  }
}

/* ===== 用户信息 ===== */

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

/* ===== 主题 ===== */

/** 获取主题参数 */
export function getThemeParams() {
  if (!TG) return null
  return TG.themeParams || {}
}

/** 监听主题变化 */
export function onThemeChange(callback) {
  if (!TG) return () => {}
  const handler = () => callback(getThemeParams())
  try { TG.onEvent('themeChanged', handler) } catch (e) { /* noop */ }
  return () => { try { TG.offEvent('themeChanged', handler) } catch (e) { /* noop */ } }
}

/** 应用 Telegram 主题到 CSS 变量（与现有深色金调做兼容合并） */
export function applyTelegramTheme(params) {
  if (!params) return
  const root = document.documentElement
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
    if (params[tgKey]) root.style.setProperty(cssVar, params[tgKey])
  })
  root.classList.add('telegram-env')
}

/* ===== BackButton（需要 6.1+） ===== */

let _backClickHandler = null

export function showBackButton(onClick) {
  if (!TG?.BackButton || !isVersionAtLeast('6.1')) return
  try {
    // 先解绑旧 handler，避免重复绑定
    if (_backClickHandler) {
      TG.BackButton.offClick(_backClickHandler)
    }
    _backClickHandler = onClick
    TG.BackButton.onClick(onClick)
    TG.BackButton.show()
  } catch (e) { /* 静默降级 */ }
}

export function hideBackButton() {
  if (!TG?.BackButton || !isVersionAtLeast('6.1')) return
  try {
    TG.BackButton.hide()
    if (_backClickHandler) {
      TG.BackButton.offClick(_backClickHandler)
      _backClickHandler = null
    }
  } catch (e) { /* 静默降级 */ }
}

/* ===== MainButton（需要 6.0+，基础可用） ===== */

let _mainClickHandler = null

export function showMainButton(text, onClick, options = {}) {
  if (!TG?.MainButton) return
  try {
    TG.MainButton.setText(text)
    if (options.color) TG.MainButton.setParams({ color: options.color })
    if (_mainClickHandler) TG.MainButton.offClick(_mainClickHandler)
    _mainClickHandler = onClick
    TG.MainButton.onClick(onClick)
    TG.MainButton.show()
  } catch (e) { /* 静默降级 */ }
}

export function hideMainButton() {
  if (!TG?.MainButton) return
  try {
    TG.MainButton.hide()
    if (_mainClickHandler) {
      TG.MainButton.offClick(_mainClickHandler)
      _mainClickHandler = null
    }
  } catch (e) { /* 静默降级 */ }
}

export function setMainButtonLoading(loading) {
  if (!TG?.MainButton) return
  try {
    if (loading) TG.MainButton.showProgress()
    else TG.MainButton.hideProgress()
  } catch (e) { /* 静默降级 */ }
}

/* ===== HapticFeedback 触觉反馈 ===== */

export const haptic = {
  impact(style = 'medium') {
    try { TG?.HapticFeedback?.impactOccurred?.(style) } catch (e) { /* noop */ }
  },
  success() {
    try { TG?.HapticFeedback?.notificationOccurred?.('success') } catch (e) { /* noop */ }
  },
  warning() {
    try { TG?.HapticFeedback?.notificationOccurred?.('warning') } catch (e) { /* noop */ }
  },
  error() {
    try { TG?.HapticFeedback?.notificationOccurred?.('error') } catch (e) { /* noop */ }
  },
  selection() {
    try { TG?.HapticFeedback?.selectionChanged?.() } catch (e) { /* noop */ }
  }
}

/* ===== 分享 ===== */

export function shareToTelegram(text, url = '') {
  if (!TG) {
    if (navigator.share) {
      navigator.share({ title: text, text, url }).catch(() => {})
    }
    return
  }
  try {
    const shareUrl = url || window.location.href
    TG.openTelegramLink(`https://t.me/share/url?url=${encodeURIComponent(shareUrl)}&text=${encodeURIComponent(text)}`)
  } catch (e) { /* noop */ }
}

/* ===== 关闭 Mini App ===== */

export function closeApp() {
  try { TG?.close?.() } catch (e) { /* noop */ }
}

/* ===== 版本信息 ===== */

export function getTgVersion() {
  return TG?.version || ''
}

export function getPlatform() {
  return TG?.platform || ''
}
