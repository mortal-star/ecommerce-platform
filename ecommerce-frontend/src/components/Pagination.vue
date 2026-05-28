<template>
  <div v-if="visible" class="pagination-wrapper" :class="{ 'is-small': small }">
    <div v-if="showSummary" class="summary">共 {{ total }} 条，{{ totalPages }} 页</div>
    <el-pagination
      v-model:current-page="currentPageProxy"
      v-model:page-size="pageSizeProxy"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :small="small"
      :disabled="disabled"
      :pager-count="pagerCount"
      background
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
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
  layout: { type: String, default: 'sizes, prev, pager, next, jumper' },
  small: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  showSummary: { type: Boolean, default: true },
  hideOnSinglePage: { type: Boolean, default: false },
  pagerCount: { type: Number, default: 7 }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

const totalPages = computed(() => {
  if (!props.total || !props.pageSize) return 0
  return Math.ceil(props.total / props.pageSize)
})

const visible = computed(() => {
  if (props.total <= 0) return false
  if (props.hideOnSinglePage && totalPages.value <= 1) return false
  return true
})

const currentPageProxy = computed({
  get: () => props.currentPage,
  set: value => emit('update:currentPage', value)
})

const pageSizeProxy = computed({
  get: () => props.pageSize,
  set: value => emit('update:pageSize', value)
})

function handleCurrentChange(page) {
  emit('change', { pageNum: page, pageSize: props.pageSize })
}

function handleSizeChange(size) {
  emit('update:currentPage', 1)
  emit('change', { pageNum: 1, pageSize: size })
}
</script>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
  padding: 16px 0;

  &.is-small {
    margin-top: 12px;
    padding: 8px 0;
  }
}

.summary {
  color: #64748b;
  font-size: 14px;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .pagination-wrapper {
    justify-content: center;
  }

  .summary {
    width: 100%;
    text-align: center;
  }
}
</style>
