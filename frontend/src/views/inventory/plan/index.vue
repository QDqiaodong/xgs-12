<template>
  <div class="inventory-plan">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点计划</span>
        </div>
      </template>
      <div class="content-placeholder">
        <el-empty description="盘点计划页面" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.inventory-plan {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .content-placeholder {
    min-height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
