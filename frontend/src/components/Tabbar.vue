<template>
  <van-tabbar v-model="active" route active-color="#d4a84b" inactive-color="#6a5a40" safe-area-inset-bottom>
    <van-tabbar-item to="/" icon="wap-home">首页</van-tabbar-item>
    <van-tabbar-item to="/lottery" icon="gift">彩票</van-tabbar-item>
    <van-tabbar-item to="/casino" icon="gold-coin-o">娱乐城</van-tabbar-item>
    <van-tabbar-item to="/promo" icon="coupon-o">优惠</van-tabbar-item>
    <van-tabbar-item to="/user/center" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref } from 'vue'

const active = ref(0)
</script>

<style scoped>
:deep(.van-tabbar) {
  background: linear-gradient(180deg, #1a130a 0%, #0d0a06 100%);
  border-top: 1px solid rgba(212, 168, 75, 0.15);
}
:deep(.van-tabbar-item__icon) {
  font-size: 20px;
}
:deep(.van-tabbar-item__text) {
  font-size: 11px;
}
</style>
