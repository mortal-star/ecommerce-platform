<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div>
        <h3>{{ companyName }}</h3>
        <p>{{ copyright }}</p>
      </div>
      <div class="footer-section">
        <h4>联系我们</h4>
        <p>电话：{{ phone }}</p>
        <p>邮箱：{{ email }}</p>
      </div>
      <div class="footer-section">
        <h4>友情链接</h4>
        <div class="friend-links">
          <a v-for="link in links" :key="link.name" :href="link.url" target="_blank" rel="noreferrer">
            {{ link.name }}
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
defineProps({
  companyName: { type: String, default: '电商平台' },
  copyright: { type: String, default: 'Copyright © 2026 Ecommerce Platform. All Rights Reserved.' },
  phone: { type: String, default: '400-888-8888' },
  email: { type: String, default: 'support@example.com' },
  links: {
    type: Array,
    default: () => [
      { name: 'Element Plus', url: 'https://element-plus.org' },
      { name: 'Vue', url: 'https://vuejs.org' },
      { name: 'Vite', url: 'https://vitejs.dev' }
    ]
  }
})
</script>

<style scoped lang="scss">
.app-footer {
  margin-top: 48px;
  color: #cbd5e1;
  background: #0f172a;
}

.footer-inner {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 32px;
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
  padding: 42px 0;
}

h3,
h4 {
  margin: 0 0 12px;
  color: #fff;
}

p {
  margin: 6px 0;
}

.friend-links {
  display: flex;
  flex-direction: column;
  gap: 8px;

  a:hover {
    color: #60a5fa;
  }
}
</style>
