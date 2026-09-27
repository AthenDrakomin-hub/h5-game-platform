export default [
  {
    path: '/promo',
    name: 'Promo',
    component: () => import('@/views/promo/PromoList.vue'),
    meta: { title: '优惠活动' }
  },
  {
    path: '/promo/detail/:id',
    name: 'PromoDetail',
    component: () => import('@/views/promo/PromoDetail.vue'),
    meta: { title: '活动详情', hideTabbar: true }
  }
]
