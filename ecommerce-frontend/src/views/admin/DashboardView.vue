<template>
  <section class="dashboard">
    <div class="page-head">
      <div>
        <h2 class="page-title">数据看板</h2>
        <p class="page-subtitle">欢迎回来，{{ adminName }}！这里是您的运营数据概览。</p>
      </div>
      <el-button :icon="Refresh" plain @click="refreshAll" :loading="loading">刷新数据</el-button>
    </div>

    <!-- 核心数据卡 -->
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
              <div class="stat-extra" v-if="stat.extra !== null && stat.extra !== undefined">
                今日 <b>+{{ stat.extra }}</b>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势 + 状态分布 -->
    <el-row :gutter="20" class="chart-grid">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>销量趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">近 7 天</el-radio-button>
                <el-radio-button :value="14">近 14 天</el-radio-button>
                <el-radio-button :value="30">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <VChart v-if="!trendEmpty" class="chart-box" :option="trendOption" autoresize />
          <Empty v-else description="暂无销量数据" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>订单状态分布</template>
          <VChart v-if="!statusEmpty" class="chart-box" :option="statusOption" autoresize />
          <Empty v-else description="暂无订单数据" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 热销商品 -->
    <el-card shadow="never" class="top-products">
      <template #header>
        <div class="card-header">
          <span>热销商品 TOP {{ topProducts.length || 10 }}</span>
          <el-button text type="primary" @click="$router.push('/admin/products')">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table v-if="topProducts.length" :data="topProducts" stripe>
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.mainImage || fallbackImage" fit="cover" class="product-thumb" />
              <span class="product-name">{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="140" align="right" />
        <el-table-column label="销售额" width="180" align="right">
          <template #default="{ row }">
            <span class="amount">￥{{ Number(row.salesAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else description="暂无销售数据" />
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  ArrowRight,
  DataLine,
  Money,
  Refresh,
  ShoppingCart,
  User
} from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent
} from 'echarts/components'
import {
  getDashboardStats,
  getSalesTrend,
  getOrderStatusStats,
  getTopProducts
} from '@/api'
import { useUserStore } from '@/stores'
import Empty from '@/components/Empty.vue'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  GridComponent,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

const userStore = useUserStore()
const adminName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')
const fallbackImage = 'https://placehold.co/80x80/cccccc/666666?text=Product'

const loading = ref(false)
const trendDays = ref(7)

const data = ref({
  totalUsers: 0,
  totalOrders: 0,
  totalSalesAmount: 0,
  todayUsers: 0,
  todayOrders: 0,
  todaySalesAmount: 0
})

const trend = ref({ dates: [], amounts: [], orderCounts: [] })
const orderStatus = ref([])
const topProducts = ref([])

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

const trendEmpty = computed(() => {
  const t = trend.value
  if (!t.dates?.length) return true
  const totalAmount = (t.amounts || []).reduce((s, v) => s + Number(v || 0), 0)
  const totalCount = (t.orderCounts || []).reduce((s, v) => s + Number(v || 0), 0)
  return totalAmount === 0 && totalCount === 0
})

const trendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' }
  },
  legend: {
    data: ['销售额', '订单数'],
    top: 0
  },
  grid: { left: 12, right: 20, bottom: 24, top: 40, containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trend.value.dates
  },
  yAxis: [
    {
      type: 'value',
      name: '销售额 (元)',
      position: 'left',
      axisLabel: { formatter: '￥{value}' }
    },
    {
      type: 'value',
      name: '订单数',
      position: 'right',
      minInterval: 1
    }
  ],
  series: [
    {
      name: '销售额',
      type: 'line',
      smooth: true,
      yAxisIndex: 0,
      data: (trend.value.amounts || []).map(v => Number(v || 0)),
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(99, 102, 241, 0.4)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
          ]
        }
      },
      lineStyle: { width: 3, color: '#6366f1' },
      itemStyle: { color: '#6366f1' }
    },
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: (trend.value.orderCounts || []).map(v => Number(v || 0)),
      lineStyle: { width: 2, color: '#10b981' },
      itemStyle: { color: '#10b981' }
    }
  ]
}))

const statusColors = ['#6366f1', '#06b6d4', '#10b981', '#22c55e', '#94a3b8', '#f59e0b', '#ef4444', '#a855f7']

const statusEmpty = computed(() => {
  const total = orderStatus.value.reduce((s, v) => s + Number(v.count || 0), 0)
  return total === 0
})

const statusOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', left: 'left', top: 'middle', textStyle: { fontSize: 12 } },
  series: [
    {
      name: '订单状态',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['65%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      labelLine: { show: false },
      data: orderStatus.value.map((s, i) => ({
        name: s.label,
        value: Number(s.count || 0),
        itemStyle: { color: statusColors[i % statusColors.length] }
      }))
    }
  ]
}))

async function loadStats() {
  try {
    data.value = (await getDashboardStats()) || data.value
  } catch (e) { /* keep zeros */ }
}

async function loadTrend() {
  try {
    trend.value = (await getSalesTrend({ days: trendDays.value })) || { dates: [], amounts: [], orderCounts: [] }
  } catch (e) {
    trend.value = { dates: [], amounts: [], orderCounts: [] }
  }
}

async function loadOrderStatus() {
  try {
    orderStatus.value = (await getOrderStatusStats()) || []
  } catch (e) {
    orderStatus.value = []
  }
}

async function loadTopProducts() {
  try {
    topProducts.value = (await getTopProducts({ limit: 10 })) || []
  } catch (e) {
    topProducts.value = []
  }
}

async function refreshAll() {
  loading.value = true
  try {
    await Promise.all([
      loadStats(),
      loadTrend(),
      loadOrderStatus(),
      loadTopProducts()
    ])
  } finally {
    loading.value = false
  }
}

onMounted(refreshAll)
</script>

<style scoped lang="scss">
.dashboard {
  padding: 4px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
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
  flex-shrink: 0;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-box {
  height: 320px;
}

.top-products {
  margin-bottom: 24px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: #f3f4f6;
  flex-shrink: 0;
}

.product-name {
  color: #1f2937;
  font-size: 14px;
}

.amount {
  color: #ef4444;
  font-weight: 600;
}
</style>
