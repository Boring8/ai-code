import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/user-management',
      name: 'userManagement',
      component: () => import('@/views/UserManagement.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/user/edit/:id',
      name: 'userEdit',
      component: () => import('@/views/UserEdit.vue')
    }
  ],
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // 这里可以添加权限检查逻辑
    // 暂时允许访问，在组件内部进行权限控制
  }
  next()
})

export default router
