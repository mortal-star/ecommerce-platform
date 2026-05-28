<template>
  <header class="app-header">
    <div class="header-main">
      <RouterLink class="logo" to="/">
        <span v-if="logo" class="logo-image">
          <img :src="logo" :alt="title" />
        </span>
        <span v-else class="logo-mark">{{ title.slice(0, 1).toUpperCase() }}</span>
        <span>{{ title }}</span>
      </RouterLink>

      <el-input
        v-if="showSearch"
        v-model="keyword"
        class="search-input"
        :placeholder="searchPlaceholder"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" :loading="searching" @click="handleSearch" />
        </template>
      </el-input>

      <div class="header-actions">
        <el-dropdown v-if="userStore.isLoggedIn" trigger="click" @command="handleUserCommand">
          <el-button text>
            <el-avatar v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :size="28" />
            <el-icon v-else><User /></el-icon>
            <span class="username">{{ username }}</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item command="addresses">收货地址</el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdmin" divided command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else type="primary" plain @click="goLogin">登录 / 注册</el-button>

        <el-badge :value="cartCount" :hidden="cartCount <= 0" :max="99">
          <el-button :icon="ShoppingCart" circle @click="goCart" />
        </el-badge>
      </div>
    </div>

    <nav v-if="categories.length" class="category-nav">
      <a
        v-for="item in categories"
        :key="item.id ?? item.name"
        :class="{ 'is-active': isActiveCategory(item) }"
        @click="handleCategoryClick(item)"
      >
        {{ item.name }}
      </a>
    </nav>
  </header>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ShoppingCart, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const props = defineProps({
  title: { type: String, default: '电商平台' },
  logo: { type: String, default: '' },
  searchPlaceholder: { type: String, default: '搜索商品名称、关键词' },
  showSearch: { type: Boolean, default: true },
  searching: { type: Boolean, default: false },
  categories: {
    type: Array,
    default: () => [
      { id: 0, name: '全部商品' },
      { id: 1, name: '手机数码' },
      { id: 2, name: '电脑办公' },
      { id: 3, name: '服饰鞋包' },
      { id: 4, name: '家居生活' }
    ]
  },
  cartCount: { type: Number, default: 0 }
})

const emit = defineEmits(['search', 'category-click', 'logout'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const keyword = ref('')

watch(
  () => route.query.keyword,
  value => {
    if (typeof value === 'string') keyword.value = value
  },
  { immediate: true }
)

const username = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '用户中心'
})

function handleSearch() {
  const value = keyword.value.trim()
  emit('search', value)
  router.push({ path: '/products', query: value ? { keyword: value } : {} })
}

function isActiveCategory(item) {
  if (route.path !== '/products' && route.path !== '/') return false
  const queryId = route.query.categoryId
  const itemId = item.id ?? ''
  if (!queryId && !itemId) return true
  return String(queryId || '') === String(itemId || '')
}

function handleCategoryClick(item) {
  emit('category-click', item)
  const query = item.id ? { categoryId: item.id } : {}
  router.push({ path: '/products', query })
}

function handleUserCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    emit('logout')
    router.push('/login')
    return
  }
  const routes = {
    profile: '/user/profile',
    orders: '/orders',
    favorites: '/user/favorites',
    addresses: '/user/addresses',
    admin: '/admin'
  }
  if (routes[command]) router.push(routes[command])
}

function goLogin() {
  router.push({ name: 'Login', query: { redirect: route.fullPath } })
}

function goCart() {
  router.push('/cart')
}
</script>

<style scoped lang="scss">
.app-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid #e5e7eb;
  backdrop-filter: blur(14px);
}

.header-main {
  display: flex;
  align-items: center;
  gap: 28px;
  width: min(1200px, calc(100% - 32px));
  height: 70px;
  margin: 0 auto;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #1d4ed8;
  font-size: 22px;
  font-weight: 800;
  white-space: nowrap;
}

.logo-mark {
  display: grid;
  width: 34px;
  height: 34px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  border-radius: 10px;
  place-items: center;
}

.logo-image img {
  display: block;
  width: 34px;
  height: 34px;
  object-fit: cover;
  border-radius: 10px;
}

.search-input {
  flex: 1;
  max-width: 560px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}

.username {
  margin-left: 6px;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  width: min(1200px, calc(100% - 32px));
  min-height: 42px;
  margin: 0 auto;
  padding: 4px 0;
  color: #475569;
  font-size: 14px;

  a {
    display: inline-flex;
    align-items: center;
    padding: 4px 0;
    border-bottom: 2px solid transparent;
    cursor: pointer;
    transition: color 0.2s ease, border-color 0.2s ease;
  }

  a.is-active,
  a:hover {
    color: #2563eb;
    border-bottom-color: #2563eb;
  }
}

@media (max-width: 768px) {
  .header-main {
    flex-wrap: wrap;
    height: auto;
    padding: 12px 0;
    gap: 12px;
  }

  .search-input {
    order: 3;
    width: 100%;
    max-width: none;
  }

  .header-actions {
    margin-left: auto;
  }
}
</style>
