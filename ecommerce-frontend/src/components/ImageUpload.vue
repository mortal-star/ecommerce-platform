<template>
  <div class="image-upload">
    <el-upload
      :action="action"
      :headers="headers"
      :data="data"
      :name="name"
      :drag="drag"
      :multiple="multiple"
      :limit="effectiveLimit"
      :file-list="fileList"
      :list-type="listType"
      :auto-upload="autoUpload"
      :accept="accept"
      :disabled="disabled"
      :before-upload="beforeUpload"
      :http-request="httpRequest || undefined"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :on-exceed="handleExceed"
      :on-progress="handleProgress"
    >
      <slot name="trigger">
        <template v-if="drag && listType !== 'picture-card'">
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">将图片拖到此处，或<em>点击上传</em></div>
        </template>
        <template v-else>
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div v-if="listType !== 'picture-card'" class="upload-text">点击上传图片</div>
        </template>
      </slot>
      <template #tip>
        <div v-if="tip" class="upload-tip">{{ tip }}</div>
      </template>
    </el-upload>

    <el-dialog v-model="previewVisible" width="720px" append-to-body :title="previewTitle">
      <img class="preview-image" :src="previewUrl" alt="preview" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import { getAuthorizationHeader } from '@/utils/auth'

const props = defineProps({
  modelValue: { type: [String, Array], default: () => [] },
  action: { type: String, default: '/api/files/upload' },
  name: { type: String, default: 'file' },
  data: { type: Object, default: () => ({}) },
  multiple: { type: Boolean, default: false },
  limit: { type: Number, default: 0 },
  drag: { type: Boolean, default: true },
  autoUpload: { type: Boolean, default: true },
  disabled: { type: Boolean, default: false },
  listType: {
    type: String,
    default: 'picture-card',
    validator: v => ['text', 'picture', 'picture-card'].includes(v)
  },
  maxSize: { type: Number, default: 5 },
  accept: { type: String, default: 'image/jpeg,image/jpg,image/png,image/webp,image/gif' },
  tip: { type: String, default: '支持 jpg、png、webp，单张不超过 5MB' },
  responseUrlKey: { type: String, default: '' },
  httpRequest: { type: Function, default: null }
})

const emit = defineEmits([
  'update:modelValue',
  'success',
  'remove',
  'error',
  'exceed',
  'progress'
])

const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('图片预览')

const headers = computed(() => {
  const authorization = getAuthorizationHeader()
  return authorization ? { Authorization: authorization } : {}
})

const effectiveLimit = computed(() => {
  if (props.limit > 0) return props.limit
  return props.multiple ? 9 : 1
})

const fileList = computed(() => {
  const urls = Array.isArray(props.modelValue)
    ? props.modelValue
    : props.modelValue
      ? [props.modelValue]
      : []
  return urls.map((url, index) => ({ name: `image-${index + 1}`, url, status: 'success' }))
})

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLtMax = file.size / 1024 / 1024 < props.maxSize
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLtMax) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

function extractUrl(file) {
  if (!file) return ''
  if (file.url) return file.url
  if (!file.response) return ''
  const response = file.response
  if (props.responseUrlKey) {
    return getByPath(response, props.responseUrlKey) || ''
  }
  if (typeof response === 'string') return response
  return (
    response?.data?.url ||
    response?.url ||
    (typeof response?.data === 'string' ? response.data : '') ||
    ''
  )
}

function getByPath(obj, path) {
  return path.split('.').reduce((acc, key) => (acc == null ? acc : acc[key]), obj)
}

function syncValue(files) {
  const urls = files.map(extractUrl).filter(Boolean)
  emit('update:modelValue', props.multiple ? urls : urls[0] || '')
}

function handleSuccess(response, file, files) {
  syncValue(files)
  emit('success', { response, file, files })
}

function handleRemove(file, files) {
  syncValue(files)
  emit('remove', { file, files })
}

function handlePreview(file) {
  previewUrl.value = file.url || extractUrl(file)
  previewTitle.value = file.name || '图片预览'
  previewVisible.value = true
}

function handleError(error, file, files) {
  ElMessage.error('图片上传失败')
  emit('error', { error, file, files })
}

function handleExceed(files) {
  ElMessage.warning(`最多上传 ${effectiveLimit.value} 张图片`)
  emit('exceed', files)
}

function handleProgress(event, file, files) {
  emit('progress', { event, file, files })
}
</script>

<style scoped lang="scss">
.image-upload {
  :deep(.el-upload-dragger) {
    padding: 24px;
  }
}

.upload-icon {
  color: #2563eb;
  font-size: 28px;
}

.upload-text {
  margin-top: 8px;
  color: #64748b;
  font-size: 13px;

  em {
    color: #2563eb;
    font-style: normal;
  }
}

.upload-tip {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.preview-image {
  display: block;
  max-width: 100%;
  margin: 0 auto;
}
</style>
