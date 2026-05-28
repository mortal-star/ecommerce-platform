<template>
  <div class="admin-login">
    <div class="login-bg">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <div class="login-panel">
      <div class="brand-side">
        <div class="brand-logo">
          <el-icon :size="48"><Goods /></el-icon>
        </div>
        <h1 class="brand-title">电商管理后台</h1>
        <p class="brand-desc">高效管理商品、订单与用户</p>
        <ul class="brand-features">
          <li><el-icon><Check /></el-icon> 多维度数据看板</li>
          <li><el-icon><Check /></el-icon> 商品与订单全流程管理</li>
          <li><el-icon><Check /></el-icon> 灵活的权限分配</li>
        </ul>
      </div>

      <div class="form-side">
        <h2 class="form-title">管理员登录</h2>
        <p class="form-subtitle">请使用管理员账号登录后台</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="account">
            <el-input
              v-model="form.account"
              :prefix-icon="User"
              placeholder="管理员账号"
              autocomplete="username"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="密码"
              show-password
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <div class="form-row">
              <el-checkbox v-model="form.remember">记住账号</el-checkbox>
              <RouterLink class="back-link" to="/">返回前台</RouterLink>
            </div>
          </el-form-item>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="tips">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>
              <span>默认账号：<b>admin</b> / 默认密码：<b>admin123</b>（仅供本地测试）</span>
            </template>
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Check, Goods } from '@element-plus/icons-vue'
import { adminLogin } from '@/api'
import { useUserStore } from '@/stores'

const REMEMBER_KEY = 'ADMIN_REMEMBERED_ACCOUNT'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = ref({
  account: '',
  password: '',
  remember: true
})

const rules = {
  account: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    form.value.account = remembered
    form.value.remember = true
  }
})

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }

  loading.value = true
  try {
    const data = await adminLogin({
      account: form.value.account,
      password: form.value.password
    })

    if (form.value.remember) {
      localStorage.setItem(REMEMBER_KEY, form.value.account)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }

    userStore.setLoginInfo({
      token: data.token,
      userInfo: {
        id: data.userId,
        username: data.username,
        nickname: data.nickname,
        userType: data.userType
      },
      roles: data.roles || []
    })

    ElMessage.success('登录成功')
    const redirect = route.query.redirect && String(route.query.redirect).startsWith('/admin')
      ? String(route.query.redirect)
      : '/admin'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-login {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #6366f1 50%, #8b5cf6 100%);
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  filter: blur(40px);

  &-1 {
    width: 480px;
    height: 480px;
    top: -120px;
    left: -120px;
  }

  &-2 {
    width: 360px;
    height: 360px;
    bottom: -100px;
    right: -80px;
    background: rgba(255, 255, 255, 0.06);
  }

  &-3 {
    width: 280px;
    height: 280px;
    top: 40%;
    right: 30%;
    background: rgba(99, 102, 241, 0.18);
  }
}

.login-panel {
  position: relative;
  z-index: 1;
  width: 920px;
  max-width: calc(100vw - 32px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 30px 80px -20px rgba(15, 23, 42, 0.4);
  display: flex;
  overflow: hidden;
}

.brand-side {
  width: 380px;
  padding: 56px 48px;
  background: linear-gradient(160deg, #312e81 0%, #4f46e5 60%, #7c3aed 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px;
}

.brand-desc {
  margin: 0 0 32px;
  opacity: 0.85;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    opacity: 0.9;
  }
}

.form-side {
  flex: 1;
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 24px;
  margin: 0 0 8px;
  color: #1f2937;
}

.form-subtitle {
  margin: 0 0 32px;
  color: #6b7280;
}

.form-row {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-link {
  color: #6366f1;
  text-decoration: none;
  font-size: 14px;

  &:hover {
    text-decoration: underline;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.tips {
  margin-top: 24px;
}

@media (max-width: 768px) {
  .login-panel {
    flex-direction: column;
    width: calc(100vw - 24px);
  }

  .brand-side {
    width: 100%;
    padding: 32px 24px;
  }

  .form-side {
    padding: 32px 24px;
  }
}
</style>
