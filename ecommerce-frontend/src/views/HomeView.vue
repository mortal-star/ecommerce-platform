<template>
  <section class="home-view">
    <div class="home-container">
      <!-- 顶部：分类侧栏 + 轮播图 -->
      <div class="hero-row">
        <aside class="category-aside">
          <h3 class="aside-title">全部分类</h3>
          <ul class="category-list">
            <li
              v-for="cat in topCategories"
              :key="cat.id"
              @click="goCategory(cat)"
            >
              <span class="cat-name">{{ cat.name }}</span>
              <el-icon class="cat-arrow"><ArrowRight /></el-icon>
            </li>
            <li v-if="!topCategories.length" class="placeholder">暂无分类</li>
          </ul>
        </aside>

        <div class="banner-wrap">
          <el-carousel
            v-if="banners.length"
            :interval="4000"
            height="380px"
            arrow="hover"
          >
            <el-carousel-item
              v-for="b in banners"
              :key="b.id"
              @click="onBannerClick(b)"
            >
              <div
                class="banner-slide"
                :style="{ backgroundImage: `url(${b.imageUrl || fallbackBanner})` }"
              >
                <div class="banner-text">
                  <h2>{{ b.title }}</h2>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
          <div v-else class="banner-empty">
            <Empty size="large" description="暂无轮播图" />
          </div>

          <div class="hot-search-row">
            <span class="hot-label">热搜：</span>
            <el-tag
              v-for="kw in hotSearchKeywords"
              :key="kw"
              class="hot-tag"
              type="info"
              size="small"
              @click="goSearch(kw)"
            >
              {{ kw }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 热门商品 -->
      <section class="product-section">
        <div class="section-head">
          <div>
            <h2>热门商品</h2>
            <span class="subtitle">销量榜推荐</span>
          </div>
          <a class="more" @click="$router.push('/products?sortBy=sales&sortOrder=desc')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </a>
        </div>
        <div v-if="loadingHot" class="loading-row">加载中...</div>
        <div v-else-if="hotProducts.length" class="product-grid">
          <ProductCard
            v-for="p in hotProducts"
            :key="p.id"
            :product="p"
            @add-cart="handleAddCart"
            @favorite="handleFavorite"
          />
        </div>
        <Empty v-else size="default" description="暂无热门商品" />
      </section>

      <!-- 新品上市 -->
      <section class="product-section">
        <div class="section-head">
          <div>
            <h2>新品上市</h2>
            <span class="subtitle">最新到货</span>
          </div>
          <a class="more" @click="$router.push('/products?sortBy=createTime&sortOrder=desc')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </a>
        </div>
        <div v-if="loadingNew" class="loading-row">加载中...</div>
        <div v-else-if="newProducts.length" class="product-grid">
          <ProductCard
            v-for="p in newProducts"
            :key="p.id"
            :product="p"
            @add-cart="handleAddCart"
            @favorite="handleFavorite"
          />
        </div>
        <Empty v-else size="default" description="暂无新品" />
      </section>

      <!-- 限时促销 -->
      <section class="product-section">
        <div class="section-head">
          <div>
            <h2>限时促销</h2>
            <span class="subtitle">折扣专区</span>
          </div>
          <a class="more" @click="$router.push('/products')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </a>
        </div>
        <div v-if="loadingPromo" class="loading-row">加载中...</div>
        <div v-else-if="promoProducts.length" class="product-grid">
          <ProductCard
            v-for="p in promoProducts"
            :key="p.id"
            :product="p"
            @add-cart="handleAddCart"
            @favorite="handleFavorite"
          />
        </div>
        <Empty v-else size="default" description="暂无促销商品" />
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { getBanners, getCategories, getProducts, addCart, addFavorite } from '@/api'
import Empty from '@/components/Empty.vue'
import ProductCard from '@/components/ProductCard.vue'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const fallbackBanner =
  'https://placehold.co/1200x380/2563eb/ffffff?text=Welcome+to+ECommerce'

const banners = ref([])
const topCategories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const promoProducts = ref([])
const loadingHot = ref(false)
const loadingNew = ref(false)
const loadingPromo = ref(false)

const hotSearchKeywords = ref([
  '手机', '笔记本', '运动鞋', '耳机', '智能手表', '相机', '羽绒服', '玩具'
])

async function loadBanners() {
  try {
    const data = await getBanners({ status: 1 })
    banners.value = data || []
  } catch (e) {
    banners.value = []
  }
}

async function loadCategories() {
  try {
    const data = await getCategories({ parentId: 0, status: 1 })
    topCategories.value = (data || []).slice(0, 10)
  } catch (e) {
    topCategories.value = []
  }
}

async function loadProducts(target, params, loadingRef, filterPromo = false) {
  loadingRef.value = true
  try {
    const data = await getProducts(params)
    let records = data?.records || []
    if (filterPromo) {
      records = records.filter(p => Number(p.originalPrice || 0) > Number(p.price || 0))
    }
    target.value = records
  } catch (e) {
    target.value = []
  } finally {
    loadingRef.value = false
  }
}

function goCategory(cat) {
  router.push({ path: '/products', query: { categoryId: cat.id } })
}

function goSearch(keyword) {
  router.push({ path: '/products', query: { keyword } })
}

function onBannerClick(banner) {
  if (banner.linkUrl) {
    if (/^https?:/.test(banner.linkUrl)) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

async function handleAddCart(product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: '/' } })
    return
  }
  try {
    await addCart({
      productId: product.id,
      skuId: product.defaultSkuId || product.id,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (e) {
    // 拦截器已提示
  }
}

async function handleFavorite(product) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: '/' } })
    return
  }
  try {
    await addFavorite(product.id)
    ElMessage.success('已收藏')
  } catch (e) {}
}

