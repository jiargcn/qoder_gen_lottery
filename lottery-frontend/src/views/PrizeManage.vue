<template>
  <div class="prize-manage">
    <div class="header">
      <h1>🎁 {{ activityName }} - 奖项管理</h1>
      <div>
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleCreate">
          ➕ 添加奖项
        </el-button>
      </div>
    </div>

    <div class="content">
      <el-table :data="prizes" style="width: 100%">
        <el-table-column prop="prizeName" label="奖项名称" width="150" />
        <el-table-column prop="prizeLevel" label="奖项等级" width="100" />
        <el-table-column prop="totalQuota" label="总名额" width="100" />
        <el-table-column prop="drawnCount" label="已抽取" width="100" />
        <el-table-column prop="giftName" label="奖品名称" width="200" />
        <el-table-column prop="giftValue" label="奖品价值" width="120">
          <template #default="{ row }">
            {{ row.giftValue ? `¥${row.giftValue}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.drawnCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑奖项对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editMode ? '编辑奖项' : '添加奖项'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="奖项名称">
          <el-input v-model="form.prizeName" placeholder="例如：一等奖" />
        </el-form-item>
        <el-form-item label="奖项等级">
          <el-input-number v-model="form.prizeLevel" :min="1" :max="10" />
          <span style="margin-left: 10px; color: #999; font-size: 12px;">
            数字越小等级越高，1为最高等级
          </span>
        </el-form-item>
        <el-form-item label="总名额">
          <el-input-number v-model="form.totalQuota" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="奖品名称">
          <el-input v-model="form.giftName" placeholder="例如：iPhone 15 Pro" />
        </el-form-item>
        <el-form-item label="奖品价值">
          <el-input-number 
            v-model="form.giftValue" 
            :min="0" 
            :precision="2"
            :controls="false"
          />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item label="奖品描述">
          <el-input 
            v-model="form.giftDescription" 
            type="textarea" 
            rows="3"
            placeholder="请输入奖品描述"
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id
const activityName = ref('')
const prizes = ref([])
const dialogVisible = ref(false)
const editMode = ref(false)
const form = ref({
  prizeId: '',
  prizeName: '',
  prizeLevel: 1,
  totalQuota: 1,
  giftName: '',
  giftValue: 0,
  giftDescription: ''
})

// 加载奖项列表
const loadPrizes = async () => {
  try {
    const data = await request.get(`/lottery/activities/${activityId}/prizes`)
    prizes.value = data.sort((a, b) => a.prizeLevel - b.prizeLevel)
    
    // 加载活动信息
    const activity = await request.get(`/lottery/activities/${activityId}`)
    activityName.value = activity.activityName
  } catch (error) {
    ElMessage.error('加载奖项列表失败')
  }
}

// 返回
const goBack = () => {
  router.push('/activities')
}

// 创建奖项
const handleCreate = () => {
  editMode.value = false
  form.value = {
    prizeId: '',
    prizeName: '',
    prizeLevel: prizes.value.length + 1,
    totalQuota: 1,
    giftName: '',
    giftValue: 0,
    giftDescription: ''
  }
  dialogVisible.value = true
}

// 编辑奖项
const handleEdit = (row) => {
  editMode.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存奖项
const handleSave = async () => {
  try {
    // 问题3：验证奖项等级是否重复
    const existingPrize = prizes.value.find(p => 
      p.prizeLevel === form.value.prizeLevel && 
      p.prizeId !== form.value.prizeId
    )
    
    if (existingPrize) {
      ElMessage.error(`奖项等级 ${form.value.prizeLevel} 已存在（${existingPrize.prizeName}），请使用其他等级`)
      return
    }
    
    const data = {
      ...form.value,
      activityId: activityId
    }
    
    if (editMode.value) {
      await request.put(`/lottery/prizes/${form.value.prizeId}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/lottery/prizes', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadPrizes()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 删除奖项
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${row.prizeName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.delete(`/lottery/prizes/${row.prizeId}`)
    ElMessage.success('删除成功')
    loadPrizes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadPrizes()
})
</script>

<style scoped lang="scss">
.prize-manage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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
    color: #f5576c;
  }
}

.content {
  background: white;
  padding: 20px;
  border-radius: 10px;
}
</style>
