<template>
  <div class="participant-manage">
    <div class="header">
      <h1>👥 {{ activityName }} - 参会人员管理</h1>
      <div>
        <el-button @click="goBack">返回</el-button>
        <el-button type="success" @click="handleBatchCreate">
          📋 批量添加
        </el-button>
        <el-button type="primary" @click="handleCreate">
          ➕ 添加人员
        </el-button>
      </div>
    </div>

    <div class="content">
      <el-table :data="participants" style="width: 100%">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="employeeNo" label="工号" width="150" />
        <el-table-column prop="department" label="部门" width="200" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="isWinner" label="中奖状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isWinner ? 'success' : 'info'">
              {{ row.isWinner ? '已中奖' : '未中奖' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.isWinner"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 批量添加对话框 -->
    <el-dialog 
      v-model="batchDialogVisible" 
      title="批量添加人员"
      width="700px"
    >
      <el-alert
        title="使用说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        每行一个姓名，可以快速添加多个参会人员。如需详细信息，请在添加后单独编辑。
      </el-alert>
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="姓名列表" required>
          <el-input
            v-model="batchForm.names"
            type="textarea"
            :rows="10"
            placeholder="请输入姓名，每行一个，例如：&#10;张三&#10;李四&#10;王五"
          />
        </el-form-item>
        <el-form-item label="统一部门">
          <el-input v-model="batchForm.department" placeholder="可选，为所有人员设置相同部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSave">批量添加</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑人员对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editMode ? '编辑人员' : '添加人员'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id
const activityName = ref('')
const participants = ref([])
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const editMode = ref(false)

const form = ref({
  participantId: '',
  name: '',
  employeeNo: '',
  department: '',
  phone: '',
  email: ''
})

const batchForm = ref({
  names: '',
  department: ''
})

// 加载活动信息
const loadActivity = async () => {
  try {
    const data = await request.get(`/lottery/activities/${activityId}`)
    activityName.value = data.activityName
  } catch (error) {
    ElMessage.error('加载活动信息失败')
  }
}

// 加载参会人员列表
const loadParticipants = async () => {
  try {
    const data = await request.get(`/lottery/activities/${activityId}/participants`)
    participants.value = data
  } catch (error) {
    ElMessage.error('加载人员列表失败')
  }
}

// 添加人员
const handleCreate = () => {
  editMode.value = false
  form.value = {
    participantId: '',
    name: '',
    employeeNo: '',
    department: '',
    phone: '',
    email: ''
  }
  dialogVisible.value = true
}

// 批量添加人员
const handleBatchCreate = () => {
  batchForm.value = {
    names: '',
    department: ''
  }
  batchDialogVisible.value = true
}

// 批量保存
const handleBatchSave = async () => {
  if (!batchForm.value.names.trim()) {
    ElMessage.warning('请输入至少一个姓名')
    return
  }

  try {
    // 按行分割姓名
    const names = batchForm.value.names
      .split('\n')
      .map(name => name.trim())
      .filter(name => name.length > 0)
    
    if (names.length === 0) {
      ElMessage.warning('请输入有效的姓名')
      return
    }

    // 批量创建参会人员
    let successCount = 0
    let failCount = 0
    
    for (const name of names) {
      try {
        await request.post('/lottery/participants', {
          activityId: activityId,
          name: name,
          department: batchForm.value.department || null,
          employeeNo: null,
          phone: null,
          email: null
        })
        successCount++
      } catch (error) {
        console.error(`添加 ${name} 失败:`, error)
        failCount++
      }
    }

    batchDialogVisible.value = false
    
    if (failCount === 0) {
      ElMessage.success(`成功添加 ${successCount} 个人员`)
    } else {
      ElMessage.warning(`成功 ${successCount} 个，失败 ${failCount} 个`)
    }
    
    loadParticipants()
  } catch (error) {
    ElMessage.error('批量添加失败')
  }
}

// 编辑人员
const handleEdit = (row) => {
  editMode.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存人员
const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入姓名')
    return
  }

  try {
    const data = {
      ...form.value,
      activityId: activityId
    }
    
    if (editMode.value) {
      await request.put(`/lottery/participants/${form.value.participantId}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post(`/lottery/participants`, data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadParticipants()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 删除人员
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${row.name} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await request.delete(`/lottery/participants/${row.participantId}`)
    ElMessage.success('删除成功')
    loadParticipants()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 返回
const goBack = () => {
  router.push('/activities')
}

onMounted(() => {
  loadActivity()
  loadParticipants()
})
</script>

<style scoped lang="scss">
.participant-manage {
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
  
  > div {
    display: flex;
    gap: 10px;
  }
}

.content {
  background: white;
  padding: 20px;
  border-radius: 10px;
}
</style>
