<template>
  <div class="dashboard-container">
    <div class="stats-row">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card warning">
            <div class="stat-icon">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.warningCount }}</div>
              <div class="stat-label">库存预警数</div>
            </div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              <span>12%</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card pending">
            <div class="stat-icon">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApproval }}</div>
              <div class="stat-label">待审批数</div>
            </div>
            <div class="stat-trend down">
              <el-icon><Bottom /></el-icon>
              <span>5%</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card cost">
            <div class="stat-icon">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(stats.monthlyCost) }}</div>
              <div class="stat-label">本月消耗成本</div>
            </div>
            <div class="stat-trend up">
              <el-icon><Top /></el-icon>
              <span>8%</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card transfer">
            <div class="stat-icon">
              <el-icon :size="28"><Sort /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inTransit }}</div>
              <div class="stat-label">在途调拨数</div>
            </div>
            <div class="stat-trend">
              <el-icon><Minus /></el-icon>
              <span>持平</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="main-content">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">消耗趋势图</span>
              <div class="card-actions">
                <el-radio-group v-model="chartRange" size="small">
                  <el-radio-button label="week">近一周</el-radio-button>
                  <el-radio-button label="month">近一月</el-radio-button>
                  <el-radio-button label="year">近一年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="quick-actions-card">
          <template #header>
            <span class="card-title">快捷入口</span>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="router.push('/inventory/inbound')">
              <div class="action-icon inbound">
                <el-icon :size="24"><Download /></el-icon>
              </div>
              <span>新增入库</span>
            </div>
            <div class="action-item" @click="router.push('/inventory/outbound')">
              <div class="action-icon outbound">
                <el-icon :size="24"><Upload /></el-icon>
              </div>
              <span>新增出库</span>
            </div>
            <div class="action-item" @click="router.push('/inventory/stock')">
              <div class="action-icon stock">
                <el-icon :size="24"><Search /></el-icon>
              </div>
              <span>库存查询</span>
            </div>
            <div class="action-item" @click="router.push('/consumables/list')">
              <div class="action-icon consumable">
                <el-icon :size="24"><Goods /></el-icon>
              </div>
              <span>耗材管理</span>
            </div>
            <div class="action-item" @click="router.push('/reports/inventory')">
              <div class="action-icon report">
                <el-icon :size="24"><DataAnalysis /></el-icon>
              </div>
              <span>库存报表</span>
            </div>
            <div class="action-item" @click="router.push('/procurement/orders')">
              <div class="action-icon procurement">
                <el-icon :size="24"><ShoppingCart /></el-icon>
              </div>
              <span>采购订单</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="bottom-content">
      <el-col :xs="24" :lg="12">
        <el-card class="warning-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="warning-icon"><Warning /></el-icon>
                库存预警列表
              </span>
              <el-link type="primary" :underline="false" @click="router.push('/inventory/stock')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-link>
            </div>
          </template>
          <el-table :data="warningList" style="width: 100%" size="small" stripe>
            <el-table-column prop="name" label="耗材名称" min-width="120" />
            <el-table-column prop="code" label="耗材编码" width="100" />
            <el-table-column prop="stock" label="当前库存" width="90" align="center">
              <template #default="{ row }">
                <span :class="{ 'low-stock': row.stock <= row.warningStock }">
                  {{ row.stock }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="warningStock" label="预警值" width="80" align="center" />
            <el-table-column prop="storeName" label="所属门店" width="100" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handlePurchase(row)">
                  补货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="todo-icon"><List /></el-icon>
                待办事项
              </span>
              <el-link type="primary" :underline="false">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-link>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="item in todoList" :key="item.id" class="todo-item" :class="item.priority">
              <div class="todo-left">
                <el-tag :type="getPriorityType(item.priority)" size="small">
                  {{ getPriorityText(item.priority) }}
                </el-tag>
                <span class="todo-title">{{ item.title }}</span>
              </div>
              <div class="todo-right">
                <span class="todo-time">{{ item.time }}</span>
                <el-button type="primary" size="small" @click="handleTodo(item)">
                  处理
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()
const chartRef = ref<HTMLElement>()
const chartRange = ref('week')
let chartInstance: echarts.ECharts | null = null

const stats = ref({
  warningCount: 8,
  pendingApproval: 5,
  monthlyCost: 125680,
  inTransit: 3
})

const warningList = ref([
  { id: 1, name: 'A4打印纸', code: 'HC001', stock: 50, warningStock: 100, storeName: '总店' },
  { id: 2, name: '签字笔', code: 'HC002', stock: 200, warningStock: 500, storeName: '分店A' },
  { id: 3, name: '胶带', code: 'HC003', stock: 30, warningStock: 50, storeName: '总店' },
  { id: 4, name: '文件夹', code: 'HC004', stock: 15, warningStock: 30, storeName: '分店B' },
  { id: 5, name: '订书机', code: 'HC005', stock: 8, warningStock: 20, storeName: '总店' }
])

const todoList = ref([
  { id: 1, title: '采购订单PO2024001待审批', priority: 'high', time: '10分钟前', type: 'approval' },
  { id: 2, title: '调拨单DB2024002待确认', priority: 'medium', time: '30分钟前', type: 'transfer' },
  { id: 3, title: '耗材"打印纸"库存不足，请及时补货', priority: 'high', time: '1小时前', type: 'stock' },
  { id: 4, title: '供应商"XX公司"资质即将到期', priority: 'low', time: '2小时前', type: 'supplier' },
  { id: 5, title: '月度库存盘点任务待完成', priority: 'medium', time: '3小时前', type: 'task' }
])

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const map: Record<string, string> = {
    high: '紧急',
    medium: '一般',
    low: '普通'
  }
  return map[priority] || '普通'
}

