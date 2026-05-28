<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <h3>{{ companyName }}</h3>
        <p v-if="slogan" class="slogan">{{ slogan }}</p>
        <slot name="brand-extra" />
      </div>

      <template v-if="sections.length">
        <div v-for="section in sections" :key="section.title" class="footer-section">
          <h4>{{ section.title }}</h4>
          <ul>
            <li v-for="item in section.items" :key="item.label || item.name || item.url">
              <a
                v-if="item.url"
                :href="item.url"
                :target="item.target || '_blank'"
                rel="noreferrer"
              >{{ item.label || item.name }}</a>
              <span v-else>{{ item.label || item.name }}</span>
            </li>
          </ul>
        </div>
      </template>

      <template v-else>
        <div class="footer-section">
          <h4>联系我们</h4>
          <p>电话：{{ phone }}</p>
          <p>邮箱：{{ email }}</p>
          <slot name="contact-extra" />
        </div>
        <div class="footer-section">
          <h4>友情链接</h4>
          <ul>
            <li v-for="link in links" :key="link.name">
              <a :href="link.url" target="_blank" rel="noreferrer">{{ link.name }}</a>
            </li>
          </ul>
        </div>
      </template>

      <slot name="extra" />
    </div>

    <div class="footer-bottom">
      <span>{{ copyright }}</span>
      <a v-if="icp" :href="icpUrl" target="_blank" rel="noreferrer">{{ icp }}</a>
    </div>
  </footer>
</template>

<script setup>
defineProps({
  companyName: { type: String, default: '电商平台' },
  slogan: { type: String, default: '一站式电商服务平台' },
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
  },
  sections: { type: Array, default: () => [] },
  icp: { type: String, default: '' },
  icpUrl: { type: String, default: 'https://beian.miit.gov.cn/' }
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
  grid-template-columns: 2fr repeat(auto-fit, minmax(160px, 1fr));
  gap: 32px;
  width: min(1200px, calc(100% - 32px));
  margin: 0 auto;
  padding: 42px 0;
}

.footer-brand {
  h3 {
    margin: 0 0 12px;
    color: #fff;
    font-size: 20px;
  }

  .slogan {
    margin: 0;
    color: #94a3b8;
    font-size: 14px;
  }
}

.footer-section {
  h4 {
    margin: 0 0 12px;
    color: #fff;
    font-size: 15px;
  }

  ul {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin: 0;
    padding: 0;
    list-style: none;
  }

  a {
    color: #cbd5e1;
    transition: color 0.2s ease;

    &:hover {
      color: #60a5fa;
    }
  }

  p {
    margin: 6px 0;
  }
}

.footer-bottom {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  padding: 16px 0;
  color: #64748b;
  font-size: 13px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);

  a {
    color: #94a3b8;
  }
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr;
    padding: 32px 0;
  }
}
</style>
