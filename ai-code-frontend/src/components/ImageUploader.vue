<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message, Upload, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons-vue'
import { imageUploadService } from '@/utils/uploadService'

interface Props {
  value?: string | string[]
  multiple?: boolean
  maxCount?: number
  accept?: string
  disabled?: boolean
  showPreview?: boolean
  showDelete?: boolean
  uploadText?: string
  size?: 'small' | 'middle' | 'large'
}

interface Emits {
  (e: 'update:value', value: string | string[]): void
  (e: 'change', value: string | string[]): void
  (e: 'upload-success', urls: string[]): void
  (e: 'upload-error', error: any): void
  (e: 'delete', url: string): void
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  multiple: false,
  maxCount: 9,
  accept: 'image/*',
  disabled: false,
  showPreview: true,
  showDelete: true,
  uploadText: '上传图片',
  size: 'middle'
})

const emit = defineEmits<Emits>()

// 响应式数据
const fileList = ref<any[]>([])
const uploading = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')
const previewTitle = ref('')

// 计算属性
const currentValue = computed(() => {
  if (props.multiple) {
    return Array.isArray(props.value) ? props.value : []
  } else {
    return typeof props.value === 'string' ? props.value : ''
  }
})

// 监听value变化，同步到fileList
watch(currentValue, (newValue) => {
  if (props.multiple) {
    const urls = Array.isArray(newValue) ? newValue : []
    fileList.value = urls.map((url, index) => ({
      uid: `-${index}`,
      name: `image-${index}`,
      status: 'done',
      url: url,
      thumbUrl: url
    }))
  } else {
    const url = typeof newValue === 'string' ? newValue : ''
    if (url) {
      fileList.value = [{
        uid: '-1',
        name: 'image',
        status: 'done',
        url: url,
        thumbUrl: url
      }]
    } else {
      fileList.value = []
    }
  }
}, { immediate: true })

// 上传前处理
const beforeUpload = (file: File) => {
  return imageUploadService.isImageFile(file) && file.size / 1024 / 1024 < 5
}

// 自定义上传
const customUpload = async (options: any) => {
  const { file, onSuccess, onError, onProgress } = options
  
  try {
    uploading.value = true
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      onProgress({ percent: Math.random() * 100 })
    }, 100)
    
    // 使用上传服务
    const result = await imageUploadService.uploadSingleFile(file)
    
    clearInterval(progressInterval)
    
    if (result.success && result.data) {
      const imageUrl = result.data as string
      onSuccess({ url: imageUrl })
      
      // 更新文件列表
      const newFile = {
        uid: file.uid,
        name: file.name,
        status: 'done',
        url: imageUrl,
        thumbUrl: imageUrl
      }
      
      if (props.multiple) {
        fileList.value.push(newFile)
        const urls = fileList.value.map(f => f.url)
        emit('update:value', urls)
        emit('change', urls)
        emit('upload-success', urls)
      } else {
        fileList.value = [newFile]
        emit('update:value', imageUrl)
        emit('change', imageUrl)
        emit('upload-success', [imageUrl])
      }
    } else {
      onError(new Error(result.error || '上传失败'))
      emit('upload-error', result.error)
    }
  } catch (error) {
    onError(error)
    emit('upload-error', error)
    message.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 删除图片
const handleRemove = async (file: any) => {
  try {
    if (file.url) {
      const success = await imageUploadService.deleteFile(file.url)
      if (success) {
        message.success('删除成功')
      } else {
        message.error('删除失败')
        return
      }
    }
    
    // 从文件列表中移除
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
      
      if (props.multiple) {
        const urls = fileList.value.map(f => f.url)
        emit('update:value', urls)
        emit('change', urls)
      } else {
        emit('update:value', '')
        emit('change', '')
      }
      
      emit('delete', file.url)
    }
  } catch (error) {
    console.error('删除图片失败:', error)
    message.error('删除失败，请重试')
  }
}

// 预览图片
const handlePreview = (file: any) => {
  previewImage.value = file.url || file.thumbUrl
  previewVisible.value = true
  previewTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1)
}

// 关闭预览
const handlePreviewCancel = () => {
  previewVisible.value = false
  previewTitle.value = ''
}

// 上传状态变化
const handleChange = (info: any) => {
  if (info.file.status === 'uploading') {
    uploading.value = true
  } else if (info.file.status === 'done') {
    uploading.value = false
  } else if (info.file.status === 'error') {
    uploading.value = false
    message.error('上传失败')
  }
}

// 计算上传按钮样式
const uploadButtonStyle = computed(() => {
  const sizeMap = {
    small: { width: '80px', height: '80px' },
    middle: { width: '100px', height: '100px' },
    large: { width: '120px', height: '120px' }
  }
  return sizeMap[props.size]
})
</script>

<template>
  <div class="image-uploader">
    <Upload
      :file-list="fileList"
      :before-upload="beforeUpload"
      :custom-request="customUpload"
      :multiple="multiple"
      :accept="accept"
      :disabled="disabled || uploading"
      :show-upload-list="false"
      @change="handleChange"
      class="upload-component"
    >
      <div 
        v-if="fileList.length < maxCount" 
        class="upload-button"
        :style="uploadButtonStyle"
      >
        <div v-if="uploading" class="uploading">
          <a-spin />
          <div class="upload-text">上传中...</div>
        </div>
        <div v-else class="upload-content">
          <PlusOutlined />
          <div class="upload-text">{{ uploadText }}</div>
        </div>
      </div>
    </Upload>
    
    <!-- 图片列表 -->
    <div v-if="fileList.length > 0" class="image-list">
      <div 
        v-for="file in fileList" 
        :key="file.uid"
        class="image-item"
        :style="uploadButtonStyle"
      >
        <img 
          :src="file.url || file.thumbUrl" 
          :alt="file.name"
          class="image-preview"
        />
        
        <!-- 操作按钮 -->
        <div class="image-actions">
          <a-button
            v-if="showPreview"
            type="text"
            size="small"
            class="action-btn preview-btn"
            @click="handlePreview(file)"
          >
            <EyeOutlined />
          </a-button>
          
          <a-button
            v-if="showDelete"
            type="text"
            size="small"
            class="action-btn delete-btn"
            @click="handleRemove(file)"
          >
            <DeleteOutlined />
          </a-button>
        </div>
      </div>
    </div>
    
    <!-- 预览模态框 -->
    <Modal
      :open="previewVisible"
      :title="previewTitle"
      :footer="null"
      @cancel="handlePreviewCancel"
    >
      <img :src="previewImage" style="width: 100%" />
    </Modal>
  </div>
</template>

<style scoped>
.image-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-component {
  display: inline-block;
}

.upload-button {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-button:hover {
  border-color: #1890ff;
  background: #f0f8ff;
}

.upload-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.uploading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-text {
  font-size: 12px;
  color: #666;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #d9d9d9;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-actions {
  opacity: 1;
}

.action-btn {
  color: white !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important;
  border-radius: 4px !important;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
}

.preview-btn:hover {
  color: #1890ff !important;
}

.delete-btn:hover {
  color: #ff4d4f !important;
}
</style>
