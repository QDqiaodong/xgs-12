import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      }
    ]
  },
  {
    path: '/inventory',
    component: Layout,
    redirect: '/inventory/stock',
    meta: { title: '库存管理', icon: 'Box' },
    children: [
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('@/views/inventory/stock/index.vue'),
        meta: { title: '库存查询', icon: 'Search' }
      },
      {
        path: 'inbound',
        name: 'Inbound',
        component: () => import('@/views/inventory/inbound/index.vue'),
        meta: { title: '入库管理', icon: 'Download' }
      },
      {
        path: 'outbound',
        name: 'Outbound',
        component: () => import('@/views/inventory/outbound/index.vue'),
        meta: { title: '调拨出库', icon: 'Upload' }
      },
      {
        path: 'transfer',
        name: 'Transfer',
        component: () => import('@/views/inventory/transfer/index.vue'),
        meta: { title: '调拨管理', icon: 'Sort' }
      },
      {
        path: 'transfer/receive',
        name: 'TransferReceive',
        component: () => import('@/views/inventory/transfer/receive.vue'),
        meta: { title: '门店签收', icon: 'CircleCheck' }
      },
      {
        path: 'plan',
        name: 'InventoryPlan',
        component: () => import('@/views/inventory/plan/index.vue'),
        meta: { title: '盘点计划', icon: 'Document' }
      },
      {
        path: 'execute',
        name: 'InventoryExecute',
        component: () => import('@/views/inventory/execute/index.vue'),
        meta: { title: '盘点执行', icon: 'Edit' }
      },
      {
        path: 'analysis',
        name: 'InventoryAnalysis',
        component: () => import('@/views/inventory/analysis/index.vue'),
        meta: { title: '盘点分析', icon: 'DataLine' }
      }
    ]
  },
  {
    path: '/procurement',
    component: Layout,
    redirect: '/procurement/orders',
    meta: { title: '采购管理', icon: 'ShoppingCart' },
    children: [
      {
        path: 'orders',
        name: 'ProcurementOrders',
        component: () => import('@/views/procurement/orders/index.vue'),
        meta: { title: '采购订单', icon: 'Document' }
      },
      {
        path: 'suppliers',
        name: 'Suppliers',
        component: () => import('@/views/procurement/suppliers/index.vue'),
        meta: { title: '供应商管理', icon: 'OfficeBuilding' }
      }
    ]
  },
  {
    path: '/consumables',
    component: Layout,
    redirect: '/consumables/list',
    meta: { title: '耗材管理', icon: 'Goods' },
    children: [
      {
        path: 'list',
        name: 'ConsumablesList',
        component: () => import('@/views/consumables/list/index.vue'),
        meta: { title: '耗材列表', icon: 'List' }
      },
      {
        path: 'category',
        name: 'ConsumablesCategory',
        component: () => import('@/views/consumables/category/index.vue'),
        meta: { title: '分类管理', icon: 'Folder' }
      }
    ]
  },
  {
    path: '/stores',
    component: Layout,
    redirect: '/stores/list',
    meta: { title: '门店管理', icon: 'Shop' },
    children: [
      {
        path: 'list',
        name: 'StoresList',
        component: () => import('@/views/stores/list/index.vue'),
        meta: { title: '门店列表', icon: 'List' }
      },
      {
        path: 'requisition',
        name: 'StoreRequisition',
        component: () => import('@/views/stores/requisition/index.vue'),
        meta: { title: '补货申请台', icon: 'ShoppingCart' }
      },
      {
        path: 'consumption',
        name: 'StoreConsumption',
        component: () => import('@/views/stores/consumption/index.vue'),
        meta: { title: '月度消耗统计', icon: 'DataLine' }
      }
    ]
  },
  {
    path: '/reports',
    component: Layout,
    redirect: '/reports/stock',
    meta: { title: '报表统计', icon: 'DataAnalysis' },
    children: [
      {
        path: 'stock',
        name: 'StockReport',
        component: () => import('@/views/reports/inventory/index.vue'),
        meta: { title: '库存报表', icon: 'DataLine' }
      },
      {
        path: 'consumption',
        name: 'ConsumptionReport',
        component: () => import('@/views/reports/consumption/index.vue'),
        meta: { title: '消耗报表', icon: 'TrendCharts' }
      },
      {
        path: 'transfer',
        name: 'TransferReport',
        component: () => import('@/views/reports/transfer/index.vue'),
        meta: { title: '调拨报表', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/users',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/users/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/system/roles/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/system/logs/index.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - 连锁零售门店耗材进销存管控系统`
  }

  const userStore = useUserStore()
  const isLoginPage = to.path === '/login'
  const hasToken = !!userStore.token

  if (!hasToken) {
    if (!isLoginPage) {
      next('/login')
      return
    }
    next()
    return
  }

  if (isLoginPage) {
    next('/')
    return
  }

  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      userStore.logout()
      next('/login')
      return
    }
  }

  next()
})

export default router
export { routes }