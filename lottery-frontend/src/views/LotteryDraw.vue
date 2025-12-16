<template>
  <div class="lottery-draw">
    <!-- 公司名称栏 -->
    <div class="company-bar">
      <div class="company-name">🏭 {{ companyName }}</div>
      <div class="user-info">
        <span>👤 {{ username }}</span>
      </div>
    </div>
    
    <!-- 顶部导航 -->
    <div class="header">
      <h1>🎉 {{ activityName }} 🎉</h1>
      <el-button @click="goBack">返回活动列表</el-button>
    </div>

    <div class="main-content">
      <!-- 左侧：参会人员 -->
      <div class="participant-list">
        <h3>👥 参会人员</h3>
        <div class="participant-stats">
          <div class="stat-item">
            <div class="label">总人数</div>
            <div class="value">{{ allParticipants.length }}</div>
          </div>
          <div class="stat-item">
            <div class="label">已中奖</div>
            <div class="value">{{ allParticipants.length - remainingParticipants.length }}</div>
          </div>
          <div class="stat-item">
            <div class="label">未中奖</div>
            <div class="value">{{ remainingParticipants.length }}</div>
          </div>
        </div>
        <div class="participant-grid">
          <div 
            v-for="p in allParticipants" 
            :key="p.participantId"
            :class="['participant-card', p.isWinner ? 'winner' : 'available']"
          >
            <div class="name">{{ p.name }}</div>
            <div v-if="p.prizeName" class="prize-tag">{{ p.prizeName }}</div>
          </div>
        </div>
      </div>

      <!-- 中间：抽奖区域 -->
      <div class="lottery-area">
        <!-- 当前奖项 -->
        <div class="current-prize">
          <h2>{{ currentPrize?.prizeName || '请选择奖项' }}</h2>
          <div class="quota">
            名额：{{ currentPrize?.drawnCount || 0 }}/{{ currentPrize?.totalQuota || 0 }}
          </div>
          <div v-if="currentPrize?.giftName" class="prize-name">
            🎁 {{ currentPrize.giftName }}
          </div>
        </div>

        <!-- 滚动显示区 -->
        <div class="display-area">
          <div v-if="!isRolling && !currentWinner" class="ready-text">
            🎊 准备就绪，开始抽奖吧！ 🎊
          </div>
          <div v-else class="rolling-names">
            <div :class="['name-card', currentWinner ? 'winner-card' : '']">
              <!-- 确保显示的是字符串 -->
              {{ typeof (currentRollingName || currentWinner) === 'string' 
                  ? (currentRollingName || currentWinner) 
                  : (currentRollingName?.name || currentWinner?.name || '未知') }}
            </div>
          </div>
        </div>

        <!-- 控制按钮 -->
        <div class="control-center">
          <el-button 
            :class="['lottery-btn', isRolling ? 'stop' : 'start']"
            @click="toggleLottery"
            :disabled="!canDraw"
            size="large"
          >
            {{ isRolling ? '🛑 停止抽奖' : '🎯 开始抽奖' }}
          </el-button>
          <br>
          <el-button 
            class="reset-btn" 
            @click="handleReset"
            :disabled="isRolling || isActivityCompleted"
          >
            🔄 重置抽奖
          </el-button>
          <el-button
            class="undo-btn"
            type="warning"
            @click="handleUndoLast"
            :disabled="isRolling || !canUndo || isActivityCompleted"
          >
            ↩ 撤销上一次中奖
          </el-button>
          <el-button
            class="save-btn"
            type="primary"
            @click="handleSaveResults"
            :disabled="!canSaveResults || isRolling || isActivityCompleted"
          >
            💾 保存本次抽奖结果
          </el-button>
        </div>
      </div>

      <!-- 右侧：中奖名单 -->
      <div class="winner-history">
        <h3>🎁 中奖名单</h3>
        <div v-if="Object.keys(winners).length === 0" class="no-data">
          暂无中奖记录
        </div>
        <div v-else>
          <div 
            v-for="prize in prizes" 
            :key="prize.prizeId"
            class="winner-group"
          >
            <template v-if="winners[prize.prizeName]?.length > 0">
              <h4>{{ prize.prizeName }} ({{ winners[prize.prizeName].length }}/{{ prize.totalQuota }})</h4>
              <div v-if="prize.giftName" class="prize-gift">🎁 {{ prize.giftName }}</div>
              <div class="winner-list">
                <div 
                  v-for="(name, index) in winners[prize.prizeName]" 
                  :key="index"
                  class="winner-tag"
                >
                  <!-- 确保显示的是字符串 -->
                  {{ index + 1 }}. {{ typeof name === 'string' ? name : name?.name || '未知' }}
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLotteryStore } from '@/stores/lottery'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const lotteryStore = useLotteryStore()

