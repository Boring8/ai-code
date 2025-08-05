<template>
  <div id="userLoginPage">
    <div class="login-container">
      <div class="login-header">
        <div class="logo-section">
          <span class="logo-text">AI Code</span>
        </div>
        <h1 class="login-title">用户登录</h1>
        <p class="login-subtitle">欢迎使用 AI Code 系统</p>
      </div>
      
      <a-form 
        :model="formState" 
        name="login" 
        autocomplete="off" 
        @finish="handleSubmit"
        class="login-form"
      >
        <a-form-item 
          name="userAccount" 
          :rules="[{ required: true, message: '请输入账号' }]"
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
        
        <a-form-item class="form-item">
          <a-button 
            type="primary" 
            html-type="submit" 
            size="large"
            class="login-btn"
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>
      
      <div class="login-footer">
        <p class="register-tip">
          还没有账号？
          <RouterLink to="/user/register" class="register-link">立即注册</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  try {
    const res = await userLogin(values)
    // 登录成功，把登录态保存到全局状态中
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (error) {
    message.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

/* 背景装饰 */
#userLoginPage::before {
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

.login-container {
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

.login-header {
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

.login-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
}

.login-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 400;
}

.login-form {
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

.login-btn {
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

.login-btn:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
}

.login-footer {
  text-align: center;
}

.register-tip {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 32px 24px;
  }
  
  .logo-text {
    font-size: 28px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-subtitle {
    font-size: 14px;
  }
}
</style>
