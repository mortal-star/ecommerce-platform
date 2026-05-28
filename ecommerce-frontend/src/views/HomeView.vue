<template>
  <section class="page-container">
    <el-card class="page-card hero">
      <h1>Vue3 电商平台前端</h1>
      <p class="text-muted">基于 Vue 3、Vite、Element Plus、Pinia、Vue Router 和 Axios 初始化完成。</p>
      <el-space>
        <el-button type="primary" @click="$router.push('/products')">浏览商品</el-button>
        <el-button @click="$router.push('/login')">立即登录</el-button>
      </el-space>
    </el-card>
  </section>
</template>

<style scoped lang="scss">
.hero {
  padding: 32px;

  h1 {
    margin: 0 0 12px;
    font-size: 36px;
  }

  p {
    margin-bottom: 24px;
    font-size: 16px;
  }
}
</style>
