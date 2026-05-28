<template>
  <section class="page-container login-page">
    <el-card class="page-card login-card">
      <h2>账号登录</h2>
      <el-form label-position="top">
        <el-form-item label="账号">
          <el-input placeholder="用户名 / 邮箱 / 手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="login-button">登录</el-button>
      </el-form>
    </el-card>
  </section>
</template>

<style scoped lang="scss">
.login-page {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 420px;

  h2 {
    margin-top: 0;
  }
}

.login-button {
  width: 100%;
}
</style>
