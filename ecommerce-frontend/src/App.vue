<template>
  <el-config-provider>
    <RouterView v-if="isBlankLayout" />
    <div v-else class="app-shell">
      <AppHeader :cart-count="cartCount" />
      <main class="app-main">
        <RouterView />
      </main>
      <AppFooter
        :icp="icp"
        :sections="footerSections"
      />
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()
const isBlankLayout = computed(() => {
  const layout = route.meta?.layout
  return layout === 'admin' || layout === 'blank'
})

const cartCount = ref(0)
const icp = ''

const footerSections = [
  {
    title: '帮助中心',
    items: [
      { label: '购物指南', url: '#' },
      { label: '订单查询', url: '#' },
      { label: '配送方式', url: '#' },
      { label: '售后服务', url: '#' }
    ]
  },
  {
    title: '关于我们',
    items: [
      { label: '公司介绍', url: '#' },
      { label: '联系我们', url: '#' },
      { label: '加入我们', url: '#' }
    ]
  },
  {
    title: '友情链接',
    items: [
      { label: 'Element Plus', url: 'https://element-plus.org' },
      { label: 'Vue', url: 'https://vuejs.org' },
      { label: 'Vite', url: 'https://vitejs.dev' }
    ]
  }
]
</script>

<style scoped lang="scss">
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-main {
  flex: 1;
}
</style>
