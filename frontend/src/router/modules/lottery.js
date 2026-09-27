export default [
  {
    path: '/lottery',
    name: 'Lottery',
    component: () => import('@/views/lottery/LotteryHall.vue'),
    meta: { title: '彩票大厅' }
  },
  {
    path: '/results',
    name: 'Results',
    component: () => import('@/views/lottery/Results.vue'),
    meta: { title: '开奖结果', hideTabbar: true }
  },
  {
    path: '/details',
    name: 'Details',
    component: () => import('@/views/lottery/Details.vue'),
    meta: { title: '投注明细', hideTabbar: true }
  },
  {
    path: '/pk10/:code',
    name: 'Pk10Game',
    component: () => import('@/views/lottery/Pk10Game.vue'),
    meta: { title: 'PK10', hideTabbar: true }
  },
  {
    path: '/lhc/:code',
    name: 'LhcGame',
    component: () => import('@/views/lottery/LhcGame.vue'),
    meta: { title: '六合彩', hideTabbar: true }
  },
  {
    path: '/ssc/:code',
    name: 'SscGame',
    component: () => import('@/views/lottery/SscGame.vue'),
    meta: { title: '时时彩', hideTabbar: true }
  },
  {
    path: '/pc28/:code',
    name: 'Pc28Game',
    component: () => import('@/views/lottery/Pc28Game.vue'),
    meta: { title: '加拿大28', hideTabbar: true }
  }
]
