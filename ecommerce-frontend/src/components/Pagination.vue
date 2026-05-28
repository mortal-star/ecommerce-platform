<template>
  <div v-if="total > 0" class="pagination-wrapper">
    <div class="summary">共 {{ total }} 条，{{ totalPages }} 页</div>
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      background
      @update:current-page="handleCurrentChange"
      @update:page-size="handleSizeChange"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizes: { type: Array, default: () => [10, 20, 50, 100] },
  layout: { type: String, default: 'sizes, prev, pager, next, jumper' }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 0)

function handleCurrentChange(page) {
  emit('update:currentPage', page)
  emit('change', { pageNum: page, pageSize: props.pageSize })
}

function handleSizeChange(size) {
  emit('update:pageSize', size)
  emit('update:currentPage', 1)
  emit('change', { pageNum: 1, pageSize: size })
}
</script>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding: 16px 0;
}

.summary {
  color: #64748b;
  font-size: 14px;
  white-space: nowrap;
}
</style>
