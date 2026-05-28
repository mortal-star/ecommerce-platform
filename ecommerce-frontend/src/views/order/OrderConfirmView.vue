<template>
  <section class="order-confirm-view">
    <div class="confirm-container">
      <h2 class="page-title">订单确认</h2>

      <div v-if="loading" class="loading-block">加载中...</div>

      <template v-else-if="selectedItems.length">
        <!-- 收货地址 -->
        <section class="step-block">
          <div class="block-head">
            <h3>收货地址</h3>
            <el-button link type="primary" @click="$router.push('/user/addresses')">管理地址</el-button>
          </div>
          <div v-if="addresses.length" class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="addr-cell"
              :class="{ active: selectedAddressId === addr.id }"
              @click="selectedAddressId = addr.id"
            >
              <div class="addr-head">
                <span class="name">{{ addr.receiverName }}</span>
                <span class="phone">{{ addr.receiverPhone }}</span>
                <el-tag v-if="addr.defaultFlag === 1" type="warning" size="small">默认</el-tag>
              </div>
              <p class="addr-body">
                {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
              </p>
              <el-icon v-if="selectedAddressId === addr.id" class="active-mark">
                <CircleCheckFilled />
              </el-icon>
            </div>
          </div>
          <Empty v-else size="small" description="还没有收货地址">
            <el-button type="primary" @click="$router.push('/user/addresses')">去新增地址</el-button>
          </Empty>
        </section>

        <!-- 商品清单 -->
        <section class="step-block">
          <div class="block-head"><h3>商品清单</h3></div>
          <div class="goods-table">
            <div class="thead">
              <span class="col-product">商品</span>
              <span class="col-price">单价</span>
              <span class="col-qty">数量</span>
              <span class="col-subtotal">小计</span>
            </div>
            <div v-for="item in selectedItems" :key="item.cart.id" class="trow">
              <div class="col-product">
                <el-image :src="item.product?.mainImage || fallbackImage" class="thumb" fit="cover" />
                <div class="meta">
                  <p class="name">{{ item.product?.name }}</p>
                  <p v-if="item.spec" class="spec">{{ item.spec.skuName || item.spec.attributes }}</p>
                </div>
              </div>
              <span class="col-price">￥{{ formatPrice(item.spec?.price ?? item.product?.price) }}</span>
              <span class="col-qty">×{{ item.cart.quantity }}</span>
              <span class="col-subtotal">
                ￥{{ formatPrice(item.itemAmount ?? (item.cart.quantity * (item.spec?.price ?? item.product?.price ?? 0))) }}
              </span>
            </div>
          </div>
        </section>

        <!-- 备注 + 支付方式 -->
        <section class="step-block">
          <div class="block-head"><h3>支付方式</h3></div>
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
        </section>

        <section class="step-block">
          <div class="block-head"><h3>订单备注</h3></div>
          <el-input
            v-model="remark"
            type="textarea"
            :rows="2"
            :maxlength="200"
            show-word-limit
            placeholder="对商品或配送有特殊要求？写下来吧（选填）"
          />
        </section>

        <!-- 结算栏 -->
        <div class="submit-bar">
          <div class="amount-detail">
            <p>商品总额：<span>￥{{ formatPrice(totalAmount) }}</span></p>
            <p>运费：<span>￥{{ formatPrice(freight) }}</span></p>
            <p class="pay-line">
              应付：<span class="pay-price">￥{{ formatPrice(payAmount) }}</span>
            </p>
          </div>
          <el-button
            type="danger"
            size="large"
            :loading="submitting"
            :disabled="!selectedAddressId"
            @click="handleSubmit"
          >提交订单</el-button>
        </div>
      </template>

      <Empty v-else size="large" description="还没有选中任何商品，去购物车选择吧">
        <el-button type="primary" @click="$router.push('/cart')">返回购物车</el-button>
      </Empty>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleCheckFilled,
  Wallet,
  CreditCard,
  ChatLineRound
} from '@element-plus/icons-vue'
import {
  listCart,
  listAddresses,
  createOrder
} from '@/api'
import Empty from '@/components/Empty.vue'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const items = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const paymentType = ref(2)
const remark = ref('')

