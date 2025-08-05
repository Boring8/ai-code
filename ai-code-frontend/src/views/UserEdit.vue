<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Form, Input, Select, Button, Avatar, Card, Space, Divider, Typography } from 'ant-design-vue'
import { UserOutlined, SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import * as userController from '@/api/userController'

const { Title } = Typography
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取路由参数中的用户ID
const userId = computed(() => route.params.id as string)

// 表单数据
const userForm = reactive({
  id: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user'
})

// 表单引用
const formRef = ref()
const loading = ref(false)
const userInfo = ref<API.UserVO | null>(null)

// 角色选项
const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' }
]

// 权限检查
const canEditUser = computed(() => {
  // 管理员可以编辑所有用户
  if (userStore.isAdmin) {
    return true
  }
  // 普通用户只能编辑自己的信息
  return userStore.userInfo?.id === userId.value
})

// 是否为编辑自己的信息
const isEditingSelf = computed(() => {
  return userStore.userInfo?.id === userId.value
})

// 是否可以修改角色
const canChangeRole = computed(() => {
  // 管理员可以修改所有用户的角色
  if (userStore.isAdmin) {
    return true
  }
  // 普通用户不能修改任何用户的角色（包括自己的）
  return false
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true
    const response = await userController.getUserVoById({ id: userId.value })
    const { code, data } = response.data
    
    if (code === 0 && data) {
      userInfo.value = data
      // 填充表单数据
      Object.assign(userForm, {
        id: data.id?.toString() || '',
        userName: data.userName || '',
        userAvatar: data.userAvatar || '',
        userProfile: data.userProfile || '',
        userRole: data.userRole || 'user'
      })
    } else {
      message.error('获取用户信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    message.error('获取用户信息失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 保存用户信息
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    // 权限检查：如果用户没有权限修改角色，则保持原始角色不变
    const dataToSave = { ...userForm }
    if (!canChangeRole.value) {
      dataToSave.userRole = userInfo.value?.userRole || 'user'
    }
    
    const response = await userController.updateUser({
      ...dataToSave,
      id: dataToSave.id
    })
    const { code } = response.data
    
    if (code === 0) {
      message.success('保存成功')
      // 如果是编辑自己的信息，更新本地用户信息
      if (isEditingSelf.value) {
        await userStore.getUserInfo()
      }
      router.back()
    } else {
      message.error('保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 表单验证规则
const formRules: any = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="user-edit-page">
    <!-- 页面标题区域 -->
    <div class="header-section">
      <div class="title-container">
        <Title :level="1" class="main-title">
          {{ isEditingSelf ? '编辑个人信息' : '编辑用户信息' }}
          <span class="title-icon">👤</span>
        </Title>
        <p class="subtitle">
          {{ isEditingSelf ? '修改您的个人信息和设置' : '修改用户的基本信息和权限' }}
        </p>
      </div>
    </div>

    <!-- 编辑表单 -->
    <div class="content-section">
      <Card class="content-card">
        <div v-if="!canEditUser" class="permission-denied">
          <Title :level="3" style="color: #ff4d4f;">权限不足</Title>
          <p>您没有权限编辑此用户的信息</p>
          <Button type="primary" @click="router.back()">返回</Button>
        </div>
        
        <Form
          v-else
          ref="formRef"
          :model="userForm"
          :rules="formRules"
          layout="vertical"
          class="edit-form"
        >
          <!-- 用户头像 -->
          <div class="avatar-section">
            <Avatar 
              :src="userForm.userAvatar" 
              :size="80"
              class="user-avatar"
            >
              <template #icon><UserOutlined /></template>
            </Avatar>
            <div class="avatar-info">
              <Title :level="4">{{ userForm.userName || '用户' }}</Title>
              <p class="user-role">{{ userForm.userRole === 'admin' ? '管理员' : '普通用户' }}</p>
            </div>
          </div>

          <Divider />

          <!-- 基本信息 -->
          <Form.Item label="用户名" name="userName">
            <Input 
              v-model:value="userForm.userName" 
              placeholder="请输入用户名"
              size="large"
            />
          </Form.Item>

          <Form.Item label="头像URL" name="userAvatar">
            <Input 
              v-model:value="userForm.userAvatar" 
              placeholder="请输入头像URL"
              size="large"
            />
          </Form.Item>

          <Form.Item label="个人简介" name="userProfile">
            <Input.TextArea 
              v-model:value="userForm.userProfile" 
              placeholder="请输入个人简介"
              :rows="4"
              size="large"
            />
          </Form.Item>

          <Form.Item 
            v-if="canChangeRole" 
            label="用户角色" 
            name="userRole"
          >
            <Select 
              v-model:value="userForm.userRole" 
              placeholder="请选择角色"
              size="large"
            >
              <Select.Option value="user">普通用户</Select.Option>
              <Select.Option value="admin">管理员</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item v-else label="用户角色">
            <Input 
              :value="userForm.userRole === 'admin' ? '管理员' : '普通用户'"
              disabled
              size="large"
            />
          </Form.Item>

          <!-- 操作按钮 -->
          <Form.Item class="form-actions">
            <Space size="large">
              <Button 
                type="primary" 
                size="large"
                :loading="loading" 
                @click="handleSave"
              >
                <template #icon><SaveOutlined /></template>
                保存
              </Button>
              <Button 
                size="large"
                @click="router.back()"
              >
                <template #icon><ArrowLeftOutlined /></template>
                返回
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.user-edit-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #e8f5e8 100%);
  padding: 24px;
}

.header-section {
  text-align: center;
  margin-bottom: 32px;
}

.title-container {
  max-width: 600px;
  margin: 0 auto;
}

.main-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.title-icon {
  font-size: 2rem;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.content-section {
  max-width: 800px;
  margin: 0 auto;
}

.content-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: none;
}

.permission-denied {
  text-align: center;
  padding: 48px 24px;
}

.edit-form {
  padding: 24px 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.user-avatar {
  border: 4px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-info {
  flex: 1;
}

.avatar-info h4 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.user-role {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.form-actions {
  margin-top: 32px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-edit-page {
    padding: 16px;
  }
  
  .main-title {
    font-size: 2rem;
  }
  
  .title-icon {
    width: 40px;
    height: 40px;
    font-size: 1.5rem;
  }
  
  .content-card {
    padding: 16px;
  }
  
  .edit-form {
    padding: 16px 0;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
}

@media (max-width: 576px) {
  .main-title {
    font-size: 1.5rem;
    flex-direction: column;
    gap: 8px;
  }
  
  .title-icon {
    width: 35px;
    height: 35px;
    font-size: 1.2rem;
  }
  
  .form-actions .ant-space {
    flex-direction: column;
    width: 100%;
  }
  
  .form-actions .ant-btn {
    width: 100%;
  }
}
</style> 