const handlePurchase = (row: any) => {
  router.push({
    path: '/procurement/orders',
    query: { consumableId: row.id, consumableName: row.name }
  })
}

const handleTodo = (item: any) => {
  switch (item.type) {
    case 'approval':
      router.push('/procurement/orders')
      break
    case 'transfer':
      router.push('/inventory/transfer')
      break
    case 'stock':
      router.push('/inventory/stock')
      break
    case 'supplier':
      router.push('/procurement/suppliers')
      break
    default:
      break
  }
}

const getChartData = (range: string) => {
  const data = {
    week: {
      xAxis: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      inbound: [120, 132, 101, 134, 90, 230, 210],
      outbound: [82, 93, 90, 93, 129, 133, 132]
    },
    month: {
      xAxis: ['第1周', '第2周', '第3周', '第4周'],
      inbound: [520, 632, 501, 634],
      outbound: [382, 493, 390, 493]
    },
    year: {
      xAxis: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      inbound: [1200, 1320, 1010, 1340, 900, 2300, 2100, 1820, 1930, 1900, 2130, 2020],
      outbound: [820, 932, 901, 934, 1290, 1330, 1320, 1020, 1130, 1100, 1230, 1120]
    }
  }
  return data[range as keyof typeof data] || data.week
}

const initChart = () => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const chartData = getChartData(chartRange.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: {
        color: '#374151'
      }
    },
    legend: {
      data: ['入库数量', '出库数量'],
      bottom: 0,
      textStyle: {
        color: '#6B7280'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.xAxis,
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#6B7280'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#F3F4F6'
        }
      },
      axisLabel: {
        color: '#6B7280'
      }
    },
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#1E3A8A'
        },
        itemStyle: {
          color: '#1E3A8A',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(30, 58, 138, 0.3)' },
            { offset: 1, color: 'rgba(30, 58, 138, 0.05)' }
          ])
        },
        data: chartData.inbound
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#059669'
        },
        itemStyle: {
          color: '#059669',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(5, 150, 105, 0.3)' },
            { offset: 1, color: 'rgba(5, 150, 105, 0.05)' }
          ])
        },
        data: chartData.outbound
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(chartRange, () => {
  initChart()
})

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #F3F4F6;
  min-height: calc(100vh - 60px);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    color: #fff;
  }
  
  .stat-info {
    flex: 1;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: #1F2937;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 14px;
    color: #6B7280;
    margin-top: 4px;
  }
  
  .stat-trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
    
    &.up {
      color: #DC2626;
      background: #FEF2F2;
    }
    
    &.down {
      color: #059669;
      background: #ECFDF5;
    }
  }
  
  &.warning .stat-icon {
    background: linear-gradient(135deg, #F59E0B, #FBBF24);
  }
  
  &.pending .stat-icon {
    background: linear-gradient(135deg, #8B5CF6, #A78BFA);
  }
  
  &.cost .stat-icon {
    background: linear-gradient(135deg, #1E3A8A, #3B82F6);
  }
  
  &.transfer .stat-icon {
    background: linear-gradient(135deg, #059669, #10B981);
  }
}

.main-content {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
  
  .chart-container {
    height: 300px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-actions-card {
  height: 400px;
  
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    padding: 8px 0;
  }
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #F3F4F6;
    }
    
    span {
      font-size: 13px;
      color: #374151;
    }
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    
    &.inbound {
      background: linear-gradient(135deg, #1E3A8A, #3B82F6);
    }
    
    &.outbound {
      background: linear-gradient(135deg, #059669, #10B981);
    }
    
    &.stock {
      background: linear-gradient(135deg, #8B5CF6, #A78BFA);
    }
    
    &.consumable {
      background: linear-gradient(135deg, #EC4899, #F472B6);
    }
    
    &.report {
      background: linear-gradient(135deg, #F59E0B, #FBBF24);
    }
    
    &.procurement {
      background: linear-gradient(135deg, #06B6D4, #22D3EE);
    }
  }
}

.bottom-content {
  .el-col {
    margin-bottom: 20px;
  }
}

.warning-card {
  .warning-icon {
    color: #F59E0B;
  }
  
  .low-stock {
    color: #DC2626;
    font-weight: 600;
  }
}

.todo-card {
  .todo-icon {
    color: #1E3A8A;
  }
  
  .todo-list {
    max-height: 280px;
    overflow-y: auto;
  }
  
  .todo-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    background: #F9FAFB;
    transition: all 0.2s;
    
    &:hover {
      background: #F3F4F6;
    }
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &.high {
      border-left: 3px solid #DC2626;
    }
    
    &.medium {
      border-left: 3px solid #F59E0B;
    }
    
    &.low {
      border-left: 3px solid #6B7280;
    }
  }
  
  .todo-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
  }
  
  .todo-title {
    font-size: 14px;
    color: #374151;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .todo-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .todo-time {
    font-size: 12px;
    color: #9CA3AF;
    white-space: nowrap;
  }
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-radio-button__inner) {
  border-radius: 0;
}

:deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 6px 0 0 6px;
}

:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 6px 6px 0;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #1E3A8A;
  border-color: #1E3A8A;
}
</style>