// 安全返回 - 防止深层页面返回跳出应用
export function installSafeBack(router) {
  if (router.__safeBackInstalled) return

  const originalBack = router.back.bind(router)
  const HOME_PATH = '/'

  const redirectMap = [
    [/^\/user\/recharge/, '/user/center'],
    [/^\/user\/withdraw/, '/user/center'],
    [/^\/user\/(orders|bet-records|transaction)/, '/user/center'],
    [/^\/user\/(message|settings|vip)/, '/user/center'],
    [/^\/pk10|^\/ssc|^\/lhc|^\/pc28/, '/lottery'],
    [/^\/casino-sub-games|^\/km-games/, '/casino'],
    [/^\/promo\/detail/, '/promo']
  ]

  function getFallback(path) {
    for (const [pattern, fallback] of redirectMap) {
      if (pattern.test(path)) return fallback
    }
    return HOME_PATH
  }

  router.back = function() {
    const current = router.currentRoute.value
    const canGoBack = window.history.length > 1
    if (canGoBack) {
      originalBack()
    } else {
      router.replace(getFallback(current.path))
    }
  }

  router.safeBack = function(fallback) {
    const current = router.currentRoute.value
    const canGoBack = window.history.length > 1
    if (canGoBack) {
      originalBack()
    } else {
      router.replace(fallback || getFallback(current.path))
    }
  }

  router.__safeBackInstalled = true
}
