import { defineStore } from 'pinia'
import { ref } from 'vue'
import { homeApi } from '@/api/home'

export const useAppStore = defineStore('app', () => {
  const siteConfig = ref(null)
  const showAuthPopup = ref(false)
  const authDefaultTab = ref('login')
  const authRedirect = ref(null)
  const tabbarOverlayDepth = ref(0)

  async function loadConfig() {
    try {
      const data = await homeApi.getConfig()
      siteConfig.value = data
      if (data?.siteName) {
        document.title = data.siteName
      }
    } catch (e) {
      // 静默失败，使用默认配置
    }
  }

  function openAuthPopup(tab = 'login', redirect = null) {
    authDefaultTab.value = tab
    authRedirect.value = redirect
    showAuthPopup.value = true
  }

  function closeAuthPopup() {
    showAuthPopup.value = false
  }

  function getAndClearAuthRedirect() {
    const redirect = authRedirect.value
    authRedirect.value = null
    return redirect
  }

  function pushTabbarOverlay() {
    tabbarOverlayDepth.value++
  }

  function popTabbarOverlay() {
    tabbarOverlayDepth.value = Math.max(0, tabbarOverlayDepth.value - 1)
  }

  return {
    siteConfig,
    showAuthPopup,
    authDefaultTab,
    authRedirect,
    tabbarOverlayDepth,
    loadConfig,
    openAuthPopup,
    closeAuthPopup,
    getAndClearAuthRedirect,
    pushTabbarOverlay,
    popTabbarOverlay
  }
})
