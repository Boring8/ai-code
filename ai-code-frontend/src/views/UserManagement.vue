<script setup lang="ts">
import { ref, reactive, onMounted, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal, Form, Input, Select, Button, Table, Space, Avatar, Tag, Popconfirm } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined, EditOutlined, DeleteOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import * as userController from '@/api/userController'

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
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
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
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined,
      userRole: searchForm.userRole || undefined,
      userProfile: searchForm.userProfile || undefined
    })
    
    const { code, data } = response.data
    if (code === 0 && data) {
      userList.value = data.records || []
      pagination.total = data.totalRow || 0
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
  pagination.current = 1
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
  pagination.current = 1
  fetchUserList()
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
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
const handleDeleteUser = async (userId: string) => {
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
const handleEditUser = (userId: string) => {
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
      onClick: () => handleEditUser(record.id!)
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
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
    customRender: ({ record }: { record: API.UserVO }) => h(Space, {}, {
      default: () => [
        h(Button, {
          type: 'link',
          size: 'small',
          onClick: () => handleEditUser(record.id!)
        }, {
          icon: () => h(EditOutlined),
          default: () => '编辑'
        }),
        h(Popconfirm, {
          title: '确定要删除这个用户吗？',
          onConfirm: () => handleDeleteUser(record.id!),
          okText: '确定',
          cancelText: '取消'
        }, {
          default: () => h(Button, {
            type: 'link',
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
  <div class="user-management">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <div class="page-header">
      <div class="header-content">
        <h1>用户管理</h1>
        <p class="header-subtitle">管理系统用户信息和权限</p>
      </div>
      <Button 
        type="primary" 
        @click="addModalVisible = true"
        class="add-user-btn"
      >
        <template #icon><PlusOutlined /></template>
        添加用户
      </Button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-title">
        <SearchOutlined class="search-icon" />
        <span>搜索筛选</span>
      </div>
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
        <Form.Item label="个人简介">
          <Input 
            v-model:value="searchForm.userProfile" 
            placeholder="请输入个人简介"
            allow-clear
            class="search-input"
          />
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
    </div>

    <!-- 用户列表表格 -->
    <div class="table-section">
      <Table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
        class="user-table"
      />
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
.user-management {
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
  justify-content: space-between;
  align-items: center;
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

.add-user-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 6px;
  padding: 0 20px;
  height: 36px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.add-user-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.search-section {
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

.search-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.search-icon {
  color: #667eea;
  font-size: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.search-input,
.search-select {
  border-radius: 6px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
}

.search-input:focus,
.search-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 6px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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

.table-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-table {
  border-radius: 12px;
}

/* 深度样式覆盖 */
:deep(.ant-table-thead > tr > th) {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e8e8e8;
  padding: 12px 8px;
  font-size: 13px;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 10px 8px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  font-size: 13px;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
  transform: scale(1.005);
}

/* 创建时间列样式 */
:deep(.ant-table-tbody > tr > td:nth-child(6)) {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
  border-left: 2px solid #ff6b6b;
  font-weight: 500;
  color: #d63384;
}

/* 操作列样式 */
:deep(.ant-table-tbody > tr > td:nth-child(7)) {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
  border-left: 2px solid #ff6b6b;
}

:deep(.ant-table-tbody > tr:hover > td:nth-child(6)),
:deep(.ant-table-tbody > tr:hover > td:nth-child(7)) {
  background: linear-gradient(135deg, #ffe8e8, #ffd6d6);
  transform: scale(1.01);
}

/* 分页样式 */
:deep(.ant-pagination) {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
  padding: 12px 16px;
  border-top: 2px solid #ff6b6b;
  margin: 0;
}

:deep(.ant-pagination-total-text) {
  color: #d63384;
  font-weight: 600;
  font-size: 12px;
}

:deep(.ant-pagination-item) {
  border-radius: 6px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  min-width: 28px;
  height: 28px;
  line-height: 26px;
}

:deep(.ant-pagination-item:hover) {
  border-color: #ff6b6b;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

:deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  border-color: #ff6b6b;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

:deep(.ant-pagination-item-active a) {
  color: white;
  font-weight: 600;
}

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: 6px;
  border: 2px solid #e8e8e8;
  transition: all 0.3s ease;
  min-width: 28px;
  height: 28px;
  line-height: 26px;
}

:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  border-color: #ff6b6b;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

:deep(.ant-pagination-options) {
  margin-left: 12px;
}

:deep(.ant-select-selector) {
  border-radius: 6px !important;
  border: 2px solid #e8e8e8 !important;
  transition: all 0.3s ease;
  height: 28px !important;
  line-height: 26px !important;
}

:deep(.ant-select-focused .ant-select-selector) {
  border-color: #ff6b6b !important;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.1) !important;
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

/* 操作按钮样式 */
:deep(.ant-btn-link) {
  border-radius: 4px;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 12px;
  padding: 2px 6px;
}

:deep(.ant-btn-link:hover) {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

:deep(.ant-btn-dangerous) {
  color: #ff6b6b;
}

:deep(.ant-btn-dangerous:hover) {
  background: rgba(255, 107, 107, 0.1);
  color: #ee5a24;
}

/* 模态框样式 */
:deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.ant-modal-header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
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
  .user-management {
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
  
  .search-section {
    padding: 12px 16px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-form .ant-form-item {
    margin-bottom: 12px;
  }
  
  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
    font-size: 12px;
  }
}
</style> 