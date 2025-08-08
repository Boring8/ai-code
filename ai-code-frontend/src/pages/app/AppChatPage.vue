<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
        <!-- 显示当前在线用户 -->
        <div v-if="onlineUsers.length > 0" class="online-users">
          <span class="online-label">在线用户：</span>
          <a-avatar 
            v-for="user in onlineUsers" 
            :key="user.id" 
            :src="user.userAvatar" 
            :title="user.userName"
            size="small"
          />
        </div>
      </div>
      <div class="header-right">
        <a-button type="default" @click="showAppDetail">
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button type="primary" @click="deployApp" :loading="deploying">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署按钮
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button 
              type="link" 
              @click="loadMoreHistory" 
              :loading="loadingHistory"
              size="small"
            >
              <template #icon>
                <ReloadOutlined />
              </template>
              加载更多历史消息
            </a-button>
          </div>

          <div v-for="(message, index) in messages" :key="message.id || index" class="message-item">
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">
                {{ message.content }}
                <div v-if="message.userName" class="message-user">
                  {{ message.userName }}
                </div>
              </div>
              <div class="message-avatar">
                <a-avatar :src="message.userAvatar || loginUserStore.loginUser.userAvatar" />
              </div>
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <a-avatar :src="aiAvatar" />
              </div>
              <div class="message-content">
                <MarkdownRenderer v-if="message.content" :content="message.content" />
                <div v-if="message.loading" class="loading-indicator">
                  <a-spin size="small" />
                  <span>AI 正在思考...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-container">
          <!-- 权限提示 -->
          <div v-if="!hasChatPermission" class="permission-alert">
            <a-alert 
              message="您没有对话权限" 
              description="请联系应用创建者邀请您加入团队，或者您需要成为应用的所有者才能进行对话。"
              type="warning" 
              show-icon 
              banner
            />
          </div>
          <div class="input-wrapper">
            <a-tooltip v-if="!hasChatPermission" title="您没有对话权限，请联系应用创建者邀请您加入团队" placement="top">
              <a-textarea
                v-model:value="userInput"
                placeholder="请描述你想生成的网站，越详细效果越好哦"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating || !hasChatPermission"
              />
            </a-tooltip>
            <a-tooltip v-else-if="!canEdit" title="其他用户正在对话中，请稍候..." placement="top">
              <a-textarea
                v-model:value="userInput"
                placeholder="请描述你想生成的网站，越详细效果越好哦"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating || !canEdit"
              />
            </a-tooltip>
            <a-textarea
              v-else
              v-model:value="userInput"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              :rows="4"
              :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating"
            />
            <div class="input-actions">
              <a-button
                type="primary"
                @click="sendMessage"
                :loading="isGenerating"
                :disabled="!hasChatPermission || !canEdit"
              >
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </div>
          <!-- 显示当前编辑状态 -->
          <div v-if="currentEditingUser && currentEditingUser.id !== loginUserStore.loginUser.id" class="editing-status">
            <a-alert 
              :message="`${currentEditingUser.userName} 正在对话中...`" 
              type="info" 
              show-icon 
              banner
            />
          </div>
        </div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-section">
        <div class="preview-header">
          <h3>生成后的网页展示</h3>
          <div class="preview-actions">
            <a-button v-if="previewUrl" type="link" @click="openInNewTab">
              <template #icon>
                <ExportOutlined />
              </template>
              新窗口打开
            </a-button>
          </div>
        </div>
        <div class="preview-content">
          <div v-if="!previewUrl && !isGenerating" class="preview-placeholder">
            <div class="placeholder-icon">🌐</div>
            <p>网站文件生成完成后将在这里展示</p>
          </div>
          <div v-else-if="isGenerating" class="preview-loading">
            <a-spin size="large" />
            <p>正在生成网站...</p>
          </div>
          <iframe
            v-else
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
            @load="onIframeLoad"
          ></iframe>
        </div>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="appInfo"
      :show-actions="isOwner || isAdmin"
      @edit="editApp"
      @delete="deleteApp"
    />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      @open-site="openDeployedSite"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  getAppById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
  checkUserInApp,
} from '@/api/appController'
import { listAppChatHistory } from '../../api/chatHistoryController'
import { CodeGenTypeEnum } from '@/utils/codeGenTypes'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import aiAvatar from '@/assets/logo.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'
import { toAppIdNumber, toAppIdString, getAppIdForApi } from '@/utils/appIdUtils'

