<template>
  <div id="userRegisterPage">
    <div class="register-container">
      <div class="register-header">
        <div class="logo-section">
          <span class="logo-text">AI Code</span>
        </div>
        <h1 class="register-title">用户注册</h1>
        <p class="register-subtitle">创建您的 AI Code 账户</p>
      </div>
      
      <a-form 
        :model="formState" 
        name="register" 
        autocomplete="off" 
        @finish="handleSubmit"
        class="register-form"
      >
        <a-form-item 
          name="userAccount" 
          :rules="[
            { required: true, message: '请输入账号' },
            { min: 4, message: '账号长度不能小于 4 位' }
          ]"
          class="form-item"
        >
          <a-input 
            v-model:value="formState.userAccount" 
            placeholder="请输入账号"
            size="large"
            class="form-input"
          >
            <template #prefix>
              <span class="input-icon">👤</span>
            </template>
          </a-input>
        </a-form-item>
        
        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
          class="form-item"
        >
          <a-input-password 
            v-model:value="formState.userPassword" 
            placeholder="请输入密码"
            size="large"
            class="form-input"
          >
            <template #prefix>
              <span class="input-icon">🔒</span>
            </template>
          </a-input-password>
        </a-form-item>
        
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validatePassword }
          ]"
          class="form-item"
        >
          <a-input-password 
            v-model:value="formState.checkPassword" 
            placeholder="请确认密码"
            size="large"
            class="form-input"
          >
            <template #prefix>
              <span class="input-icon">🔒</span>
            </template>
          </a-input-password>
        </a-form-item>
        
        <a-form-item class="form-item">
          <a-button 
            type="primary" 
            html-type="submit" 
            size="large"
            class="register-btn"
            :loading="loading"
          >
            注册
          </a-button>
        </a-form-item>
      </a-form>
      
      <div class="register-footer">
        <p class="login-tip">
          已有账号？
          <RouterLink to="/user/login" class="login-link">立即登录</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userRegister } from '@/api/userController.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const router = useRouter()
const loading = ref(false)

// 验证密码确认
const validatePassword = async (_rule: any, value: string) => {
  if (value === '') {
    throw new Error('请确认密码')
  } else if (value !== formState.userPassword) {
    throw new Error('两次输入的密码不一致')
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  try {
    const res = await userRegister(values)
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      router.push('/user/login')
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    message.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

/* 背景装饰 */
#userRegisterPage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(24, 144, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(64, 169, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.register-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 48px 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(24, 144, 255, 0.1);
  position: relative;
  z-index: 2;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-section {
  margin-bottom: 24px;
}

.logo-text {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
}

.register-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 400;
}

.register-form {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-input {
  border-radius: 8px;
  border: 1px solid #d9e7f5;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.form-input:focus,
.form-input:hover {
  border-color: #1890ff;
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.input-icon {
  font-size: 16px;
  color: #999;
}

.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 16px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
}

.register-footer {
  text-align: center;
}

.login-tip {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 32px 24px;
  }
  
  .logo-text {
    font-size: 28px;
  }
  
  .register-title {
    font-size: 24px;
  }
  
  .register-subtitle {
    font-size: 14px;
  }
}
</style>
