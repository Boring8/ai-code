<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Form, Input, Select, Button, Avatar, Card, Space, Divider } from 'ant-design-vue'
import { UserOutlined, SaveOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import * as userController from '@/api/userController'

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
        id: data.id,
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
      // 保持原始角色不变
      dataToSave.userRole = userInfo.value?.userRole || 'user'
    }
    
    const response = await userController.updateUser(dataToSave)
    const { code } = response.data
    
    if (code === 0) {
      message.success('保存成功')
      
      // 如果编辑的是当前登录用户，更新store中的用户信息
      if (userStore.userInfo?.id === userId.value) {
        userStore.updateUserInfo(dataToSave)
      }
      
      // 返回上一页
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

// 返回上一页
const handleBack = () => {
  router.back()
}

// 表单验证规则
const formRules = computed(() => {
  const rules: any = {
    userName: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
    ]
  }
  
  // 只有当用户可以修改角色时才添加角色验证规则
  if (canChangeRole.value) {
    rules.userRole = [
      { required: true, message: '请选择角色', trigger: 'change' }
    ]
  }
  
  return rules
})

onMounted(() => {
  // 权限检查
  if (!canEditUser.value) {
    message.error('您没有权限编辑此用户信息')
    router.push('/')
    return
  }
  
  fetchUserInfo()
})
</script>

