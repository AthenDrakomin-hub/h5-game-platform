import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const balance = ref(0)
  const vipLevel = ref(1)
  const profile = ref(null)
  const loading = ref(false)

  const formattedBalance = computed(() => {
    return balance.value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  })

  async function fetchBalance() {
    try {
      const data = await userApi.getBalance()
      balance.value = data.balance || 0
      vipLevel.value = data.vipLevel || 1
      return data
    } catch (e) {
      return null
    }
  }

  async function fetchProfile() {
    loading.value = true
    try {
      const data = await userApi.getProfile()
      profile.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  function setBalance(val) {
    balance.value = val
  }

  return {
    balance,
    vipLevel,
    profile,
    loading,
    formattedBalance,
    fetchBalance,
    fetchProfile,
    setBalance
  }
})
