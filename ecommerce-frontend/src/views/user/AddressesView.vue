<template>
  <div class="addresses-page">
    <div class="head-row">
      <h2 class="page-title">收货地址</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增地址</el-button>
    </div>

    <div v-if="loading" class="loading-row">加载中...</div>
    <div v-else-if="addresses.length" class="address-grid">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="address-card"
        :class="{ 'is-default': addr.defaultFlag === 1 }"
      >
        <div class="addr-head">
          <span class="name">{{ addr.receiverName }}</span>
          <span class="phone">{{ addr.receiverPhone }}</span>
          <el-tag v-if="addr.defaultFlag === 1" type="warning" size="small">默认</el-tag>
        </div>
        <p class="addr-body">
          {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
        </p>
        <p v-if="addr.postalCode" class="addr-extra">邮编：{{ addr.postalCode }}</p>
        <div class="addr-actions">
          <el-button
            v-if="addr.defaultFlag !== 1"
            link
            type="primary"
            @click="onSetDefault(addr)"
          >设为默认</el-button>
          <el-button link @click="openEdit(addr)">修改</el-button>
          <el-button link type="danger" @click="onDelete(addr)">删除</el-button>
        </div>
      </div>
    </div>
    <Empty v-else size="large" description="还没有添加收货地址">
      <el-button type="primary" @click="openCreate">新增地址</el-button>
    </Empty>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '修改地址' : '新增地址'"
      width="560"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="所在地区">
          <div class="region-row">
            <el-input v-model="form.province" placeholder="省" />
            <el-input v-model="form.city" placeholder="市" />
            <el-input v-model="form.district" placeholder="区/县" />
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="form.detailAddress"
            type="textarea"
            :rows="2"
            placeholder="街道、楼栋、门牌号"
          />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="form.postalCode" placeholder="选填" maxlength="10" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="defaultFlagModel" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api'
import Empty from '@/components/Empty.vue'

const addresses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  defaultFlag: 0
})

const defaultFlagModel = computed({
  get: () => form.value.defaultFlag === 1,
  set: v => { form.value.defaultFlag = v ? 1 : 0 }
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function loadAddresses() {
  loading.value = true
  try {
    const data = await listAddresses()
    addresses.value = data || []
  } catch (e) {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    postalCode: '',
    defaultFlag: 0
  }
}

function openCreate() {
  editing.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(addr) {
  editing.value = addr
  form.value = {
    receiverName: addr.receiverName || '',
    receiverPhone: addr.receiverPhone || '',
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detailAddress: addr.detailAddress || '',
    postalCode: addr.postalCode || '',
    defaultFlag: addr.defaultFlag || 0
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    if (editing.value) {
      await updateAddress(editing.value.id, form.value)
      ElMessage.success('地址已更新')
    } else {
      await createAddress(form.value)
      ElMessage.success('地址已添加')
    }
    dialogVisible.value = false
    await loadAddresses()
  } catch (e) {} finally {
    saving.value = false
  }
}

async function onSetDefault(addr) {
  try {
    await setDefaultAddress(addr.id)
    ElMessage.success('已设为默认地址')
    await loadAddresses()
  } catch (e) {}
}

async function onDelete(addr) {
  try {
    await ElMessageBox.confirm('确定删除该地址？', '提示', { type: 'warning' })
  } catch { return }
  try {
    await deleteAddress(addr.id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (e) {}
}

onMounted(loadAddresses)
</script>

<style scoped lang="scss">
.addresses-page {
  .head-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  .page-title {
    margin: 0;
    font-size: 20px;
  }
}

.loading-row {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.address-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  transition: border-color 0.2s;

  &:hover {
    border-color: #2563eb;
  }
  &.is-default {
    border-color: #f59e0b;
    background: #fffbeb;
  }

  .addr-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .name {
      font-weight: 500;
      font-size: 15px;
    }
    .phone {
      color: #64748b;
    }
  }
  .addr-body {
    margin: 0 0 6px;
    color: #475569;
    font-size: 13px;
    line-height: 1.5;
  }
  .addr-extra {
    margin: 0 0 8px;
    color: #94a3b8;
    font-size: 12px;
  }
  .addr-actions {
    display: flex;
    justify-content: flex-end;
    gap: 4px;
    padding-top: 8px;
    border-top: 1px dashed #f1f5f9;
  }
}

.region-row {
  display: flex;
  gap: 8px;

  :deep(.el-input) {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>
