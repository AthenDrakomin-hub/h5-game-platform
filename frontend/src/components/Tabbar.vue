<template>
  <van-tabbar v-model="active" route active-color="#e8b860" inactive-color="#808080" safe-area-inset-bottom>
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
  background: #1a1a1a;
  border-top: 1px solid #333;
}
:deep(.van-tabbar-item__icon) {
  font-size: 20px;
}
:deep(.van-tabbar-item__text) {
  font-size: 11px;
}
</style>