const { 
  prizes, 
  currentPrizeIndex, 
  allParticipants, 
  remainingParticipants,
  winners,
  isRolling,
  currentRollingName,
  currentPrize,
  currentActivity,
  winnerStack
} = storeToRefs(lotteryStore)

const activityId = route.params.id || localStorage.getItem('currentActivityId') || 'demo'
const activityName = ref('')
const currentWinner = ref('')
const companyName = ref(localStorage.getItem('companyName') || '某某公司')
const username = ref(localStorage.getItem('username') || '用户')

// 记录初始中奖名单，用于区分“本次新增”的中奖
const initialWinners = ref({})

// 活动是否已完成
const isActivityCompleted = computed(() => {
  return currentActivity.value && currentActivity.value.status === 'COMPLETED'
})

// 是否可以抽奖（活动未完成、有奖项、有剩余人员、当前奖项未满）
const canDraw = computed(() => {
  if (isActivityCompleted.value) return false
  if (!currentPrize.value || remainingParticipants.value.length === 0) {
    return false
  }
  
  const drawnCount = currentPrize.value.drawnCount || 0
  const totalQuota = currentPrize.value.totalQuota || 0
  
  return drawnCount < totalQuota
})

// 是否可以撤销
const canUndo = computed(() => {
  return winnerStack.value.length > 0 && !isActivityCompleted.value
})

// 是否可以保存结果：有新增中奖记录且活动未完成
const canSaveResults = computed(() => {
  if (isActivityCompleted.value) return false
  // 当前 winners 和 initialWinners 的差集中有数据就可以保存
  for (const prize of prizes.value) {
    const prizeName = prize.prizeName
    const cur = winners.value[prizeName] || []
    const init = initialWinners.value[prizeName] || []
    if (cur.length > init.length) {
      return true
    }
  }
  return false
})

// 加载抽奖数据
const loadData = async () => {
  try {
    const data = await request.get(`/lottery/activities/${activityId}/data`)
    lotteryStore.loadLotteryData(data)
    activityName.value = data.activity.activityName
    // 记录初始 winners（复制一份）
    initialWinners.value = JSON.parse(JSON.stringify(winners.value || {}))
  } catch (error) {
    ElMessage.error('加载抽奖数据失败')
  }
}

// 开始/停止抽奖
const toggleLottery = async () => {
  if (isActivityCompleted.value) {
    ElMessage.warning('该活动已保存并锁定，不能继续抽奖')
    return
  }

  if (isRolling.value) {
    // 停止抽奖，获取中奖者名字（字符串）
    const winnerName = lotteryStore.stopRolling()
    
    const actualWinnerName = winnerName
    currentWinner.value = actualWinnerName
    
    if (!actualWinnerName) {
      ElMessage.error('中奖者姓名为空')
      return
    }
    
    try {
      const winnerParticipant = allParticipants.value.find(p => p.name === actualWinnerName)
      
      if (!winnerParticipant) {
        ElMessage.error('找不到中奖者信息')
        return
      }
      
      // 仅在前端更新状态，不立即保存到后端
      lotteryStore.saveWinner({ name: actualWinnerName })
      createFireworks()
      
    } catch (error) {
      ElMessage.error('处理中奖结果失败')
    }
  } else {
    // 开始抽奖
    if (!canDraw.value) {
      if (!currentPrize.value) {
        ElMessage.warning('所有奖项已抽完！')
      } else if (currentPrize.value.drawnCount >= currentPrize.value.totalQuota) {
        ElMessage.warning(`当前奖项「${currentPrize.value.prizeName}」已抽完！`)
      } else if (remainingParticipants.value.length === 0) {
        ElMessage.warning('所有人员都已中奖！')
      }
      return
    }
    
    currentWinner.value = ''
    lotteryStore.startRolling()
  }
}

// 撤销最后一个中奖人员
const handleUndoLast = () => {
  if (!canUndo.value) {
    ElMessage.warning('没有可以撤销的中奖记录')
    return
  }
  lotteryStore.undoLastWinner()
  currentWinner.value = ''
}

