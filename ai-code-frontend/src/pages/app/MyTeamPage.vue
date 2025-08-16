<template>
  <div id="myTeamPage">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <span class="page-icon">👥</span>
          我的团队
        </h1>
        <p class="page-subtitle">查看您参与的团队应用</p>
      </div>

      <!-- 搜索和筛选区域 -->
      <div class="search-section">
        <div class="search-form">
          <div class="form-row">


            <div class="form-item">
              <label class="form-label">应用名称：</label>
              <a-input
                v-model:value="searchForm.appName"
                placeholder="输入应用名称"
                style="width: 200px"
                allow-clear
              />
            </div>
            <div class="form-item">
              <label class="form-label">生成类型：</label>
                             <a-select
                 v-model:value="searchForm.codeGenType"
                 placeholder="选择生成类型"
                 style="width: 200px"
                 allow-clear
               >
                 <a-select-option value="Native HTML Mode">原生 HTML 模式</a-select-option>
                 <a-select-option value="Native Multi-File Mode">原生多文件模式</a-select-option>
                 <a-select-option value="Vue Engineering Mode">Vue 工程模式</a-select-option>
               </a-select>
            </div>
            <div >
              <a-button type="primary" @click="handleSearch" :loading="loading">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button style="margin-left: 8px" @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 应用列表 -->
      <div class="section">
        <div class="section-header">
          <h2 class="section-title">
            {{ getSectionTitle() }}
            <span class="app-count">({{ pagination.total }})</span>
          </h2>
        </div>
        
        <div class="app-grid">
          <AppCard
            v-for="app in apps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
            @team-management="viewTeamManagement"
            @like="handleLike"
          />
          <div v-if="apps.length === 0 && !loading" class="empty-state">
            <div class="empty-icon">👥</div>
            <p class="empty-text">{{ getEmptyText() }}</p>
            <p class="empty-subtext">{{ getEmptySubtext() }}</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="pagination-wrapper">
          <a-pagination
            v-model:current="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { listMyTeamAppByPage } from '@/api/appUserController'
import { likeApp, unlikeApp, isUserLikedApp } from '../../api/thumbController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用数据
const apps = ref<API.AppVO[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  appName: '',
  codeGenType: '',
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 获取区域标题
const getSectionTitle = () => {
  return '团队应用'
}

// 获取空状态文本
const getEmptyText = () => {
  return '暂无团队应用'
}

// 获取空状态副文本
const getEmptySubtext = () => {
  return '您还没有参与任何团队应用'
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  loadApps()
}

// 重置处理
const handleReset = () => {
  searchForm.appName = ''
  searchForm.codeGenType = ''
  pagination.current = 1
  loadApps()
}

// 加载应用数据
const loadApps = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  loading.value = true
  try {
    // 只查询团队应用
    const res = await listMyTeamAppByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
      appName: searchForm.appName || undefined,
    })

    if (res.data.code === 0 && res.data.data) {
      apps.value = res.data.data.records || []
      pagination.total = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '加载应用失败')
    }
  } catch (error) {
    console.error('加载应用失败：', error)
    message.error('加载应用失败')
  } finally {
    loading.value = false
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
      await loadApps()
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
  loadApps()
})
</script>

<style scoped>
#myTeamPage {
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

/* 页面标题 */
.page-header {
  text-align: center;
  padding: 50px 0 50px;
  margin-bottom: 40px;
}

.page-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 20px;
  line-height: 1.1;
  color: #1F2937;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  letter-spacing: -0.025em;
}

.page-icon {
  font-size: 38px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.page-subtitle {
  font-size: 18px;
  margin: 0;
  color: #6B7280;
  font-weight: 500;
  letter-spacing: 0.025em;
}

/* 搜索区域 */
.search-section {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.95) 100%);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-form {
  width: 100%;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0;
  letter-spacing: 0.025em;
}

/* 区域 */
.section {
  margin-bottom: 60px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: #1F2937;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  letter-spacing: -0.025em;
}

.app-count {
  font-size: 18px;
  color: #6B7280;
  font-weight: 500;
  background: rgba(59, 130, 246, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

/* 应用网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 28px;
  margin-bottom: 40px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 252, 0.8) 100%);
  border-radius: 16px;
  border: 2px dashed rgba(59, 130, 246, 0.3);
  min-height: 320px;
  grid-column: 1 / -1;
  backdrop-filter: blur(10px);
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
  opacity: 0.6;
  filter: grayscale(0.3);
}

.empty-text {
  color: #4B5563;
  font-size: 20px;
  margin: 0 0 12px;
  font-weight: 600;
  letter-spacing: 0.025em;
}

.empty-subtext {
  color: #6B7280;
  font-size: 15px;
  margin: 0;
  text-align: center;
  line-height: 1.5;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 24px 20px;
  }
  
  .page-header {
    padding: 30px 0 40px;
  }
  
  .page-title {
    font-size: 32px;
  }
  
  .page-icon {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 16px;
  }
  
  .search-section {
    padding: 20px;
    margin-bottom: 30px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .form-item {
    width: 100%;
  }
  
  .form-item .ant-input,
  .form-item .ant-select {
    width: 100% !important;
  }
  
  .app-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .empty-state {
    padding: 80px 20px;
    min-height: 280px;
  }
  
  .empty-icon {
    font-size: 56px;
  }
  
  .empty-text {
    font-size: 18px;
  }
  
  .empty-subtext {
    font-size: 14px;
  }
}
</style>
