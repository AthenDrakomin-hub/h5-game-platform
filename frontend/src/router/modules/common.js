export default [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hideTabbar: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', hideTabbar: true }
  },
  {
    path: '/register/:code',
    name: 'InviteRegister',
    component: () => import('@/views/Register.vue'),
    meta: { title: '推广注册', hideTabbar: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/common/Chat.vue'),
    meta: { title: '客服中心', hideTabbar: true }
  },
  {
    path: '/service',
    name: 'Service',
    component: () => import('@/views/common/Chat.vue'),
    meta: { title: '在线客服', hideTabbar: true }
  }
]
