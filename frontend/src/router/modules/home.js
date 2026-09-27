export default [
  {
    path: '/splash',
    name: 'Splash',
    component: () => import('@/views/Splash.vue'),
    meta: { title: '欢迎', hideTabbar: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/sponsor',
    name: 'Sponsor',
    component: () => import('@/views/Sponsor.vue'),
    meta: { title: '赞助', hideTabbar: true }
  }
]
