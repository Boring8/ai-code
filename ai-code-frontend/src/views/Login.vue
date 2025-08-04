<script setup lang="ts">
import { ref, reactive, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Card, Form, Input, Button, Typography, message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'

const { Title } = Typography
const { Item } = Form

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单数据
const formData = reactive({
  userAccount: '',
  userPassword: ''
})

// 加载状态
const loading = ref(false)

// 表单规则
const rules = {
  userAccount: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async (values: any) => {
  try {
    loading.value = true
    const success = await userStore.login(values.userAccount, values.userPassword)
    
    if (success) {
      message.success('登录成功')
      // 跳转到之前的页面或首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } else {
      message.error('登录失败，请检查用户名和密码')
    }
  } catch (error) {
    message.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <Card class="login-card">
        <div class="login-header">
          <Title :level="2" class="login-title">用户登录</Title>
          <p class="login-subtitle">欢迎使用 AI Code 系统</p>
        </div>
        
        <Form
          :model="formData"
          :rules="rules"
          layout="vertical"
          @finish="handleLogin"
          class="login-form"
        >
          <Item label="用户名" name="userAccount">
            <Input
              v-model:value="formData.userAccount"
              size="large"
              placeholder="请输入用户名"
              :prefix="h(UserOutlined)"
            />
          </Item>
          
          <Item label="密码" name="userPassword">
            <Input.Password
              v-model:value="formData.userPassword"
              size="large"
              placeholder="请输入密码"
              :prefix="h(LockOutlined)"
            />
          </Item>
          
          <Item>
            <Button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
              block
            >
              登录
            </Button>
          </Item>
          
          <div class="login-footer">
            <span class="register-link">
              还没有账号？
              <a @click="goToRegister">立即注册</a>
            </span>
          </div>
        </Form>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
    min-height: 88vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  margin-bottom: 8px !important;
  color: #1890ff;
}

.login-subtitle {
  color: #666;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.register-link {
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
  margin-left: 4px;
}

.register-link a:hover {
  text-decoration: underline;
}
</style> 