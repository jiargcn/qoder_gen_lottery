<template>
  <div class="tenant-manage">
    <!-- 公司名称栏 -->
    <div class="company-bar">
      <div class="company-name">🏭 系统管理</div>
      <div class="user-info">
        <span>👤 {{ username }}</span>
        <el-button size="small" @click="goBack">返回</el-button>
      </div>
    </div>
    
    <div class="header">
      <h1>🏢 租户管理</h1>
      <el-button type="primary" @click="handleCreate">
        ➕ 创建租户
      </el-button>
    </div>

    <div class="content">
      <el-table :data="tenants" style="width: 100%">
        <el-table-column prop="tenantName" label="租户名称" width="200" />
        <el-table-column prop="companyName" label="公司名称" width="200" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="contactEmail" label="联系邮箱" width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑租户对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editMode ? '编辑租户' : '创建租户'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="租户名称">
          <el-input v-model="form.tenantName" placeholder="请输入租户名称（唯一标识）" />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="form.remarks" 
            type="textarea" 
            rows="3"
            placeholder="请输入备注信息"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const tenants = ref([])
const dialogVisible = ref(false)
const editMode = ref(false)
const username = ref(localStorage.getItem('username') || '超级管理员')

const form = ref({
  tenantId: '',
  tenantName: '',
  companyName: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  remarks: '',
  status: 'ACTIVE'
})

// 加载租户列表
const loadTenants = async () => {
  try {
    const data = await request.get('/admin/tenants')
    tenants.value = data
  } catch (error) {
    ElMessage.error('加载租户列表失败')
  }
}

// 创建租户
const handleCreate = () => {
  editMode.value = false
  form.value = {
    tenantId: '',
    tenantName: '',
    companyName: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    remarks: '',
    status: 'ACTIVE'
  }
  dialogVisible.value = true
}

// 编辑租户
const handleEdit = (row) => {
  editMode.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存租户
const handleSave = async () => {
  try {
    if (editMode.value) {
      await request.put(`/admin/tenants/${form.value.tenantId}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/tenants', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTenants()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

// 切换租户状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}租户「${row.companyName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.put(`/admin/tenants/${row.tenantId}`, {
      ...row,
      status: newStatus
    })
    
    ElMessage.success(`${action}成功`)
    loadTenants()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 返回
const goBack = () => {
  router.push('/activities')
}

onMounted(() => {
  loadTenants()
})
</script>

<style scoped lang="scss">
.tenant-manage {
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
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px;
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
