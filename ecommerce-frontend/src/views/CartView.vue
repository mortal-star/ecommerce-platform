<template>
  <section class="cart-view">
    <div class="cart-container">
      <h2 class="page-title">我的购物车</h2>

      <div v-if="loading" class="loading-block">加载中...</div>

      <template v-else-if="items.length">
        <div class="cart-table">
          <div class="table-head">
            <div class="col col-check">
              <el-checkbox
                :model-value="allSelected"
                :indeterminate="someSelected && !allSelected"
                @change="toggleAll"
              />
            </div>
            <div class="col col-product">商品信息</div>
            <div class="col col-price">单价</div>
            <div class="col col-qty">数量</div>
            <div class="col col-subtotal">小计</div>
            <div class="col col-actions">操作</div>
          </div>

          <div v-for="item in items" :key="item.cart.id" class="table-row">
            <div class="col col-check">
              <el-checkbox
                :model-value="item.cart.selected === 1"
                @change="toggleOne(item)"
              />
            </div>
            <div class="col col-product">
              <el-image
                :src="item.product?.mainImage || fallbackImage"
                class="thumb"
                fit="cover"
                @click="goDetail(item.product)"
              />
              <div class="meta">
                <h4 @click="goDetail(item.product)">{{ item.product?.name || '商品已下架' }}</h4>
                <p v-if="item.spec" class="spec">规格：{{ item.spec.skuName || item.spec.attributes }}</p>
              </div>
            </div>
            <div class="col col-price">￥{{ formatPrice(item.spec?.price ?? item.product?.price) }}</div>
            <div class="col col-qty">
              <el-input-number
                :model-value="item.cart.quantity"
                :min="1"
                :max="Math.max(item.spec?.stock ?? item.product?.stock ?? 999, 1)"
                size="small"
                @change="val => updateQuantity(item, val)"
              />
            </div>
            <div class="col col-subtotal">
              ￥{{ formatPrice(item.itemAmount ?? (item.cart.quantity * (item.spec?.price ?? item.product?.price ?? 0))) }}
            </div>
            <div class="col col-actions">
              <el-button type="danger" link @click="removeItem(item)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 结算栏 -->
        <div class="checkout-bar">
          <div class="left">
            <el-checkbox
              :model-value="allSelected"
              :indeterminate="someSelected && !allSelected"
              @change="toggleAll"
            >全选</el-checkbox>
            <el-button type="danger" link @click="removeSelected">删除选中</el-button>
            <span class="info">已选 <strong>{{ selectedItems.length }}</strong> 件商品</span>
          </div>
          <div class="right">
            <span class="total-label">合计：</span>
            <span class="total-price">￥{{ formatPrice(totalAmount) }}</span>
            <el-button
              type="primary"
              size="large"
              :disabled="selectedItems.length === 0"
              @click="goCheckout"
            >去结算</el-button>
          </div>
        </div>
      </template>

      <Empty v-else size="large" description="购物车空空如也，去逛逛吧">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </Empty>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCart,
  updateCartQuantity,
  updateCartSelected,
  deleteCart
} from '@/api'
import Empty from '@/components/Empty.vue'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const fallbackImage = 'https://placehold.co/120x120/cccccc/666666?text=Product'

const selectedItems = computed(() => items.value.filter(i => i.cart.selected === 1))
const allSelected = computed(() =>
  items.value.length > 0 && items.value.every(i => i.cart.selected === 1)
)
const someSelected = computed(() => items.value.some(i => i.cart.selected === 1))
const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, i) => {
    const itemAmount = i.itemAmount != null
      ? Number(i.itemAmount)
      : Number(i.cart.quantity) * Number(i.spec?.price ?? i.product?.price ?? 0)
    return sum + itemAmount
  }, 0)
)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

