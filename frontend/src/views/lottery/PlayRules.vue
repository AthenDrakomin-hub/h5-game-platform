<template>
  <div class="rules-page">
    <van-nav-bar title="玩法规则" left-arrow @click-left="$router.back()" />
    <div class="rules-content">
      <van-collapse v-model="activeNames" accordion>
        <van-collapse-item v-for="rule in rules" :key="rule.title" :title="rule.title" :name="rule.title">
          <div class="rule-content" v-html="rule.content"></div>
        </van-collapse-item>
      </van-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeNames = ref(['PC蛋蛋'])

const rules = ref([
  {
    title: 'PC蛋蛋',
    content: `
      <p><strong>玩法说明：</strong>每期开出3个号码（0-9），组成0-27的和值。</p>
      <p><strong>大小：</strong>和值14-27为大，0-13为小。</p>
      <p><strong>单双：</strong>和值为奇数是单，偶数是双。</p>
      <p><strong>极值：</strong>和值0-5为极小，22-27为极大。</p>
      <p><strong>豹子：</strong>三个号码完全相同（如555）。</p>
      <p><strong>对子：</strong>恰好两个号码相同（如556）。</p>
    `
  },
  {
    title: 'PK10赛车',
    content: `
      <p><strong>玩法说明：</strong>10辆赛车竞速，开出1-10的名次排列。</p>
      <p><strong>名次：</strong>可猜第1-10名的具体号码。</p>
      <p><strong>大小：</strong>第N名号码01-05为小，06-10为大。</p>
      <p><strong>冠亚和：</strong>第一名+第二名的和值，≥12为大，≤11为小。</p>
      <p><strong>龙虎：</strong>第一名大于第十名为龙，小于为虎。</p>
    `
  },
  {
    title: '时时彩',
    content: `
      <p><strong>玩法说明：</strong>每期开出5个号码（0-9），万/千/百/十/个。</p>
      <p><strong>定位：</strong>可猜万/千/百/十/个位的具体号码。</p>
      <p><strong>大小：</strong>某位0-4为小，5-9为大。</p>
      <p><strong>五星：</strong>全部5位号码相同。</p>
      <p><strong>和值：</strong>5个号码之和，0-45。</p>
    `
  },
  {
    title: '六合彩',
    content: `
      <p><strong>玩法说明：</strong>每期开出6个正码+1个特码（01-49）。</p>
      <p><strong>特码：</strong>猜特码具体数字。</p>
      <p><strong>大小：</strong>特码01-24为小，25-49为大。</p>
      <p><strong>波色：</strong>红波/蓝波/绿波，按特码所属波色判定。</p>
      <p><strong>生肖：</strong>特码所属生肖（鼠牛虎兔龙蛇马羊猴鸡狗猪）。</p>
      <p><strong>尾数：</strong>特码个位数（0-9）。</p>
    `
  },
  {
    title: '通用规则',
    content: `
      <p><strong>开奖时间：</strong>各彩种按固定周期开奖，具体以页面倒计时为准。</p>
      <p><strong>投注截止：</strong>封盘后不可投注，已投注单按当期开奖结算。</p>
      <p><strong>中奖结算：</strong>开奖后自动结算，中奖金额=投注金额×赔率。</p>
      <p><strong>资金安全：</strong>余额操作采用乐观锁机制，防止并发超扣。</p>
    `
  }
])
</script>

<style scoped>
.rules-page { min-height: 100vh; background: var(--app-bg); }
:deep(.van-nav-bar) { background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg); }
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) { color: var(--gold-1) !important; }
.rules-content { padding: 12px; }
:deep(.van-collapse), :deep(.van-collapse-item) { background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), var(--card-bg) !important; border-color: rgba(255,255,255,0.1) !important; }
:deep(.van-cell__title) { color: #f0d080 !important; font-weight: 600; }
:deep(.van-collapse-item__content) { color: var(--gold-1) !important; }
.rule-content { font-size: 14px; line-height: 1.8; }
.rule-content p { margin: 8px 0; }
.rule-content strong { color: #f0d080; }
</style>
