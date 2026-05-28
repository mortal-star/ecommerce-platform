<template>
  <section class="admin-profile">
    <div class="page-head">
      <div>
        <h2 class="page-title">管理员个人中心</h2>
        <p class="page-subtitle">查看账号信息、修改密码</p>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="10">
        <el-card v-loading="loadingProfile" shadow="never" class="info-card">
          <template #header>
            <div class="card-title">个人信息</div>
          </template>
          <div v-if="profile" class="profile-body">
            <el-avatar :size="80" :src="profile.avatar || ''">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <div class="profile-name">{{ profile.nickname || profile.username }}</div>
            <el-tag size="small" type="warning">管理员</el-tag>
            <el-descriptions :column="1" class="profile-desc" border>
              <el-descriptions-item label="账号">{{ profile.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ profile.nickname || '—' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ profile.email || '—' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ profile.phone || '—' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderLabel(profile.gender) }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatTime(profile.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="最近登录">{{ formatTime(profile.lastLoginTime) || '—' }}</el-descriptions-item>
            </el-descriptions>
            <el-button type="primary" plain @click="openEditProfile">修改资料</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="password-card">
          <template #header>
            <div class="card-title">修改密码</div>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            style="max-width: 420px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6~32 位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submittingPwd" @click="submitPassword">提交修改</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="editVisible" title="修改资料" width="480px" @closed="resetEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="editForm.avatar" placeholder="可选" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingEdit" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { adminGetProfile, adminUpdateProfile, adminChangePassword } from '@/api/admin'

const loadingProfile = ref(false)
const submittingPwd = ref(false)
const submittingEdit = ref(false)

const profile = ref(null)

const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6~32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== passwordForm.newPassword) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

const editVisible = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 0
})

const editRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

function genderLabel(code) {
  switch (code) {
    case 1: return '男'
    case 2: return '女'
    default: return '保密'
  }
}

async function loadProfile() {
  loadingProfile.value = true
  try {
    const { data } = await adminGetProfile()
    profile.value = data
  } catch (err) {
    ElMessage.error(err?.message || '加载个人信息失败')
  } finally {
    loadingProfile.value = false
  }
}

function openEditProfile() {
  if (!profile.value) return
  Object.assign(editForm, {
    nickname: profile.value.nickname || '',
    email: profile.value.email || '',
    phone: profile.value.phone || '',
    avatar: profile.value.avatar || '',
    gender: profile.value.gender ?? 0
  })
  editVisible.value = true
}

function resetEditForm() {
  Object.assign(editForm, { nickname: '', email: '', phone: '', avatar: '', gender: 0 })
  editFormRef.value?.resetFields()
}

async function submitEdit() {
  if (!editFormRef.value) return
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  submittingEdit.value = true
  try {
    await adminUpdateProfile({
      nickname: editForm.nickname,
      email: editForm.email || null,
      phone: editForm.phone || null,
      avatar: editForm.avatar || null,
      gender: editForm.gender
    })
    ElMessage.success('资料修改成功')
    editVisible.value = false
    await loadProfile()
  } catch (err) {
    ElMessage.error(err?.message || '修改失败')
  } finally {
    submittingEdit.value = false
  }
}

async function submitPassword() {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  submittingPwd.value = true
  try {
    await adminChangePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    resetPasswordForm()
  } catch (err) {
    ElMessage.error(err?.message || '密码修改失败')
  } finally {
    submittingPwd.value = false
  }
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(loadProfile)
</script>

<style scoped lang="scss">
.admin-profile {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.profile-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.profile-desc {
  width: 100%;
  margin-top: 12px;
}

.info-card,
.password-card {
  height: 100%;
}

@media (max-width: 768px) {
  .info-card {
    margin-bottom: 16px;
  }
}
</style>
