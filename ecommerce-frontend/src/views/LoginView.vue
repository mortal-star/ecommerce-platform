<template>
  <section class="login-view">
    <div class="login-card">
      <div class="brand">
        <span class="logo-mark">E</span>
        <div>
          <h2>电商平台</h2>
          <p>欢迎回来，请选择下方操作</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录 -->
        <el-tab-pane label="账号登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            @submit.prevent="handleLogin"
          >
            <el-form-item label="账号" prop="account">
              <el-input
                v-model="loginForm.account"
                placeholder="用户名 / 邮箱"
                :prefix-icon="User"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <div class="row-line">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <a class="link" @click="activeTab = 'reset'">忘记密码？</a>
            </div>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >登 录</el-button>
            <div class="bottom-tip">
              还没有账号？<a class="link" @click="activeTab = 'register'">立即注册</a>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 注册 -->
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="4-64 位"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="example@domain.com"
                :prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="code">
              <div class="code-row">
                <el-input
                  v-model="registerForm.code"
                  placeholder="请输入收到的验证码"
                  size="large"
                />
                <el-button
                  :disabled="registerCountdown > 0"
                  size="large"
                  @click="sendCode('register')"
                >
                  {{ registerCountdown > 0 ? `${registerCountdown}s 后重新发送` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="6-32 位"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="选填"
                size="large"
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >注 册</el-button>
            <div class="bottom-tip">
              已有账号？<a class="link" @click="activeTab = 'login'">立即登录</a>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- 找回密码 -->
        <el-tab-pane label="找回密码" name="reset">
          <el-form
            ref="resetFormRef"
            :model="resetForm"
            :rules="resetRules"
            label-position="top"
          >
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="resetForm.email"
                placeholder="注册邮箱"
                :prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="code">
              <div class="code-row">
                <el-input
                  v-model="resetForm.code"
                  placeholder="请输入收到的验证码"
                  size="large"
                />
                <el-button
                  :disabled="resetCountdown > 0"
                  size="large"
                  @click="sendCode('reset')"
                >
                  {{ resetCountdown > 0 ? `${resetCountdown}s 后重新发送` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
              <el-input
                v-model="resetForm.password"
                type="password"
                placeholder="6-32 位"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleReset"
            >确认重置</el-button>
            <div class="bottom-tip">
              <a class="link" @click="activeTab = 'login'">返回登录</a>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, User } from '@element-plus/icons-vue'
import { login, register, resetPassword, sendEmailCode } from '@/api'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = ref({ account: '', password: '' })
const loginFormRef = ref(null)
const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerForm = ref({
  username: '',
  email: '',
  code: '',
  password: '',
  nickname: ''
})
const registerFormRef = ref(null)
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 64, message: '用户名长度 4-64 位', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ]
}
const registerCountdown = ref(0)
let registerTimer = null

const resetForm = ref({ email: '', code: '', password: '' })
const resetFormRef = ref(null)
const resetRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ]
}
const resetCountdown = ref(0)
let resetTimer = null

function startCountdown(scene) {
  if (scene === 'register') {
    registerCountdown.value = 60
    registerTimer = setInterval(() => {
      registerCountdown.value -= 1
      if (registerCountdown.value <= 0) {
        clearInterval(registerTimer)
        registerTimer = null
      }
    }, 1000)
  } else {
    resetCountdown.value = 60
    resetTimer = setInterval(() => {
      resetCountdown.value -= 1
      if (resetCountdown.value <= 0) {
        clearInterval(resetTimer)
        resetTimer = null
      }
    }, 1000)
  }
}

async function sendCode(scene) {
  const email = scene === 'register' ? registerForm.value.email : resetForm.value.email
  if (!email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  try {
    const data = await sendEmailCode({ email, scene })
    ElMessage.success(`验证码已生成${data?.code ? `：${data.code}` : ''}`)
    startCountdown(scene)
  } catch (e) {}
}

async function handleLogin() {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = await login(loginForm.value)
    userStore.setLoginInfo({
      token: data.token,
      userInfo: {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        userType: data.userType
      },
      roles: data.roles || []
    })
    if (rememberMe.value) {
      localStorage.setItem('REMEMBERED_ACCOUNT', loginForm.value.account)
    } else {
      localStorage.removeItem('REMEMBERED_ACCOUNT')
    }
    ElMessage.success('登录成功')
    const redirect = route.query.redirect && typeof route.query.redirect === 'string'
      ? route.query.redirect
      : '/'
    router.replace(redirect)
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await register(registerForm.value)
    ElMessage.success('注册成功，请登录')
    loginForm.value.account = registerForm.value.username
    activeTab.value = 'login'
  } catch (e) {} finally {
    loading.value = false
  }
}

async function handleReset() {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await resetPassword(resetForm.value)
    ElMessage.success('密码重置成功，请登录')
    loginForm.value.account = resetForm.value.email
    activeTab.value = 'login'
  } catch (e) {} finally {
    loading.value = false
  }
}

const remembered = localStorage.getItem('REMEMBERED_ACCOUNT')
if (remembered) {
  loginForm.value.account = remembered
  rememberMe.value = true
}

onBeforeUnmount(() => {
  if (registerTimer) clearInterval(registerTimer)
  if (resetTimer) clearInterval(resetTimer)
})
</script>

<style scoped lang="scss">
.login-view {
  min-height: calc(100vh - 70px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  padding: 32px 16px;
}

.login-card {
  width: 460px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .logo-mark {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #2563eb, #7c3aed);
    border-radius: 12px;
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    display: grid;
    place-items: center;
  }
  h2 {
    margin: 0;
    font-size: 20px;
  }
  p {
    margin: 4px 0 0;
    color: #94a3b8;
    font-size: 13px;
  }
}

.login-tabs {
  margin-top: 8px;
}

.row-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 13px;
}

.link {
  color: #2563eb;
  cursor: pointer;
}

.code-row {
  display: flex;
  gap: 8px;

  :deep(.el-input) {
    flex: 1;
  }
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.bottom-tip {
  text-align: center;
  margin-top: 16px;
  color: #64748b;
  font-size: 13px;
}
</style>
