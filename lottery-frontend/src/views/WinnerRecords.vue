<template>
  <div class="winner-records">
    <div class="header">
      <h1>🏆 {{ activityName }} - 中奖记录</h1>
      <el-button @click="goBack">返回</el-button>
    </div>

    <div class="content">
      <!-- 统计信息 -->
      <div class="stats">
        <el-card>
          <div class="stat-item">
            <div class="label">总参与人数</div>
            <div class="value">{{ totalParticipants }}</div>
          </div>
        </el-card>
        <el-card>
          <div class="stat-item">
            <div class="label">中奖人数</div>
            <div class="value">{{ totalWinners }}</div>
          </div>
        </el-card>
        <el-card>
          <div class="stat-item">
            <div class="label">中奖率</div>
            <div class="value">{{ winRate }}%</div>
          </div>
        </el-card>
      </div>

      <!-- 按奖项分组显示 -->
      <div v-for="prize in prizeGroups" :key="prize.prizeName" class="prize-group">
        <h2>{{ prize.prizeName }} ({{ prize.winners.length }}{{ prize.totalQuota ? '/' + prize.totalQuota : '' }})</h2>
        <div v-if="prize.giftName" class="gift-name">🎁 {{ prize.giftName }}</div>
        
        <el-table :data="prize.winners" stripe style="width: 100%">
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="participantName" label="姓名" width="150" />
          <el-table-column prop="employeeNo" label="工号" width="150" />
          <el-table-column prop="department" label="部门" width="200" />
          <el-table-column prop="drawTime" label="抽奖时间" width="200">
            <template #default="{ row }">
              {{ formatTime(row.drawTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" />
        </el-table>
      </div>

      <!-- 无数据提示 -->
      <el-empty v-if="prizeGroups.length === 0" description="暂无中奖记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id
const activityName = ref('')
const winners = ref([])
const activity = ref(null)

// 统计数据
const totalParticipants = computed(() => activity.value?.totalParticipants || 0)
const totalWinners = computed(() => winners.value.length)
const winRate = computed(() => {
  if (totalParticipants.value === 0) return 0
  return ((totalWinners.value / totalParticipants.value) * 100).toFixed(1)
})

// 按奖项分组
const prizeGroups = computed(() => {
  const groups = {}
  winners.value.forEach(winner => {
    if (!groups[winner.prizeName]) {
      groups[winner.prizeName] = {
        prizeName: winner.prizeName,
        giftName: winner.giftName,
        totalQuota: winner.totalQuota || 0,
        winners: []
      }
    }
    groups[winner.prizeName].winners.push(winner)
  })
  return Object.values(groups)
})

// 加载数据
const loadData = async () => {
  try {
    // 加载活动信息
    activity.value = await request.get(`/lottery/activities/${activityId}`)
    activityName.value = activity.value.activityName
    
    // 加载中奖记录
    winners.value = await request.get(`/lottery/activities/${activityId}/winners`)
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 返回
const goBack = () => {
  router.push('/activities')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.winner-records {
  min-height: 100vh;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  
  h1 {
    margin: 0;
    color: #fa709a;
  }
}

.content {
  background: white;
  padding: 20px;
  border-radius: 10px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
  
  .el-card {
    text-align: center;
  }
  
  .stat-item {
    .label {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
    }
    
    .value {
      font-size: 32px;
      font-weight: bold;
      color: #fa709a;
    }
  }
}

.prize-group {
  margin-bottom: 30px;
  
  h2 {
    color: #fa709a;
    margin-bottom: 10px;
  }
  
  .gift-name {
    color: #666;
    margin-bottom: 15px;
    font-size: 14px;
  }
}
</style>
