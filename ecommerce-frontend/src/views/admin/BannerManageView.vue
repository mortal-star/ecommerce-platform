<template>
  <section class="banner-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">轮播图管理</h2>
        <p class="page-subtitle">管理首页轮播图：上下架、排序、链接跳转</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增轮播图</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="banners" stripe empty-text="暂无轮播图" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="图片" width="180" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 140px; height: 60px; border-radius: 4px"
            />
            <div v-else class="placeholder">无图</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="linkUrl" label="跳转链接" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <a v-if="row.linkUrl" :href="row.linkUrl" target="_blank" rel="noopener" class="link">{{ row.linkUrl }}</a>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="120" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="0"
              :max="9999"
              size="small"
              controls-position="right"
              @change="(v) => handleSortChange(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="sort-bar">
        <el-button :icon="Sort" :disabled="!sortDirty" :loading="saving" type="primary" plain @click="saveSort">
          保存排序变更
        </el-button>
        <span v-if="sortDirty" class="dirty-hint">有未保存的排序变更</span>
      </div>
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="form.id ? '编辑轮播图' : '新增轮播图'"
      width="560px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="必填，2~60 字" />
        </el-form-item>
        <el-form-item label="图片 URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="可贴外链；建议尺寸 1920×500">
            <template #append>
              <el-button @click="useDemoImage">示例图</el-button>
            </template>
          </el-input>
          <el-image
            v-if="form.imageUrl"
            :src="form.imageUrl"
            fit="cover"
            style="width: 100%; max-width: 360px; height: 120px; border-radius: 6px; margin-top: 8px"
          />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="可选，点击轮播图时跳转的 URL" />
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
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Sort } from '@element-plus/icons-vue'
import {
  adminListBanners,
  adminCreateBanner,
  adminUpdateBanner,
  adminDeleteBanner,
  adminUpdateBannerSort
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const banners = ref([])
const sortChanges = ref(new Map())
const sortDirty = computed(() => sortChanges.value.size > 0)

const formVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
})

const rules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' },
    { min: 2, max: 60, message: '长度 2~60 个字符', trigger: 'blur' }
  ],
  imageUrl: [{ required: true, message: '请填写图片 URL', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await adminListBanners()
    banners.value = Array.isArray(data) ? [...data].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0)) : []
    sortChanges.value = new Map()
  } catch (err) {
    ElMessage.error(err?.message || '加载轮播图失败')
  } finally {
    loading.value = false
  }
}

function useDemoImage() {
  form.imageUrl = 'https://placehold.co/1920x500/3b82f6/ffffff?text=Banner'
}

function openCreate() {
  resetForm()
  formVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    title: row.title || '',
    imageUrl: row.imageUrl || '',
    linkUrl: row.linkUrl || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  })
  formVisible.value = true
}

function resetForm() {
  Object.assign(form, { id: null, title: '', imageUrl: '', linkUrl: '', sort: 0, status: 1 })
  formRef.value?.resetFields()
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      imageUrl: form.imageUrl.trim(),
      linkUrl: form.linkUrl || null,
      sort: form.sort ?? 0,
      status: form.status
    }
    if (form.id) {
      await adminUpdateBanner(form.id, payload)
      ElMessage.success('轮播图修改成功')
    } else {
      await adminCreateBanner(payload)
      ElMessage.success('轮播图新增成功')
    }
    formVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除轮播图「${row.title}」？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  try {
    await adminDeleteBanner(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function handleSortChange(row, value) {
  sortChanges.value.set(row.id, { id: row.id, sort: value ?? 0 })
}

async function saveSort() {
  if (!sortDirty.value) return
  saving.value = true
  try {
    await adminUpdateBannerSort(Array.from(sortChanges.value.values()))
    ElMessage.success('排序已保存')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '排序保存失败')
  } finally {
    saving.value = false
  }
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.banner-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

.placeholder {
  width: 140px;
  height: 60px;
  background: #f3f4f6;
  border-radius: 4px;
  color: #9ca3af;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.muted {
  color: #9ca3af;
}

.link {
  color: #3b82f6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.dirty-hint {
  font-size: 12px;
  color: #f59e0b;
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
}
</style>
