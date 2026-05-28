<template>
  <section class="feedback-view">
    <div class="feedback-container">
      <h2 class="page-title">用户反馈</h2>
      <p class="page-subtitle">您的每一条建议都会让我们做得更好</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="feedback-form"
      >
        <el-form-item label="问题类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio
              v-for="t in types"
              :key="t.value"
              :value="t.value"
              border
            >{{ t.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            :maxlength="500"
            show-word-limit
            placeholder="请详细描述您遇到的问题或建议"
          />
        </el-form-item>

        <el-form-item label="上传截图（选填）">
          <ImageUpload
            v-model="form.imageList"
            multiple
            :limit="4"
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="form.contact"
            placeholder="邮箱或手机号，方便我们回复您"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            提交反馈
          </el-button>
        </div>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'

const formRef = ref(null)
const submitting = ref(false)

const types = [
  { value: 'bug', label: '系统故障' },
  { value: 'feature', label: '功能建议' },
  { value: 'product', label: '商品问题' },
  { value: 'service', label: '服务投诉' },
  { value: 'other', label: '其他' }
]

const form = ref({
  type: 'bug',
  content: '',
  imageList: [],
  contact: ''
})

const rules = {
  type: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  content: [
    { required: true, message: '请填写反馈内容', trigger: 'blur' },
    { min: 5, message: '内容至少 5 个字', trigger: 'blur' }
  ],
  contact: [{ required: true, message: '请填写联系方式', trigger: 'blur' }]
}

function resetForm() {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.value.imageList = []
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }

  submitting.value = true
  try {
    const images = (form.value.imageList || []).filter(Boolean)
    const typeLabel = types.find(t => t.value === form.value.type)?.label || form.value.type
    const fullContent = `【${typeLabel}】${form.value.content}${
      images.length ? `\n附件图片：${images.join(' ')}` : ''
    }`
    await submitFeedback({
      contact: form.value.contact,
      content: fullContent
    })
    ElMessage.success('反馈已提交，感谢您的支持')
    resetForm()
  } catch (e) {} finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.feedback-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.feedback-container {
  width: min(720px, calc(100% - 32px));
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.page-subtitle {
  margin: 4px 0 24px;
  color: #94a3b8;
  font-size: 14px;
}

.feedback-form {
  :deep(.el-radio.is-bordered) {
    margin: 0 8px 8px 0;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
</style>
