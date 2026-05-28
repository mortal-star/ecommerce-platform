<template>
  <section class="product-detail-view">
    <div class="detail-container">
      <div v-if="loading" class="loading-block">加载中...</div>

      <template v-else-if="product">
        <!-- 主体：图集 + 信息 -->
        <div class="main-row">
          <div class="gallery">
            <div class="main-image">
              <el-image
                :src="currentImage || product.mainImage || fallbackImage"
                fit="cover"
                :preview-src-list="galleryUrls"
                :initial-index="currentImageIndex"
              />
            </div>
            <div v-if="galleryUrls.length > 1" class="thumbnails">
              <div
                v-for="(url, idx) in galleryUrls"
                :key="idx"
                class="thumb"
                :class="{ active: idx === currentImageIndex }"
                @mouseenter="currentImageIndex = idx"
              >
                <el-image :src="url" fit="cover" />
              </div>
            </div>
          </div>

          <div class="info">
            <h1 class="name">{{ product.name }}</h1>
            <p class="subtitle">{{ product.subtitle || '\u00A0' }}</p>

            <div class="price-block">
              <span class="price-label">价格：</span>
              <span class="price">￥{{ formatPrice(currentPrice) }}</span>
              <span v-if="originalPriceDisplay" class="original-price">
                ￥{{ originalPriceDisplay }}
              </span>
            </div>

            <div class="meta-row">
              <span>销量：<strong>{{ product.sales || 0 }}</strong></span>
              <span>库存：<strong :class="{ 'low-stock': currentStock <= 10 && currentStock > 0, 'out-stock': currentStock <= 0 }">{{ currentStock }}</strong></span>
              <span>单位：{{ product.unit || '件' }}</span>
            </div>

            <!-- 规格选择 -->
            <div v-if="specs.length" class="spec-block">
              <div class="spec-label">规格：</div>
              <div class="spec-options">
                <a
                  v-for="s in specs"
                  :key="s.id"
                  class="spec-option"
                  :class="{ active: selectedSpec?.id === s.id, disabled: (s.stock ?? 0) <= 0 }"
                  @click="selectSpec(s)"
                >
                  {{ s.skuName || s.attributes || s.skuCode }}
                </a>
              </div>
            </div>

            <!-- 数量 -->
            <div class="qty-block">
              <span class="qty-label">数量：</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="Math.max(currentStock, 1)"
                size="default"
              />
            </div>

            <!-- 按钮 -->
            <div class="action-row">
              <el-button
                type="warning"
                size="large"
                :disabled="currentStock <= 0"
                @click="handleAddCart"
              >
                <el-icon><ShoppingCart /></el-icon>
                <span>加入购物车</span>
              </el-button>
              <el-button
                type="danger"
                size="large"
                :disabled="currentStock <= 0"
                @click="handleBuyNow"
              >立即购买</el-button>
              <el-button
                :type="favorited ? 'warning' : 'default'"
                size="large"
                @click="handleFavorite"
              >
                <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon>
                <span>{{ favorited ? '已收藏' : '收藏' }}</span>
              </el-button>
            </div>
          </div>
        </div>

        <!-- Tabs -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="商品详情" name="detail">
            <div v-if="product.detail" class="detail-html" v-html="product.detail"></div>
            <Empty v-else size="default" description="暂无详情" />
          </el-tab-pane>
          <el-tab-pane label="商品参数" name="params">
            <ul class="params-list">
              <li><span>商品名称</span><strong>{{ product.name }}</strong></li>
              <li><span>副标题</span><strong>{{ product.subtitle || '-' }}</strong></li>
              <li><span>单位</span><strong>{{ product.unit || '-' }}</strong></li>
              <li><span>分类</span><strong>{{ product.categoryId ?? '-' }}</strong></li>
              <li v-if="selectedSpec"><span>规格</span><strong>{{ selectedSpec.skuName || selectedSpec.attributes || selectedSpec.skuCode }}</strong></li>
              <li v-if="selectedSpec?.attributes"><span>属性</span><strong>{{ selectedSpec.attributes }}</strong></li>
            </ul>
          </el-tab-pane>
          <el-tab-pane :label="`商品评价 (${reviewTotal})`" name="reviews">
            <div v-if="loadingReviews" class="loading-row">加载中...</div>
            <div v-else-if="reviews.length" class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-item">
                <div class="review-head">
                  <span class="reviewer">用户 {{ r.userId }}</span>
                  <el-rate v-model="r.rating" disabled :max="5" />
                  <span class="time">{{ formatDate(r.createTime) }}</span>
                </div>
                <p class="content">{{ r.content || '该用户没有填写评价内容' }}</p>
                <div v-if="parseImages(r.images).length" class="review-images">
                  <el-image
                    v-for="(img, idx) in parseImages(r.images)"
                    :key="idx"
                    :src="img"
                    fit="cover"
                    :preview-src-list="parseImages(r.images)"
                    :initial-index="idx"
                  />
                </div>
              </div>
              <div class="review-pagination">
                <Pagination
                  v-model:current-page="reviewParams.pageNum"
                  v-model:page-size="reviewParams.pageSize"
                  :total="reviewTotal"
                  :show-summary="false"
                  layout="prev, pager, next"
                  @change="loadReviews"
                />
              </div>
            </div>
            <Empty v-else size="default" description="暂无评价" />
          </el-tab-pane>
        </el-tabs>
      </template>

      <Empty v-else size="large" description="商品不存在或已下架" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
