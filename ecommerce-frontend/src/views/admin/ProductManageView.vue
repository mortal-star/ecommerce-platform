<template>
  <section class="product-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品管理</h2>
        <p class="page-subtitle">维护商品库，支持新增、编辑、上下架、删除以及 Excel 导入导出</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Upload" plain @click="triggerImport">导入 Excel</el-button>
        <el-button :icon="Download" plain @click="exportExcel">导出 Excel</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增商品</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="关键字">
          <el-input
            v-model="filter.keyword"
            placeholder="商品名称 / 副标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filter.categoryId" placeholder="全部" clearable filterable style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option
              v-for="cat in flatCategories"
              :key="cat.id"
              :label="cat.fullName"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="products" stripe empty-text="暂无商品" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="主图" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="row.mainImage"
              :preview-src-list="[row.mainImage]"
              fit="cover"
              style="width: 48px; height: 48px; border-radius: 4px"
            />
            <div v-else class="placeholder">无图</div>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="prod-meta">
              <div class="prod-name">{{ row.name }}</div>
              <div class="prod-sub">{{ row.subtitle || '—' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="140">
          <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="price" label="售价" width="100" align="right">
          <template #default="{ row }">¥{{ Number(row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              text
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @change="loadList"
      />
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑商品' : '新增商品'"
      width="720px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-steps :active="step" finish-status="success" simple>
        <el-step title="基本信息" />
        <el-step title="图文详情" />
        <el-step title="价格库存" />
      </el-steps>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px" class="form-body">
        <template v-if="step === 0">
          <el-form-item label="商品分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="选择分类" filterable style="width: 100%">
              <el-option
                v-for="cat in flatCategories"
                :key="cat.id"
                :label="cat.fullName"
                :value="cat.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" maxlength="60" show-word-limit placeholder="必填，2~60 字" />
          </el-form-item>
          <el-form-item label="副标题">
            <el-input v-model="form.subtitle" maxlength="120" show-word-limit placeholder="可选，营销卖点" />
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="form.unit" maxlength="10" placeholder="如：件 / 套 / 盒" style="width: 180px" />
          </el-form-item>
        </template>

        <template v-else-if="step === 1">
          <el-form-item label="主图" prop="mainImage">
            <el-input v-model="form.mainImage" placeholder="主图 URL（开发期可贴外链）">
              <template #append>
                <el-button @click="useDemoImage">示例图</el-button>
              </template>
            </el-input>
            <el-image
              v-if="form.mainImage"
              :src="form.mainImage"
              fit="cover"
              style="width: 88px; height: 88px; border-radius: 6px; margin-top: 8px"
            />
          </el-form-item>
          <el-form-item label="详情内容">
            <el-input
              v-model="form.detail"
              type="textarea"
              :rows="8"
              placeholder="支持 HTML / 富文本片段；如需更强编辑可后续接入 wangEditor"
              maxlength="5000"
              show-word-limit
            />
            <div class="form-hint">提示：当前以纯文本/HTML 编辑，后续可接入富文本编辑器以提升体验</div>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="售价 (¥)" prop="price">
            <el-input-number v-model="form.price" :precision="2" :min="0" :step="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="原价 (¥)">
            <el-input-number v-model="form.originalPrice" :precision="2" :min="0" :step="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" :max="999999" controls-position="right" />
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
            <span class="form-hint">数值越小排在越前</span>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <el-button :disabled="step === 0" @click="step -= 1">上一步</el-button>
        <el-button v-if="step < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-else type="primary" :loading="submitting" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>

    <input ref="fileInputRef" type="file" accept=".xlsx,.xls" style="display: none" @change="onFileChange" />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshLeft, Upload, Download } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  adminListProducts,
  adminCreateProduct,
  adminUpdateProduct,
  adminDeleteProduct,
  adminUpdateProductStatus,
  adminListCategories,
  adminImportProducts,
  adminExportProductsUrl
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const products = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const categories = ref([])

const filter = reactive({
  keyword: '',
  categoryId: null,
  status: null
})

const formVisible = ref(false)
const formRef = ref(null)
const step = ref(0)
const form = reactive({
  id: null,
  categoryId: null,
  name: '',
  subtitle: '',
  mainImage: '',
  detail: '',
  price: 0,
  originalPrice: 0,
  stock: 0,
  sales: 0,
  unit: '件',
  status: 1,
  sort: 0
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 60, message: '长度 2~60 个字符', trigger: 'blur' }
  ],
  mainImage: [{ required: true, message: '请填写主图 URL', trigger: 'blur' }],
  price: [{ required: true, message: '请填写售价', trigger: 'change' }],
  stock: [{ required: true, message: '请填写库存', trigger: 'change' }]
}

const fileInputRef = ref(null)

