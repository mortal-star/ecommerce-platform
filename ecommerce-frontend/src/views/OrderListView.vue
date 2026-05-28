<template>
  <section class="order-list-view">
    <div class="order-container">
      <h2 class="page-title">我的订单</h2>

      <!-- 状态 Tab -->
      <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="onStatusChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
        <el-tab-pane label="退款中" name="5" />
      </el-tabs>

      <div v-if="loading" class="loading-block">加载中...</div>

      <div v-else-if="orders.length" class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-head">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-time">{{ formatDate(order.createTime) }}</span>
            <el-tag :type="statusType(order.status)" effect="plain">
              {{ statusLabel(order.status) }}
            </el-tag>
          </div>

          <div class="order-body">
            <div class="order-info">
              <p class="info-row">收货人：{{ order.receiverName }} ({{ order.receiverPhone }})</p>
              <p class="info-row">地址：{{ order.province }}{{ order.city }}{{ order.district }}{{ order.detailAddress }}</p>
              <p v-if="order.remark" class="info-row">备注：{{ order.remark }}</p>
            </div>
            <div class="order-amount">
              <span class="label">实付：</span>
              <span class="amount">￥{{ formatPrice(order.payAmount) }}</span>
            </div>
          </div>

          <div class="order-actions">
            <el-button size="small" @click="viewDetail(order)">查看详情</el-button>
            <el-button
              v-if="order.status === 0"
              type="primary"
              size="small"
              @click="goPay(order)"
            >去支付</el-button>
            <el-button
              v-if="order.status === 0"
              size="small"
              @click="onCancel(order)"
            >取消订单</el-button>
            <el-button
              v-if="order.status === 2"
              type="primary"
              size="small"
              @click="onConfirmReceive(order)"
            >确认收货</el-button>
            <el-button
              v-if="order.status === 2"
              size="small"
              @click="onViewLogistics(order)"
            >查看物流</el-button>
            <el-button
              v-if="order.status === 1 || order.status === 2"
              size="small"
              @click="onApplyRefund(order)"
            >申请退款</el-button>
            <el-button
              v-if="order.status === 3"
              type="warning"
              size="small"
              @click="onComment(order)"
            >评价</el-button>
          </div>
        </div>

        <div class="pagination-wrap">
          <Pagination
            v-model:current-page="filters.pageNum"
            v-model:page-size="filters.pageSize"
            :total="total"
            @change="loadOrders"
          />
        </div>
      </div>

      <Empty v-else size="large" description="暂无订单">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </Empty>
    </div>

    <!-- 物流弹窗 -->
    <el-dialog v-model="logisticsVisible" title="物流信息" width="480">
      <p v-if="currentOrder?.logisticsCompany">
        物流公司：{{ currentOrder.logisticsCompany }}
      </p>
      <p v-if="currentOrder?.logisticsNo">
        运单号：{{ currentOrder.logisticsNo }}
      </p>
      <Empty
        v-if="!currentOrder?.logisticsCompany && !currentOrder?.logisticsNo"
        size="small"
        description="暂无物流信息"
      />
      <p v-else class="logistics-tip">
        请前往物流公司官网查询具体运输状态
      </p>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="640">
      <template v-if="detailData">
        <h4>{{ detailData.order.orderNo }}</h4>
        <p>状态：{{ statusLabel(detailData.order.status) }}</p>
        <p>下单时间：{{ formatDate(detailData.order.createTime) }}</p>
        <p>收货：{{ detailData.order.receiverName }} {{ detailData.order.receiverPhone }}</p>
        <p>地址：{{ detailData.order.province }}{{ detailData.order.city }}{{ detailData.order.district }}{{ detailData.order.detailAddress }}</p>
        <el-divider />
        <h4>商品清单</h4>
        <div v-for="item in detailData.items" :key="item.id" class="detail-item">
          <el-image :src="item.productImage || fallbackImage" class="item-image" fit="cover" />
          <div class="item-info">
            <p class="name">{{ item.productName }}</p>
            <p class="spec">{{ item.skuName || '' }}</p>
          </div>
          <div class="item-price">
            ￥{{ formatPrice(item.price) }} × {{ item.quantity }}
          </div>
        </div>
        <el-divider />
        <div class="detail-total">
          <span>商品总额：￥{{ formatPrice(detailData.order.totalAmount) }}</span>
          <span>运费：￥{{ formatPrice(detailData.order.freightAmount) }}</span>
          <span>实付：<strong>￥{{ formatPrice(detailData.order.payAmount) }}</strong></span>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listOrders,
  cancelOrder,
  confirmReceive,
  applyRefund,
  getOrderDetail
} from '@/api'
import Pagination from '@/components/Pagination.vue'
import Empty from '@/components/Empty.vue'

