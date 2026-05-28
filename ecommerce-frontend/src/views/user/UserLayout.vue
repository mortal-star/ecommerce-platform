<template>
  <section class="user-layout">
    <div class="user-container">
      <aside class="user-aside">
        <div class="user-card">
          <el-avatar :size="64" :src="avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="user-meta">
            <p class="nickname">{{ nickname }}</p>
            <p class="username">{{ username }}</p>
          </div>
        </div>
        <el-menu
          :default-active="route.path"
          router
          class="user-menu"
        >
          <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <span>{{ m.label }}</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="user-main">
        <RouterView />
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  User,
  List,
  Star,
  Location,
  Lock,
  Edit
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const userStore = useUserStore()

const avatar = computed(() => userStore.userInfo?.avatar || '')
const nickname = computed(() => userStore.userInfo?.nickname || '未设置昵称')
const username = computed(() => userStore.userInfo?.username || '')

const menus = [
  { path: '/user/profile', label: '个人中心', icon: User },
  { path: '/orders', label: '我的订单', icon: List },
  { path: '/user/favorites', label: '我的收藏', icon: Star },
  { path: '/user/addresses', label: '收货地址', icon: Location },
  { path: '/user/account', label: '账号设置', icon: Edit },
  { path: '/user/password', label: '修改密码', icon: Lock }
]
</script>

<style scoped lang="scss">
.user-layout {
  background: #f5f7fa;
  padding: 16px 0 40px;
  min-height: 600px;
}
.user-container {
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
}

.user-aside {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  align-self: start;
}

.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 12px;
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
  color: #fff;
  text-align: center;

  .user-meta {
    margin-top: 12px;
  }
  .nickname {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
  .username {
    margin: 4px 0 0;
    opacity: 0.8;
    font-size: 12px;
  }
}

.user-menu {
  border-right: none;
}

.user-main {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  min-height: 600px;
}

@media (max-width: 768px) {
  .user-container {
    grid-template-columns: 1fr;
  }
  .user-aside {
    margin-bottom: 16px;
  }
}
</style>