const flatCategories = computed(() => {
  const list = categories.value
  const map = new Map(list.map((c) => [c.id, c]))
  return list.map((c) => {
    const names = [c.name]
    let cur = c
    while (cur && cur.parentId) {
      const parent = map.get(cur.parentId)
      if (!parent) break
      names.unshift(parent.name)
      cur = parent
    }
    return { ...c, fullName: names.join(' / ') }
  })
})

function categoryName(id) {
  const c = flatCategories.value.find((cat) => cat.id === id)
  return c ? c.fullName : '—'
}

async function loadCategories() {
  try {
    const { data } = await adminListCategories()
    categories.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.warn('加载分类失败', err)
  }
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filter.keyword || undefined,
      categoryId: filter.categoryId ?? undefined,
      status: filter.status ?? undefined
    }
    const { data } = await adminListProducts(params)
    products.value = data?.list || []
    total.value = data?.total || 0
  } catch (err) {
    ElMessage.error(err?.message || '加载商品失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  filter.keyword = ''
  filter.categoryId = null
  filter.status = null
  pageNum.value = 1
  loadList()
}

function openCreate() {
  resetForm()
  formVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    categoryId: row.categoryId,
    name: row.name,
    subtitle: row.subtitle || '',
    mainImage: row.mainImage || '',
    detail: row.detail || '',
    price: Number(row.price ?? 0),
    originalPrice: Number(row.originalPrice ?? 0),
    stock: Number(row.stock ?? 0),
    sales: Number(row.sales ?? 0),
    unit: row.unit || '件',
    status: row.status ?? 1,
    sort: row.sort ?? 0
  })
  step.value = 0
  formVisible.value = true
}

function resetForm() {
  step.value = 0
  Object.assign(form, {
    id: null,
    categoryId: null,
    name: '',
    subtitle: '',
    mainImage: '',
    detail: '',
    price: 0,
    originalPrice: 0,
    stock: 0,
    sales: 0,
    unit: '件',
    status: 1,
    sort: 0
  })
  formRef.value?.resetFields()
}

function useDemoImage() {
  form.mainImage = 'https://placehold.co/400x400/3b82f6/ffffff?text=Product'
}

async function nextStep() {
  if (step.value === 0) {
    const ok = await formRef.value?.validateField(['categoryId', 'name']).catch(() => false)
    if (!ok) return
  } else if (step.value === 1) {
    const ok = await formRef.value?.validateField(['mainImage']).catch(() => false)
    if (!ok) return
  }
  step.value += 1
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      categoryId: form.categoryId,
      name: form.name.trim(),
      subtitle: form.subtitle,
      mainImage: form.mainImage,
      detail: form.detail,
      price: form.price,
      originalPrice: form.originalPrice,
      stock: form.stock,
      sales: form.sales,
      unit: form.unit,
      status: form.status,
      sort: form.sort
    }
    if (form.id) {
      await adminUpdateProduct(form.id, payload)
      ElMessage.success('商品修改成功')
    } else {
      await adminCreateProduct(payload)
      ElMessage.success('商品新增成功')
    }
    formVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定${next === 1 ? '上架' : '下架'}商品「${row.name}」？`, '操作确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await adminUpdateProductStatus(row.id, next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '操作失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.name}」？此操作不可恢复。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  try {
    await adminDeleteProduct(row.id)
    ElMessage.success('删除成功')
    if (products.value.length === 1 && pageNum.value > 1) pageNum.value -= 1
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function triggerImport() {
  fileInputRef.value?.click()
}

async function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    const { data } = await adminImportProducts(formData)
    ElMessage.success(`导入成功，共 ${data?.count ?? 0} 条`)
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '导入失败')
  } finally {
    e.target.value = ''
  }
}

function exportExcel() {
  const a = document.createElement('a')
  a.href = adminExportProductsUrl()
  a.target = '_blank'
  a.rel = 'noopener'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(async () => {
  await loadCategories()
  await loadList()
})
</script>

<style scoped lang="scss">
.product-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.head-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.filter-card :deep(.el-card__body) {
  padding: 16px;
}

.placeholder {
  width: 48px;
  height: 48px;
  background: #f3f4f6;
  border-radius: 4px;
  color: #9ca3af;
  font-size: 12px;
  line-height: 48px;
  text-align: center;
}

.prod-meta {
  line-height: 1.4;
}

.prod-name {
  font-weight: 500;
  color: #1f2937;
}

.prod-sub {
  color: #9ca3af;
  font-size: 12px;
}

.form-body {
  margin-top: 20px;
}

.form-hint {
  margin-left: 12px;
  color: #9ca3af;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
    align-items: stretch;
  }
  .head-actions {
    width: 100%;
  }
}
</style>
