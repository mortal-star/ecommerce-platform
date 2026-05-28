<template>
  <section class="placeholder">
    <el-card shadow="never">
      <div class="content">
        <el-icon :size="64" color="#94a3b8"><Tools /></el-icon>
        <h2>{{ title }}</h2>
        <p>该模块将在后续迭代中实现，敬请期待。</p>
        <el-button type="primary" plain @click="$router.push('/admin')">返回数据看板</el-button>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Tools } from '@element-plus/icons-vue'

const route = useRoute()
const title = computed(() => route.meta?.title || '功能开发中')
</script>

<style scoped lang="scss">
.placeholder {
  padding: 24px 0;
}

.content {
  text-align: center;
  padding: 80px 20px;

  h2 {
    margin: 16px 0 8px;
    color: #1f2937;
  }

  p {
    color: #6b7280;
    margin-bottom: 24px;
  }
}
</style>