onMounted(() => {
  loadBanners()
  loadCategories()
  loadProducts(hotProducts, { sortBy: 'sales', sortOrder: 'desc', pageSize: 8 }, loadingHot)
  loadProducts(newProducts, { sortBy: 'createTime', sortOrder: 'desc', pageSize: 8 }, loadingNew)
  loadProducts(promoProducts, { sortBy: 'sort', sortOrder: 'asc', pageSize: 12 }, loadingPromo, true)
})
</script>

<style scoped lang="scss">
.home-view {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.home-container {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
}

.hero-row {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.category-aside {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  min-height: 380px;

  .aside-title {
    margin: 4px 8px 8px;
    font-size: 16px;
  }
}
.category-list {
  list-style: none;
  margin: 0;
  padding: 0;

  li {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 8px;
    cursor: pointer;
    border-radius: 8px;
    font-size: 14px;
    transition: background-color 0.2s;

    &:hover {
      background: #eff6ff;
      color: #2563eb;
    }
    &.placeholder {
      color: #94a3b8;
      cursor: default;
      &:hover { background: transparent; color: #94a3b8; }
    }
  }
  .cat-arrow {
    color: #94a3b8;
    font-size: 14px;
  }
}

.banner-wrap {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.banner-empty {
  height: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-slide {
  width: 100%;
  height: 100%;
  background-position: center;
  background-size: cover;
  cursor: pointer;
  position: relative;
}
.banner-text {
  position: absolute;
  bottom: 24px;
  left: 32px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0,0,0,0.4);
  h2 {
    margin: 0;
    font-size: 28px;
  }
}

.hot-search-row {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  border-top: 1px solid #f1f5f9;
  font-size: 13px;

  .hot-label {
    color: #64748b;
  }
  .hot-tag {
    cursor: pointer;
  }
}

.product-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    margin: 0;
    font-size: 20px;
    display: inline-block;
  }
  .subtitle {
    color: #94a3b8;
    font-size: 13px;
    margin-left: 8px;
  }
  .more {
    color: #2563eb;
    cursor: pointer;
    font-size: 13px;
    display: inline-flex;
    align-items: center;
    gap: 2px;
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
  padding: 32px 0;
}

@media (max-width: 960px) {
  .hero-row {
    grid-template-columns: 1fr;
  }
  .category-aside {
    min-height: auto;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
