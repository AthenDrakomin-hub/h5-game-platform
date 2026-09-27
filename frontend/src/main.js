import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './styles/variables.css'
import './styles/global.css'
import { initTelegram, isTelegram, getThemeParams, applyTelegramTheme } from './utils/telegram'

// 开发环境启动 MSW
async function bootstrap() {
  // Telegram Mini App 初始化（环境变量开启且在 Telegram 客户端内）
  const tgEnabled = import.meta.env.VITE_TELEGRAM_ENABLED === 'true'
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
