import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    redirect: '/activities',
    children: [
      {
        path: 'activities',
        name: 'ActivityList',
        component: () => import('@/views/ActivityList.vue')
      },
      {
        path: 'activities/create',
        name: 'ActivityCreate',
        component: () => import('@/views/ActivityCreate.vue')
      },
      {
        path: 'lottery/:id',
        name: 'LotteryDraw',
        component: () => import('@/views/LotteryDraw.vue')
      },
      {
        path: 'history',
        name: 'HistoryQuery',
        component: () => import('@/views/HistoryQuery.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
  } else {
    next()
  }
})

export default router
