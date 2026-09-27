<template>
  <div class="simple-page">
    <van-nav-bar :title="title" left-arrow @click-left="$router.back()" />
    <div class="simple-content">
      <div class="content-card" v-html="content"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta?.title || '详情')

const content = computed(() => {
  const id = route.params.id
  const faqs = {
    '1': '<h3>如何充值？</h3><p>进入"我的-充值"，选择支付方式（USDT/银行卡/三方支付），输入金额后按提示完成付款。USDT充值自动到账，其他方式需客服审核。</p>',
    '2': '<h3>如何提现？</h3><p>进入"我的-提现"，绑定银行卡或USDT地址后申请提现。提现需先设置资金密码，审核通过后到账。每日免费提现1次。</p>',
    '3': '<h3>开奖时间？</h3><p>各彩种按固定周期开奖：PC蛋蛋每5分钟一期，PK10每5分钟一期，时时彩每5分钟一期，六合彩按官方开奖时间。具体以页面倒计时为准。</p>',
    '4': '<h3>中奖后多久到账？</h3><p>开奖后系统自动结算，中奖金额实时到账余额，可在"交易记录"中查看。</p>',
    '5': '<h3>忘记密码怎么办？</h3><p>登录页点击"忘记密码"，通过绑定手机号或联系客服重置密码。资金密码需联系客服验证身份后重置。</p>',
    '6': '<h3>账号被封禁怎么办？</h3><p>如账号被封禁，请联系在线客服申诉。涉及违规操作（套利、欺诈等）的账号将永久封禁。</p>'
  }
  return faqs[id] || '<h3>帮助中心</h3><p>如需更多帮助，请联系在线客服，我们将竭诚为您服务。</p><p><strong>客服时间：</strong>全天24小时</p><p><strong>响应时间：</strong>平均5分钟内</p>'
})
</script>

<style scoped>
.simple-page { min-height: 100vh; background: #0d0d0d; }
:deep(.van-nav-bar) { background: #1a1a1a; }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: #f0f0f0 !important; }
.simple-content { padding: 16px; }
.content-card { background: #1a1a1a; border-radius: 12px; padding: 20px; color: #ccc; line-height: 1.8; }
.content-card :deep(h3) { color: #e8b860; margin-top: 0; }
.content-card :deep(strong) { color: #e8b860; }
</style>
