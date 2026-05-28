<template>
  <el-container class="admin-shell">
    <!-- Sidebar -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="admin-aside">
      <div class="aside-brand" :class="{ 'is-collapsed': collapsed }">
        <el-icon :size="24" color="#fff"><Goods /></el-icon>
        <span v-if="!collapsed" class="brand-text">电商管理后台</span>
      </div>

      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="#1f2937"
          text-color="#cbd5e1"
          active-text-color="#fbbf24"
          router
          unique-opened
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-sub-menu v-if="item.children?.length" :index="item.path">
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
              >
                <el-icon><component :is="child.icon" /></el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item v-else :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- Top bar -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-button
            text
            class="collapse-btn"
            :icon="collapsed ? Expand : Fold"
            @click="toggleCollapse"
          />
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="crumb in breadcrumbs"
              :key="crumb.path"
            >
              {{ crumb.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip content="返回前台" placement="bottom">
            <el-button text :icon="House" @click="$router.push('/')" />
          </el-tooltip>
          <el-tooltip content="刷新" placement="bottom">
            <el-button text :icon="Refresh" @click="handleRefresh" />
          </el-tooltip>

          <el-dropdown trigger="click" @command="handleCommand">
            <span class="admin-info">
              <el-avatar v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" :size="32" />
              <el-icon v-else :size="28"><UserFilled /></el-icon>
              <span class="admin-name">{{ adminName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="frontend">
                  <el-icon><House /></el-icon> 返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main content -->
      <el-main class="admin-main">
        <RouterView v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  CaretBottom,
  DataLine,
  Document,
  Expand,
  Fold,
  Goods,
  House,
  Picture,
  Refresh,
  Setting,
  ShoppingCart,
  SwitchButton,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const COLLAPSE_KEY = 'ADMIN_SIDEBAR_COLLAPSED'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(localStorage.getItem(COLLAPSE_KEY) === '1')

const menuItems = [
  { path: '/admin', title: '数据看板', icon: DataLine },
  { path: '/admin/users', title: '用户管理', icon: User },
  {
    path: '/admin/products',
    title: '商品管理',
    icon: Goods,
    children: [
      { path: '/admin/products', title: '商品列表', icon: Goods },
      { path: '/admin/categories', title: '分类管理', icon: Document },
      { path: '/admin/reviews', title: '商品评价', icon: Document }
    ]
  },
  { path: '/admin/orders', title: '订单管理', icon: ShoppingCart },
  {
    path: '/admin/content',
    title: '内容管理',
    icon: Picture,
    children: [
      { path: '/admin/banners', title: '轮播图', icon: Picture },
      { path: '/admin/notices', title: '系统公告', icon: Document },
      { path: '/admin/feedback', title: '用户反馈', icon: Document }
    ]
  },
  { path: '/admin/settings', title: '系统设置', icon: Setting }
]

const adminName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')

const activeMenu = computed(() => route.meta?.activeMenu || route.path)

const breadcrumbs = computed(() => {
  const list = []
  for (let i = 1; i < route.matched.length; i++) {
    const r = route.matched[i]
    if (r.meta?.title) {
      list.push({ path: r.path, title: r.meta.title })
    }
  }
  if (route.meta?.title && (!list.length || list[list.length - 1].title !== route.meta.title)) {
    list.push({ path: route.path, title: route.meta.title })
  }
  return list
})

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem(COLLAPSE_KEY, collapsed.value ? '1' : '0')
}

function handleRefresh() {
  router.go(0)
}

watch(
  () => window.innerWidth,
  () => {
    if (window.innerWidth < 768) collapsed.value = true
  }
)

onMounted(() => {
  if (window.innerWidth < 768) collapsed.value = true
  window.addEventListener('resize', handleResize)
})

function handleResize() {
  if (window.innerWidth < 768 && !collapsed.value) {
    collapsed.value = true
  }
}

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确认退出登录吗？', '提示', { type: 'warning' })
    } catch { return }
    userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/admin/login')
  } else if (cmd === 'profile') {
    router.push('/admin/profile')
  } else if (cmd === 'frontend') {
    router.push('/')
  }
}
</script>

<style scoped lang="scss">
.admin-shell {
  height: 100vh;
}

.admin-aside {
  background: #1f2937;
  transition: width 0.25s ease;
  border-right: 1px solid #111827;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.aside-brand {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #111827;
  color: #fff;
  flex-shrink: 0;

  .brand-text {
    font-weight: 600;
    font-size: 16px;
    white-space: nowrap;
  }

  &.is-collapsed {
    padding: 0;
    justify-content: center;
  }
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background: rgba(251, 191, 36, 0.1);
}

.main-container {
  background: #f3f4f6;
}

.admin-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  height: 36px;
  width: 36px;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 6px;
  transition: background 0.2s;

  &:hover {
    background: #f3f4f6;
  }
}

.admin-name {
  font-size: 14px;
  color: #374151;
}

.admin-main {
  padding: 20px;
  overflow: auto;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 768px) {
  .breadcrumb {
    display: none;
  }

  .admin-name {
    display: none;
  }
}
</style>