const fallbackImage = 'https://placehold.co/80x80/cccccc/666666?text=Product'
const freight = ref(0)

const selectedItems = computed(() => items.value.filter(i => i.cart.selected === 1))
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => {
    const amount = i.itemAmount != null
      ? Number(i.itemAmount)
      : Number(i.cart.quantity) * Number(i.spec?.price ?? i.product?.price ?? 0)
    return sum + amount
  }, 0)
)
const payAmount = computed(() => totalAmount.value + Number(freight.value || 0))

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

async function loadData() {
  loading.value = true
  try {
    const [cart, addrs] = await Promise.all([listCart(), listAddresses()])
    items.value = cart || []
    addresses.value = addrs || []
    const def = addresses.value.find(a => a.defaultFlag === 1)
    selectedAddressId.value = def?.id || addresses.value[0]?.id || null
  } catch (e) {
    items.value = []
    addresses.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (!selectedItems.value.length) {
    ElMessage.warning('没有可结算的商品')
    return
  }
  submitting.value = true
  try {
    const data = await createOrder({
      addressId: selectedAddressId.value,
      remark: remark.value
    })
    ElMessage.success('订单创建成功')
    const orderId = data?.order?.id
    if (orderId) {
      router.replace({
        path: '/order/pay',
        query: { orderId, paymentType: paymentType.value }
      })
    } else {
      router.replace('/orders')
    }
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.order-confirm-view {
  background: #f5f7fa;
  padding: 16px 0 120px;
  min-height: 600px;
}
.confirm-container {
  width: min(1200px, calc(100% - 32px));
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

.step-block {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;

  .block-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.addr-cell {
  position: relative;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: border-color 0.2s;

  &:hover {
    border-color: #2563eb;
  }

  &.active {
    border-color: #2563eb;
    background: #eff6ff;
  }

  .addr-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    font-size: 14px;

    .name { font-weight: 500; }
    .phone { color: #64748b; }
  }
  .addr-body {
    margin: 0;
    font-size: 13px;
    color: #475569;
    line-height: 1.5;
  }
  .active-mark {
    position: absolute;
    top: 8px;
    right: 8px;
    color: #2563eb;
    font-size: 20px;
  }
}

.goods-table {
  .thead, .trow {
    display: grid;
    grid-template-columns: 1fr 100px 80px 100px;
    align-items: center;
    padding: 10px 0;
    font-size: 14px;
  }
  .thead {
    color: #94a3b8;
    background: #f8fafc;
    padding: 10px 12px;
    border-radius: 4px;
  }
  .trow {
    border-bottom: 1px solid #f1f5f9;
    padding: 12px 12px;
  }
}
.col-product {
  display: flex;
  align-items: center;
  gap: 12px;

  .thumb {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f1f5f9;
  }
  .meta {
    .name { margin: 0 0 4px; font-size: 14px; }
    .spec { margin: 0; color: #94a3b8; font-size: 12px; }
  }
}
.col-price, .col-qty {
  color: #475569;
}
.col-subtotal {
  color: #ef4444;
  font-weight: 500;
}

.payment-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;

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

.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  padding: 16px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4px 12px rgba(15, 23, 42, 0.08);

  .amount-detail {
    text-align: right;
    p {
      margin: 0 0 4px;
      font-size: 13px;
      color: #64748b;
    }
    .pay-line {
      margin-top: 4px;
      font-size: 14px;
      color: #475569;
    }
    .pay-price {
      color: #ef4444;
      font-size: 24px;
      font-weight: 700;
      margin-left: 4px;
    }
  }
}
</style>
