<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Card, Row, Col, Typography, Button, Avatar, Space, Tag, Input, Pagination, Empty, Spin } from 'ant-design-vue'
import { SendOutlined, UploadOutlined, StarOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PermissionControl from '@/components/PermissionControl.vue'
import * as appController from '@/api/appController'
import { getRouteId, safeBigIntToString } from '@/utils/bigIntUtils'
// 移除类型导入，使用 any 类型

const { Title, Paragraph } = Typography
const { TextArea } = Input
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const promptInput = ref('')
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const loading = ref(false)
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)

// 分页数据
const myAppsPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  searchName: ''
})

const featuredAppsPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  searchName: ''
})

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 方法
const createApp = async () => {
  if (!promptInput.value.trim()) {
    return
  }
  
  try {
    loading.value = true
    const response = await appController.addApp({
      appName: '新应用',
      initPrompt: promptInput.value,
      codeGenType: 'react'
    })
    
    if (response.data.code === 0 && response.data.data) {
      const appId = response.data.data
      router.push(`/app/chat/${appId}`)
    }
  } catch (error) {
    console.error('创建应用失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMyApps = async () => {
  if (!isLoggedIn.value) return
  
  try {
    myAppsLoading.value = true
    const params = {
      pageNum: myAppsPagination.current,
      pageSize: myAppsPagination.pageSize,
      appName: myAppsPagination.searchName || undefined
    }
    
    const response = await appController.listMyAppByPage(params)
    if (response.data.code === 0 && response.data.data) {
      myApps.value = response.data.data.records || []
      myAppsPagination.total = response.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败:', error)
  } finally {
    myAppsLoading.value = false
  }
}

const loadFeaturedApps = async () => {
  try {
    featuredAppsLoading.value = true
    const params = {
      pageNum: featuredAppsPagination.current,
      pageSize: featuredAppsPagination.pageSize,
      appName: featuredAppsPagination.searchName || undefined,
      isFeatured: true
    }
    
    const response = await appController.listFeaturedAppByPage(params)
    if (response.data.code === 0 && response.data.data) {
      featuredApps.value = response.data.data.records || []
      featuredAppsPagination.total = response.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败:', error)
  } finally {
    featuredAppsLoading.value = false
  }
}

const handleMyAppsPageChange = (page: number) => {
  myAppsPagination.current = page
  loadMyApps()
}

const handleFeaturedAppsPageChange = (page: number) => {
  featuredAppsPagination.current = page
  loadFeaturedApps()
}

const goToAppChat = (appId: string | number) => {
  router.push(`/app/chat/${getRouteId(appId)}`)
}

const goToAppEdit = (appId: string | number) => {
  router.push(`/app/edit/${getRouteId(appId)}`)
}

const deleteMyApp = async (appId: string | number) => {
  try {
    const response = await appController.deleteMyApp({ id: safeBigIntToString(appId) })
    if (response.data.code === 0) {
      loadMyApps()
    }
  } catch (error) {
    console.error('删除应用失败:', error)
  }
}

const getTimeAgo = (timeStr: string) => {
  const now = new Date()
  const time = new Date(timeStr)
  const diff = now.getTime() - time.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(hours / 24)
  const weeks = Math.floor(days / 7)
  
  if (weeks > 0) return `${weeks}周前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  return '刚刚'
}

onMounted(() => {
  loadFeaturedApps()
  if (isLoggedIn.value) {
    loadMyApps()
  }
})
</script>

<template>
  <div class="home-page">
    <!-- 网站标题区域 -->
    <div class="header-section">
      <div class="title-container">
        <Title :level="1" class="main-title">
          一句话 呈所想
          <span class="logo-icon">🐱</span>
        </Title>
        <Paragraph class="subtitle">
          与 AI 对话轻松创建应用和网站
        </Paragraph>
      </div>
    </div>

    <!-- 用户提示词输入框 -->
    <div class="prompt-section">
      <div class="prompt-container">
        <TextArea
          v-model:value="promptInput"
          placeholder="使用 NoCode 创建一个高效的小工具,帮我计算......."
          :rows="4"
          class="prompt-input"
          :disabled="!isLoggedIn"
        />
        <div class="prompt-controls">
          <div class="left-controls">
            <Button size="small" class="control-btn">
              <template #icon><UploadOutlined /></template>
              @ 上传
            </Button>
            <Button size="small" class="control-btn">
              <template #icon><StarOutlined /></template>
              ※ 优化
            </Button>
          </div>
          <Button
            type="primary"
            shape="circle"
            :loading="loading"
            :disabled="!isLoggedIn || !promptInput.trim()"
            @click="createApp"
            class="send-btn"
          >
            <template #icon><SendOutlined /></template>
          </Button>
        </div>
      </div>
    </div>

    <!-- 快捷分类按钮 -->
    <div class="category-section">
      <Space wrap>
        <Button class="category-btn">波普风电商页面</Button>
        <Button class="category-btn">企业网站</Button>
        <Button class="category-btn">电商运营后台</Button>
        <Button class="category-btn">暗黑话题社区</Button>
      </Space>
    </div>

    <!-- 我的应用列表 -->
    <div v-if="isLoggedIn" class="apps-section">
      <Title :level="3" class="section-title">我的作品</Title>
      <Spin :spinning="myAppsLoading">
        <div v-if="myApps.length > 0" class="apps-grid">
          <Card
            v-for="app in myApps"
            :key="app.id"
            class="app-card"
            hoverable
          >
            <div class="app-preview">
              <div class="app-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                <div v-else class="placeholder-cover">
                  <span>预览</span>
                </div>
              </div>
            </div>
            <div class="app-info">
              <Title :level="5" class="app-name">{{ app.appName }}</Title>
              <Paragraph class="app-time">创建于 {{ getTimeAgo(app.createTime || '') }}</Paragraph>
              <div class="app-actions">
                <Space>
                  <Button size="small" @click="goToAppChat(app.id!)">
                    <template #icon><EyeOutlined /></template>
                    查看
                  </Button>
                  <Button size="small" @click="goToAppEdit(app.id!)">
                    <template #icon><EditOutlined /></template>
                    编辑
                  </Button>
                  <Button size="small" danger @click="deleteMyApp(app.id!)">
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </Button>
                </Space>
              </div>
            </div>
          </Card>
        </div>
        <Empty v-else description="暂无应用" />
        <div v-if="myApps.length > 0" class="pagination-container">
          <Pagination
            v-model:current="myAppsPagination.current"
            :total="myAppsPagination.total"
            :page-size="myAppsPagination.pageSize"
            @change="handleMyAppsPageChange"
          />
        </div>
      </Spin>
    </div>

    <!-- 精选应用列表 -->
    <div class="apps-section">
      <Title :level="3" class="section-title">精选案例</Title>
      <Spin :spinning="featuredAppsLoading">
        <div v-if="featuredApps.length > 0" class="apps-grid">
          <Card
            v-for="app in featuredApps"
            :key="app.id"
            class="app-card"
            hoverable
            @click="goToAppChat(app.id!)"
          >
            <div class="app-preview">
              <div class="app-cover">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                <div v-else class="placeholder-cover">
                  <span>预览</span>
                </div>
              </div>
            </div>
            <div class="app-info">
              <Title :level="5" class="app-name">{{ app.appName }}</Title>
              <div class="app-meta">
                <Tag v-if="app.codeGenType" color="blue">{{ app.codeGenType }}</Tag>
                <span class="app-author">NoCode 官方</span>
              </div>
            </div>
          </Card>
        </div>
        <Empty v-else description="暂无精选应用" />
        <div v-if="featuredApps.length > 0" class="pagination-container">
          <Pagination
            v-model:current="featuredAppsPagination.current"
            :total="featuredAppsPagination.total"
            :page-size="featuredAppsPagination.pageSize"
            @change="handleFeaturedAppsPageChange"
          />
        </div>
      </Spin>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3f2fd 0%, #e8f5e8 100%);
  padding: 24px;
}

.header-section {
  text-align: center;
  margin-bottom: 48px;
}

.title-container {
  max-width: 600px;
  margin: 0 auto;
}

.main-title {
  font-size: 3rem;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.logo-icon {
  font-size: 2.5rem;
  background: #00bcd4;
  color: white;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.subtitle {
  font-size: 1.2rem;
  color: #666;
  margin: 0;
}

.prompt-section {
  max-width: 800px;
  margin: 0 auto 32px;
}

.prompt-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.prompt-input {
  border: none;
  resize: vertical;
  font-size: 16px;
  min-height: 120px;
}

.prompt-input:focus {
  box-shadow: none;
}

.prompt-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.left-controls {
  display: flex;
  gap: 8px;
}

.control-btn {
  border-radius: 20px;
  font-size: 12px;
}

.send-btn {
  width: 40px;
  height: 40px;
}

.category-section {
  text-align: center;
  margin-bottom: 48px;
}

.category-btn {
  border-radius: 20px;
  height: 40px;
  padding: 0 20px;
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.category-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
}

.apps-section {
  max-width: 1200px;
  margin: 0 auto 48px;
}

.section-title {
  margin-bottom: 24px;
  color: #1a1a1a;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.app-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s;
}

.app-card:hover {
  transform: translateY(-4px);
}

.app-preview {
  height: 200px;
  overflow: hidden;
}

.app-cover {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-cover {
  color: #999;
  font-size: 14px;
}

.app-info {
  padding: 16px;
}

.app-name {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.app-time {
  color: #666;
  font-size: 12px;
  margin: 0 0 12px 0;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-author {
  font-size: 12px;
  color: #666;
}

.app-actions {
  margin-top: 12px;
}

.pagination-container {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }
  
  .logo-icon {
    width: 40px;
    height: 40px;
    font-size: 1.5rem;
  }
  
  .apps-grid {
    grid-template-columns: 1fr;
  }
  
  .prompt-container {
    padding: 16px;
  }
}
</style> 