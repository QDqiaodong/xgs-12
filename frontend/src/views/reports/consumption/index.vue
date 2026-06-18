<template>
  <div class="consumption-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消耗报表</span>
        </div>
      </template>
      <div class="content-placeholder">
        <el-empty description="消耗报表页面" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.consumption-report {
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
