<template>
  <div class="login-container">
    <div class="login-box">
      <h1>年会抽奖系统</h1>
      <el-form :model="form" class="login-form">
        <el-form-item>
          <el-input v-model="form.tenantCode" placeholder="租户代码">
            <template #prepend>租户</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名">
            <template #prepend>用户名</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码">
            <template #prepend>密码</template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="goToRegister" style="width: 100%">
            注册账号
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const form = reactive({
  tenantCode: '',
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.tenantCode || !form.username || !form.password) {
    ElMessage.warning('请填写租户代码、用户名和密码')
    return
  }
  
  try {
    const res = await request.post('/auth/login', form)
    localStorage.setItem('token', res.token)
    localStorage.setItem('tenantId', res.tenantId || res.userInfo?.tenantId)
    localStorage.setItem('tenantCode', form.tenantCode)
    ElMessage.success('登录成功')
    router.push('/activities') // 跳转到活动列表
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查租户代码、用户名和密码')
    }
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #ff0000 0%, #cc0000 50%, #ff6b6b 100%);
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  width: 450px;
}

.login-box h1 {
  text-align: center;
  color: #cc0000;
  margin-bottom: 30px;
}

.login-form :deep(.el-input-group__prepend) {
  background-color: #f5f7fa;
  color: #606266;
  min-width: 80px;
  justify-content: center;
}
</style>
