<template>
  <section class="notice-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">公告管理</h2>
        <p class="page-subtitle">发布、修改、删除系统公告</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="notices" stripe empty-text="暂无公告" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="摘要" min-width="280">
          <template #default="{ row }">
            <span class="excerpt">{{ excerpt(row.content) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">{{ formatTime(row.publishTime) || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openView(row)">查看</el-button>
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑公告' : '发布公告'"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="必填，2~100 字" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="支持 HTML / 富文本片段；后续可接入 wangEditor 等编辑器"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="0">保存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" :title="active?.title || '公告详情'" width="720px">
      <div v-if="active" class="notice-detail">
        <div class="meta-row">
          <span>状态：</span>
          <el-tag :type="active.status === 1 ? 'success' : 'info'" size="small">
            {{ active.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
          <span class="muted">发布时间：{{ formatTime(active.publishTime) || '—' }}</span>
        </div>
        <div class="content-body" v-html="active.content || '<p>暂无内容</p>'" />
      </div>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  adminListNotices,
  adminCreateNotice,
  adminUpdateNotice,
  adminDeleteNotice
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const notices = ref([])

const formVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  content: '',
  status: 1
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度 2~100 个字符', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }]
}

const viewVisible = ref(false)
const active = ref(null)

function excerpt(content) {
  if (!content) return '—'
  const text = String(content).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  return text.length > 80 ? `${text.slice(0, 80)}…` : text
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await adminListNotices()
    notices.value = Array.isArray(data) ? data : []
  } catch (err) {
    ElMessage.error(err?.message || '加载公告失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  formVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    title: row.title || '',
    content: row.content || '',
    status: row.status ?? 1
  })
  formVisible.value = true
}

function openView(row) {
  active.value = row
  viewVisible.value = true
}

function resetForm() {
  Object.assign(form, { id: null, title: '', content: '', status: 1 })
  formRef.value?.resetFields()
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      status: form.status
    }
    if (form.id) {
      await adminUpdateNotice(form.id, payload)
      ElMessage.success('公告修改成功')
    } else {
      await adminCreateNotice(payload)
      ElMessage.success('公告发布成功')
    }
    formVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  try {
    await adminDeleteNotice(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.notice-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.excerpt {
  color: #4b5563;
  font-size: 13px;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4b5563;
}

.muted {
  color: #9ca3af;
}

.content-body {
  padding: 16px;
  background: #f9fafb;
  border-radius: 6px;
  line-height: 1.7;
  color: #1f2937;
  white-space: pre-wrap;
}

.content-body :deep(p) {
  margin: 0 0 8px;
}

.content-body :deep(img) {
  max-width: 100%;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
