<template>
  <section class="page-container">
    <el-card class="page-card">
      <template #header>商品详情</template>
      <Empty title="商品详情页面" :description="`当前商品ID：${$route.params.id}`" />
    </el-card>
  </section>
</template>

<script setup>
import Empty from '@/components/Empty.vue'
</script>
