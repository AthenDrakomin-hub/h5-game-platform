<template>
  <div class="bank-cards-page">
    <van-nav-bar title="银行卡管理" left-arrow @click-left="$router.back()">
      <template #right>
        <span class="add-btn" @click="goAdd">添加</span>
      </template>
    </van-nav-bar>
    <div class="bank-cards-content">
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#f0d080">加载中...</van-loading>
      </div>
      <div v-else class="card-list">
        <div
          v-for="card in cards"
          :key="card.id"
          class="bank-card"
          :class="{ default: card.isDefault }"
        >
          <div class="card-header">
            <span class="bank-icon">🏦</span>
            <span class="bank-name">{{ card.bankName }}</span>
            <span v-if="card.isDefault" class="default-tag">默认</span>
          </div>
          <div class="card-number">{{ card.cardNumber }}</div>
          <div class="card-footer">
            <span class="holder-name">{{ card.holderName }}</span>
            <div class="card-actions">
              <span class="action-btn" @click="setDefault(card)" v-if="!card.isDefault">设为默认</span>
              <span class="action-btn delete" @click="deleteCard(card)">删除</span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!loading && cards.length === 0" class="empty-state">
        <van-empty description="暂无银行卡" />
        <van-button type="primary" class="add-card-btn" @click="goAdd">添加银行卡</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()
const cards = ref([])
const loading = ref(false)

async function loadCards() {
  loading.value = true
  try {
    cards.value = await userApi.getBankCards()
  } catch (e) {
    cards.value = []
  } finally {
    loading.value = false
  }
}

function goAdd() {
  router.push('/user/bank-cards/form')
}

function setDefault(card) {
  cards.value.forEach(c => c.isDefault = c.id === card.id)
  showSuccessToast('已设为默认')
}

async function deleteCard(card) {
  try {
    await showConfirmDialog({
      title: '删除银行卡',
      message: `确定要删除 ${card.bankName} (${card.cardNumber}) 吗？`,
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await userApi.deleteBankCard(card.id)
    cards.value = cards.value.filter(c => c.id !== card.id)
    showSuccessToast('删除成功')
  } catch (e) {
    // 取消或失败
  }
}

onMounted(() => {
  loadCards()
})
</script>

<style scoped>
.bank-cards-page {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--app-bg-2) 0%, var(--app-bg) 30%, var(--app-bg) 100%);
}
:deep(.van-nav-bar) {
  background: linear-gradient(rgba(255,255,255,0.14) 0%, rgba(255,255,255,0) 26%), linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
}
:deep(.van-nav-bar__title), :deep(.van-icon-arrow-left), :deep(.van-nav-bar__right) {
  color: var(--gold-1) !important;
}
.add-btn {
  font-size: 14px;
  color: #f0d080;
  cursor: pointer;
}
.bank-cards-content {
  padding: 12px;
}
.loading-wrap {
  padding: 40px;
  text-align: center;
}
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bank-card {
  background: linear-gradient(145deg, rgba(31,26,21,0.82), rgba(11,10,8,0.6));
  border-radius: 14px;
  padding: 16px;
  border: 1px solid #3a3a4e;
  position: relative;
}
.bank-card.default {
  border-color: #f0d080;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.bank-icon {
  font-size: 24px;
}
.bank-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--gold-1);
  flex: 1;
}
.default-tag {
  font-size: 10px;
  background: #f0d080;
  color: #3a2610;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 700;
}
.card-number {
  font-size: 20px;
  color: #f0d080;
  letter-spacing: 2px;
  margin-bottom: 16px;
  font-variant-numeric: tabular-nums;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.holder-name {
  font-size: 13px;
  color: #aaa;
}
.card-actions {
  display: flex;
  gap: 12px;
}
.action-btn {
  font-size: 12px;
  color: #f0d080;
  cursor: pointer;
}
.action-btn.delete {
  color: #ee0a24;
}
.empty-state {
  padding: 60px 20px;
  text-align: center;
}
.add-card-btn {
  margin-top: 20px;
  background: linear-gradient(180deg, #f0d080 0%, #d4a84b 50%, #b8923a 100%) !important;
  border: none !important;
  color: #3a2610 !important;
  width: 200px;
}
:deep(.van-empty__description) {
  color: #6a5a40;
}
</style>
