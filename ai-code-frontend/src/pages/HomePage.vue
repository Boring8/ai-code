<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppByPage } from '@/api/appController'
import { listMyTeamAppByPage } from '@/api/appUserController'
import { likeApp, unlikeApp, isUserLikedApp } from '../api/thumbController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'
import UploadButton from '@/components/UploadButton.vue'


const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 上传相关状态
const uploadedImages = ref<string[]>([])
const uploading = ref(false)



// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 团队应用数据
const teamApps = ref<API.AppVO[]>([])
const teamAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})



// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}


// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {

    
    const res = await addApp({
      initPrompt: userPrompt.value,
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载团队应用
const loadTeamApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyTeamAppByPage({
      pageNum: teamAppsPage.current,
      pageSize: teamAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      teamApps.value = res.data.data.records || []
      teamAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载团队应用失败：', error)
  }
}



// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 团队管理
const viewTeamManagement = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/detail/${appId}`)
  }
}

// 处理点赞/取消点赞
const handleLike = async (appId: string | number | undefined, liked: boolean) => {
  if (!appId) {
    message.error('应用ID无效')
    return
  }

  try {
    let success = false
    if (liked) {
      // 点赞
      const res = await likeApp({ appId: Number(appId) })
      success = res.data.code === 0 && res.data.data
    } else {
      // 取消点赞
      const res = await unlikeApp({ appId: Number(appId) })
      success = res.data.code === 0 && res.data.data
    }

    if (success) {
      message.success(liked ? '点赞成功' : '取消点赞成功')
      // 重新加载数据以更新点赞状态
      await loadMyApps()
      await loadTeamApps()
    } else {
      message.error(liked ? '点赞失败' : '取消点赞失败')
    }
  } catch (error) {
    console.error('点赞操作失败：', error)
    message.error('操作失败，请稍后重试')
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadTeamApps()
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 主要内容区域 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">一句话 呈所想</h1>
          <p class="hero-subtitle">与 AI 对话轻松创建应用和网站</p>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-section">
        <div class="input-container">

    
          <a-textarea
            v-model:value="userPrompt"
            placeholder="使用 easen AI 创建一个应用........"
            :rows="3"
            :maxlength="1000"
            class="prompt-input"
            @keydown.enter.exact.prevent="createApp"
          />
          <div class="input-actions">
            <div class="left-actions">
              <!-- 左侧可以放置其他操作按钮 -->
            </div>
            <div class="right-actions">
              <a-button 
                type="primary" 
                size="large" 
                @click="createApp" 
                :loading="creating"
                class="submit-btn"
              >
                <template #icon>
                  <span class="submit-icon">▶</span>
                </template>
              </a-button>
            </div>
          </div>
        </div>
        

      </div>

      <!-- 快捷模板 -->
      <div class="template-section">
        <div class="template-grid">
          <a-button
            type="default"
            class="template-btn"
            @click="setPrompt('创建一个现代化的个人博客网站，包含文章列表、分类标签、评论系统、搜索功能等')"
          >
            个人博客网站
          </a-button>
          <a-button
            type="default"
            class="template-btn"
            @click="setPrompt('设计一个专业的企业官网，包含公司介绍、产品服务、团队介绍、联系我们等页面')"
          >
            企业官网
          </a-button>
        </div>
      </div>

      <!-- 我的作品 -->
      <div v-if="loginUserStore.loginUser.id" class="section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="section-icon">📁</span>
            我的作品
          </h2>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
            @team-management="viewTeamManagement"
            @like="handleLike"
          />
          <div v-if="myApps.length === 0" class="empty-state">
            <div class="empty-icon">📄</div>
            <p class="empty-text">暂无应用</p>
          </div>
        </div>
        <div v-if="myAppsPage.total > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 我的团队 -->
      <div v-if="loginUserStore.loginUser.id && teamApps.length > 0" class="section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="section-icon">👥</span>
            我的团队
          </h2>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in teamApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
            @team-management="viewTeamManagement"
            @like="handleLike"
          />
        </div>
        <div v-if="teamAppsPage.total > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="teamAppsPage.current"
            v-model:page-size="teamAppsPage.pageSize"
            :total="teamAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个团队应用`"
            @change="loadTeamApps"
          />
        </div>
      </div>


    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #f5f9ff 100%);
  position: relative;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  position: relative;
  z-index: 2;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 60px 0 40px;
  margin-bottom: 40px;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px;
  line-height: 1.2;
  color: #1a1a1a;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 18px;
  margin: 0;
  color: #666;
  font-weight: 400;
}

/* 输入区域 */
.input-section {
  margin-bottom: 40px;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(24, 144, 255, 0.08);
  backdrop-filter: blur(20px);
}

.prompt-input {
  border: none;
  background: transparent;
  font-size: 16px;
  padding: 0;
  margin-bottom: 20px;
  resize: none;
  box-shadow: none;
  line-height: 1.6;
}

.prompt-input:focus {
  box-shadow: none;
  border: none;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mode-selector {
  display: flex;
  align-items: center;
}

.mode-select {
  min-width: 140px;
  border-radius: 8px;
}

.mode-select :deep(.ant-select-selector) {
  border-radius: 8px !important;
  border: 1px solid rgba(24, 144, 255, 0.2) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(10px);
}

.mode-select :deep(.ant-select-selector:hover) {
  border-color: rgba(24, 144, 255, 0.4) !important;
}

.mode-select :deep(.ant-select-focused .ant-select-selector) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1) !important;
}
.action-btn {
  color: #666;
  border: none;
  background: transparent;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.action-icon {
  font-size: 14px;
}

.submit-btn {
  width: 56px !important;
  height: 56px !important;
  min-width: 56px !important;
  min-height: 56px !important;
  max-width: 56px !important;
  max-height: 56px !important;
  border-radius: 50% !important;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.3);
  transition: all 0.3s ease;
  position: relative;
  padding: 0 !important;
  flex-shrink: 0;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-radius: 50%;
  z-index: -1;
  opacity: 0.3;
  transition: opacity 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.4);
}

