import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNoticeStore = defineStore('notice', () => {
  const hasViewed = ref(sessionStorage.getItem('noticeViewed') === 'true')
  const showNotice = ref(false)

  function markAsViewed() {
    hasViewed.value = true
    showNotice.value = false
    sessionStorage.setItem('noticeViewed', 'true')
  }

  function closeNotice() {
    showNotice.value = false
  }

  function resetNotice() {
    hasViewed.value = false
    sessionStorage.removeItem('noticeViewed')
  }

  function tryShow() {
    if (!hasViewed.value) {
      showNotice.value = true
    }
  }

  return {
    hasViewed,
    showNotice,
    markAsViewed,
    closeNotice,
    resetNotice,
    tryShow
  }
})
