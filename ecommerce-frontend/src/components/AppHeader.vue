<template>
  <header class="app-header">
    <div class="header-main">
      <RouterLink class="logo" to="/">
        <span class="logo-mark">E</span>
        <span>{{ title }}</span>
      </RouterLink>

      <el-input
        v-model="keyword"
        class="search-input"
        :placeholder="searchPlaceholder"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>

      <div class="header-actions">
        <el-dropdown v-if="userStore.isLoggedIn" trigger="click" @command="handleUserCommand">
          <el-button text>
            <el-icon><User /></el-icon>
            <span>{{ username }}</span>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else type="primary" plain @click="router.push('/login')">登录 / 注册</el-button>

        <el-badge :value="cartCount" :hidden="cartCount <= 0">
          <el-button :icon="ShoppingCart" circle @click="router.push('/cart')" />
        </el-badge>
      </div>
    </div>

    <nav class="category-nav">
      <RouterLink v-for="item in categories" :key="item.id || item.name" :to="categoryPath(item)">
        {{ item.name }}
      </RouterLink>
    </nav>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ShoppingCart, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const props = defineProps({
  title: { type: String, default: '电商平台' },
  searchPlaceholder: { type: String, default: '搜索商品名称、关键词' },
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

const emit = defineEmits(['search', 'category-click'])

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

const username = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '用户中心')

function handleSearch() {
  const value = keyword.value.trim()
  emit('search', value)
  router.push({ path: '/products', query: value ? { keyword: value } : {} })
}

function categoryPath(item) {
  if (!item.id) return '/products'
  return { path: '/products', query: { categoryId: item.id } }
}

function handleUserCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'orders') {
    router.push('/orders')
  } else if (command === 'admin') {
    router.push('/admin')
  } else {
    router.push('/user/profile')
  }
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

.category-nav {
  display: flex;
  gap: 28px;
  width: min(1200px, calc(100% - 32px));
  height: 42px;
  margin: 0 auto;
  color: #475569;
  font-size: 14px;

  a {
    display: inline-flex;
    align-items: center;
    border-bottom: 2px solid transparent;
  }

  .router-link-active,
  a:hover {
    color: #2563eb;
    border-bottom-color: #2563eb;
  }
}
</style>
