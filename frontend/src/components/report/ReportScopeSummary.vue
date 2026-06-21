<template>
  <el-card class="scope-summary-card">
    <template #header>
      <div class="card-header">
        <span class="card-title">
          <el-icon class="title-icon"><DocumentChecked /></el-icon>
          {{ title }}
        </span>
        <el-tag v-if="items.length" type="info" size="small" effect="plain">
          已应用 {{ items.length }} 项筛选
        </el-tag>
        <el-tag v-else type="success" size="small" effect="plain">
          未筛选 · 全部数据
        </el-tag>
      </div>
    </template>

    <div v-if="items.length" class="scope-list">
      <div v-for="item in items" :key="item.label" class="scope-item">
        <span class="scope-label">{{ item.label }}</span>
        <el-tag
          :type="(item.tagType as any) || 'primary'"
          size="small"
          effect="light"
          class="scope-value"
        >
          {{ item.value }}
        </el-tag>
      </div>
    </div>

    <div v-else class="scope-empty">
      <el-icon><InfoFilled /></el-icon>
      <span>{{ emptyText }}</span>
    </div>
  </el-card>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    items: Array<{ label: string; value: string; tagType?: string }>
    emptyText?: string
  }>(),
  {
    title: '报表口径',
    emptyText: '未设置筛选条件，当前展示全部数据'
  }
)
</script>

<style scoped lang="scss">
.scope-summary-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-icon {
    color: #1e3a8a;
  }

  .scope-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 24px;
  }

  .scope-item {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .scope-label {
    font-size: 13px;
    color: #6b7280;
    white-space: nowrap;
  }

  .scope-value {
    max-width: 260px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .scope-empty {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #9ca3af;
  }
}
</style>