.submit-btn:hover::before {
  opacity: 0.5;
}

.submit-icon {
  font-size: 20px;
  color: white;
  font-weight: bold;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 上传图片预览区域 */
.uploaded-images {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(24, 144, 255, 0.1);
}

.images-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  font-weight: 500;
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(24, 144, 255, 0.2);
  transition: all 0.3s ease;
}

.image-item:hover {
  border-color: rgba(24, 144, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.delete-btn {
  color: white !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important;
  border-radius: 50% !important;
  width: 24px !important;
  height: 24px !important;
  min-width: 24px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 12px !important;
  padding: 0 !important;
}

.delete-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  color: #ff4d4f !important;
}

/* 模板区域 */
.template-section {
  margin-bottom: 60px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.template-btn {
  height: 56px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(24, 144, 255, 0.15);
  color: #333;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(24, 144, 255, 0.3);
  color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.15);
}

/* 区域标题 */
.section {
  margin-bottom: 60px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
}

.team-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1890ff;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.team-link:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.team-icon {
  font-size: 16px;
}

.section-icon {
  font-size: 24px;
}

/* 应用网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 2px dashed rgba(24, 144, 255, 0.2);
  min-height: 200px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  color: #999;
  font-size: 16px;
  margin: 0;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 20px 16px;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .input-container {
    padding: 24px 20px;
  }
  
  .template-grid {
    grid-template-columns: 1fr;
    max-width: 100%;
  }
  
  .template-btn {
    height: 48px;
    font-size: 13px;
  }
  
  .submit-btn {
    width: 48px !important;
    height: 48px !important;
    min-width: 48px !important;
    min-height: 48px !important;
    max-width: 48px !important;
    max-height: 48px !important;
  }
  
  .submit-icon {
    font-size: 18px;
  }
  
  .app-grid {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .team-link {
    align-self: flex-end;
  }
  
  .input-actions {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .right-actions {
    justify-content: center;
  }
  
  .mode-select {
    min-width: 120px;
  }
}
</style>
