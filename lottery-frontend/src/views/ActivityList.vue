<template>
  <div class="activity-list">
    <!-- 公司名称栏 -->
    <div class="company-bar">
      <div class="company-name">🏭 {{ companyName }}</div>
      <div class="user-info">
        <el-dropdown @command="handleUserCommand">
          <span class="user-dropdown">
            👤 {{ username }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">📝 查看信息</el-dropdown-item>
              <el-dropdown-item command="password">🔒 修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">🚺 退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <div class="header">
      <h1>🎉 抽奖活动管理</h1>
      <div>
        <el-button v-if="isAdmin" type="warning" @click="handleManageTenants">
          🏢 租户管理
        </el-button>
        <el-button type="primary" @click="handleCreate">
          ➕ 创建活动
        </el-button>
      </div>
    </div>

    <div class="content">
      <el-table :data="activities" style="width: 100%">
        <el-table-column prop="activityName" label="活动名称" width="200" />
        <el-table-column prop="startTime" label="活动日期" width="150">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }}
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
        <el-table-column label="操作" width="500">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleManageParticipants(row)">
              人员管理
            </el-button>
            <el-button size="small" type="success" @click="handleManagePrizes(row)">
              奖项管理
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              :disabled="row.status === 'COMPLETED'"
              @click="handleDraw(row)"
            >
              开始抽奖
            </el-button>
            <el-button size="small" type="info" @click="handleViewWinners(row)">
              查看记录
            </el-button>
            <el-button 
              v-if="row.status === 'COMPLETED'"
              size="small" 
              type="danger" 
              @click="handleRevoke(row)"
            >
              撤销
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

    <!-- 用户信息对话框 -->
    <el-dialog 
      v-model="profileDialogVisible" 
      title="个人信息"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userProfile.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userProfile.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userProfile.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ userProfile.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ getRoleName(userProfile.role) }}</el-descriptions-item>
        <el-descriptions-item label="所属租户">{{ companyName }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="profileDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="passwordDialogVisible" 
      title="修改密码"
      width="450px"
    >
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码" required>
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const activities = ref([])
const dialogVisible = ref(false)
const editMode = ref(false)
const companyName = ref(localStorage.getItem('tenantName') || '公司')
const username = ref(localStorage.getItem('realName') || localStorage.getItem('username') || '用户')
const isAdmin = ref(localStorage.getItem('role') === 'SUPER_ADMIN')

const form = ref({
  activityId: '',
  activityName: '',
  activityDate: '',
  location: '',
  description: ''
})

const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)

const userProfile = ref({
  username: localStorage.getItem('username') || '',
  realName: localStorage.getItem('realName') || '',
  email: localStorage.getItem('email') || '',
  phone: localStorage.getItem('phone') || '',
  role: localStorage.getItem('role') || ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载活动列表
const loadActivities = async () => {
  try {
    const data = await request.get('/lottery/activities')
    activities.value = data

    // 计算每个活动的奖项数
    for (const act of activities.value) {
      try {
        const prizes = await request.get(`/lottery/activities/${act.activityId}/prizes`)
        act.totalPrizes = prizes.length
      } catch {
        act.totalPrizes = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载活动列表失败')
  }
}

// 编辑活动
const handleEdit = (row) => {
  editMode.value = true
  form.value = { 
    ...row,
    activityDate: row.startTime ? new Date(row.startTime) : ''
  }
  dialogVisible.value = true
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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 保存活动
const handleSave = async () => {
  try {
    const payload = {
      ...form.value,
      // 使用 startTime 与后端实体对应
      startTime: form.value.activityDate || null
    }

    if (editMode.value) {
      await request.put(`/lottery/activities/${form.value.activityId}`, payload)
      ElMessage.success('更新成功')
    } else {
      await request.post('/lottery/activities', payload)
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

// 人员管理
const handleManageParticipants = (row) => {
  router.push(`/activities/${row.activityId}/participants`)
}

// 开始抽奖
const handleDraw = (row) => {
  localStorage.setItem('currentActivityId', row.activityId)
  router.push(`/lottery/${row.activityId}`)
}

// 问题2：查看抽奖记录
const handleViewWinners = (row) => {
  router.push(`/activities/${row.activityId}/winners`)
}

// 撤销已结束活动
const handleRevoke = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要撤销活动「${row.activityName}」吗？撤销后将删除所有中奖记录，并允许再次抽奖。`,
      '警告',
      {
        confirmButtonText: '确定撤销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.post(`/lottery/activities/${row.activityId}/revoke`)
    ElMessage.success('活动已撤销，可以再次抽奖')
    loadActivities()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '撤销失败')
    }
  }
}

// 用户下拉菜单命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      profileDialogVisible.value = true
      break
    case 'password':
      passwordDialogVisible.value = true
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  try {
    await request.post('/auth/change-password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    setTimeout(() => {
      handleLogout()
    }, 1500)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  }
}

// 获取角色名称
const getRoleName = (role) => {
  const roleMap = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'OPERATOR': '操作员',
    'VIEWER': '观察者'
  }
  return roleMap[role] || role
}

// 租户管理
const handleManageTenants = () => {
  router.push('/admin/tenants')
}

// 退出登录
const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped lang="scss">
.activity-list {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
    
    .user-dropdown {
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px;
  margin-top: 20px;
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
  margin: 0 20px 20px 20px;
}
</style>
