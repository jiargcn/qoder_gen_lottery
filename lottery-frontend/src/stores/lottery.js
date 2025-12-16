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
  const winnerStack = ref([]) // 用于撤销功能的中奖堆栈
  
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
      // 问题1：按 prizeLevel 降序排序（数字大的先抽，即从低等级开始）
      prizes.value = data.prizes.sort((a, b) => b.prizeLevel - a.prizeLevel)
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
    
    // 根据中奖情况确定当前奖项索引
    // 如果没有中奖记录，从第一个奖项开始
    if (!data.winners || Object.keys(data.winners).length === 0) {
      currentPrizeIndex.value = 0
    } else {
      // 找到第一个未抽完的奖项
      let foundIndex = 0
      for (let i = 0; i < prizes.value.length; i++) {
        const prize = prizes.value[i]
        const prizeWinners = winners.value[prize.prizeName] || []
        if (prizeWinners.length < prize.totalQuota) {
          foundIndex = i
          break
        }
        // 如果当前奖项已满，继续检查下一个
        if (i < prizes.value.length - 1) {
          foundIndex = i + 1
        }
      }
      currentPrizeIndex.value = foundIndex
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
    
    // 保存到堆栈，用于撤销
    winnerStack.value.push({
      prizeName: prize.prizeName,
      prizeIndex: currentPrizeIndex.value,
      winnerName: winnerName,
      drawnCount: prize.drawnCount || 0
    })
    
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
    
    // 问题2：更新奖项的已抽取数量
    if (currentPrize.value) {
      currentPrize.value.drawnCount = (currentPrize.value.drawnCount || 0) + 1
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
    winnerStack.value = [] // 清空堆栈
    
    // 重置所有参与者的中奖状态
    allParticipants.value.forEach(p => {
      p.isWinner = false
      p.prizeName = null
    })
    
    // 重置奖项的已抽取数量
    prizes.value.forEach(prize => {
      prize.drawnCount = 0
    })
  }
  
  // 撤销最后一个中奖
  const undoLastWinner = () => {
    if (winnerStack.value.length === 0) return
    
    const lastWinner = winnerStack.value.pop()
    const { prizeName, prizeIndex, winnerName, drawnCount } = lastWinner
    
    // 从中奖名单中移除
    if (winners.value[prizeName]) {
      const idx = winners.value[prizeName].lastIndexOf(winnerName)
      if (idx !== -1) {
        winners.value[prizeName].splice(idx, 1)
      }
    }
    
    // 恢复参与者状态
    const participant = allParticipants.value.find(p => p.name === winnerName)
    if (participant) {
      participant.isWinner = false
      participant.prizeName = null
      // 添加回剩余参与者
      if (!remainingParticipants.value.find(p => p.name === winnerName)) {
        remainingParticipants.value.push(participant)
      }
    }
    
    // 恢复奖项的已抽取数量
    const prize = prizes.value[prizeIndex]
    if (prize) {
      prize.drawnCount = drawnCount
    }
    
    // 恢复当前奖项索引（如果撤销的是上一个奖项）
    if (prizeIndex < currentPrizeIndex.value) {
      currentPrizeIndex.value = prizeIndex
    }
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
    winnerStack,
    loadLotteryData,
    startRolling,
    stopRolling,
    saveWinner,
    resetLottery,
    undoLastWinner
  }
})
