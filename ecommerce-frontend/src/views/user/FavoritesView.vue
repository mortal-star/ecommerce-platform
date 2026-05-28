<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <div v-if="loading" class="loading-row">加载中...</div>
    <div v-else-if="favorites.length" class="product-grid">
      <div v-for="fav in favorites" :key="fav.favorite.id" class="fav-cell">
        <ProductCard
          v-if="fav.product"
          :product="fav.product"
          :show-favorite="false"
          @add-cart="handleAddCart(fav)"
        />
        <div v-else class="fallback-card">商品已下架</div>
        <el-button
          class="remove-btn"
          type="danger"
          link
          @click="handleRemove(fav)"
        >取消收藏</el-button>
      </div>
    </div>
    <Empty v-else size="large" description="还没有收藏任何商品">
      <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
    </Empty>

    <div v-if="!loading && total > 0" class="pagination-wrap">
      <Pagination
        v-model:current-page="filters.pageNum"
        v-model:page-size="filters.pageSize"
        :total="total"
        @change="loadFavorites"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFavorites, cancelFavorite, addCart } from '@/api'
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination.vue'
import Empty from '@/components/Empty.vue'

const favorites = ref([])
const total = ref(0)
const loading = ref(false)
const filters = reactive({ pageNum: 1, pageSize: 12 })

async function loadFavorites() {
  loading.value = true
  try {
    const data = await listFavorites({
      pageNum: filters.pageNum,
      pageSize: filters.pageSize
    })
    favorites.value = data?.records || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    favorites.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleRemove(fav) {
  try {
    await ElMessageBox.confirm('取消该商品的收藏？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await cancelFavorite(fav.product.id)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (e) {}
}

async function handleAddCart(fav) {
  if (!fav.product) return
  try {
    await addCart({
      productId: fav.product.id,
      skuId: fav.product.id,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (e) {}
}

onMounted(loadFavorites)
</script>

<style scoped lang="scss">
.favorites-page {
  .page-title {
    margin: 0 0 20px;
    font-size: 20px;
  }
}

.loading-row {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.fav-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .remove-btn {
    align-self: flex-end;
  }
}

.fallback-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 60px 0;
  text-align: center;
  color: #94a3b8;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
