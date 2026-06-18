<template>
  <div class="store-consumption">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>月度消耗统计</span>
        </div>
      </template>
      <div class="content-placeholder">
        <el-empty description="月度消耗统计页面" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.store-consumption {
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
