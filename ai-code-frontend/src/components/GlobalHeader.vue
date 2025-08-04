<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, Button, Avatar, Space, Dropdown } from 'ant-design-vue'
import { UserOutlined, HomeOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

// 菜单配置
const menuItems = ref([
  {
    key: 'home',
    icon: () => h(HomeOutlined),
    label: '首页',
    path: '/'
  },
  {
    key: 'settings',
    icon: () => h(SettingOutlined),
    label: '设置',
    path: '/settings'
  }
])

const router = useRouter()
const userStore = useUserStore()

// 用户下拉菜单项
const userMenuItems = [
  {
    key: 'profile',
    label: '个人资料',
    icon: () => h(UserOutlined)
  },
  {
    key: 'settings',
    label: '设置',
    icon: () => h(SettingOutlined)
  },
  {
    type: 'divider' as const
  },
  {
    key: 'logout',
    label: '退出登录',
    icon: () => h(LogoutOutlined)
  }
]

// 处理菜单点击
const handleMenuClick = ({ key }: { key: string | number }) => {
  const menuItem = menuItems.value.find(item => item.key === key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 处理用户菜单点击
const handleUserMenuClick = ({ key }: { key: string | number }) => {
  switch (key) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
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

// 处理登出
const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
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
          <Dropdown :menu="{ items: userMenuItems, onClick: handleUserMenuClick }" placement="bottomRight">
            <Space class="user-info">
                             <Avatar :src="userStore.userInfo.userAvatar" :size="32">
                <template #icon><UserOutlined /></template>
              </Avatar>
                             <span class="user-name">{{ userStore.userInfo.userName || userStore.userInfo.userAccount }}</span>
            </Space>
          </Dropdown>
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