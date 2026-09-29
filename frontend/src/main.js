import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './styles/variables.css'
import './styles/global.css'
import { initTelegram, isTelegram, getThemeParams, applyTelegramTheme } from './utils/telegram'
import TelegramOnlyGuide from './components/TelegramOnlyGuide.vue'

async function bootstrap() {
  const tgEnabled = import.meta.env.VITE_TELEGRAM_ENABLED === 'true'

  // 生产环境必须在 Telegram 内打开；非 Telegram 环境直接渲染引导页，不启动 App
  if (tgEnabled && !isTelegram) {
    console.warn('[bootstrap] 非 Telegram 环境，渲染引导页')
    createApp(TelegramOnlyGuide).mount('#app')
    return
  }

  // Telegram Mini App 初始化
  if (tgEnabled && isTelegram) {
    initTelegram()
    applyTelegramTheme(getThemeParams())
  }

  if (import.meta.env.VITE_USE_MOCK === 'true') {
    const { worker } = await import('./mock/browser')
    await worker.start({
      onUnhandledRequest: 'bypass',
      quiet: true
    })
  }

  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.use(Vant)
  app.mount('#app')
}

bootstrap()
