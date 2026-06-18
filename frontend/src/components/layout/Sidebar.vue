<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': collapsed }">
    <div class="logo">
      <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
      <span v-show="!collapsed" class="logo-text">进销存系统</span>
    </div>
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :unique-opened="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu
            v-if="route.children && route.children.length > 0"
            :index="route.path"
          >
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <template v-for="child in route.children" :key="child.path">
              <el-sub-menu
                v-if="child.children && child.children.length > 0 && !child.meta?.hidden"
                :index="`${route.path}/${child.path}`"
              >
                <template #title>
                  <el-icon><component :is="child.meta?.icon" /></el-icon>
                  <span>{{ child.meta?.title }}</span>
                </template>
                <el-menu-item
                  v-for="grandChild in child.children"
                  :key="grandChild.path"
                  :index="`${route.path}/${child.path}/${grandChild.path}`"
                >
                  <el-icon><component :is="grandChild.meta?.icon" /></el-icon>
                  <span>{{ grandChild.meta?.title }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item
                v-else-if="!child.meta?.hidden"
                :index="`${route.path}/${child.path}`"
              >
                <el-icon><component :is="child.meta?.icon" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>
          <el-menu-item v-else :index="route.path">
            <el-icon><component :is="route.meta?.icon" /></el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { routes } from '@/router/index'

defineProps<{
  collapsed: boolean
}>()

const route = useRoute()

const activeMenu = computed(() => route.path)

const menuRoutes = computed(() => {
  return routes.filter(r => !r.meta?.hidden && r.component)
})
</script>

<style scoped lang="scss">
.sidebar-container {
  position: fixed;
  left: 0;
  top: 0;
  width: 210px;
  height: 100vh;
  background-color: #304156;
  transition: width 0.3s;
  z-index: 1001;

  &.is-collapsed {
    width: 64px;

    .logo-text {
      display: none;
    }
  }
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  background-color: #263445;
  overflow: hidden;

  .logo-img {
    width: 32px;
    height: 32px;
  }

  .logo-text {
    margin-left: 10px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  &:hover {
    background-color: #263445 !important;
  }
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
}
</style>