<template>
  <div class="user-edit">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <div class="page-header">
      <Button 
        type="link" 
        @click="handleBack"
        class="back-button"
      >
        <template #icon><ArrowLeftOutlined /></template>
        返回
      </Button>
      <div class="header-content">
        <h1>编辑用户信息</h1>
        <p class="header-subtitle">管理用户资料和权限设置</p>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 用户基本信息卡片 -->
      <Card class="info-card" :bordered="false">
        <template #title>
          <div class="card-title">
            <UserOutlined class="title-icon" />
            <span>用户基本信息</span>
          </div>
        </template>
        <div class="user-info">
          <div class="avatar-section">
            <Avatar 
              :src="userInfo?.userAvatar" 
              :size="100"
              class="user-avatar"
            >
              <template #icon><UserOutlined /></template>
            </Avatar>
            <div class="avatar-badge" v-if="userInfo?.userRole === 'admin'">
              <span>管理员</span>
            </div>
          </div>
          <div class="user-details">
            <h3 class="user-name">{{ userInfo?.userName || '未知用户' }}</h3>
            <div class="detail-item">
              <span class="detail-label">账号:</span>
              <span class="detail-value">{{ userInfo?.userAccount }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">角色:</span>
              <span class="detail-value role-badge" :class="userInfo?.userRole === 'admin' ? 'admin' : 'user'">
                {{ userInfo?.userRole === 'admin' ? '管理员' : '普通用户' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间:</span>
              <span class="detail-value">{{ userInfo?.createTime }}</span>
            </div>
          </div>
        </div>
      </Card>

      <!-- 编辑表单卡片 -->
      <Card class="form-card" :bordered="false">
        <template #title>
          <div class="card-title">
            <SaveOutlined class="title-icon" />
            <span>编辑信息</span>
          </div>
        </template>
        <Form
          ref="formRef"
          :model="userForm"
          :rules="formRules"
          layout="vertical"
          :label-col="{ span: 24 }"
          :wrapper-col="{ span: 24 }"
          class="edit-form"
        >
          <Form.Item label="用户名" name="userName" class="form-item">
            <Input 
              v-model:value="userForm.userName" 
              placeholder="请输入用户名"
              :maxlength="20"
              show-count
              class="custom-input"
            />
          </Form.Item>

          <Form.Item label="头像URL" name="userAvatar" class="form-item">
            <Input 
              v-model:value="userForm.userAvatar" 
              placeholder="请输入头像URL"
              class="custom-input"
            />
            <div class="avatar-preview" v-if="userForm.userAvatar">
              <div class="preview-label">预览效果:</div>
              <Avatar 
                :src="userForm.userAvatar" 
                :size="60"
                class="preview-avatar"
              >
                <template #icon><UserOutlined /></template>
              </Avatar>
            </div>
          </Form.Item>

          <Form.Item label="个人简介" name="userProfile" class="form-item">
            <Input.TextArea 
              v-model:value="userForm.userProfile" 
              placeholder="请输入个人简介"
              :rows="4"
              :maxlength="200"
              show-count
              class="custom-textarea"
            />
          </Form.Item>

          <Form.Item 
            label="角色" 
            name="userRole"
            v-if="canChangeRole"
            class="form-item"
          >
            <Select 
              v-model:value="userForm.userRole" 
              placeholder="请选择角色"
              class="custom-select"
            >
              <Select.Option value="user">普通用户</Select.Option>
              <Select.Option value="admin">管理员</Select.Option>
            </Select>

          </Form.Item>

          <Form.Item class="form-actions">
            <Space size="large">
              <Button 
                type="primary" 
                @click="handleSave"
                :loading="loading"
                size="large"
                class="save-button"
              >
                <template #icon><SaveOutlined /></template>
                保存更改
              </Button>
              <Button 
                @click="handleBack"
                size="large"
                class="cancel-button"
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.user-edit {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 88vh;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 150px;
  height: 150px;
  top: 5%;
  right: 5%;
  animation-delay: 0s;
}

.circle-2 {
  width: 100px;
  height: 100px;
  top: 50%;
  left: 3%;
  animation-delay: 2s;
}

.circle-3 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 50%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.back-button {
  padding: 6px 12px;
  height: auto;
  font-size: 14px;
  color: #667eea;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(-2px);
}

.header-content h1 {
  margin: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.header-subtitle {
  margin: 2px 0 0 0;
  color: #666;
  font-size: 12px;
  font-weight: 400;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.info-card,
.form-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.info-card:hover,
.form-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.15);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.title-icon {
  color: #667eea;
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
}

.avatar-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  border: 3px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.03);
}

.avatar-badge {
  position: absolute;
  bottom: -6px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(238, 90, 36, 0.3);
}

.user-details {
  flex: 1;
}

.user-name {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 0;
}

.detail-label {
  font-weight: 600;
  color: #666;
  min-width: 70px;
  margin-right: 8px;
  font-size: 13px;
}

.detail-value {
  color: #333;
  font-weight: 500;
  font-size: 13px;
}

.role-badge {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
}

.role-badge.admin {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
}

.role-badge.user {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
}

.edit-form {
  padding: 4px 0;
}

.form-item {
  margin-bottom: 16px;
}

.custom-input,
.custom-textarea,
.custom-select {
  border-radius: 6px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
}

.custom-input:focus,
.custom-textarea:focus,
.custom-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.avatar-preview {
  margin-top: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  text-align: center;
  border: 2px dashed #dee2e6;
}

.preview-label {
  font-size: 11px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.preview-avatar {
  border: 2px solid #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
}

.form-tip {
  margin-top: 6px;
  padding: 6px 10px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 4px;
  border-left: 2px solid #667eea;
}

.form-tip small {
  font-size: 11px;
  color: #667eea;
  font-weight: 500;
}

.form-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.save-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 6px;
  padding: 0 24px;
  height: 36px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.save-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.cancel-button {
  border-radius: 6px;
  padding: 0 24px;
  height: 36px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: #f5f5f5;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .user-edit {
    padding: 12px;
  }
  
  .page-header {
    padding: 12px 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-content h1 {
    font-size: 20px;
  }
  
  .user-info {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .detail-item {
    justify-content: center;
  }
  
  .save-button,
  .cancel-button {
    width: 100%;
    margin-bottom: 8px;
  }
}

/* 深度样式覆盖 */
:deep(.ant-card-head) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 12px 16px 8px;
}

:deep(.ant-card-body) {
  padding: 16px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

:deep(.ant-input::placeholder),
:deep(.ant-select-selection-placeholder) {
  color: #999;
}

:deep(.ant-select-selector) {
  border-radius: 6px !important;
  border: 2px solid #e8e8e8 !important;
}

:deep(.ant-select-focused .ant-select-selector) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
}
</style> 