<template>
  <section class="user-list">
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-subtitle">管理平台用户、查看详情、启用/禁用、删除账号</p>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="账号 / 昵称 / 手机号 / 邮箱"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="filter.userType" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="users"
        stripe
        empty-text="暂无用户数据"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar v-if="row.avatar" :src="row.avatar" :size="36" />
              <el-icon v-else :size="32" color="#94a3b8"><UserFilled /></el-icon>
              <div class="user-meta">
                <div class="username">{{ row.username }}</div>
                <div class="nickname">{{ row.nickname || '—' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 2" type="warning">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastLoginTime) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.userType !== 2"
              text
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.userType !== 2"
              text
              type="danger"
              size="small"
              @click="confirmDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <Pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @change="loadList"
        />
      </div>
    </el-card>

    <!-- 用户详情 Dialog -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ detail.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ detail.email || '—' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderLabel(detail.gender) }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          <el-tag v-if="detail.userType === 2" type="warning">管理员</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detail.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatTime(detail.createTime) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="最近登录" :span="2">{{ formatTime(detail.lastLoginTime) || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Search, UserFilled } from '@element-plus/icons-vue'
import {
  listAdminUsers,
  getAdminUserDetail,
  updateAdminUserStatus,
  deleteAdminUser
} from '@/api'
import Pagination from '@/components/Pagination.vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filter = reactive({
  keyword: '',
  status: null,
  userType: null
})

const detailVisible = ref(false)
const detail = ref(null)

function formatTime(t) {
  if (!t) return ''
  const s = String(t).replace('T', ' ')
  return s.length > 19 ? s.slice(0, 19) : s
}

function genderLabel(g) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
}

async function loadList() {
  loading.value = true
  try {
    const data = await listAdminUsers({
      keyword: filter.keyword || undefined,
      status: filter.status ?? undefined,
      userType: filter.userType ?? undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    users.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    users.value = []
    total.value = 0
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
  filter.status = null
  filter.userType = null
  pageNum.value = 1
  loadList()
}

async function openDetail(row) {
  try {
    detail.value = await getAdminUserDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    detail.value = row
    detailVisible.value = true
  }
}

async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  const action = target === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}用户「${row.username}」吗？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await updateAdminUserStatus(row.id, target)
    ElMessage.success(`已${action}`)
    row.status = target
  } catch (e) { /* error toast handled by request interceptor */ }
}

async function confirmDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除用户「${row.username}」后不可恢复，确认继续？`,
      '危险操作',
      { type: 'error', confirmButtonText: '确认删除', confirmButtonClass: 'el-button--danger' }
    )
  } catch { return }
  try {
    await deleteAdminUser(row.id)
    ElMessage.success('删除成功')
    if (users.value.length === 1 && pageNum.value > 1) {
      pageNum.value -= 1
    }
    loadList()
  } catch (e) { /* handled */ }
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.user-list {
  padding: 4px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 16px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1f2937;
}

.page-subtitle {
  margin: 0;
  color: #6b7280;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.username {
  font-weight: 500;
  color: #1f2937;
}

.nickname {
  font-size: 12px;
  color: #6b7280;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
