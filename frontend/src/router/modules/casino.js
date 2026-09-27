export default [
  {
    path: '/casino',
    name: 'Casino',
    component: () => import('@/views/casino/CasinoHall.vue'),
    meta: { title: '娱乐城' }
  },
  {
    path: '/casino-sub-games',
    name: 'CasinoSubGames',
    component: () => import('@/views/casino/CasinoSubGames.vue'),
    meta: { title: '子游戏', hideTabbar: true }
  },
  {
    path: '/km-games',
    name: 'KmGames',
    component: () => import('@/views/casino/KmGames.vue'),
    meta: { title: 'KM游戏', hideTabbar: true }
  },
  {
    path: '/km-game',
    name: 'KmGame',
    component: () => import('@/views/casino/GameFrame.vue'),
    meta: { title: '游戏', hideTabbar: true }
  },
  {
    path: '/ng-game',
    name: 'NgGame',
    component: () => import('@/views/casino/GameFrame.vue'),
    meta: { title: '游戏', hideTabbar: true }
  },
  {
    path: '/ms-game',
    name: 'MsGame',
    component: () => import('@/views/casino/GameFrame.vue'),
    meta: { title: '游戏', hideTabbar: true }
  },
  {
    path: '/gsc-game',
    name: 'GscGame',
    component: () => import('@/views/casino/GameFrame.vue'),
    meta: { title: '游戏', hideTabbar: true }
  },
  {
    path: '/fs-game',
    name: 'FsGame',
    component: () => import('@/views/casino/GameFrame.vue'),
    meta: { title: '游戏', hideTabbar: true }
  }
]
