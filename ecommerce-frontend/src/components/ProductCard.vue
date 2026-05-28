<template>
  <el-card
    class="product-card"
    :class="{ 'is-disabled': outOfStock }"
    shadow="hover"
    :body-style="{ padding: '0' }"
    @click="goDetail"
  >
    <div class="image-wrap">
      <el-image :src="imageUrl" fit="cover" lazy>
        <template #error>
          <div class="image-error">暂无图片</div>
        </template>
      </el-image>
      <el-tag
        v-if="discountTag"
        class="discount-tag"
        type="danger"
        effect="dark"
        size="small"
      >
        {{ discountTag }}
      </el-tag>
      <div v-if="outOfStock" class="out-of-stock-mask">已售罄</div>
      <el-button
        v-if="showFavorite"
        class="favorite-button"
        :icon="favorited ? StarFilled : Star"
        circle
        :type="favorited ? 'warning' : 'default'"
        @click.stop="toggleFavorite"
      />
    </div>

    <div class="product-info">
      <h3 :title="product.name">{{ product.name }}</h3>
      <p class="subtitle">{{ product.subtitle || '\u00A0' }}</p>
      <div class="price-row">
        <span class="price">￥{{ formatPrice(product.price) }}</span>
        <span v-if="originalPriceDisplay" class="original-price">￥{{ originalPriceDisplay }}</span>
      </div>
      <div class="meta-row">
        <span class="sales">已售 {{ product.sales || 0 }}</span>
        <span
          v-if="product.stock !== undefined && product.stock !== null"
          class="stock"
          :class="{ low: isLowStock }"
        >
          库存 {{ product.stock }}
        </span>
      </div>
      <el-button
        v-if="showAddCart"
        type="primary"
        class="cart-button"
        :icon="ShoppingCart"
        :disabled="outOfStock"
        :loading="loadingAddCart"
        @click.stop="addCart"
      >
        {{ outOfStock ? '已售罄' : '加入购物车' }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'

const props = defineProps({
  product: { type: Object, required: true },
  favorited: { type: Boolean, default: false },
  showFavorite: { type: Boolean, default: true },
  showAddCart: { type: Boolean, default: true },
  loadingAddCart: { type: Boolean, default: false },
  fallbackImage: { type: String, default: 'https://via.placeholder.com/400x300?text=Product' },
  detailRoute: { type: Function, default: null },
  lowStockThreshold: { type: Number, default: 10 }
})

const emit = defineEmits(['add-cart', 'favorite', 'click'])
const router = useRouter()

const imageUrl = computed(() => {
  return props.product.mainImage || props.product.imageUrl || props.fallbackImage
})

const outOfStock = computed(() => {
  const stock = props.product.stock
  return stock !== undefined && stock !== null && Number(stock) <= 0
})

const isLowStock = computed(() => {
  const stock = Number(props.product.stock)
  return Number.isFinite(stock) && stock > 0 && stock <= props.lowStockThreshold
})

const originalPriceDisplay = computed(() => {
  const original = Number(props.product.originalPrice)
  const current = Number(props.product.price)
  if (!Number.isFinite(original) || original <= 0) return ''
  if (!Number.isFinite(current) || original <= current) return ''
  return original.toFixed(2)
})

const discountTag = computed(() => {
  if (!originalPriceDisplay.value) return ''
  const original = Number(props.product.originalPrice)
  const current = Number(props.product.price)
  const ratio = current / original
  const tenths = Math.round(ratio * 100) / 10
  return `${tenths}折`
})

function formatPrice(value) {
  const number = Number(value || 0)
  return number.toFixed(2)
}

function goDetail() {
  emit('click', props.product)
  if (props.detailRoute) {
    const target = props.detailRoute(props.product)
    if (target) router.push(target)
    return
  }
  if (props.product.id !== undefined) {
    router.push(`/products/${props.product.id}`)
  }
}

function addCart() {
  if (outOfStock.value) return
  emit('add-cart', props.product)
}

function toggleFavorite() {
  emit('favorite', props.product)
}
</script>

<style scoped lang="scss">
.product-card {
  overflow: hidden;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
  }

  &.is-disabled {
    cursor: not-allowed;
    opacity: 0.85;
  }
}

.image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #f1f5f9;

  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
}

.image-error {
  display: grid;
  width: 100%;
  height: 100%;
  color: #94a3b8;
  place-items: center;
}

.discount-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  font-weight: 700;
}

.out-of-stock-mask {
  position: absolute;
  inset: 0;
  display: grid;
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  background: rgba(15, 23, 42, 0.5);
  place-items: center;
}

.favorite-button {
  position: absolute;
  top: 12px;
  right: 12px;
}

.product-info {
  padding: 16px;

  h3 {
    margin: 0;
    overflow: hidden;
    color: #0f172a;
    font-size: 16px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.subtitle {
  height: 20px;
  margin: 8px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 12px;
}

.price {
  color: #ef4444;
  font-size: 20px;
  font-weight: 800;
}

.original-price {
  color: #94a3b8;
  font-size: 13px;
  text-decoration: line-through;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 14px;
  color: #94a3b8;
  font-size: 13px;
}

.stock.low {
  color: #f97316;
}

.cart-button {
  width: 100%;
}
</style>
