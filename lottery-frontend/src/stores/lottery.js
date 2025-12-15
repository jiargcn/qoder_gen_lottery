import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLotteryStore = defineStore('lottery', () => {
  const currentActivity = ref(null)
  const prizes = ref([])
  const currentPrizeIndex = ref(0)
  const allParticipants = ref([])
  const remainingParticipants = ref([])
  const winners = ref({})
  const isRolling = ref(false)
  const currentRollingName = ref('')
  
  let rollingInterval = null
  
  const loadLotteryData = async (activityId) => {
    // TODO: 调用后端API加载数据
  }
  
  const startRolling = () => {
    if (remainingParticipants.value.length === 0) return
    
    isRolling.value = true
    rollingInterval = setInterval(() => {
      const randomIndex = Math.floor(Math.random() * remainingParticipants.value.length)
      currentRollingName.value = remainingParticipants.value[randomIndex]
    }, 50)
  }
  
  const stopRolling = async () => {
    clearInterval(rollingInterval)
    isRolling.value = false
    
    const prize = prizes.value[currentPrizeIndex.value]
    const winnerName = currentRollingName.value
    
    if (!winners.value[prize.prizeName]) {
      winners.value[prize.prizeName] = []
    }
    winners.value[prize.prizeName].push(winnerName)
    
    remainingParticipants.value = remainingParticipants.value.filter(
      name => name !== winnerName
    )
    
    // TODO: 调用后端API保存中奖记录
  }
  
  return {
    currentActivity,
    prizes,
    currentPrizeIndex,
    allParticipants,
    remainingParticipants,
    winners,
    isRolling,
    currentRollingName,
    loadLotteryData,
    startRolling,
    stopRolling
  }
})