import {
  getProductDetail,
  listProductReviews,
  addCart,
  addFavorite,
  cancelFavorite
} from '@/api'
import Empty from '@/components/Empty.vue'
import Pagination from '@/components/Pagination.vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fallbackImage = 'https://placehold.co/600x600/cccccc/666666?text=Product'

const loading = ref(false)
const product = ref(null)
const images = ref([])
const specs = ref([])
const favorited = ref(false)
const selectedSpec = ref(null)
const quantity = ref(1)
const currentImageIndex = ref(0)
const activeTab = ref('detail')

const reviews = ref([])
const reviewTotal = ref(0)
const loadingReviews = ref(false)
const reviewParams = reactive({ pageNum: 1, pageSize: 5 })

const galleryUrls = computed(() => {
  const urls = images.value
    .map(img => img.imageUrl || img.url)
    .filter(Boolean)
  if (product.value?.mainImage && !urls.includes(product.value.mainImage)) {
    urls.unshift(product.value.mainImage)
  }
  return urls.length ? urls : product.value?.mainImage ? [product.value.mainImage] : []
})

const currentImage = computed(() => galleryUrls.value[currentImageIndex.value])

const currentPrice = computed(() => {
  if (selectedSpec.value?.price != null) return selectedSpec.value.price
  return product.value?.price
})

const currentStock = computed(() => {
  if (selectedSpec.value?.stock != null) return Number(selectedSpec.value.stock)
  return Number(product.value?.stock ?? 0)
})

const originalPriceDisplay = computed(() => {
  const original = Number(
    selectedSpec.value?.originalPrice ?? product.value?.originalPrice ?? 0
  )
  const current = Number(currentPrice.value ?? 0)
  if (!original || original <= current) return ''
  return original.toFixed(2)
})

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

function formatDate(value) {
  if (!value) return ''
  try {
    const d = new Date(value)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  } catch (e) {
    return String(value)
  }
}

function parseImages(s) {
  if (!s) return []
  if (Array.isArray(s)) return s
  try {
    const parsed = JSON.parse(s)
    if (Array.isArray(parsed)) return parsed
  } catch (e) {
    return String(s).split(',').filter(Boolean)
  }
  return []
}

async function loadDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const data = await getProductDetail(id)
    if (!data) {
      product.value = null
      return
    }
    product.value = data.product
    images.value = data.images || []
    specs.value = data.specs || []
    favorited.value = Boolean(data.favorite)
    selectedSpec.value = specs.value.find(s => (s.stock ?? 0) > 0) || specs.value[0] || null
    currentImageIndex.value = 0
  } catch (e) {
    product.value = null
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  if (!route.params.id) return
  loadingReviews.value = true
  try {
    const data = await listProductReviews(route.params.id, {
      pageNum: reviewParams.pageNum,
      pageSize: reviewParams.pageSize
    })
    reviews.value = data?.records || []
    reviewTotal.value = Number(data?.total || 0)
  } catch (e) {
    reviews.value = []
    reviewTotal.value = 0
  } finally {
    loadingReviews.value = false
  }
}

function selectSpec(spec) {
  if ((spec.stock ?? 0) <= 0) return
  selectedSpec.value = spec
  if (quantity.value > currentStock.value) {
    quantity.value = Math.max(currentStock.value, 1)
  }
}

function requireLogin() {
  if (userStore.isLoggedIn) return true
  ElMessage.warning('请先登录')
  router.push({ name: 'Login', query: { redirect: route.fullPath } })
  return false
}

async function handleAddCart() {
  if (!requireLogin()) return
  if (currentStock.value <= 0) {
    ElMessage.error('商品已售罄')
    return
  }
  try {
    await addCart({
      productId: product.value.id,
      skuId: selectedSpec.value?.id || product.value.id,
      quantity: quantity.value
    })
    ElMessage.success('已加入购物车')
  } catch (e) {}
}

