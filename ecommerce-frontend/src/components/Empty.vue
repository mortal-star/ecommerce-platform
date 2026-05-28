<template>
  <div class="empty-state" :style="{ minHeight }">
    <slot name="icon">
      <el-icon class="empty-icon" :size="iconSize">
        <component :is="icon" />
      </el-icon>
    </slot>
    <div class="empty-title">{{ title }}</div>
    <div v-if="description" class="empty-description">{{ description }}</div>
    <div v-if="$slots.default" class="empty-action">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { Box } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  icon: { type: [String, Object], default: Box },
  iconSize: { type: Number, default: 64 },
  minHeight: { type: String, default: '260px' }
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #94a3b8;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
  color: #cbd5e1;
}

.empty-title {
  color: #475569;
  font-size: 18px;
  font-weight: 700;
}

.empty-description {
  margin-top: 8px;
  font-size: 14px;
}

.empty-action {
  margin-top: 20px;
}
</style>