import {
  CloudUploadOutlined,
  SendOutlined,
  ExportOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<string>()

// WebSocket相关
let websocket: WebSocket | null = null
const onlineUsers = ref<API.UserVO[]>([])
const currentEditingUser = ref<API.UserVO | null>(null)
const canEdit = computed(() => !currentEditingUser.value || currentEditingUser.value.id === loginUserStore.loginUser.id)

// 对话相关
interface Message {
  id?: number
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
  userName?: string
  userAvatar?: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 对话历史相关
const loadingHistory = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>('')

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 权限相关
const isOwner = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 用户是否有对话权限
const hasChatPermission = ref(false)

// 应用详情相关
const appDetailVisible = ref(false)

// WebSocket连接
const connectWebSocket = () => {
  if (!appId.value) return
console.log(appId.value);

      try {
      // 构建WebSocket URL
      const wsUrl = API_BASE_URL.replace('http', 'ws') + `/ws/app?appId=${appId.value}`
      console.log('正在连接WebSocket:', wsUrl)
      websocket = new WebSocket(wsUrl)

    websocket.onopen = () => {
      console.log('WebSocket连接已建立')
      // 发送进入编辑状态的消息
      sendWebSocketMessage({
        type: 'USER_ENTER_EDIT',
        editAction: 'enter'
      })
    }

    websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    websocket.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
      message.error('WebSocket连接失败，团队协作功能不可用')
    }

    websocket.onclose = () => {
      console.log('WebSocket连接已关闭')
    }
  } catch (error) {
    console.error('建立WebSocket连接失败:', error)
  }
}

// 发送WebSocket消息
const sendWebSocketMessage = (message: any) => {
  if (websocket && websocket.readyState === WebSocket.OPEN) {
    websocket.send(JSON.stringify(message))
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (data: any) => {
  switch (data.type) {
    case 'INFO':
      // 处理用户加入/离开通知
      if (data.message.includes('加入编辑')) {
        // 用户加入，添加到在线用户列表
        if (data.user && !onlineUsers.value.find(u => u.id === data.user.id)) {
          onlineUsers.value.push(data.user)
        }
      } else if (data.message.includes('离开编辑')) {
        // 用户离开，从在线用户列表移除
        if (data.user) {
          onlineUsers.value = onlineUsers.value.filter(u => u.id !== data.user.id)
        }
      }
      break
    case 'USER_ENTER_EDIT':
      // 用户开始对话
      if (data.user) {
        currentEditingUser.value = data.user
        if (data.user.id !== loginUserStore.loginUser.id) {
          message.info(`${data.user.userName} 开始对话`)
        }
      }
      break
    case 'USER_EXIT_EDIT':
      // 用户结束对话
      if (data.user && data.user.id === currentEditingUser.value?.id) {
        currentEditingUser.value = null
        if (data.user.id !== loginUserStore.loginUser.id) {
          message.info(`${data.user.userName} 结束对话`)
        }
      }
      break
    case 'AI_EDIT_ACTION':
      // AI对话内容
      if (data.user && data.user.id !== loginUserStore.loginUser.id) {
        // 其他用户的AI对话，添加到消息列表
        const aiMessageIndex = messages.value.length
        messages.value.push({
          type: 'ai',
          content: '',
          loading: true,
          userName: data.user.userName,
          userAvatar: data.user.userAvatar
        })
        
        // 模拟AI回复（实际应该从后端获取）
        setTimeout(() => {
          if (messages.value[aiMessageIndex]) {
            messages.value[aiMessageIndex].content = '这是其他用户的AI对话内容...'
            messages.value[aiMessageIndex].loading = false
          }
        }, 2000)
      }
      break
  }
}

// 关闭WebSocket连接
const closeWebSocket = () => {
  if (websocket) {
    // 发送退出编辑状态的消息
    sendWebSocketMessage({
      type: 'USER_EXIT_EDIT',
      editAction: 'exit'
    })
    
    setTimeout(() => {
      if (websocket) {
        websocket.close()
        websocket = null
      }
    }, 100)
  }
}

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 检查用户对话权限
const checkChatPermission = async () => {
  if (!appId.value) return false

  try {
    const res = await checkUserInApp({
      appId: getAppIdForApi(appId.value) as number,
      userId: loginUserStore.loginUser.id
    })
    
    if (res.data.code === 0) {
      return res.data.data || false
    } else {
      console.error('检查用户权限失败：', res.data.message)
      return false
    }
  } catch (error) {
    console.error('检查用户权限失败：', error)
    return false
  }
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    // 使用智能AppId处理，大数值保持字符串格式，小数值转换为number
    const res = await getAppById({ id: getAppIdForApi(id) as string })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data

      // 检查用户对话权限
      hasChatPermission.value = await checkChatPermission()

      // 加载对话历史
      await loadChatHistory()

      // 检查是否需要自动发送初始提示词
      if (appInfo.value.initPrompt && isOwner.value && messages.value.length === 0) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }

      // 如果有至少2条对话记录，展示网站
      if (messages.value.length >= 2) {
        updatePreview()
      }

      // 建立WebSocket连接
      connectWebSocket()
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 加载对话历史
const loadChatHistory = async (loadMore = false) => {
  if (!appId.value) return

  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10,
    }

    // 如果是加载更多，添加游标参数
    if (loadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }

    const res = await listAppChatHistory(params)
    
    if (res.data.code === 0 && res.data.data) {
      const historyData = res.data.data
      const historyMessages = historyData.records || []
      
      // 转换历史消息格式
      const formattedMessages: Message[] = historyMessages.map(msg => ({
        id: msg.id,
        type: msg.messageType === 'user' ? 'user' : 'ai',
        content: msg.message || '',
        createTime: msg.createTime,
      }))

      if (loadMore) {
        // 加载更多时，将新消息插入到现有消息前面
        // 后端返回的是按时间倒序排列（最新的在前），所以需要反转顺序
        // 这样插入到前面的就是更老的消息
        messages.value.unshift(...formattedMessages.reverse())
      } else {
        // 首次加载时，直接替换消息列表
        // 后端返回的是按时间倒序排列，需要反转确保老消息在上方
        messages.value = formattedMessages.reverse()
      }

      // 更新分页信息
      hasMoreHistory.value = historyData.totalRow ? messages.value.length < historyData.totalRow : false
      
      // 更新游标
      if (historyMessages.length > 0) {
        // 由于我们反转了消息顺序，所以最老的消息现在是数组的第一个元素
        const oldestMessage = historyMessages[0]
        lastCreateTime.value = oldestMessage.createTime || ''
      }

      await nextTick()
      if (!loadMore) {
        scrollToBottom()
      }
    } else {
      message.error('加载对话历史失败：' + res.data.message)
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
    userName: loginUserStore.loginUser.userName,
    userAvatar: loginUserStore.loginUser.userAvatar
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value || !canEdit.value || !hasChatPermission.value) {
    return
  }

  const message = userInput.value.trim()
  userInput.value = ''

  // 发送WebSocket消息通知其他用户
  sendWebSocketMessage({
    type: 'USER_EDIT_ACTION',
    editAction: message
  })

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: message,
    userName: loginUserStore.loginUser.userName,
    userAvatar: loginUserStore.loginUser.userAvatar
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })

    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 更新预览
const updatePreview = () => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
    const newPreviewUrl = getStaticPreviewUrl(codeGenType, appId.value)
    previewUrl.value = newPreviewUrl
    previewReady.value = true
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    // 使用智能AppId处理，大数值保持字符串格式，小数值转换为number
    const res = await deployAppApi({
      appId: getAppIdForApi(appId.value) as string,
    })

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      message.success('部署成功')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败：', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})

// 清理资源
onUnmounted(() => {
  // 关闭WebSocket连接
  closeWebSocket()
  
  // EventSource 会在组件卸载时自动清理
})
</script>

<style scoped>
#appChatPage {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #fdfdfd;
}

/* 顶部栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.online-users {
  display: flex;
  align-items: center;
  gap: 8px;
}

.online-label {
  font-size: 12px;
  color: #666;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 8px;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.message-item {
  margin-bottom: 12px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  word-wrap: break-word;
  position: relative;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #1a1a1a;
  padding: 8px 12px;
}

.message-user {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 4px;
}

.message-avatar {
  flex-shrink: 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 输入区域 */
.input-container {
  padding: 16px;
  background: white;
}

.permission-alert {
  margin-bottom: 12px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper .ant-input {
  padding-right: 50px;
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.editing-status {
  margin-top: 8px;
}

/* 右侧预览区域 */
.preview-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.preview-loading p {
  margin-top: 16px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }

  .chat-section,
  .preview-section {
    flex: none;
    height: 50vh;
  }
}

@media (max-width: 768px) {
  .header-bar {
    padding: 12px 16px;
  }

  .app-name {
    font-size: 16px;
  }

  .main-content {
    padding: 8px;
    gap: 8px;
  }

  .message-content {
    max-width: 85%;
  }
}
</style>
