<template>
  <div class="inventory-execute">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点执行</span>
        </div>
      </template>
      <div class="content-placeholder">
        <el-empty description="盘点执行页面" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.inventory-execute {
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
