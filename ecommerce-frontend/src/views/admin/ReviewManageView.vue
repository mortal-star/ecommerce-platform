<template>
  <section class="review-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品评价管理</h2>
        <p class="page-subtitle">查看所有用户评价，删除违规内容</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="评价内容"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="商品ID">
          <el-input
            v-model="filter.productId"
            placeholder="精确匹配"
            clearable
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="filter.rating" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option v-for="n in 5" :key="n" :label="`${n} 星`" :value="n" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="reviews" stripe empty-text="暂无评价数据" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="productId" label="商品ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column label="评分" width="170" align="center">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <span v-if="parseImages(row.images).length === 0" class="muted">—</span>
            <el-image
              v-else
              :src="parseImages(row.images)[0]"
              :preview-src-list="parseImages(row.images)"
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="评价详情" width="640px">
      <el-descriptions v-if="active" :column="2" border>
        <el-descriptions-item label="ID">{{ active.id }}</el-descriptions-item>
        <el-descriptions-item label="商品 ID">{{ active.productId }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ active.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单项 ID">{{ active.orderItemId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate :model-value="active.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ formatTime(active.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">
          <div class="content-block">{{ active.content || '—' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="评价图片" :span="2">
          <div v-if="parseImages(active.images).length === 0" class="muted">—</div>
          <div v-else class="image-list">
            <el-image
              v-for="src in parseImages(active.images)"
              :key="src"
              :src="src"
              :preview-src-list="parseImages(active.images)"
              fit="cover"
              style="width: 88px; height: 88px; border-radius: 6px"
            />
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { adminListReviews, adminDeleteReview } from '@/api/admin'

const loading = ref(false)
const reviews = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filter = reactive({
  keyword: '',
  productId: '',
  rating: null
})

const detailVisible = ref(false)
const active = ref(null)

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images
  return String(images)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      productId: filter.productId || undefined,
      rating: filter.rating ?? undefined
    }
    const { data } = await adminListReviews(params)
    reviews.value = data?.list || []
    total.value = data?.total || 0
  } catch (err) {
    ElMessage.error(err?.message || '加载评价失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  filter.keyword = ''
  filter.productId = ''
  filter.rating = null
  pageNum.value = 1
  loadList()
}

function openDetail(row) {
  active.value = row
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除该评价？此操作不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  try {
    await adminDeleteReview(row.id)
    ElMessage.success('评价删除成功')
    if (reviews.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
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
.review-manage {
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

.muted {
  color: #9ca3af;
}

.content-block {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #1f2937;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