// 保存本次抽奖结果
const handleSaveResults = async () => {
  if (!canSaveResults.value) {
    ElMessage.warning('当前没有可保存的新增中奖记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      '保存后该活动将锁定，不能再进行抽奖或重置，确认保存？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    // 计算新增的中奖人员（相对 initialWinners 的差集）
    const payloads = []

    for (const prize of prizes.value) {
      const prizeName = prize.prizeName
      const cur = winners.value[prizeName] || []
      const init = initialWinners.value[prizeName] || []

      // 简单按“数量差异+名字不在初始列表中”来识别新增
      const newNames = cur.filter(name => !init.includes(name))
      newNames.forEach((name, idx) => {
        const participant = allParticipants.value.find(p => p.name === name)
        if (!participant) return
        payloads.push({
          activityId,
          prizeId: prize.prizeId,
          participantId: participant.participantId,
          // 序号：在当前奖项中的位置（从1开始）
          drawSequence: (init.length + idx + 1)
        })
      })
    }

    if (payloads.length === 0) {
      ElMessage.info('没有新的中奖记录需要保存')
      return
    }

    // 逐个调用后端保存
    for (const p of payloads) {
      await request.post('/lottery/winners', {
        ...p,
        drawTime: new Date().toISOString()
      })
    }

    // 更新活动状态为 COMPLETED，锁定活动
    const activityPayload = {
      ...currentActivity.value,
      status: 'COMPLETED'
    }
    await request.put(`/lottery/activities/${activityId}`, activityPayload)
    if (currentActivity.value) {
      currentActivity.value.status = 'COMPLETED'
    }

    // 更新 initialWinners，避免重复保存
    initialWinners.value = JSON.parse(JSON.stringify(winners.value || {}))

    ElMessage.success('保存成功，本次活动已锁定')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存中奖结果失败')
  }
}

// 重置抽奖
const handleReset = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有中奖记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.post(`/lottery/activities/${activityId}/reset`)
    
    // 重新加载数据，确保状态同步
    await loadData()
    
    currentWinner.value = ''
    ElMessage.success('重置成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

// 返回
const goBack = () => {
  router.push('/activities')
}

// 烟花特效
const createFireworks = () => {
  // 简化版烟花效果
  ElMessage({
    message: '🎉 恭喜中奖！🎉',
    type: 'success',
    duration: 3000,
    showClose: true
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
/* 复用原有样式 - 简化版 */
.lottery-draw {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff0000 0%, #cc0000 50%, #ff6b6b 100%);
  padding: 0;
}

.company-bar {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  
  .company-name {
    font-size: 20px;
    font-weight: bold;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 15px;
    
    span {
      font-size: 14px;
    }
  }
}

.header {
  text-align: center;
  margin: 20px 20px 30px 20px;
  padding-top: 10px;
  
  h1 {
    color: #fff;
    font-size: 3em;
    text-shadow: 0 0 20px rgba(255, 215, 0, 0.8);
    margin-bottom: 10px;
  }
}

.main-content {
  display: grid;
  grid-template-columns: 350px 1fr 350px;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.participant-list, .winner-history {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 20px;
  max-height: 800px;
  overflow-y: auto;
  
  h3 {
    color: #cc0000;
    margin-bottom: 15px;
    border-bottom: 2px solid #ffd700;
    padding-bottom: 10px;
  }
}

.participant-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 15px;
  padding: 10px;
  background: linear-gradient(135deg, #ffe4e1 0%, #ffcccb 100%);
  border-radius: 8px;
  
  .stat-item {
    text-align: center;
    
    .label {
      font-size: 0.85em;
      color: #666;
    }
    
    .value {
      font-size: 1.3em;
      font-weight: bold;
      color: #cc0000;
    }
  }
}

.participant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.participant-card {
  background: white;
  padding: 10px;
  border-radius: 8px;
  text-align: center;
  border: 2px solid #e0e0e0;
  transition: all 0.3s;
  
  &.winner {
    background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
    border-color: #ffd700;
    font-weight: bold;
    
    .name {
      color: #cc0000;
    }
  }
  
  .prize-tag {
    font-size: 0.75em;
    color: #ff0000;
    margin-top: 3px;
  }
}

.lottery-area {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 30px;
}

.current-prize {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  border-radius: 12px;
  
  h2 {
    color: #cc0000;
    font-size: 2em;
  }
  
  .quota {
    color: #ff0000;
    font-size: 1.3em;
    font-weight: bold;
  }
  
  .prize-name {
    font-size: 1.2em;
    color: #ff6600;
    margin-top: 10px;
  }
}

.display-area {
  background: linear-gradient(135deg, #ff0000 0%, #cc0000 100%);
  min-height: 400px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  
  .ready-text {
    color: white;
    font-size: 1.5em;
    text-align: center;
  }
  
  .name-card {
    background: rgba(255, 255, 255, 0.95);
    padding: 60px 80px;
    border-radius: 20px;
    font-size: 3em;
    font-weight: bold;
    color: #cc0000;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    
    &.winner-card {
      animation: winnerPulse 1s ease-in-out infinite;
      background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
    }
  }
}

@keyframes winnerPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.control-center {
  text-align: center;
}

.lottery-btn {
  padding: 20px 80px;
  font-size: 1.8em;
  border-radius: 50px;
  font-weight: bold;
  
  &.start {
    background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  }
  
  &.stop {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }
}

.winner-group {
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  border-radius: 10px;
  
  h4 {
    color: #cc0000;
    margin-bottom: 10px;
  }
  
  .prize-gift {
    color: #666;
    margin-bottom: 10px;
  }
}

.winner-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.winner-tag {
  background: white;
  padding: 8px 15px;
  border-radius: 20px;
  color: #cc0000;
  font-weight: bold;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>
