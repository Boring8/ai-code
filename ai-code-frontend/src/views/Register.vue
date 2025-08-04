<script setup lang="ts">
import { ref, reactive, h } from 'vue'
import { useRouter } from 'vue-router'
import { Card, Form, Input, Button, Typography, message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, MailOutlined, UserAddOutlined } from '@ant-design/icons-vue'
import * as userController from '@/api/userController'

const { Title } = Typography
const { Item } = Form

const router = useRouter()

// 表单数据
const formData = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

// 加载状态
const loading = ref(false)

// 表单规则
const rules = {
  userAccount: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string) => {
        if (value !== formData.userPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 处理注册
const handleRegister = async (values: any) => {
  try {
    loading.value = true
    
    // 构建注册数据
    const registerData = {
      userAccount: values.userAccount,
      userPassword: values.userPassword,
      checkPassword: values.checkPassword
    }
    
    const response = await userController.userRegister(registerData)
    const { code, message: msg } = response.data
    
    if (code === 0) {
      message.success('注册成功，请登录')
      // 跳转到登录页面
      router.push('/login')
    } else {
      message.error(msg || '注册失败，请稍后重试')
    }
  } catch (error: any) {
    console.error('注册失败:', error)
    message.error(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <Card class="register-card">
        <div class="register-header">
          <Title :level="2" class="register-title">用户注册</Title>
          <p class="register-subtitle">欢迎加入 AI Code 系统</p>
        </div>
        
        <Form
          :model="formData"
          :rules="rules"
          layout="vertical"
          @finish="handleRegister"
          class="register-form"
        >
          <Item label="用户名" name="userAccount">
            <Input
              v-model:value="formData.userAccount"
              size="large"
              placeholder="请输入用户名（3-20个字符）"
              :prefix="h(UserOutlined)"
            />
          </Item>
          
          <Item label="密码" name="userPassword">
                         <Input.Password
               v-model:value="formData.userPassword"
               size="large"
               placeholder="请输入密码（至少8位）"
               :prefix="h(LockOutlined)"
             />
          </Item>
          
          <Item label="确认密码" name="checkPassword">
            <Input.Password
              v-model:value="formData.checkPassword"
              size="large"
              placeholder="请再次输入密码"
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
              注册
            </Button>
          </Item>
          
          <div class="register-footer">
            <span class="login-link">
              已有账号？
              <a @click="goToLogin">立即登录</a>
            </span>
          </div>
        </Form>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 88vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  margin-bottom: 8px !important;
  color: #1890ff;
}

.register-subtitle {
  color: #666;
  margin: 0;
}

.register-form {
  margin-top: 24px;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
}

.login-link {
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
  margin-left: 4px;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    max-width: 100%;
  }
  
  .register-page {
    padding: 16px;
  }
}
</style> 