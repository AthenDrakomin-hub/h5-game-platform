<template>
  <div class="sponsor-page">
    <van-nav-bar title="赞助合作" left-arrow @click-left="$router.back()" />
    <div class="sponsor-content">
      <div class="sponsor-hero">
        <div class="sponsor-icon">🤝</div>
        <h2>诚邀合作伙伴</h2>
        <p>本平台长期寻求优质合作伙伴</p>
      </div>
      <div class="sponsor-list">
        <div class="sponsor-item">
          <van-icon name="gold-coin-o" size="24" color="#e8b860" />
          <div class="sponsor-item-info">
            <h3>代理合作</h3>
            <p>高额佣金分成，实时结算</p>
          </div>
        </div>
        <div class="sponsor-item">
          <van-icon name="shop-o" size="24" color="#e8b860" />
          <div class="sponsor-item-info">
            <h3>品牌赞助</h3>
            <p>首页Banner、活动冠名</p>
          </div>
        </div>
        <div class="sponsor-item">
          <van-icon name="friends-o" size="24" color="#e8b860" />
          <div class="sponsor-item-info">
            <h3>渠道推广</h3>
            <p>CPS/CPA多种合作模式</p>
          </div>
        </div>
      </div>
      <div class="sponsor-contact">
        <p>联系邮箱：partner@example.com</p>
        <p>联系客服：在线客服</p>
      </div>
      <div class="sponsor-footer">
        <p>本页面仅供技术学习演示</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.sponsor-page {
  min-height: 100vh;
  background: #0d0d0d;
}
:deep(.van-nav-bar) {
  background: #1a1a1a;
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left) {
  color: #f0f0f0 !important;
}
.sponsor-content {
  padding: 20px 16px;
}
.sponsor-hero {
  text-align: center;
  padding: 30px 0;
}
.sponsor-icon {
  font-size: 56px;
  margin-bottom: 12px;
}
.sponsor-hero h2 {
  font-size: 22px;
  color: #e8b860;
  margin: 0 0 8px 0;
}
.sponsor-hero p {
  font-size: 14px;
  color: #888;
  margin: 0;
}
.sponsor-list {
  margin-top: 20px;
}
.sponsor-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #1a1a1a;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #2a2a2a;
}
.sponsor-item-info h3 {
  font-size: 15px;
  color: #f0f0f0;
  margin: 0 0 4px 0;
}
.sponsor-item-info p {
  font-size: 12px;
  color: #888;
  margin: 0;
}
.sponsor-contact {
  margin-top: 30px;
  text-align: center;
  padding: 20px;
  background: #1a1a1a;
  border-radius: 12px;
}
.sponsor-contact p {
  font-size: 13px;
  color: #aaa;
  margin: 6px 0;
}
.sponsor-footer {
  text-align: center;
  margin-top: 30px;
}
.sponsor-footer p {
  font-size: 11px;
  color: #444;
}
</style>
