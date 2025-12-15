import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
  
  // 计算当前奖项
  const currentPrize = computed(() => {
    if (currentPrizeIndex.value >= prizes.value.length) {
      return null
    }
    return prizes.value[currentPrizeIndex.value]
  })
  
  const loadLotteryData = (data) => {
    if (data.activity) {
      currentActivity.value = data.activity
    }
    if (data.prizes) {
      prizes.value = data.prizes
    }
    if (data.participants) {
      allParticipants.value = data.participants
      remainingParticipants.value = data.participants.filter(p => !p.isWinner)
    }
    if (data.winners) {
      // 确保 winners 中存储的是名字字符串，而不是对象
      const processedWinners = {}
      for (const [prizeName, winnerList] of Object.entries(data.winners)) {
        processedWinners[prizeName] = winnerList.map(winner => 
          typeof winner === 'string' ? winner : winner.name
        )
      }
      winners.value = processedWinners
    }
  }
  
  const startRolling = () => {
    if (remainingParticipants.value.length === 0) return
    
    isRolling.value = true
    rollingInterval = setInterval(() => {
      const randomIndex = Math.floor(Math.random() * remainingParticipants.value.length)
      // 获取参与者对象的名字
      currentRollingName.value = remainingParticipants.value[randomIndex].name
    }, 50)
  }
  
  const stopRolling = () => {
    clearInterval(rollingInterval)
    isRolling.value = false
    
    const prize = prizes.value[currentPrizeIndex.value]
    // 直接使用名字字符串
    const winnerName = currentRollingName.value
    
    if (!winners.value[prize.prizeName]) {
      winners.value[prize.prizeName] = []
    }
    winners.value[prize.prizeName].push(winnerName)
    
    // 从剩余参与者中移除中奖者（比较名字）
    remainingParticipants.value = remainingParticipants.value.filter(
      p => p.name !== winnerName
    )
    
    return winnerName
  }
  
  const saveWinner = ({ name }) => {
    // 更新参会人员的中奖状态
    const participant = allParticipants.value.find(p => p.name === name)
    if (participant) {
      participant.isWinner = true
      participant.prizeName = currentPrize.value?.prizeName
    }
    
    // 检查当前奖项是否已满，如果满了自动切换到下一个奖项
    const currentPrizeName = currentPrize.value?.prizeName
    const currentWinners = winners.value[currentPrizeName] || []
    const totalQuota = currentPrize.value?.totalQuota || 0
    
    if (currentWinners.length >= totalQuota) {
      // 当前奖项已满，自动切换到下一个奖项
      if (currentPrizeIndex.value < prizes.value.length - 1) {
        currentPrizeIndex.value++
      }
    }
  }
  
  const resetLottery = () => {
    currentPrizeIndex.value = 0
    remainingParticipants.value = [...allParticipants.value]
    winners.value = {}
    isRolling.value = false
    currentRollingName.value = ''
    
    // 重置所有参与者的中奖状态
    allParticipants.value.forEach(p => {
      p.isWinner = false
      p.prizeName = null
    })
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
    currentPrize,
    loadLotteryData,
    startRolling,
    stopRolling,
    saveWinner,
    resetLottery
  }
})
