<template>
  <div class="register-container">
    <div class="register-box">
      <h1>注册租户</h1>
      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="tenantCode">
          <el-input v-model="form.tenantCode" placeholder="租户代码（小写字母和数字，3-20位）">
            <template #prepend>租户代码</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="租户名称（如：XX公司）">
            <template #prepend>租户名称</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="adminUsername">
          <el-input v-model="form.adminUsername" placeholder="管理员用户名">
            <template #prepend>用户名</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="adminPassword">
          <el-input v-model="form.adminPassword" type="password" placeholder="管理员密码">
            <template #prepend>密码</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码">
            <template #prepend>确认密码</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="adminEmail">
          <el-input v-model="form.adminEmail" placeholder="管理员邮箱（可选）">
            <template #prepend>邮箱</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="goToLogin" style="width: 100%">
            返回登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  tenantCode: '',
  tenantName: '',
  adminUsername: '',
  adminPassword: '',
  confirmPassword: '',
  adminEmail: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.adminPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  tenantCode: [
    { required: true, message: '请输入租户代码', trigger: 'blur' },
    { pattern: /^[a-z0-9]{3,20}$/, message: '租户代码只能包含小写字母和数字，长度3-20位', trigger: 'blur' }
  ],
  tenantName: [
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '租户名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  adminUsername: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    const res = await request.post('/auth/register', {
      tenantCode: form.tenantCode,
      tenantName: form.tenantName,
      adminUsername: form.adminUsername,
      adminPassword: form.adminPassword,
      adminEmail: form.adminEmail || null
    })
    ElMessage.success({
      message: `注册成功！请记住您的租户代码：${form.tenantCode}，登录时需要使用`,
      duration: 5000,
      showClose: true
    })
    
    // 稍微延迟后跳转，让用户看到提示
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '注册失败')
    } else {
      ElMessage.error('注册失败，请重试')
    }
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #ff0000 0%, #cc0000 50%, #ff6b6b 100%);
  padding: 20px;
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 500px;
}

.register-box h1 {
  text-align: center;
  color: #cc0000;
  margin-bottom: 30px;
  font-size: 28px;
}

.register-form {
  margin-top: 20px;
}

.register-form .el-form-item {
  margin-bottom: 20px;
}

.register-form :deep(.el-input-group__prepend) {
  background-color: #f5f7fa;
  color: #606266;
  min-width: 90px;
  justify-content: center;
}
</style>
