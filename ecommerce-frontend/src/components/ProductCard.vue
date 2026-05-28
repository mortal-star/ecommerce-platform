<template>
  <el-card class="product-card" shadow="hover" :body-style="{ padding: '0' }" @click="goDetail">
    <div class="image-wrap">
      <el-image :src="product.mainImage || product.imageUrl || fallbackImage" fit="cover" lazy>
        <template #error>
          <div class="image-error">暂无图片</div>
        </template>
      </el-image>
      <el-button class="favorite-button" :icon="Star" circle :type="favorited ? 'warning' : 'default'" @click.stop="toggleFavorite" />
    </div>

    <div class="product-info">
      <h3 :title="product.name">{{ product.name }}</h3>
      <p v-if="product.subtitle" class="subtitle">{{ product.subtitle }}</p>
      <div class="meta-row">
        <span class="price">￥{{ formatPrice(product.price) }}</span>
        <span class="sales">已售 {{ product.sales || 0 }}</span>
      </div>
      <el-button type="primary" class="cart-button" :icon="ShoppingCart" @click.stop="addCart">
        加入购物车
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ShoppingCart, Star } from '@element-plus/icons-vue'

const props = defineProps({
  product: { type: Object, required: true },
  favorited: { type: Boolean, default: false },
  fallbackImage: { type: String, default: 'https://via.placeholder.com/400x300?text=Product' }
})

const emit = defineEmits(['add-cart', 'favorite', 'click'])
const router = useRouter()

function formatPrice(value) {
  const number = Number(value || 0)
  return number.toFixed(2)
}

function goDetail() {
  emit('click', props.product)
  router.push(`/products/${props.product.id}`)
}

function addCart() {
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

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 14px 0;
}

.price {
  color: #ef4444;
  font-size: 20px;
  font-weight: 800;
}

.sales {
  color: #94a3b8;
  font-size: 13px;
}

.cart-button {
  width: 100%;
}
</style>
