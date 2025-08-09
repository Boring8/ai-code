<template>
  <div class="team-management">
    <!-- 团队管理区域 -->
    <a-card 
      v-if="appInfo.isTeam" 
      class="team-management-card" 
      title="团队管理"
      :loading="teamLoading"
    >
      <template #extra>
        <a-button 
          v-if="isCreator" 
          type="primary" 
          @click="showInviteModal = true"
          :loading="inviteLoading"
        >
          <template #icon>
            <UserAddOutlined />
          </template>
          邀请成员
        </a-button>
      </template>

      <!-- 成员列表 -->
      <div class="team-members">
        <a-list
          :data-source="teamMembers"
          :loading="teamLoading"
          :pagination="pagination"
        >
          <template #renderItem="{ item }">
            <a-list-item class="member-item">
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar :src="item.userAvatar" :size="48">
                    {{ item.userName?.charAt(0) }}
                  </a-avatar>
                </template>
                <template #title>
                  <div class="member-info">
                    <span class="member-name">{{ item.userName }}</span>
                    <a-tag v-if="item.isCreate" color="red">创建者</a-tag>
                    <a-tag v-else color="blue">成员</a-tag>
                  </div>
                </template>
                <template #description>
                  <div class="member-details">
                    <p class="member-account">账号：{{ item.userAccount }}</p>
                    <p class="member-join-time">加入时间：{{ formatTime(item.joinTime) }}</p>
                    <p v-if="item.userProfile" class="member-profile">{{ item.userProfile }}</p>
                  </div>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button 
                  v-if="isCreator && !item.isCreate" 
                  type="link" 
                  danger
                  @click="handleRemoveMember(item)"
                  :loading="removeLoading === item.userId"
                >
                  移除
                </a-button>
                
              </template>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </a-card>

    <!-- 非团队应用提示 -->
    <a-card v-else class="team-info-card" title="团队信息">
      <a-empty description="这是一个个人应用，不包含团队管理功能" />
    </a-card>

    <!-- 邀请成员模态框 -->
         <a-modal
       v-model:open="showInviteModal"
       title="邀请成员"
       @ok="handleInviteMember"
       @cancel="showInviteModal = false"
       :confirm-loading="inviteLoading"
       :width="600"
       :ok-text="inviteForm.userId ? '发送邀请' : '请先选择用户'"
       :ok-button-props="{ disabled: !inviteForm.userId }"
     >
             <div class="invite-form">
         <a-form :model="inviteForm" layout="vertical">
           <!-- 当前选择提示 -->
           <a-form-item v-if="inviteForm.userId" label="当前选择">
             <a-alert 
               :message="`已选择用户: ${searchResults.find(u => u.id === inviteForm.userId)?.userName || '未知用户'}`"
               type="success" 
               show-icon
             />
           </a-form-item>
           
           <a-form-item label="搜索用户" required>
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="输入用户ID或用户名搜索"
              @search="searchUsers"
              :loading="searchLoading"
              enter-button
            />
          </a-form-item>
          
          <!-- 搜索结果 -->
          <a-form-item v-if="searchResults.length > 0" label="搜索结果">
            <a-list
              :data-source="searchResults"
              size="small"
              class="search-results"
            >
              <template #renderItem="{ item }">
                <a-list-item class="search-result-item">
                  <a-list-item-meta>
                    <template #avatar>
                      <a-avatar :src="item.userAvatar" :size="32">
                        {{ item.userName?.charAt(0) }}
                      </a-avatar>
                    </template>
                    <template #title>
                      <span>{{ item.userName }}</span>
                    </template>
                    <template #description>
                      <span>{{ item.userAccount }}</span>
                    </template>
                  </a-list-item-meta>
                  <template #actions>
                                         <a-button 
                       type="primary" 
                       size="small"
                       @click="selectUser(item)"
                       :disabled="isUserInTeam(item.id)"
                     >
                       {{ isUserInTeam(item.id) ? '已在团队中' : (inviteForm.userId === item.id ? '已选择' : '选择') }}
                     </a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 移除成员确认框 -->
    <a-modal
      v-model:open="showRemoveModal"
      title="确认移除成员"
      @ok="confirmRemoveMember"
      @cancel="showRemoveModal = false"
      :confirm-loading="removeLoading"
    >
      <p>确定要移除成员 <strong>{{ selectedMember?.userName }}</strong> 吗？</p>
      <p class="text-muted">此操作不可撤销。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { UserAddOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { 
  getAppTeamMembers, 
  inviteUserToApp, 
  removeUserFromApp 
} from '../api/appUserController'
import { listUserVoByPage } from '../api/userController'
import { useLoginUserStore } from '../stores/loginUser'

// 定义 props
interface Props {
  appInfo: API.AppVO
  appId: number
}

const props = defineProps<Props>()

// 用户状态
const loginUserStore = useLoginUserStore()

// 响应式数据
const teamLoading = ref(false)
const inviteLoading = ref(false)
const removeLoading = ref<number | undefined>(undefined)
const searchLoading = ref(false)

// 团队成员
const teamMembers = ref<API.AppTeamMemberVO[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 个成员`,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page
    pagination.pageSize = pageSize
    fetchTeamMembers()
  }
})

// 邀请相关
const showInviteModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref<API.UserVO[]>([])
const inviteForm = reactive({
  userId: null as number | null
})

// 移除相关
const showRemoveModal = ref(false)
const selectedMember = ref<API.AppTeamMemberVO | null>(null)

// 计算属性
const isCreator = computed(() => {
  return props.appInfo.userId === loginUserStore.loginUser.id
})

// 获取团队成员
const fetchTeamMembers = async () => {
  if (!props.appId) return
  
  teamLoading.value = true
  try {
    const res = await getAppTeamMembers({ appId: props.appId })
    if (res.data.code === 0 && res.data.data) {
      teamMembers.value = res.data.data
      pagination.total = res.data.data.length
    } else {
      message.error(res.data.message || '获取团队成员失败')
    }
  } catch (error) {
    console.error('获取团队成员失败：', error)
    message.error('获取团队成员失败')
  } finally {
    teamLoading.value = false
  }
}

// 搜索用户
const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    message.warning('请输入搜索关键词')
    return
  }
  
  searchLoading.value = true
  try {
    const res = await listUserVoByPage({
      pageNum: 1,
      pageSize: 20,
      userName: searchKeyword.value,
      userAccount: searchKeyword.value
    })
    
    if (res.data.code === 0 && res.data.data) {
      searchResults.value = res.data.data.records || []
      if (searchResults.value.length === 0) {
        message.info('未找到相关用户')
      }
    } else {
      message.error(res.data.message || '搜索用户失败')
    }
  } catch (error) {
    console.error('搜索用户失败：', error)
    message.error('搜索用户失败')
  } finally {
    searchLoading.value = false
  }
}

// 选择用户
const selectUser = (user: API.UserVO) => {
  inviteForm.userId = user.id || null
  message.success(`已选择用户: ${user.userName}`)
}

// 检查用户是否已在团队中
const isUserInTeam = (userId: number | undefined) => {
  return teamMembers.value.some(member => member.userId === userId)
}

// 邀请成员
const handleInviteMember = async () => {
  console.log('开始邀请成员，当前表单数据:', inviteForm)
  if (!inviteForm.userId) {
    message.warning('请先选择要邀请的用户')
    return
  }
  
  inviteLoading.value = true
  try {
    const res = await inviteUserToApp({
      appId: props.appId,
      userId: inviteForm.userId
    })
    
    if (res.data.code === 0) {
      message.success('邀请发送成功')
      showInviteModal.value = false
      // 重置表单
      searchKeyword.value = ''
      searchResults.value = []
      inviteForm.userId = null
      // 刷新成员列表
      await fetchTeamMembers()
    } else {
      message.error(res.data.message || '邀请失败')
    }
  } catch (error) {
    console.error('邀请失败：', error)
    message.error('邀请失败')
  } finally {
    inviteLoading.value = false
  }
}

// 移除成员
const handleRemoveMember = (member: API.AppTeamMemberVO) => {
  selectedMember.value = member
  showRemoveModal.value = true
}

// 确认移除成员
const confirmRemoveMember = async () => {
  if (!selectedMember.value) return
  
  const userId = selectedMember.value.userId
  removeLoading.value = userId || undefined
  try {
    const res = await removeUserFromApp({
      appId: props.appId,
      userId: userId || 0
    })
    if (res.data.code === 0) {
      message.success('移除成员成功')
      showRemoveModal.value = false
      selectedMember.value = null
      // 刷新成员列表
      await fetchTeamMembers()
    } else {
      message.error(res.data.message || '移除成员失败')
    }
  } catch (error) {
    console.error('移除成员失败：', error)
    message.error('移除成员失败，请检查网络连接')
  } finally {
    removeLoading.value = undefined
  }
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 监听应用信息变化
watch(() => props.appInfo.isTeam, (newVal) => {
  if (newVal) {
    fetchTeamMembers()
  }
}, { immediate: true })

// 组件挂载时获取数据
onMounted(() => {
  if (props.appInfo.isTeam) {
    fetchTeamMembers()
  }
})
</script>

<style scoped>
.team-management {
  margin-top: 24px;
}

.team-management-card,
.team-info-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.team-members {
  margin-top: 16px;
}

.member-item {
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.member-item:hover {
  border-color: #d9d9d9;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-weight: 500;
  color: #1a1a1a;
}

.member-details {
  margin: 0;
}

.member-account,
.member-join-time,
.member-profile {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.invite-form {
  padding: 16px 0;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.search-result-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.text-muted {
  color: #999;
  font-size: 14px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .member-item {
    padding: 12px;
  }
}
</style>
