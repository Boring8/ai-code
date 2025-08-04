<script setup lang="ts">
import { ref, h, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, Button, Space } from 'ant-design-vue'
import { HomeOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import UserAvatarMenu from './UserAvatarMenu.vue'

// 菜单配置
const menuItems = computed(() => {
  const items = [
    {
      key: 'home',
      icon: () => h(HomeOutlined),
      label: '首页',
      path: '/'
    }
  ]
  
  // 只有管理员可以看到用户管理菜单
  if (userStore.canManageUsers) {
    items.push({
      key: 'userManagement',
      icon: () => h(TeamOutlined),
      label: '用户管理',
      path: '/user-management'
    })
  }
  

  
  return items
})

const router = useRouter()
const userStore = useUserStore()

// 处理菜单点击
const handleMenuClick = ({ key }: { key: string | number }) => {
  const menuItem = menuItems.value.find(item => item.key === key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 处理登录
const handleLogin = () => {
  router.push('/login')
}

// 处理注册
const handleRegister = () => {
  router.push('/register')
}

// 组件挂载时获取用户信息
onMounted(async () => {
  if (!userStore.isLoggedIn) {
    await userStore.getUserInfo()
  }
})
</script>

<template>
  <div class="global-header">
    <div class="header-left">
      <div class="logo-section">
        <img src="/logo.png" alt="Logo" class="logo" />
        <h1 class="site-title">AI Code</h1>
      </div>
      
      <Menu
        mode="horizontal"
        :items="menuItems"
        class="header-menu"
        @click="handleMenuClick"
      />
    </div>
    
    <div class="header-right">
      <Space>
        <template v-if="userStore.isLoggedIn && userStore.userInfo">
          <UserAvatarMenu :size="32" :show-name="true" />
        </template>
        <template v-else>
          <Space>
            <Button @click="handleRegister">
              注册
            </Button>
            <Button type="primary" @click="handleLogin">
              登录
            </Button>
          </Space>
        </template>
      </Space>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 48px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.header-menu {
  border-bottom: none;
  background: transparent;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-name {
  font-size: 14px;
  color: #666;
}

.user-info {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
  }
  
  .header-left {
    gap: 24px;
  }
  
  .site-title {
    font-size: 16px;
  }
  
  .user-name {
    display: none;
  }
}

@media (max-width: 576px) {
  .header-left {
    gap: 16px;
  }
  
  .site-title {
    display: none;
  }
}
</style> 