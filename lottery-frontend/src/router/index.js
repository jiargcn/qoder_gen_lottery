import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/activities',
    name: 'ActivityList',
    component: () => import('@/views/ActivityList.vue')
  },
  {
    path: '/activities/:id/prizes',
    name: 'PrizeManage',
    component: () => import('@/views/PrizeManage.vue')
  },
  {
    path: '/activities/:id/participants',
    name: 'ParticipantManage',
    component: () => import('@/views/ParticipantManage.vue')
  },
  {
    path: '/activities/:id/winners',
    name: 'WinnerRecords',
    component: () => import('@/views/WinnerRecords.vue')
  },
  {
    path: '/lottery/:id',
    name: 'LotteryDraw',
    component: () => import('@/views/LotteryDraw.vue')
  },
  {
    path: '/admin/tenants',
    name: 'TenantManage',
    component: () => import('@/views/TenantManage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 如果没有token且访问的不是登录或注册页，则跳转到登录页
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else {
    next()
  }
})

export default router
