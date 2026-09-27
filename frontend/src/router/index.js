import { createRouter, createWebHashHistory } from 'vue-router'
import commonRoutes from './modules/common'
import homeRoutes from './modules/home'
import lotteryRoutes from './modules/lottery'
import casinoRoutes from './modules/casino'
import promoRoutes from './modules/promo'
import userRoutes from './modules/user'
import redirectRoutes from './modules/redirects'

const routes = [
  ...commonRoutes,
  ...homeRoutes,
  ...lotteryRoutes,
  ...casinoRoutes,
  ...promoRoutes,
  ...userRoutes,
  ...redirectRoutes,
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/common/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置标题
  if (to.meta?.title) {
    document.title = to.meta.title
  }
  // 需要登录的路由
  if (to.meta?.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      const appStore = router.app?.config?.globalProperties?.$pinia
      // 重定向到首页并触发登录弹窗
      next({ path: '/', query: { authRedirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
