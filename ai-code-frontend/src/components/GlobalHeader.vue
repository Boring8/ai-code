<script setup lang="ts">
import { ref, h } from 'vue'
import { Menu, Button, Avatar, Space } from 'ant-design-vue'
import { UserOutlined, HomeOutlined, SettingOutlined } from '@ant-design/icons-vue'

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

// 用户信息（暂时使用模拟数据）
const userInfo = ref({
  name: '用户',
  avatar: ''
})

// 处理菜单点击
const handleMenuClick = ({ key }: { key: string | number }) => {
  const menuItem = menuItems.value.find(item => item.key === key)
  if (menuItem) {
    // 这里可以添加路由跳转逻辑
    console.log('导航到:', menuItem.path)
  }
}

// 处理登录
const handleLogin = () => {
  console.log('用户登录')
}
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
        <Avatar :src="userInfo.avatar" :size="32">
          <template #icon><UserOutlined /></template>
        </Avatar>
        <span class="user-name">{{ userInfo.name }}</span>
        <Button type="primary" @click="handleLogin">
          登录
        </Button>
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