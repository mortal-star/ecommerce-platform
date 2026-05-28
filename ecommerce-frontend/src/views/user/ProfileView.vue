<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <!-- 用户信息卡 -->
    <el-card class="info-card" shadow="never">
      <div class="info-grid">
        <div class="info-cell">
          <span class="label">用户名</span>
          <span class="value">{{ userInfo.username || '-' }}</span>
        </div>
        <div class="info-cell">
          <span class="label">昵称</span>
          <span class="value">{{ userInfo.nickname || '-' }}</span>
        </div>
        <div class="info-cell">
          <span class="label">邮箱</span>
          <span class="value">{{ userInfo.email || '-' }}</span>
        </div>
        <div class="info-cell">
          <span class="label">手机</span>
          <span class="value">{{ userInfo.phone || '-' }}</span>
        </div>
        <div class="info-cell">
          <span class="label">注册时间</span>
          <span class="value">{{ formatDate(userInfo.createTime) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 订单状态统计 -->
    <h3 class="section-title">我的订单</h3>
    <div class="status-grid">
      <div
        v-for="s in orderStatuses"
        :key="s.code"
        class="status-cell"
        @click="goOrders(s.code)"
      >
        <el-icon class="status-icon"><component :is="s.icon" /></el-icon>
        <div class="status-count">{{ s.count }}</div>
        <div class="status-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <h3 class="section-title">快捷入口</h3>
    <div class="quick-grid">
      <div class="quick-cell" @click="$router.push('/orders')">
        <el-icon><List /></el-icon>
        <span>我的订单</span>
      </div>
      <div class="quick-cell" @click="$router.push('/user/favorites')">
        <el-icon><Star /></el-icon>
        <span>我的收藏</span>
      </div>
      <div class="quick-cell" @click="$router.push('/user/addresses')">
        <el-icon><Location /></el-icon>
        <span>收货地址</span>
      </div>
      <div class="quick-cell" @click="$router.push('/user/password')">
        <el-icon><Lock /></el-icon>
        <span>修改密码</span>
      </div>
      <div class="quick-cell" @click="$router.push('/feedback')">
        <el-icon><ChatLineRound /></el-icon>
        <span>意见反馈</span>
      </div>
      <div class="quick-cell" @click="$router.push('/notices')">
        <el-icon><Bell /></el-icon>
        <span>系统公告</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  List,
  Star,
  Location,
  Lock,
  Bell,
  ChatLineRound,
  Wallet,
  Box,
  Van,
  ChatDotRound,
  Refresh
} from '@element-plus/icons-vue'
import { getProfile, listOrders } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref({})

const orderStatuses = ref([
  { code: 0, label: '待付款', icon: Wallet, count: 0 },
  { code: 1, label: '待发货', icon: Box, count: 0 },
  { code: 2, label: '待收货', icon: Van, count: 0 },
  { code: 3, label: '待评价', icon: ChatDotRound, count: 0 },
  { code: 5, label: '退款/售后', icon: Refresh, count: 0 }
])

function formatDate(value) {
  if (!value) return '-'
  try {
    const d = new Date(value)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch (e) {
    return String(value)
  }
}

function goOrders(status) {
  router.push({ path: '/orders', query: { status } })
}

async function loadProfile() {
  try {
    const data = await getProfile()
    userInfo.value = data || {}
    userStore.setUserInfo({
      ...(userStore.userInfo || {}),
      ...data
    })
  } catch (e) {
    userInfo.value = userStore.userInfo || {}
  }
}

async function loadOrderCounts() {
  for (const s of orderStatuses.value) {
    try {
      const data = await listOrders({ status: s.code, pageNum: 1, pageSize: 1 })
      s.count = Number(data?.total || 0)
    } catch (e) {
      s.count = 0
    }
  }
}

onMounted(() => {
  loadProfile()
  loadOrderCounts()
})
</script>

<style scoped lang="scss">
.profile-page {
  .page-title {
    margin: 0 0 20px;
    font-size: 20px;
  }
}

.info-card {
  margin-bottom: 24px;
  border-radius: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .label {
    color: #94a3b8;
    font-size: 13px;
  }
  .value {
    color: #1e293b;
    font-size: 15px;
  }
}

.section-title {
  margin: 24px 0 16px;
  font-size: 16px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 8px;
}

.status-cell {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px 8px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background: #eff6ff;
    .status-icon, .status-count, .status-label { color: #2563eb; }
  }

  .status-icon {
    font-size: 28px;
    color: #64748b;
  }
  .status-count {
    margin-top: 8px;
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
  }
  .status-label {
    margin-top: 2px;
    color: #64748b;
    font-size: 13px;
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.quick-cell {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px 8px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;

  .el-icon {
    font-size: 24px;
  }

  &:hover {
    background: #eff6ff;
    color: #2563eb;
  }
}

@media (max-width: 768px) {
  .status-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
