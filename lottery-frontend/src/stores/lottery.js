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
      winners.value = data.winners
    }
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
  
  const saveWinner = ({ name }) => {
    // 更新参会人员的中奖状态
    const participant = allParticipants.value.find(p => p.name === name)
    if (participant) {
      participant.isWinner = true
      participant.prizeName = currentPrize.value?.prizeName
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
