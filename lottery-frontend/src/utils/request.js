import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加 Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 添加租户 ID
    const tenantId = getTenantId()
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res.data
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

// 获取租户 ID
function getTenantId() {
  const hostname = window.location.hostname
  const parts = hostname.split('.')
  
  // 子域名模式（排除IP地址的情况）
  // IP地址的特征：所有部分都是数字
  const isIP = parts.every(part => /^\d+$/.test(part))
  
  if (parts.length > 2 && !isIP) {
    return parts[0]
  }
  
  // 从 localStorage 读取
  return localStorage.getItem('tenantId')
}

export default request
