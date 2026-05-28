<template>
  <div class="password-page">
    <h2 class="page-title">修改密码</h2>

    <el-alert
      title="提示"
      type="info"
      :closable="false"
      description="修改密码后需要重新登录，请记牢新密码。"
      show-icon
      class="tip"
    />

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="pwd-form"
    >
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="注册邮箱" />
      </el-form-item>
      <el-form-item label="邮箱验证码" prop="code">
        <div class="code-row">
          <el-input v-model="form.code" placeholder="请输入收到的验证码" />
          <el-button
            :disabled="countdown > 0"
            @click="onSendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="新密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="6-32 位"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="再次输入新密码"
          show-password
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">提交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword, sendEmailCode } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const saving = ref(false)
const countdown = ref(0)
let timer = null

const form = ref({
  email: userStore.userInfo?.email || '',
  code: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, value, cb) => {
        if (value !== form.value.password) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function onSendCode() {
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    const data = await sendEmailCode({ email: form.value.email, scene: 'reset' })
    ElMessage.success(`验证码已生成${data?.code ? `：${data.code}` : ''}`)
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e) {}
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    await resetPassword({
      email: form.value.email,
      code: form.value.code,
      password: form.value.password
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) {} finally {
    saving.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.password-page {
  .page-title {
    margin: 0 0 16px;
    font-size: 20px;
  }
}
.tip {
  margin-bottom: 20px;
  max-width: 600px;
}
.pwd-form {
  max-width: 600px;
}
.code-row {
  display: flex;
  gap: 8px;

  :deep(.el-input) {
    flex: 1;
  }
}
</style>
