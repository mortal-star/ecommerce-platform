<template>
  <section class="notice-view">
    <div class="notice-container">
      <h2 class="page-title">系统公告</h2>

      <div v-if="loading" class="loading-row">加载中...</div>

      <div v-else-if="notices.length" class="notice-list">
        <div
          v-for="n in notices"
          :key="n.id"
          class="notice-card"
          @click="openDetail(n)"
        >
          <div class="notice-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="notice-body">
            <h3 class="title">
              {{ n.title }}
              <el-tag v-if="n.top" type="warning" size="small">置顶</el-tag>
            </h3>
            <p v-if="n.summary" class="summary">{{ n.summary }}</p>
            <p class="meta">
              <span class="time">{{ formatDate(n.publishTime || n.createTime) }}</span>
              <span class="author">{{ n.author || '系统' }}</span>
            </p>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>

      <Empty v-else size="large" description="暂无公告" />
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentNotice?.title" width="640">
      <template v-if="currentNotice">
        <p class="detail-meta">
          <span>{{ formatDate(currentNotice.publishTime || currentNotice.createTime) }}</span>
          <span v-if="currentNotice.author">{{ currentNotice.author }}</span>
        </p>
        <el-divider />
        <div v-if="currentNotice.content" class="detail-content" v-html="currentNotice.content"></div>
        <p v-else class="detail-empty">公告内容为空</p>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Bell, ArrowRight } from '@element-plus/icons-vue'
import { listNotices } from '@/api'
import Empty from '@/components/Empty.vue'

const notices = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentNotice = ref(null)

function formatDate(value) {
  if (!value) return ''
  try {
    const d = new Date(value)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch (e) {
    return String(value)
  }
}

async function loadNotices() {
  loading.value = true
  try {
    const data = await listNotices({ status: 1 })
    const list = Array.isArray(data) ? data : (data?.records || [])
    notices.value = list.sort((a, b) => (b.top ? 1 : 0) - (a.top ? 1 : 0))
  } catch (e) {
    notices.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(n) {
  currentNotice.value = n
  detailVisible.value = true
}

onMounted(loadNotices)
</script>

<style scoped lang="scss">
.notice-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.notice-container {
  width: min(900px, calc(100% - 32px));
  margin: 0 auto;
}
.page-title {
  margin: 0 0 16px;
  font-size: 22px;
}

.loading-row {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  background: #fff;
  border-radius: 12px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #eff6ff;
    transform: translateY(-1px);
  }

  .notice-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #dbeafe;
    color: #2563eb;
    display: grid;
    place-items: center;
    font-size: 20px;
    flex-shrink: 0;
  }

  .notice-body {
    flex: 1;
    min-width: 0;

    .title {
      margin: 0 0 4px;
      font-size: 15px;
      display: flex;
      align-items: center;
      gap: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .summary {
      margin: 0 0 6px;
      font-size: 13px;
      color: #64748b;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .meta {
      margin: 0;
      display: flex;
      gap: 12px;
      font-size: 12px;
      color: #94a3b8;
    }
  }

  .arrow {
    color: #cbd5e1;
    font-size: 16px;
  }
}

.detail-meta {
  display: flex;
  gap: 16px;
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.detail-content {
  line-height: 1.7;
  font-size: 14px;
  color: #334155;

  :deep(img) { max-width: 100%; }
}

.detail-empty {
  color: #94a3b8;
  text-align: center;
}
</style>
