<script setup lang="ts">
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal, Form, Input, Select, Button, Table, Space, Avatar, Tag, Popconfirm, Typography, Row, Col, Card, Spin, Empty, Pagination } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined, EditOutlined, DeleteOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import * as userController from '@/api/userController'

const { Title } = Typography
const router = useRouter()
const userStore = useUserStore()

// 权限检查
const canManageUsers = computed(() => userStore.canManageUsers)

// 如果用户没有权限，重定向到首页
if (!canManageUsers.value) {
  router.push('/')
}

// 表格数据
const userList = ref<API.UserVO[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  userName: '',
  userAccount: '',
  userRole: '',
  userProfile: ''
})

// 添加用户表单
const addUserForm = reactive({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user'
})

// 模态框状态
const addModalVisible = ref(false)
const addFormRef = ref()

// 角色选项
const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' }
]

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true
    const response = await userController.listUserVoByPage({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined,
      userRole: searchForm.userRole || undefined,
      userProfile: searchForm.userProfile || undefined
    })
    
    const { code, data } = response.data
    if (code === 0 && data) {
      userList.value = data.records || []
      pagination.value.total = data.totalRow || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  pagination.value.current = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    userName: '',
    userAccount: '',
    userRole: '',
    userProfile: ''
  })
  pagination.value.current = 1
  fetchUserList()
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.current = page
  pagination.value.pageSize = pageSize
  fetchUserList()
}

// 添加用户
const handleAddUser = async () => {
  try {
    await addFormRef.value?.validate()
    
    const response = await userController.addUser(addUserForm)
    const { code } = response.data
    
    if (code === 0) {
      message.success('添加用户成功')
      addModalVisible.value = false
      resetAddForm()
      fetchUserList()
    } else {
      message.error('添加用户失败')
    }
  } catch (error) {
    console.error('添加用户失败:', error)
    message.error('添加用户失败')
  }
}