async function handleBuyNow() {
  if (!requireLogin()) return
  if (currentStock.value <= 0) {
    ElMessage.error('商品已售罄')
    return
  }
  try {
    await addCart({
      productId: product.value.id,
      skuId: selectedSpec.value?.id || product.value.id,
      quantity: quantity.value
    })
    router.push('/order/confirm')
  } catch (e) {}
}

async function handleFavorite() {
  if (!requireLogin()) return
  try {
    if (favorited.value) {
      await cancelFavorite(product.value.id)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      favorited.value = true
      ElMessage.success('已收藏')
    }
  } catch (e) {}
}

watch(
  () => route.params.id,
  () => {
    if (route.name === 'ProductDetail') {
      reviewParams.pageNum = 1
      loadDetail()
      loadReviews()
    }
  }
)

onMounted(() => {
  loadDetail()
  loadReviews()
})
</script>

<style scoped lang="scss">
.product-detail-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.detail-container {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
}

.loading-block {
  background: #fff;
  border-radius: 12px;
  padding: 80px 0;
  text-align: center;
  color: #94a3b8;
}

.main-row {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 32px;
  margin-bottom: 16px;
}

.gallery {
  .main-image {
    width: 100%;
    aspect-ratio: 1;
    background: #f1f5f9;
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }
  }

  .thumbnails {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    overflow-x: auto;
  }

  .thumb {
    flex: 0 0 64px;
    height: 64px;
    background: #f1f5f9;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;

    &.active {
      border-color: #2563eb;
    }

    :deep(.el-image) {
      width: 100%;
      height: 100%;
    }
  }
}

.info {
  .name {
    font-size: 22px;
    margin: 0 0 8px;
    line-height: 1.4;
  }
  .subtitle {
    color: #94a3b8;
    margin: 0 0 16px;
  }
}

.price-block {
  background: #fef2f2;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  align-items: baseline;
  gap: 8px;

  .price-label {
    color: #64748b;
    font-size: 13px;
  }
  .price {
    color: #dc2626;
    font-size: 28px;
    font-weight: 700;
  }
  .original-price {
    color: #94a3b8;
    text-decoration: line-through;
    font-size: 14px;
  }
}

.meta-row {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;

  strong {
    color: #1e293b;
    margin-left: 4px;
  }
  .low-stock {
    color: #f59e0b;
  }
  .out-stock {
    color: #94a3b8;
  }
}

.spec-block {
  margin-bottom: 16px;

  .spec-label {
    color: #64748b;
    font-size: 14px;
    margin-bottom: 8px;
  }

  .spec-options {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .spec-option {
    padding: 8px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    background: #fff;
    transition: all 0.15s;

    &:hover {
      border-color: #2563eb;
      color: #2563eb;
    }
    &.active {
      border-color: #2563eb;
      background: #eff6ff;
      color: #2563eb;
    }
    &.disabled {
      cursor: not-allowed;
      color: #cbd5e1;
      background: #f8fafc;
      &:hover {
        border-color: #e2e8f0;
        color: #cbd5e1;
      }
    }
  }
}

.qty-block {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;

  .qty-label {
    color: #64748b;
    font-size: 14px;
  }
}

.action-row {
  display: flex;
  gap: 12px;
}

.detail-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.detail-html {
  line-height: 1.7;

  :deep(img) {
    max-width: 100%;
  }
}

.params-list {
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid #f1f5f9;
    font-size: 14px;

    span {
      width: 120px;
      color: #94a3b8;
    }
    strong {
      color: #1e293b;
      font-weight: 500;
    }
  }
}

.review-list {
  .review-item {
    padding: 16px 0;
    border-bottom: 1px solid #f1f5f9;
  }
  .review-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
    font-size: 13px;

    .reviewer {
      color: #2563eb;
      font-weight: 500;
    }
    .time {
      color: #94a3b8;
      margin-left: auto;
    }
  }
  .content {
    margin: 0 0 8px;
    color: #334155;
    font-size: 14px;
    line-height: 1.6;
    white-space: pre-wrap;
  }
  .review-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    :deep(.el-image) {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      overflow: hidden;
    }
  }
}
.review-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
.loading-row {
  text-align: center;
  color: #94a3b8;
  padding: 32px 0;
}

@media (max-width: 960px) {
  .main-row {
    grid-template-columns: 1fr;
  }
  .gallery .main-image {
    max-width: 100%;
  }
}
</style>
