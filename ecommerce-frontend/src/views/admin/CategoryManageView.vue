<template>
  <section class="category-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">商品分类管理</h2>
        <p class="page-subtitle">管理多级商品分类树，支持新增、修改、删除与排序</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate(null)">新增一级分类</el-button>
    </div>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="treeData"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
        empty-text="暂无分类数据"
        style="width: 100%"
      >
        <el-table-column label="分类名称" min-width="240">
          <template #default="{ row }">
            <el-icon v-if="row.icon" class="cat-icon"><component :is="row.icon" /></el-icon>
            <span class="cat-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.level === 1 ? '' : 'info'">L{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="110" align="center">
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
              {{ row.status === 1 ? '启用' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.level < 3"
              text
              type="primary"
              size="small"
              @click="openCreate(row)"
            >
              新增子分类
            </el-button>
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
      :title="form.id ? '编辑分类' : '新增分类'"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="父级分类">
          <el-select v-if="!form.id" v-model="form.parentId" placeholder="选择父级（留空为一级）" clearable filterable>
            <el-option label="一级分类（无父级）" :value="0" />
            <el-option
              v-for="cat in parentChoices"
              :key="cat.id"
              :label="`L${cat.level} · ${cat.name}`"
              :value="cat.id"
            />
          </el-select>
          <span v-else class="readonly-text">
            {{ form.parentId ? parentMap.get(form.parentId)?.name : '一级分类' }}
          </span>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="2~20 字" maxlength="20" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可选，Element Plus 图标组件名" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
          <span class="form-hint">数值越小排在越前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">隐藏</el-radio>
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
  adminListCategories,
  adminCreateCategory,
  adminUpdateCategory,
  adminDeleteCategory,
  adminUpdateCategorySort
} from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const rawList = ref([])
const treeData = ref([])
const formVisible = ref(false)
const formRef = ref(null)
const sortChanges = ref(new Map())
const sortDirty = computed(() => sortChanges.value.size > 0)

const form = reactive({
  id: null,
  parentId: 0,
  name: '',
  icon: '',
  sort: 0,
  status: 1,
  level: 1
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度 2~20 个字符', trigger: 'blur' }
  ]
}

const parentMap = computed(() => {
  const map = new Map()
  rawList.value.forEach((c) => map.set(c.id, c))
  return map
})

const parentChoices = computed(() => rawList.value.filter((c) => c.level < 3))

function buildTree(list) {
  const map = new Map()
  list.forEach((c) => map.set(c.id, { ...c, children: [] }))
  const roots = []
  map.forEach((node) => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId).children.push(node)
    } else {
      roots.push(node)
    }
  })
  const sortNodes = (nodes) => {
    nodes.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    nodes.forEach((n) => sortNodes(n.children))
  }
  sortNodes(roots)
  return roots
}

async function loadData() {
  loading.value = true
  try {
    const { data } = await adminListCategories()
    rawList.value = Array.isArray(data) ? data : []
    treeData.value = buildTree(rawList.value)
    sortChanges.value = new Map()
  } catch (err) {
    ElMessage.error(err?.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

function openCreate(parent) {
  Object.assign(form, {
    id: null,
    parentId: parent ? parent.id : 0,
    name: '',
    icon: '',
    sort: 0,
    status: 1,
    level: parent ? (parent.level || 1) + 1 : 1
  })
  formVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || 0,
    name: row.name,
    icon: row.icon || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1,
    level: row.level || 1
  })
  formVisible.value = true
}

function resetForm() {
  Object.assign(form, { id: null, parentId: 0, name: '', icon: '', sort: 0, status: 1, level: 1 })
  formRef.value?.resetFields()
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      parentId: form.parentId || 0,
      name: form.name.trim(),
      icon: form.icon || null,
      sort: form.sort ?? 0,
      status: form.status,
      level: form.level
    }
    if (form.id) {
      await adminUpdateCategory(form.id, payload)
      ElMessage.success('分类修改成功')
    } else {
      await adminCreateCategory(payload)
      ElMessage.success('分类新增成功')
    }
    formVisible.value = false
    await loadData()
  } catch (err) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」？子分类会一并失效。`, '删除确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await adminDeleteCategory(row.id)
    ElMessage.success('分类删除成功')
    await loadData()
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
    await adminUpdateCategorySort(Array.from(sortChanges.value.values()))
    ElMessage.success('排序已保存')
    await loadData()
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

onMounted(loadData)
</script>

<style scoped lang="scss">
.category-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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

.cat-icon {
  margin-right: 8px;
  color: #6366f1;
}

.cat-name {
  font-weight: 500;
  color: #1f2937;
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

.readonly-text {
  color: #6b7280;
  font-size: 14px;
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
