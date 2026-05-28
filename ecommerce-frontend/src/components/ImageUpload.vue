<template>
  <el-upload
    class="image-upload"
    :action="action"
    :headers="headers"
    :drag="drag"
    :multiple="multiple"
    :limit="limit"
    :file-list="fileList"
    :list-type="listType"
    :auto-upload="autoUpload"
    :before-upload="beforeUpload"
    :on-success="handleSuccess"
    :on-error="handleError"
    :on-remove="handleRemove"
    :on-preview="handlePreview"
  >
    <el-icon class="upload-icon"><Plus /></el-icon>
    <div class="upload-text">点击或拖拽图片上传</div>
    <template #tip>
      <div v-if="tip" class="upload-tip">{{ tip }}</div>
    </template>
  </el-upload>

  <el-dialog v-model="previewVisible" width="720px" append-to-body>
    <img class="preview-image" :src="previewUrl" alt="preview" />
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAuthorizationHeader } from '@/utils/auth'

const props = defineProps({
  modelValue: { type: [String, Array], default: () => [] },
  action: { type: String, default: '/api/files/upload' },
  multiple: { type: Boolean, default: false },
  limit: { type: Number, default: 1 },
  drag: { type: Boolean, default: true },
  autoUpload: { type: Boolean, default: true },
  listType: { type: String, default: 'picture-card' },
  maxSize: { type: Number, default: 5 },
  tip: { type: String, default: '支持 jpg、png、webp，单张不超过 5MB' }
})

const emit = defineEmits(['update:modelValue', 'success', 'remove', 'error'])
const previewVisible = ref(false)
const previewUrl = ref('')

const headers = computed(() => {
  const authorization = getAuthorizationHeader()
  return authorization ? { Authorization: authorization } : {}
})

const fileList = computed(() => {
  const urls = Array.isArray(props.modelValue) ? props.modelValue : props.modelValue ? [props.modelValue] : []
  return urls.map((url, index) => ({ name: `image-${index + 1}`, url }))
})

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLtMax = file.size / 1024 / 1024 < props.maxSize
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLtMax) ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
  return isImage && isLtMax
}

function normalizeValue(urls) {
  emit('update:modelValue', props.multiple ? urls : urls[0] || '')
}

function extractUrl(response) {
  return response?.data?.url || response?.url || response?.data || ''
}

function handleSuccess(response, file, files) {
  const uploadedUrl = extractUrl(response) || file.url
  const urls = files.map(item => item.response ? extractUrl(item.response) : item.url).filter(Boolean)
  if (uploadedUrl && !urls.includes(uploadedUrl)) urls.push(uploadedUrl)
  normalizeValue(urls)
  emit('success', { response, file, files })
}

function handleRemove(file, files) {
  normalizeValue(files.map(item => item.response ? extractUrl(item.response) : item.url).filter(Boolean))
  emit('remove', { file, files })
}

function handlePreview(file) {
  previewUrl.value = file.url
  previewVisible.value = true
}

function handleError(error, file, files) {
  ElMessage.error('图片上传失败')
  emit('error', { error, file, files })
}
</script>

<style scoped lang="scss">
.image-upload {
  :deep(.el-upload-dragger) {
    padding: 18px;
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
