<template>
  <div class="stock-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存报表</span>
        </div>
      </template>
      <div class="content-placeholder">
        <el-empty description="库存报表页面" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.stock-report {
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
