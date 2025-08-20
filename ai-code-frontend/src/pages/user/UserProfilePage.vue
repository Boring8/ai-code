<template>
  <div class="user-profile-page">
    <div class="profile-container">
      <div class="profile-header">
        <h1>个人信息</h1>
        <p>管理您的账户信息和设置</p>
      </div>
      
      <div class="profile-content">
        <a-card class="profile-card">
          <div class="avatar-section">
            <a-avatar 
              :src="loginUserStore.loginUser.userAvatar" 
              :size="120"
              class="profile-avatar"
            >
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <div class="avatar-info">
              <h2>{{ loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || '未设置昵称' }}</h2>
              <p class="user-role">{{ formatUserRole(loginUserStore.loginUser.userRole) }}</p>
            </div>
          </div>
          
          <a-divider />
          
          <div class="info-section">
            <h3>基本信息</h3>
            <a-form 
              :model="formData" 
              :label-col="{ span: 6 }" 
              :wrapper-col="{ span: 18 }"
              layout="horizontal"
            >
              <a-form-item label="用户名">
                <a-input 
                  :value="loginUserStore.loginUser.userAccount" 
                  placeholder="用户名"
                  disabled
                  class="readonly-input"
                />
              </a-form-item>
              
              <a-form-item label="昵称">
                <a-input 
                  v-model:value="formData.userName" 
                  placeholder="请输入昵称"
                  :disabled="!isEditing"
                />
              </a-form-item>
              
              
              <a-form-item label="用户角色">
                <a-tag :color="getRoleColor(loginUserStore.loginUser.userRole)">
                  {{ formatUserRole(loginUserStore.loginUser.userRole) }}
                </a-tag>
              </a-form-item>
              
              <a-form-item label="注册时间">
                <span>{{ formatDate(loginUserStore.loginUser.createTime) }}</span>
              </a-form-item>
            </a-form>
          </div>
          
          <div class="action-section">
            <a-space>
              <a-button 
                v-if="!isEditing" 
                type="primary" 
                @click="startEdit"
              >
                <template #icon>
                  <EditOutlined />
                </template>
                编辑信息
              </a-button>
              <a-button 
                v-if="isEditing" 
                type="primary" 
                @click="saveProfile"
                :loading="saving"
              >
                <template #icon>
                  <SaveOutlined />
                </template>
                保存
              </a-button>
              <a-button 
                v-if="isEditing" 
                @click="cancelEdit"
              >
                取消
              </a-button>
              <a-button @click="goBack">
                <template #icon>
                  <ArrowLeftOutlined />
                </template>
                返回
              </a-button>
            </a-space>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '../../stores/loginUser'
import { updateUser } from '../../api/userController'
import { 
  EditOutlined, 
  SaveOutlined, 
  ArrowLeftOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 响应式数据
const isEditing = ref(false)
const saving = ref(false)

// 表单数据
const formData = reactive({
  userName: ''
})

// 初始化表单数据
const initFormData = () => {
  formData.userName = loginUserStore.loginUser.userName || ''
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  initFormData()
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  initFormData()
}

// 保存个人信息
const saveProfile = async () => {
  if (!formData.userName.trim()) {
    message.error('昵称不能为空')
    return
  }
  
  saving.value = true
  try {
    const res = await updateUser({
      id: loginUserStore.loginUser.id,
      userName: formData.userName
    })
    
    if (res.data.code === 0) {
      // 更新本地用户信息
      loginUserStore.setLoginUser({
        ...loginUserStore.loginUser,
        userName: formData.userName
      })
      message.success('保存成功')
      isEditing.value = false
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 格式化用户角色
const formatUserRole = (role?: string) => {
  switch (role) {
    case 'admin':
      return '管理员'
    case 'user':
      return '普通用户'
    default:
      return '未知'
  }
}

// 获取角色颜色
const getRoleColor = (role?: string) => {
  switch (role) {
    case 'admin':
      return 'red'
    case 'user':
      return 'blue'
    default:
      return 'default'
  }
}

// 格式化日期
const formatDate = (date?: string) => {
  if (!date) return '未知'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 组件挂载时初始化
onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.user-profile-page {
  min-height: 70vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  padding: 20px;
  box-sizing: border-box;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  min-height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.profile-header {
  text-align: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.profile-header h1 {
  font-size: 2.2rem;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1a1a1a;
  letter-spacing: -1px;
}

.profile-header p {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

.profile-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.profile-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(24, 144, 255, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.profile-card :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
  height: 100%;
  overflow-y: auto;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.profile-avatar {
  border: 4px solid rgba(24, 144, 255, 0.1);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.profile-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(24, 144, 255, 0.2);
}

.avatar-info h2 {
  margin: 0 0 8px 0;
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
}

.user-role {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.info-section {
  flex: 1;
  margin-bottom: 20px;
}

.info-section h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  position: relative;
  padding-left: 12px;
}

.info-section h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-radius: 2px;
}

.info-tip {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
  padding-left: 12px;
}

.info-section :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #333;
}

.info-section :deep(.ant-input) {
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s ease;
}

.info-section :deep(.ant-input:focus) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.info-section :deep(.ant-input:disabled) {
  background-color: #f5f5f5;
  color: #666;
}

.readonly-input {
  background-color: #fafafa !important;
  color: #666 !important;
  cursor: not-allowed;
}

.action-section {
  flex-shrink: 0;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(24, 144, 255, 0.1);
  margin-top: auto;
}

.action-section :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 20px;
  height: auto;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.action-section :deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 100%);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.action-section :deep(.ant-btn) {
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 20px;
  height: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile-page {
    padding: 16px;
  }
  
  .profile-container {
    min-height: calc(100vh - 32px);
  }
  
  .profile-header h1 {
    font-size: 1.8rem;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .profile-avatar {
    margin-bottom: 0;
  }
}

/* 滚动条样式 */
.profile-card :deep(.ant-card-body)::-webkit-scrollbar {
  width: 6px;
}

.profile-card :deep(.ant-card-body)::-webkit-scrollbar-track {
  background: rgba(24, 144, 255, 0.05);
  border-radius: 3px;
}

.profile-card :deep(.ant-card-body)::-webkit-scrollbar-thumb {
  background: rgba(24, 144, 255, 0.3);
  border-radius: 3px;
  transition: all 0.3s ease;
}

.profile-card :deep(.ant-card-body)::-webkit-scrollbar-thumb:hover {
  background: rgba(24, 144, 255, 0.5);
}
</style>
