<template>
  <div class="empty-state" :class="['size-' + size]" :style="containerStyle">
    <slot name="icon">
      <el-icon class="empty-icon" :size="resolvedIconSize">
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
import { computed } from 'vue'
import { Box } from '@element-plus/icons-vue'

const SIZE_MAP = {
  small: { icon: 40, minHeight: '160px' },
  default: { icon: 64, minHeight: '260px' },
  large: { icon: 96, minHeight: '360px' }
}

const props = defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  icon: { type: [String, Object], default: () => Box },
  iconSize: { type: Number, default: 0 },
  minHeight: { type: String, default: '' },
  size: {
    type: String,
    default: 'default',
    validator: v => ['small', 'default', 'large'].includes(v)
  }
})

const resolvedIconSize = computed(() => props.iconSize || SIZE_MAP[props.size].icon)

const containerStyle = computed(() => ({
  minHeight: props.minHeight || SIZE_MAP[props.size].minHeight
}))
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

.size-small .empty-title {
  font-size: 15px;
}

.size-large .empty-title {
  font-size: 22px;
}

.empty-description {
  margin-top: 8px;
  font-size: 14px;
}

.empty-action {
  margin-top: 20px;
}
</style>
