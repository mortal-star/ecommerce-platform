<template>
  <section class="dashboard">
    <h2 class="page-title">数据看板</h2>
    <p class="page-subtitle">欢迎回来，{{ adminName }}！这里是您的运营数据概览。</p>

    <el-row :gutter="20" class="stat-grid">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.bg }">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-extra" v-if="stat.extra">
                今日 <b>+{{ stat.extra }}</b>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-grid">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>销量趋势（占位）</template>
          <div class="chart-placeholder">
            <el-icon :size="48" color="#94a3b8"><DataLine /></el-icon>
            <p>5.2 数据看板将接入 ECharts 销量趋势图</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>订单状态分布（占位）</template>
          <div class="chart-placeholder">
            <el-icon :size="48" color="#94a3b8"><PieChart /></el-icon>
            <p>5.2 接入 ECharts 饼图</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  DataLine,
  Money,
  PieChart,
  ShoppingCart,
  User
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const adminName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')

const data = ref({
  totalUsers: 0,
  totalOrders: 0,
  totalSalesAmount: 0,
  todayUsers: 0,
  todayOrders: 0,
  todaySalesAmount: 0
})

const stats = computed(() => [
  {
    key: 'users',
    label: '总用户数',
    value: data.value.totalUsers ?? 0,
    extra: data.value.todayUsers,
    icon: User,
    bg: 'linear-gradient(135deg, #6366f1, #8b5cf6)'
  },
  {
    key: 'orders',
    label: '总订单数',
    value: data.value.totalOrders ?? 0,
    extra: data.value.todayOrders,
    icon: ShoppingCart,
    bg: 'linear-gradient(135deg, #06b6d4, #3b82f6)'
  },
  {
    key: 'sales',
    label: '总销售额',
    value: '￥' + Number(data.value.totalSalesAmount ?? 0).toFixed(2),
    extra: data.value.todaySalesAmount ? Number(data.value.todaySalesAmount).toFixed(2) : null,
    icon: Money,
    bg: 'linear-gradient(135deg, #f59e0b, #ef4444)'
  },
  {
    key: 'today',
    label: '今日销售额',
    value: '￥' + Number(data.value.todaySalesAmount ?? 0).toFixed(2),
    icon: DataLine,
    bg: 'linear-gradient(135deg, #10b981, #059669)'
  }
])

async function loadStats() {
  try {
    const res = await getDashboardStats()
    data.value = res || data.value
  } catch (e) {
    // ignore, keep zeros
  }
}

onMounted(loadStats)
</script>

<style scoped lang="scss">
.dashboard {
  padding: 4px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1f2937;
}

.page-subtitle {
  margin: 0 0 24px;
  color: #6b7280;
}

.stat-grid {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
}

.stat-content {
  display: flex;
  gap: 16px;
  align-items: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin: 4px 0;
  color: #111827;
}

.stat-extra {
  font-size: 12px;
  color: #6b7280;

  b {
    color: #10b981;
  }
}

.chart-grid {
  margin-bottom: 24px;
}

.chart-placeholder {
  height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: #f9fafb;
  border-radius: 8px;

  p {
    margin-top: 12px;
    font-size: 13px;
  }
}
</style>
