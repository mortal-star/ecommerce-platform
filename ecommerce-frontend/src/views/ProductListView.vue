<template>
  <section class="product-list-view">
    <div class="list-container">
      <!-- 顶部筛选条 -->
      <div class="filter-bar">
        <div class="filter-row">
          <span class="label">分类：</span>
          <a
            class="cat-link"
            :class="{ active: !filters.categoryId }"
            @click="setCategory(null)"
          >全部</a>
          <a
            v-for="cat in categories"
            :key="cat.id"
            class="cat-link"
            :class="{ active: String(filters.categoryId) === String(cat.id) }"
            @click="setCategory(cat.id)"
          >{{ cat.name }}</a>
        </div>

        <div class="filter-row sort-row">
          <span class="label">排序：</span>
          <a
            v-for="opt in sortOptions"
            :key="opt.key"
            class="sort-link"
            :class="{ active: sortKey === opt.key }"
            @click="setSort(opt)"
          >
            {{ opt.label }}
            <el-icon v-if="sortKey === opt.key && opt.toggleable">
              <CaretTop v-if="filters.sortOrder === 'asc'" />
              <CaretBottom v-else />
            </el-icon>
          </a>

          <div class="price-range">
            <span class="label">价格区间：</span>
            <el-input
              v-model="priceMin"
              size="small"
              type="number"
              placeholder="最低"
              class="price-input"
            />
            <span class="dash">-</span>
            <el-input
              v-model="priceMax"
              size="small"
              type="number"
              placeholder="最高"
              class="price-input"
            />
            <el-button size="small" type="primary" @click="applyPriceFilter">确定</el-button>
            <el-button size="small" @click="clearPriceFilter">清除</el-button>
          </div>
        </div>

        <div v-if="filters.keyword" class="filter-row keyword-row">
          <el-tag closable @close="clearKeyword">
            搜索：{{ filters.keyword }}
          </el-tag>
        </div>
      </div>

      <!-- 商品网格 -->
      <div class="result-info" v-if="!loading">
        共 <strong>{{ total }}</strong> 件商品
      </div>

      <div v-if="loading" class="loading-row">加载中...</div>
      <div v-else-if="products.length" class="product-grid">
        <ProductCard
          v-for="p in products"
          :key="p.id"
          :product="p"
          @add-cart="handleAddCart"
          @favorite="handleFavorite"
        />
      </div>
      <Empty v-else size="large" description="没有找到符合条件的商品" />

      <!-- 分页 -->
      <div v-if="!loading && total > 0" class="pagination-wrap">
        <Pagination
          v-model:current-page="filters.pageNum"
          v-model:page-size="filters.pageSize"
          :total="total"
          @change="onPageChange"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { getCategories, getProducts, addCart, addFavorite } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination.vue'
import Empty from '@/components/Empty.vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sortOptions = [
  { key: 'default', label: '综合', sortBy: 'sort', sortOrder: 'asc', toggleable: false },
  { key: 'sales', label: '销量', sortBy: 'sales', sortOrder: 'desc', toggleable: false },
  { key: 'new', label: '新品', sortBy: 'createTime', sortOrder: 'desc', toggleable: false },
  { key: 'price', label: '价格', sortBy: 'price', sortOrder: 'asc', toggleable: true }
]

const filters = reactive({
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: 'sort',
  sortOrder: 'asc',
  pageNum: 1,
  pageSize: 12
})

const sortKey = ref('default')
const priceMin = ref('')
const priceMax = ref('')
const products = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])

function readQuery() {
  const q = route.query
  filters.keyword = q.keyword || ''
  filters.categoryId = q.categoryId ? Number(q.categoryId) : null
  filters.sortBy = q.sortBy || 'sort'
  filters.sortOrder = q.sortOrder || 'asc'
  filters.minPrice = q.minPrice ? Number(q.minPrice) : null
  filters.maxPrice = q.maxPrice ? Number(q.maxPrice) : null
  filters.pageNum = q.pageNum ? Number(q.pageNum) : 1
  filters.pageSize = q.pageSize ? Number(q.pageSize) : 12

  priceMin.value = filters.minPrice != null ? String(filters.minPrice) : ''
  priceMax.value = filters.maxPrice != null ? String(filters.maxPrice) : ''

  const found = sortOptions.find(
    o => o.sortBy === filters.sortBy && (o.toggleable || o.sortOrder === filters.sortOrder)
  )
  sortKey.value = found ? found.key : 'default'
}

