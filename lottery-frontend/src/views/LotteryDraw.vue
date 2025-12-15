<template>
  <div class="lottery-draw">
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
          <el-button class="reset-btn" @click="handleReset">
            🔄 重置抽奖
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

// 获取活动ID，优先从路由参数，其次从localStorage，最后使用默认值
const activityId = route.params.id || localStorage.getItem('currentActivityId') || 'demo'
const activityName = ref('')
const currentWinner = ref('')

// 使用 storeToRefs 获取响应式状态
const { 
  prizes, 
  currentPrizeIndex, 
  allParticipants, 
  remainingParticipants,
  winners,
  isRolling,
  currentRollingName,
  currentPrize
} = storeToRefs(lotteryStore)

const canDraw = computed(() => {
  return currentPrize.value && remainingParticipants.value.length > 0
})

// 加载抽奖数据
const loadData = async () => {
  try {
    const data = await request.get(`/lottery/activities/${activityId}/data`)
    lotteryStore.loadLotteryData(data)
    activityName.value = data.activity.activityName
  } catch (error) {
    ElMessage.error('加载抽奖数据失败')
  }
}

// 开始/停止抽奖
const toggleLottery = async () => {
  if (isRolling.value) {
    // 停止抽奖，获取中奖者名字（字符串）
    const winnerName = lotteryStore.stopRolling()
    
    console.log('=== 调试信息 ===')
    console.log('1. winnerName 原始值:', winnerName)
    console.log('2. winnerName 类型:', typeof winnerName)
    console.log('3. currentRollingName.value:', currentRollingName.value)
    console.log('4. currentRollingName.value 类型:', typeof currentRollingName.value)
    
    // 直接使用返回的名字
    const actualWinnerName = winnerName
    currentWinner.value = actualWinnerName
    
    console.log('5. actualWinnerName:', actualWinnerName)
    console.log('6. allParticipants:', allParticipants.value)
    
    if (!actualWinnerName) {
      ElMessage.error('中奖者姓名为空')
      return
    }
    
    // 保存中奖记录到后端
    try {
      // 查找中奖者的完整信息
      const winnerParticipant = allParticipants.value.find(p => p.name === actualWinnerName)
      
      if (!winnerParticipant) {
        console.error('找不到中奖者:', actualWinnerName)
        console.error('所有参与者:', allParticipants.value.map(p => p.name))
        ElMessage.error('找不到中奖者信息')
        return
      }
      
      // 计算当前抽奖序号（当前奖项已中奖数量 + 1）
      const currentWinners = winners.value[currentPrize.value.prizeName] || []
      const drawSequence = currentWinners.length + 1
      
      await request.post(`/lottery/winners`, {
        activityId: activityId,
        prizeId: currentPrize.value.prizeId,
        participantId: winnerParticipant.participantId,
        drawTime: new Date().toISOString(),
        drawSequence: drawSequence
      })
      
      lotteryStore.saveWinner({ name: actualWinnerName })
      createFireworks()
      
    } catch (error) {
      console.error('保存中奖记录失败:', error)
      ElMessage.error(error.response?.data?.message || '保存中奖记录失败')
    }
  } else {
    // 开始抽奖
    currentWinner.value = ''
    lotteryStore.startRolling()
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
    lotteryStore.resetLottery()
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
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  
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
