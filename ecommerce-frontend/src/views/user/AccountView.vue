<template>
  <div class="account-page">
    <h2 class="page-title">账号设置</h2>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="account-form"
    >
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="example@domain.com" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="11 位手机号" />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="0">保密</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="生日">
        <el-date-picker
          v-model="form.birthday"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item label="头像">
        <ImageUpload v-model="avatarUrl" :limit="1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)
const avatarUrl = ref('')

const form = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  birthday: '',
  avatar: ''
})

const rules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

watch(avatarUrl, val => {
  form.value.avatar = val || ''
})

async function loadProfile() {
  try {
    const data = await getProfile()
    if (data) {
      form.value = {
        username: data.username || '',
        nickname: data.nickname || '',
        email: data.email || '',
        phone: data.phone || '',
        gender: data.gender ?? 0,
        birthday: data.birthday || '',
        avatar: data.avatar || ''
      }
      avatarUrl.value = data.avatar || ''
    }
  } catch (e) {}
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    await updateProfile({
      nickname: form.value.nickname,
      email: form.value.email,
      phone: form.value.phone,
      gender: form.value.gender,
      birthday: form.value.birthday,
      avatar: form.value.avatar
    })
    ElMessage.success('保存成功')
    userStore.setUserInfo({
      ...(userStore.userInfo || {}),
      nickname: form.value.nickname,
      avatar: form.value.avatar
    })
  } catch (e) {} finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped lang="scss">
.account-page {
  .page-title {
    margin: 0 0 20px;
    font-size: 20px;
  }
}
.account-form {
  max-width: 600px;
}
</style>