const route = useRoute()
const router = useRouter()

const orders = ref([])
const total = ref(0)
const loading = ref(false)
const activeStatus = ref('all')
const fallbackImage = 'https://placehold.co/80x80/cccccc/666666?text=Product'

const filters = reactive({
  pageNum: 1,
  pageSize: 10
})

const logisticsVisible = ref(false)
const detailVisible = ref(false)
const currentOrder = ref(null)
const detailData = ref(null)

const STATUS_LABEL = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款'
}

const STATUS_TYPE = {
  0: 'warning',
  1: 'primary',
  2: 'primary',
  3: 'success',
  4: 'info',
  5: 'danger',
  6: 'info'
}

function statusLabel(status) {
  return STATUS_LABEL[status] || '未知'
}

function statusType(status) {
  return STATUS_TYPE[status] || 'info'
}

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

function formatDate(value) {
  if (!value) return ''
  try {
    const d = new Date(value)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return String(value)
  }
}

async function loadOrders() {
  loading.value = true
  try {
    const params = {
      pageNum: filters.pageNum,
      pageSize: filters.pageSize
    }
    if (activeStatus.value !== 'all') {
      params.status = Number(activeStatus.value)
    }
    const data = await listOrders(params)
    orders.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onStatusChange() {
  filters.pageNum = 1
  router.replace({ path: '/orders', query: { status: activeStatus.value === 'all' ? undefined : activeStatus.value } })
  loadOrders()
}

async function onCancel(order) {
  try {
    await ElMessageBox.confirm('确定取消该订单？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (e) {}
}

async function onConfirmReceive(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'info' })
  } catch { return }
  try {
    await confirmReceive(order.id)
    ElMessage.success('确认收货成功')
    await loadOrders()
  } catch (e) {}
}

async function onApplyRefund(order) {
  try {
    await ElMessageBox.confirm('确定申请退款？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await applyRefund(order.id)
    ElMessage.success('退款申请已提交')
    await loadOrders()
  } catch (e) {}
}

function onViewLogistics(order) {
  currentOrder.value = order
  logisticsVisible.value = true
}

function onComment(order) {
  router.push({ path: '/review', query: { orderId: order.id } })
}

function goPay(order) {
  router.push({ path: '/order/pay', query: { orderId: order.id } })
}

async function viewDetail(order) {
  try {
    const data = await getOrderDetail(order.id)
    detailData.value = data
    detailVisible.value = true
  } catch (e) {}
}

watch(
  () => route.query.status,
  val => {
    if (val !== undefined) {
      activeStatus.value = String(val)
    } else {
      activeStatus.value = 'all'
    }
  },
  { immediate: false }
)

onMounted(() => {
  if (route.query.status !== undefined) {
    activeStatus.value = String(route.query.status)
  }
  loadOrders()
})
</script>

<style scoped lang="scss">
.order-list-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.order-container {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
}
.page-title {
  margin: 0 0 16px;
  font-size: 22px;
}

.status-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 8px 16px 0;
  margin-bottom: 16px;
}

.loading-block {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 12px;
  color: #94a3b8;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
}

.order-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
  color: #64748b;

  .order-no {
    color: #1e293b;
  }
  .order-time { color: #94a3b8; }
  .el-tag { margin-left: auto; }
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;

  .info-row {
    margin: 0 0 6px;
    font-size: 13px;
    color: #475569;
  }
  .order-amount {
    text-align: right;

    .label { color: #64748b; }
    .amount {
      color: #ef4444;
      font-size: 20px;
      font-weight: 700;
    }
  }
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;

  .item-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    background: #f1f5f9;
  }
  .item-info {
    flex: 1;
    .name { margin: 0 0 4px; font-size: 14px; }
    .spec { margin: 0; color: #94a3b8; font-size: 12px; }
  }
  .item-price {
    color: #475569;
    font-size: 13px;
  }
}

.detail-total {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  font-size: 14px;

  strong {
    color: #ef4444;
    font-size: 18px;
  }
}

.logistics-tip {
  color: #94a3b8;
  font-size: 13px;
}
</style>