// 删除用户
const handleDeleteUser = async (userId: number) => {
  try {
    const response = await userController.deleteUser({ id: userId })
    const { code } = response.data
    
    if (code === 0) {
      message.success('删除用户成功')
      fetchUserList()
    } else {
      message.error('删除用户失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    message.error('删除用户失败')
  }
}

// 编辑用户
const handleEditUser = (userId: number) => {
  router.push(`/user/edit/${userId}`)
}

// 重置添加表单
const resetAddForm = () => {
  Object.assign(addUserForm, {
    userName: '',
    userAccount: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user'
  })
  addFormRef.value?.resetFields()
}

// 表格列配置
const columns = [
  {
    title: '头像',
    key: 'userAvatar',
    width: 80,
    customRender: ({ record }: { record: API.UserVO }) => h(Avatar, {
      src: record.userAvatar,
      size: 40,
      style: { cursor: 'pointer' },
      onClick: () => record.id && handleEditUser(record.id)
    }, {
      icon: () => h(UserOutlined)
    })
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    width: 120
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 100,
    customRender: ({ record }: { record: API.UserVO }) => h(Tag, {
      color: record.userRole === 'admin' ? 'red' : 'blue'
    }, () => record.userRole === 'admin' ? '管理员' : '普通用户')
  },
  {
    title: '个人简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    customRender: ({ text }: { text: string }) => {
      if (!text) return '-'
      return new Date(text).toLocaleString('zh-CN')
    }
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
    customRender: ({ record }: { record: API.UserVO }) => h(Space, {}, {
      default: () => [
        h(Button, {
          type: 'primary',
          size: 'small',
          onClick: () => record.id && handleEditUser(record.id)
        }, {
          icon: () => h(EditOutlined),
          default: () => '编辑'
        }),
        h(Popconfirm, {
          title: '确定要删除这个用户吗？',
          onConfirm: () => record.id && handleDeleteUser(record.id),
          okText: '确定',
          cancelText: '取消'
        }, {
          default: () => h(Button, {
            size: 'small',
            danger: true
          }, {
            icon: () => h(DeleteOutlined),
            default: () => '删除'
          })
        })
      ]
    })
  }
]

// 添加用户表单验证规则
const addFormRules: any = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(() => {
  fetchUserList()
})
</script>

<template>
  <div class="user-management-page">
    <!-- 页面标题区域 -->
    <div class="header-section">
      <div class="title-container">
        <Title :level="1" class="main-title">
          用户管理
          <span class="title-icon">👥</span>
        </Title>
        <p class="subtitle">
          管理系统用户信息和权限，添加、编辑和删除用户
        </p>
      </div>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <Card class="action-card">
        <Row :gutter="16" align="middle">
          <Col :span="20">
            <Form layout="inline" :model="searchForm" class="search-form">
              <Form.Item label="用户名">
                <Input 
                  v-model:value="searchForm.userName" 
                  placeholder="请输入用户名"
                  allow-clear
                  class="search-input"
                />
              </Form.Item>
              <Form.Item label="账号">
                <Input 
                  v-model:value="searchForm.userAccount" 
                  placeholder="请输入账号"
                  allow-clear
                  class="search-input"
                />
              </Form.Item>
              <Form.Item label="角色">
                <Select 
                  v-model:value="searchForm.userRole" 
                  placeholder="请选择角色"
                  allow-clear
                  style="width: 120px"
                  class="search-select"
                >
                  <Select.Option value="user">普通用户</Select.Option>
                  <Select.Option value="admin">管理员</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item>
                <Button type="primary" @click="handleSearch" class="search-btn">
                  <template #icon><SearchOutlined /></template>
                  搜索
                </Button>
              </Form.Item>
              <Form.Item>
                <Button @click="handleReset" class="reset-btn">重置</Button>
              </Form.Item>
            </Form>
          </Col>
          <Col :span="4" style="text-align: right">
            <Button 
              type="primary" 
              size="large"
              @click="addModalVisible = true"
            >
              <template #icon><PlusOutlined /></template>
              添加用户
            </Button>
          </Col>
        </Row>
      </Card>
    </div>

    <!-- 用户列表 -->
    <div class="content-section">
      <Card class="content-card">
        <Spin :spinning="loading">
          <div v-if="userList.length > 0">
            <Table
              :columns="columns"
              :data-source="userList"
              :pagination="false"
              row-key="id"
              :scroll="{ x: 1000 }"
              class="user-table"
            />
            
            <div class="pagination-container">
              <Pagination
                v-model:current="pagination.current"
                v-model:page-size="pagination.pageSize"
                :total="pagination.total"
                :show-size-changer="true"
                :show-quick-jumper="true"
                :show-total="(total: number, range: [number, number]) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`"
                @change="handlePageChange"
              />
            </div>
          </div>
          
          <Empty 
            v-else 
            description="暂无用户数据"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
          />
        </Spin>
      </Card>
    </div>

    <!-- 添加用户模态框 -->
    <Modal
      v-model:open="addModalVisible"
      title="添加用户"
      @ok="handleAddUser"
      @cancel="resetAddForm"
      :confirm-loading="loading"
      class="add-user-modal"
    >
      <Form
        ref="addFormRef"
        :model="addUserForm"
        :rules="addFormRules"
        layout="vertical"
      >
        <Form.Item label="用户名" name="userName">
          <Input v-model:value="addUserForm.userName" placeholder="请输入用户名" />
        </Form.Item>
        <Form.Item label="账号" name="userAccount">
          <Input v-model:value="addUserForm.userAccount" placeholder="请输入账号" />
        </Form.Item>
        <Form.Item label="头像URL" name="userAvatar">
          <Input v-model:value="addUserForm.userAvatar" placeholder="请输入头像URL" />
        </Form.Item>
        <Form.Item label="个人简介" name="userProfile">
          <Input.TextArea 
            v-model:value="addUserForm.userProfile" 
            placeholder="请输入个人简介"
            :rows="3"
          />
        </Form.Item>
        <Form.Item label="角色" name="userRole">
          <Select v-model:value="addUserForm.userRole" placeholder="请选择角色">
            <Select.Option value="user">普通用户</Select.Option>
            <Select.Option value="admin">管理员</Select.Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  </div>
</template>

<style scoped>
.user-management-page {
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

.action-section {
  max-width: 1200px;
  margin: 0 auto 24px;
}

.action-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: none;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-input,
.search-select {
  border-radius: 6px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
}

.search-input:focus,
.search-select:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.search-btn {
  background: #1890ff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #40a9ff;
  transform: translateY(-1px);
}

.reset-btn {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background: #f5f5f5;
  transform: translateY(-1px);
}

.content-section {
  max-width: 1200px;
  margin: 0 auto;
}

.content-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: none;
}

.user-table {
  margin-bottom: 24px;
}

.user-table :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

.user-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5;
}

.pagination-container {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

/* 角色标签样式 */
:deep(.ant-tag) {
  border-radius: 8px;
  font-weight: 600;
  padding: 2px 8px;
  border: none;
  transition: all 0.3s ease;
  font-size: 11px;
}

:deep(.ant-tag-red) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  color: white;
  box-shadow: 0 1px 4px rgba(255, 107, 107, 0.3);
}

:deep(.ant-tag-blue) {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
  color: white;
  box-shadow: 0 1px 4px rgba(78, 205, 196, 0.3);
}

/* 模态框样式 */
:deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.ant-modal-header) {
  background: #1890ff;
  border-bottom: none;
  padding: 16px 20px;
}

:deep(.ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 16px;
}

:deep(.ant-modal-close) {
  color: white;
}

:deep(.ant-modal-body) {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management-page {
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
  
  .action-card {
    padding: 16px;
  }
  
  .content-card {
    padding: 16px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
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
}
</style> 