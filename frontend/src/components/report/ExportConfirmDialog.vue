<template>
  <el-dialog
    :model-value="modelValue"
    title="确认报表导出口径"
    width="560px"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="handleVisibleChange"
  >
    <div class="export-confirm">
      <el-alert type="warning" :closable="false" show-icon class="mb-16">
        <template #title>导出前请确认以下报表口径，口径范围内的数据将被导出。</template>
      </el-alert>

      <el-descriptions :column="1" border size="default" class="scope-desc">
        <el-descriptions-item label="报表类型">
          <el-tag type="primary" size="small" effect="light">{{ reportType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item
          v-for="item in scopeItems"
          :key="item.label"
          :label="item.label"
        >
          {{ item.value }}
        </el-descriptions-item>
        <el-descriptions-item v-if="!scopeItems.length" label="筛选条件">
          <span class="text-muted">未设置筛选条件（全部数据）</span>
        </el-descriptions-item>
        <el-descriptions-item label="数据条数">
          <span class="num-highlight">{{ dataCount }} 条</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="extraInfo" label="补充说明">
          {{ extraInfo }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="export-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>确认后将按上述口径生成并下载报表文件。</span>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleConfirm">
        <el-icon><Download /></el-icon>
        确认导出
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: boolean
    reportType: string
    scopeItems: Array<{ label: string; value: string }>
    dataCount: number
    extraInfo?: string
    loading?: boolean
  }>(),
  {
    loading: false,
    extraInfo: ''
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const handleVisibleChange = (val: boolean) => {
  emit('update:modelValue', val)
}

const handleCancel = () => {
  emit('update:modelValue', false)
  emit('cancel')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped lang="scss">
.export-confirm {
  .scope-desc {
    :deep(.el-descriptions__label) {
      width: 110px;
      color: #6b7280;
      background-color: #f9fafb;
    }

    .num-highlight {
      font-weight: 700;
      color: #1e3a8a;
      font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    }

    .text-muted {
      color: #9ca3af;
    }
  }

  .export-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 16px;
    padding: 10px 12px;
    background: #eff6ff;
    border-radius: 8px;
    font-size: 13px;
    color: #1e3a8a;
  }
}
</style>
