<template>
  <section class="feedback-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户反馈管理</h2>
        <p class="page-subtitle">查看用户反馈、回复并标记处理状态</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="全部" :value="null" />
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="feedbacks" stripe empty-text="暂无反馈" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="contact" label="联系方式" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.contact || '—' }}</template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
              {{ row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button text type="primary" size="small" @click="openReply(row)">
              {{ row.reply ? '修改回复' : '回复' }}
            </el-button>
            <el-button
              v-if="row.status !== 1"
              text
              type="success"
              size="small"
              @click="handleMarkHandled(row)"
            >
              标记已处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @change="loadList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="反馈详情" width="640px">
      <el-descriptions v-if="active" :column="1" border>
        <el-descriptions-item label="反馈 ID">{{ active.id }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ active.userId }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ active.contact || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="active.status === 1 ? 'success' : 'warning'" size="small">
            {{ active.status === 1 ? '已处理' : '未处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(active.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="反馈内容">
          <div class="content-block">{{ active.content || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="管理员回复">
          <div class="content-block">{{ active.reply || '尚未回复' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="active.replyTime" label="回复时间">
          {{ formatTime(active.replyTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog
      v-model="replyVisible"
      :title="active?.reply ? '修改回复' : '回复反馈'"
      width="560px"
      :close-on-click-modal="false"
    >
      <div v-if="active" class="reply-context">
        <div class="muted">用户反馈：</div>
        <div class="content-block">{{ active.content }}</div>
      </div>
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  adminListFeedback,
  adminReplyFeedback,
  adminMarkFeedbackHandled
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const feedbacks = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filter = reactive({ status: null })

const detailVisible = ref(false)
const replyVisible = ref(false)
const active = ref(null)
const replyForm = reactive({ reply: '' })

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filter.status ?? undefined
    }
    const { data } = await adminListFeedback(params)
    feedbacks.value = data?.list || []
    total.value = data?.total || 0
  } catch (err) {
    ElMessage.error(err?.message || '加载反馈失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  filter.status = null
  pageNum.value = 1
  loadList()
}

function openDetail(row) {
  active.value = row
  detailVisible.value = true
}

function openReply(row) {
  active.value = row
  replyForm.reply = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  if (!active.value) return
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await adminReplyFeedback(active.value.id, replyForm.reply.trim())
    ElMessage.success('回复成功')
    replyVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

async function handleMarkHandled(row) {
  try {
    await ElMessageBox.confirm(`确认将反馈「#${row.id}」标记为已处理？`, '操作确认', {
      type: 'info'
    })
  } catch {
    return
  }
  try {
    await adminMarkFeedbackHandled(row.id)
    ElMessage.success('已标记为已处理')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.feedback-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.filter-card :deep(.el-card__body) {
  padding: 16px;
}

.content-block {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #1f2937;
}

.reply-context {
  margin-bottom: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 13px;
}

.muted {
  color: #9ca3af;
  margin-bottom: 4px;
}
</style>