async function loadCart() {
  loading.value = true
  try {
    const data = await listCart()
    items.value = data || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function updateQuantity(item, val) {
  if (val == null || val < 1) return
  const original = item.cart.quantity
  item.cart.quantity = val
  try {
    await updateCartQuantity(item.cart.id, { quantity: val })
    // 重新拉取以更新 itemAmount
    await loadCart()
  } catch (e) {
    item.cart.quantity = original
  }
}

async function toggleOne(item) {
  const newSelected = item.cart.selected === 1 ? 0 : 1
  const original = item.cart.selected
  item.cart.selected = newSelected
  try {
    await updateCartSelected({ cartIds: [item.cart.id], selected: newSelected })
  } catch (e) {
    item.cart.selected = original
  }
}

async function toggleAll(val) {
  const target = val ? 1 : 0
  const ids = items.value.map(i => i.cart.id)
  items.value.forEach(i => { i.cart.selected = target })
  try {
    await updateCartSelected({ cartIds: ids, selected: target })
  } catch (e) {
    await loadCart()
  }
}

async function removeItem(item) {
  try {
    await ElMessageBox.confirm(`确定移除「${item.product?.name || '该商品'}」？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteCart(item.cart.id)
    ElMessage.success('已删除')
    await loadCart()
  } catch (e) {}
}

async function removeSelected() {
  const selected = selectedItems.value
  if (!selected.length) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除已选中的 ${selected.length} 件商品？`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  for (const i of selected) {
    try {
      await deleteCart(i.cart.id)
    } catch (e) {}
  }
  ElMessage.success('已删除选中商品')
  await loadCart()
}

function goDetail(product) {
  if (product?.id) router.push(`/products/${product.id}`)
}

function goCheckout() {
  if (!selectedItems.value.length) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  router.push('/order/confirm')
}

onMounted(loadCart)
</script>

<style scoped lang="scss">
.cart-view {
  background: #f5f7fa;
  padding: 16px 0 80px;
  min-height: 600px;
}
.cart-container {
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

.cart-table {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.table-head, .table-row {
  display: grid;
  grid-template-columns: 50px 1fr 110px 140px 130px 80px;
  align-items: center;
  padding: 14px 16px;
  font-size: 14px;
}
.table-head {
  background: #f8fafc;
  color: #64748b;
  font-weight: 500;
}
.table-row + .table-row {
  border-top: 1px solid #f1f5f9;
}
.table-row:hover {
  background: #f8fafc;
}

.col-product {
  display: flex;
  align-items: center;
  gap: 12px;

  .thumb {
    width: 72px;
    height: 72px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    flex-shrink: 0;
    background: #f1f5f9;
  }
  .meta {
    h4 {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      &:hover { color: #2563eb; }
    }
    .spec {
      margin: 0;
      color: #94a3b8;
      font-size: 12px;
    }
  }
}

.col-price, .col-subtotal {
  color: #ef4444;
  font-weight: 500;
}
.col-subtotal {
  font-size: 16px;
}

.checkout-bar {
  position: sticky;
  bottom: 0;
  margin-top: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4px 12px rgba(15, 23, 42, 0.06);

  .left {
    display: flex;
    align-items: center;
    gap: 16px;

    .info {
      color: #64748b;
      font-size: 14px;
      strong { color: #2563eb; margin: 0 4px; }
    }
  }
  .right {
    display: flex;
    align-items: center;
    gap: 12px;

    .total-label {
      color: #64748b;
      font-size: 14px;
    }
    .total-price {
      color: #ef4444;
      font-size: 24px;
      font-weight: 700;
      margin-right: 12px;
    }
  }
}

@media (max-width: 768px) {
  .table-head { display: none; }
  .table-row {
    grid-template-columns: 50px 1fr;
    grid-template-areas:
      "check product"
      "check qty"
      "check subtotal";
    gap: 8px;
    padding: 12px;
  }
  .col-price { display: none; }
  .col-actions { grid-column: 2; }
}
</style>
