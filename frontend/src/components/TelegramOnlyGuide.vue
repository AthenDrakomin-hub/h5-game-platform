<template>
  <div class="tg-guide">
    <div class="tg-guide__card">
      <div class="tg-guide__logo">🎰</div>
      <h1 class="tg-guide__title">NOVA 新星娱乐</h1>
      <p class="tg-guide__desc">
        本应用仅支持在 Telegram 内打开。<br />
        请通过 Bot 菜单进入游戏。
      </p>
      <button class="tg-guide__btn" @click="copyBot">
        {{ copied ? '已复制' : '复制 Bot 用户名' }}
      </button>
      <p class="tg-guide__bot">@NOVA_Lucky_Bot</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const BOT_USERNAME = '@NOVA_Lucky_Bot'
const copied = ref(false)

async function copyBot() {
  try {
    await navigator.clipboard.writeText(BOT_USERNAME)
  } catch {
    // 降级：选中文本
    const el = document.createElement('textarea')
    el.value = BOT_USERNAME
    document.body.appendChild(el)
    el.select()
    document.execCommand('copy')
    document.body.removeChild(el)
  }
  copied.value = true
  setTimeout(() => (copied.value = false), 2000)
}
</script>

<style scoped>
.tg-guide {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #1a1f3a 0%, #0d1024 100%);
  color: #fff;
  padding: 24px;
}
.tg-guide__card {
  text-align: center;
  max-width: 360px;
}
.tg-guide__logo {
  font-size: 64px;
  margin-bottom: 16px;
}
.tg-guide__title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 12px;
}
.tg-guide__desc {
  font-size: 14px;
  line-height: 1.7;
  color: #9aa3b8;
  margin: 0 0 24px;
}
.tg-guide__btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: #38bdf8;
  color: #0d1024;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.tg-guide__bot {
  margin-top: 16px;
  font-size: 13px;
  color: #6b7494;
}
</style>
