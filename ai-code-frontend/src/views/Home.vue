<script setup lang="ts">
import { Card, Row, Col, Typography, Button, Avatar, Space, Tag } from 'ant-design-vue'
import { UserOutlined, TeamOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PermissionControl from '@/components/PermissionControl.vue'

const { Title, Paragraph } = Typography
const router = useRouter()
const userStore = useUserStore()

// 跳转到用户管理页面
const goToUserManagement = () => {
  router.push('/user-management')
}

// 跳转到用户编辑页面
const goToUserEdit = () => {
  if (userStore.userInfo?.id) {
    router.push(`/user/edit/${userStore.userInfo.id}`)
  }
}
</script>

<template>
  <div class="home-page">
    <Row :gutter="[24, 24]">
      <Col :span="24">
        <Card>
          <Title level="2">欢迎使用 AI Code 用户权限管理系统</Title>
          <Paragraph>
            这是一个基于 Vue 3 + TypeScript + Ant Design Vue 构建的现代化用户权限管理前端系统。
          </Paragraph>
          
          <!-- 用户信息展示 -->
          <div v-if="userStore.isLoggedIn && userStore.userInfo" class="user-info-section">
            <Space direction="vertical" size="large" style="width: 100%;">
              <div class="current-user">
                <Avatar 
                  :src="userStore.userInfo.userAvatar" 
                  :size="64"
                  style="cursor: pointer;"
                  @click="goToUserEdit"
                >
                  <template #icon><UserOutlined /></template>
                </Avatar>
                <div class="user-details">
                  <Title level="4">{{ userStore.userInfo.userName || userStore.userInfo.userAccount }}</Title>
                  <Tag :color="userStore.userInfo.userRole === 'admin' ? 'red' : 'blue'">
                    {{ userStore.userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
                  </Tag>
                  <Paragraph>{{ userStore.userInfo.userProfile || '暂无个人简介' }}</Paragraph>
                </div>
              </div>
              
              <div class="action-buttons">
                <Space>
                  <Button 
                    type="primary" 
                    @click="goToUserEdit"
                  >
                    <template #icon><UserOutlined /></template>
                    编辑个人信息
                  </Button>
                  
                  <PermissionControl require-role="admin">
                    <Button 
                      type="primary" 
                      @click="goToUserManagement"
                    >
                      <template #icon><TeamOutlined /></template>
                      用户管理
                    </Button>
                  </PermissionControl>
                </Space>
              </div>
            </Space>
          </div>
          

          
          <!-- 未登录状态 -->
          <div v-else class="login-prompt">
            <Paragraph>
              请先登录以体验完整的用户权限管理功能。
            </Paragraph>
            <Space>
              <Button type="primary" @click="router.push('/login')">
                立即登录
              </Button>
              <Button @click="router.push('/register')">
                注册账号
              </Button>
            </Space>
          </div>
        </Card>
      </Col>
      
      <!-- 功能说明卡片 -->
      <Col :xs="24" :sm="12" :md="8">
        <Card title="权限管理功能">
          <Paragraph>
            <ul>
              <li>角色权限区分（管理员/普通用户）</li>
              <li>用户管理（仅管理员可见）</li>
              <li>用户信息编辑</li>
              <li>实时权限控制</li>
            </ul>
          </Paragraph>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="8">
        <Card title="技术特性">
          <Paragraph>
            <ul>
              <li>Vue 3 组合式 API</li>
              <li>TypeScript 类型支持</li>
              <li>Ant Design Vue 组件库</li>
              <li>Pinia 状态管理</li>
              <li>响应式设计</li>
            </ul>
          </Paragraph>
        </Card>
      </Col>
      
      <Col :xs="24" :sm="12" :md="8">
        <Card title="用户体验">
          <Paragraph>
            <ul>
              <li>现代化 UI 设计</li>
              <li>友好的交互反馈</li>
              <li>表单验证</li>
              <li>权限实时切换</li>
            </ul>
          </Paragraph>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.user-info-section {
  margin: 24px 0;
}

.current-user {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.user-details {
  flex: 1;
}

.user-details h4 {
  margin: 0 0 8px 0;
  color: #1890ff;
}

.action-buttons {
  margin-top: 16px;
}

.login-prompt {
  text-align: center;
  padding: 40px 20px;
}

ul {
  margin: 16px 0;
  padding-left: 20px;
}

li {
  margin: 8px 0;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .current-user {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    text-align: center;
  }
  

}
</style> 