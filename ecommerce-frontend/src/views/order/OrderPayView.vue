<template>
  <section class="order-pay-view">
    <div class="pay-container">
      <h2 class="page-title">订单支付</h2>

      <div v-if="loading" class="loading-block">加载中...</div>

      <template v-else-if="order">
        <!-- 状态切换 -->
        <div v-if="paySuccess" class="result-card success">
          <el-icon class="result-icon"><CircleCheckFilled /></el-icon>
          <h3>支付成功！</h3>
          <p>订单号：{{ order.orderNo }}</p>
          <p>实付金额：<strong>￥{{ formatPrice(order.payAmount) }}</strong></p>
          <div class="result-actions">
            <el-button type="primary" @click="$router.push('/orders')">查看订单</el-button>
            <el-button @click="$router.push('/')">继续购物</el-button>
          </div>
        </div>

        <div v-else-if="payFailed" class="result-card fail">
          <el-icon class="result-icon"><CircleCloseFilled /></el-icon>
          <h3>支付失败</h3>
          <p>{{ failMessage || '支付未完成，请稍后再试' }}</p>
          <div class="result-actions">
            <el-button type="primary" @click="retry">重试</el-button>
            <el-button @click="$router.push('/orders')">查看订单</el-button>
          </div>
        </div>

        <template v-else>
          <el-card class="order-info-card" shadow="never">
            <h3>订单信息</h3>
            <div class="info-row">
              <span class="label">订单号</span>
              <span class="value">{{ order.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">下单时间</span>
              <span class="value">{{ formatDate(order.createTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">收货人</span>
              <span class="value">{{ order.receiverName }} {{ order.receiverPhone }}</span>
            </div>
            <div class="info-row">
              <span class="label">收货地址</span>
              <span class="value">{{ order.province }}{{ order.city }}{{ order.district }}{{ order.detailAddress }}</span>
            </div>
            <el-divider />
            <div class="info-row big">
              <span class="label">应付金额</span>
              <span class="value pay-amount">￥{{ formatPrice(order.payAmount) }}</span>
            </div>
          </el-card>

          <el-card class="pay-method-card" shadow="never">
            <h3>选择支付方式</h3>
            <el-radio-group v-model="paymentType" class="payment-group">
              <el-radio :value="1" border>
                <el-icon><Wallet /></el-icon>
                <span>余额支付</span>
              </el-radio>
              <el-radio :value="2" border>
                <el-icon><CreditCard /></el-icon>
                <span>支付宝</span>
              </el-radio>
              <el-radio :value="3" border>
                <el-icon><ChatLineRound /></el-icon>
                <span>微信支付</span>
              </el-radio>
            </el-radio-group>

            <div class="pay-action-row">
              <span class="amount-label">应付：<strong>￥{{ formatPrice(order.payAmount) }}</strong></span>
              <el-button
                type="danger"
                size="large"
                :loading="paying"
                @click="handlePay"
              >立即支付</el-button>
            </div>
          </el-card>
        </template>
      </template>

      <Empty v-else size="large" description="订单不存在或已被处理">
        <el-button type="primary" @click="$router.push('/orders')">返回订单列表</el-button>
      </Empty>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleCheckFilled,
  CircleCloseFilled,
  Wallet,
  CreditCard,
  ChatLineRound
} from '@element-plus/icons-vue'
import { getOrderDetail, payOrder } from '@/api'
import Empty from '@/components/Empty.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const paying = ref(false)
const order = ref(null)
const paymentType = ref(2)
const paySuccess = ref(false)
const payFailed = ref(false)
const failMessage = ref('')

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

async function loadOrder() {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.warning('订单参数缺失')
    router.replace('/orders')
    return
  }
  loading.value = true
  try {
    const data = await getOrderDetail(orderId)
    order.value = data?.order || null
    if (route.query.paymentType) {
      paymentType.value = Number(route.query.paymentType)
    }
    if (order.value && order.value.status !== 0) {
      ElMessage.info('该订单不是待付款状态')
      paySuccess.value = order.value.status === 1 || order.value.status === 2 || order.value.status === 3
    }
  } catch (e) {
    order.value = null
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  if (!order.value) return
  paying.value = true
  try {
    const data = await payOrder(order.value.id, { paymentType: paymentType.value })
    if (data?.order?.status === 1 || data?.order?.payTime) {
      paySuccess.value = true
      payFailed.value = false
      order.value = data.order
    } else {
      paySuccess.value = true
      order.value = data?.order || order.value
    }
  } catch (e) {
    payFailed.value = true
    failMessage.value = e?.message || '支付未完成，请稍后再试'
  } finally {
    paying.value = false
  }
}

function retry() {
  payFailed.value = false
  failMessage.value = ''
}

onMounted(loadOrder)
</script>

<style scoped lang="scss">
.order-pay-view {
  background: #f5f7fa;
  padding: 16px 0 80px;
  min-height: 600px;
}
.pay-container {
  width: min(900px, calc(100% - 32px));
  margin: 0 auto;
}
.page-title {
  margin: 0 0 16px;
  font-size: 22px;
}

.loading-block {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 12px;
  color: #94a3b8;
}

.order-info-card, .pay-method-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;

  h3 {
    margin: 0 0 16px;
    font-size: 16px;
  }
}

.info-row {
  display: flex;
  padding: 8px 0;
  font-size: 14px;

  .label {
    width: 100px;
    color: #94a3b8;
  }
  .value {
    color: #1e293b;
    flex: 1;
  }

  &.big {
    align-items: center;
    .pay-amount {
      color: #ef4444;
      font-size: 24px;
      font-weight: 700;
    }
  }
}

.payment-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 24px;

  :deep(.el-radio.is-bordered) {
    padding: 12px 20px;
    height: auto;
    margin: 0;

    .el-radio__label {
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.pay-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;

  .amount-label {
    color: #64748b;
    font-size: 14px;
    strong {
      color: #ef4444;
      font-size: 22px;
      font-weight: 700;
      margin-left: 4px;
    }
  }
}

.result-card {
  background: #fff;
  border-radius: 12px;
  padding: 60px 24px;
  text-align: center;

  .result-icon {
    font-size: 80px;
    margin-bottom: 16px;
  }

  &.success .result-icon { color: #10b981; }
  &.fail .result-icon { color: #ef4444; }

  h3 {
    margin: 0 0 12px;
    font-size: 24px;
  }
  p {
    margin: 0 0 4px;
    color: #64748b;

    strong {
      color: #ef4444;
      font-weight: 700;
    }
  }
  .result-actions {
    margin-top: 24px;
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}
</style>