function syncQuery() {
  const query = {}
  if (filters.keyword) query.keyword = filters.keyword
  if (filters.categoryId) query.categoryId = filters.categoryId
  if (filters.minPrice != null) query.minPrice = filters.minPrice
  if (filters.maxPrice != null) query.maxPrice = filters.maxPrice
  if (filters.sortBy !== 'sort') query.sortBy = filters.sortBy
  if (filters.sortOrder !== 'asc') query.sortOrder = filters.sortOrder
  if (filters.pageNum !== 1) query.pageNum = filters.pageNum
  if (filters.pageSize !== 12) query.pageSize = filters.pageSize
  router.replace({ path: '/products', query })
}

async function loadCategories() {
  try {
    const data = await getCategories({ parentId: 0, status: 1 })
    categories.value = data || []
  } catch (e) {
    categories.value = []
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = {
      pageNum: filters.pageNum,
      pageSize: filters.pageSize,
      sortBy: filters.sortBy,
      sortOrder: filters.sortOrder
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.minPrice != null) params.minPrice = filters.minPrice
    if (filters.maxPrice != null) params.maxPrice = filters.maxPrice

    const data = await getProducts(params)
    products.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function setCategory(id) {
  filters.categoryId = id
  filters.pageNum = 1
  syncQuery()
  loadProducts()
}

function setSort(opt) {
  if (sortKey.value === opt.key && opt.toggleable) {
    filters.sortOrder = filters.sortOrder === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = opt.key
    filters.sortBy = opt.sortBy
    filters.sortOrder = opt.sortOrder
  }
  filters.pageNum = 1
  syncQuery()
  loadProducts()
}

function applyPriceFilter() {
  const minNum = priceMin.value === '' ? null : Number(priceMin.value)
  const maxNum = priceMax.value === '' ? null : Number(priceMax.value)
  if (minNum != null && maxNum != null && minNum > maxNum) {
    ElMessage.warning('最低价不能高于最高价')
    return
  }
  filters.minPrice = minNum
  filters.maxPrice = maxNum
  filters.pageNum = 1
  syncQuery()
  loadProducts()
}

function clearPriceFilter() {
  priceMin.value = ''
  priceMax.value = ''
  filters.minPrice = null
  filters.maxPrice = null
  filters.pageNum = 1
  syncQuery()
  loadProducts()
}

function clearKeyword() {
  filters.keyword = ''
  filters.pageNum = 1
  syncQuery()
  loadProducts()
}

function onPageChange() {
  syncQuery()
  loadProducts()
}

async function handleAddCart(product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await addCart({
      productId: product.id,
      skuId: product.defaultSkuId || product.id,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (e) {}
}

async function handleFavorite(product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await addFavorite(product.id)
    ElMessage.success('已收藏')
  } catch (e) {}
}

watch(
  () => route.query,
  () => {
    if (route.name !== 'Products') return
    readQuery()
    loadProducts()
  }
)

onMounted(() => {
  readQuery()
  loadCategories()
  loadProducts()
})
</script>

<style scoped lang="scss">
.product-list-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.list-container {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
}

.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px 0;

  & + .filter-row {
    border-top: 1px dashed #f1f5f9;
  }

  .label {
    color: #64748b;
    font-size: 13px;
    flex-shrink: 0;
  }
}

.cat-link, .sort-link {
  padding: 4px 10px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: background-color 0.15s;

  &:hover {
    background: #eff6ff;
    color: #2563eb;
  }

  &.active {
    background: #2563eb;
    color: #fff;
  }
}

.sort-row {
  gap: 12px;
}

.price-range {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 16px;

  .price-input {
    width: 90px;
  }
  .dash {
    color: #94a3b8;
  }
}

.keyword-row {
  padding-top: 8px;
}

.result-info {
  font-size: 14px;
  color: #64748b;
  margin: 0 4px 12px;

  strong {
    color: #2563eb;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.loading-row {
  text-align: center;
  color: #94a3b8;
  padding: 60px 0;
  background: #fff;
  border-radius: 12px;
}

.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 960px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .price-range {
    margin-left: 0;
    margin-top: 8px;
  }
}
@media (max-width: 560px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
