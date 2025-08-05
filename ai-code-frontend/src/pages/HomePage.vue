<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppByPage, listFeaturedAppByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 代码生成类型
const codeGenType = ref('html')

// 代码生成类型选项
const codeGenTypeOptions = [
  { label: '原生 HTML 模式', value: 'html' },
  { label: '原生多文件模式', value: 'multi_file' },
]

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
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
    // 生成应用名称：取用户输入的前10位
    const appName = userPrompt.value.trim().substring(0, 10)
    
    const res = await addApp({
      appName: appName,
      initPrompt: userPrompt.value.trim(),
      codeGenType: codeGenType.value,
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

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listFeaturedAppByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
      priority: 99,
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
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

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
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
            placeholder="使用 NoCode 创建一个高效的小工具,帮我计算........"
            :rows="3"
            :maxlength="1000"
            class="prompt-input"
          />
          <div class="input-actions">
            <div class="left-actions">
              <a-button type="text" class="action-btn">
                <template #icon>
                  <span class="action-icon">📤</span>
                </template>
                上传
              </a-button>
              <a-button type="text" class="action-btn">
                <template #icon>
                  <span class="action-icon">✨</span>
                </template>
                优化
              </a-button>
            </div>
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
      <div class="section">
        <h2 class="section-title">
          <span class="section-icon">📁</span>
          我的作品
        </h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
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

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">
          <span class="section-icon">⭐</span>
          精选案例
        </h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
          <div v-if="featuredApps.length === 0" class="empty-state">
            <div class="empty-icon">📄</div>
            <p class="empty-text">暂无案例</p>
          </div>
        </div>
        <div v-if="featuredAppsPage.total > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
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

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 24px;
}

/* 应用网格 */
.app-grid,
.featured-grid {
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
  
  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: 24px;
  }
}
</style>
