<template>
  <div class="activity-list">
    <div class="header">
      <h1>🎉 抽奖活动管理</h1>
      <el-button type="primary" @click="handleCreate">
        ➕ 创建活动
      </el-button>
    </div>

    <div class="content">
      <el-table :data="activities" style="width: 100%">
        <el-table-column prop="activityName" label="活动名称" width="200" />
        <el-table-column prop="activityDate" label="活动日期" width="150">
          <template #default="{ row }">
            {{ formatDate(row.activityDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="活动地点" width="150" />
        <el-table-column prop="totalParticipants" label="参与人数" width="100" />
        <el-table-column prop="totalPrizes" label="奖项数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="handleManagePrizes(row)">
              奖项管理
            </el-button>
            <el-button size="small" type="primary" @click="handleDraw(row)">
              开始抽奖
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑活动对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editMode ? '编辑活动' : '创建活动'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动日期">
          <el-date-picker 
            v-model="form.activityDate" 
            type="date" 
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            rows="3"
            placeholder="请输入活动描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const activities = ref([])
const dialogVisible = ref(false)
const editMode = ref(false)
const form = ref({
  activityId: '',
  activityName: '',
  activityDate: '',
  location: '',
  description: ''
})

// 加载活动列表
const loadActivities = async () => {
  try {
    const data = await request.get('/lottery/activities')
    activities.value = data
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 创建活动
const handleCreate = () => {
  editMode.value = false
  form.value = {
    activityId: '',
    activityName: '',
    activityDate: '',
    location: '',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑活动
const handleEdit = (row) => {
  editMode.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存活动
const handleSave = async () => {
  try {
    if (editMode.value) {
      await request.put(`/lottery/activities/${form.value.activityId}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/lottery/activities', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadActivities()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 奖项管理
const handleManagePrizes = (row) => {
  router.push(`/activities/${row.activityId}/prizes`)
}

// 开始抽奖
const handleDraw = (row) => {
  localStorage.setItem('currentActivityId', row.activityId)
  router.push(`/lottery/${row.activityId}`)
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped lang="scss">
.activity-list {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    color: #667eea;
  }
}

.content {
  background: white;
  padding: 20px;
  border-radius: 10px;
}
</style>
