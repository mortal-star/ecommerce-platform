<template>
  <el-config-provider>
    <div class="app-shell">
      <header class="app-header">
        <RouterLink class="brand" to="/">电商平台</RouterLink>
        <nav class="nav-links">
          <RouterLink to="/products">商品</RouterLink>
          <RouterLink to="/cart">购物车</RouterLink>
          <RouterLink to="/orders">订单</RouterLink>
          <RouterLink to="/admin">后台</RouterLink>
        </nav>
      </header>
      <main>
        <RouterView />
      </main>
    </div>
  </el-config-provider>
</template>

<style scoped lang="scss">
.app-shell {
  min-height: 100vh;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  color: #2563eb;
  font-size: 22px;
  font-weight: 800;
}

.nav-links {
  display: flex;
  gap: 24px;
  color: #475569;
  font-size: 15px;
}

.router-link-active {
  color: #2563eb;
  font-weight: 700;
}
</style>
