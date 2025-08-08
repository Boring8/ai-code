<template>
  <div class="app-detail-page">
    <!-- 加载状态 -->
    <a-spin :spinning="loading" tip="加载中...">
      <div class="app-detail-container">
        <!-- 应用基本信息 -->
        <a-card class="app-info-card" title="应用信息">
          <div class="app-info-content">
            <div class="app-header">
              <div class="app-cover">
                <a-image
                  :src="appInfo.cover || '/src/assets/logo.png'"
                  :width="120"
                  :height="120"
                  :fallback="'/src/assets/logo.png'"
                />
              </div>
              <div class="app-basic-info">
                <h2 class="app-name">{{ appInfo.appName }}</h2>
                                 <div class="app-meta">
                   <a-tag :color="appInfo.isTeam ? 'blue' : 'green'">
                     {{ appInfo.isTeam ? '团队应用' : '个人应用' }}
                   </a-tag>
                   <a-tag color="orange">{{ appInfo.codeGenType }}</a-tag>
                   <span class="create-time">创建时间：{{ formatTime(appInfo.createTime) }}</span>
                   <a-popconfirm
                     title="确定要将此应用转换为团队应用吗？"
                     description="转换后可以邀请其他用户加入团队，但此操作不可撤销。"
                     @confirm="convertToTeamApp"
                     ok-text="确定"
                     cancel-text="取消"
                   >
                     <a-button 
                       v-if="!appInfo.isTeam && isOwner" 
                       type="primary" 
                       size="small"
                       :loading="converting"
                     >
                       变成团队应用
                     </a-button>
                   </a-popconfirm>
                 </div>
                <p class="app-description">{{ appInfo.initPrompt }}</p>
              </div>
            </div>
          </div>
        </a-card>

        <!-- 团队管理组件 -->
        <TeamManagement 
          :app-info="appInfo" 
          :app-id="appId" 
        />
      </div>
    </a-spin>


  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAppById, updateMyApp } from '../../api/appController'
import { useLoginUserStore } from '../../stores/loginUser'
import TeamManagement from '../../components/TeamManagement.vue'

// 路由参数
const route = useRoute()
const appId = computed(() => route.params.id)

// 用户状态
const loginUserStore = useLoginUserStore()

// 响应式数据
const loading = ref(false)
const converting = ref(false)

// 应用信息
const appInfo = ref<API.AppVO>({})

// 计算属性
const isOwner = computed(() => {
  return appInfo.value.userId === loginUserStore.loginUser.id
})



// 获取应用信息
const fetchAppInfo = async () => {
  if (!appId.value) return
  
  loading.value = true
  try {
    const res = await getAppById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
    } else {
      message.error(res.data.message || '获取应用信息失败')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
  } finally {
    loading.value = false
  }
}

// 转换为团队应用
const convertToTeamApp = async () => {
  if (!appInfo.value.id) return
  
  converting.value = true
  try {
    const res = await updateMyApp({
      id: appInfo.value.id,
      isTeam: 1
    })
    
    if (res.data.code === 0) {
      message.success('应用已成功转换为团队应用')
      // 重新获取应用信息
      await fetchAppInfo()
    } else {
      message.error('转换失败：' + res.data.message)
    }
  } catch (error) {
    console.error('转换失败：', error)
    message.error('转换失败，请重试')
  } finally {
    converting.value = false
  }
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 页面加载时获取数据
onMounted(() => {
  fetchAppInfo()
})
</script>

<style scoped>
.app-detail-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.app-detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.app-info-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-info-content {
  padding: 16px 0;
}

.app-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.app-cover {
  flex-shrink: 0;
}

.app-basic-info {
  flex: 1;
}

.app-name {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.create-time {
  color: #666;
  font-size: 14px;
}

.app-description {
  margin: 0;
  color: #666;
  line-height: 1.6;
  max-width: 600px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .app-detail-page {
    padding: 16px;
  }
  
  .app-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .app-cover {
    align-self: center;
  }
  
  .app-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
