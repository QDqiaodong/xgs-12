<template>
  <div class="page-container">
    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#1E3A8A', '--status-bg': '#EFF6FF' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Money /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">¥{{ formatNumber(totalAmount) }}</div>
            <div class="status-label">消耗总金额</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#059669', '--status-bg': '#ECFDF5' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Goods /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ formatQuantity(totalQuantity) }}</div>
            <div class="status-label">消耗总数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#F59E0B', '--status-bg': '#FFFBEB' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Collection /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ materialKindCount }} <span class="unit">种</span></div>
            <div class="status-label">涉及耗材数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#7C3AED', '--status-bg': '#F5F3FF' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ filteredList.length }} <span class="unit">笔</span></div>
            <div class="status-label">消耗记录数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><Filter /></el-icon>
            报表筛选
          </span>
        </div>
      </template>
      <el-form :model="queryForm" inline>
        <el-form-item label="门店">
          <el-select v-model="queryForm.storeId" placeholder="全部门店" clearable style="width: 160px">
            <el-option v-for="s in storeOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="耗材名称">
          <el-input v-model="queryForm.keyword" placeholder="耗材编码/名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
          <el-button type="warning" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ReportScopeSummary :items="scopeItems" class="mb-20" />

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><DataAnalysis /></el-icon>
            消耗报表明细
            <el-tag type="info" size="small" style="margin-left: 8px">
              共 {{ filteredList.length }} 条 | 总金额 ¥{{ formatNumber(filteredAmount) }}
            </el-tag>
          </span>
        </div>
      </template>

      <el-table
        :data="pagedList"
        style="width: 100%"
        stripe
        border
        height="560"
        show-summary
        :summary-method="getSummaries"
      >
        <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
        <el-table-column label="耗材编码" width="120" prop="materialCode" />
        <el-table-column label="耗材名称" min-width="160" prop="materialName" fixed="left" />
        <el-table-column label="规格" width="140" prop="specification" />
        <el-table-column label="单位" width="80" align="center" prop="unit" />
        <el-table-column label="分类" width="120" prop="categoryName" />
        <el-table-column label="门店" width="100" prop="storeName" />
        <el-table-column label="消耗数量" width="110" align="right">
          <template #default="{ row }">
            <span class="num-text">{{ formatQuantity(row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平均单价" width="120" align="right">
          <template #default="{ row }">
            <span class="num-text">¥{{ formatNumber(row.avgCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消耗金额" width="130" align="right">
          <template #default="{ row }">
            <span class="num-text text-primary">¥{{ formatNumber(row.totalCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额占比" width="140">
          <template #default="{ row }">
            <el-progress
              :percentage="Number(getPercentage(row.totalCost))"
              :stroke-width="12"
              :show-text="false"
              :color="getProgressColor(row.totalCost)"
            />
            <span class="progress-text">{{ getPercentage(row.totalCost) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="最近消耗日期" width="130" align="center" prop="consumeDate" />
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <ExportConfirmDialog
      v-model="exportDialogVisible"
      report-type="消耗报表"
      :scope-items="scopeItems"
      :data-count="filteredList.length"
      extra-info="按耗材维度汇总各门店在筛选周期内的消耗数量与金额。"
      @confirm="confirmExport"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ReportScopeSummary from '@/components/report/ReportScopeSummary.vue'
import ExportConfirmDialog from '@/components/report/ExportConfirmDialog.vue'

interface ConsumptionRow {
  id: number
  materialCode: string
  materialName: string
  specification: string
  unit: string
  categoryId: number
  categoryName: string
  storeId: number
  storeName: string
  quantity: number
  avgCost: number
  totalCost: number
  consumeDate: string
}

const storeOptions = [
  { label: '总店', value: 1 },
  { label: '分店A', value: 2 },
  { label: '分店B', value: 3 },
  { label: '分店C', value: 4 }
]

const categoryOptions = [
  { label: '办公用品', value: 1 },
  { label: '清洁用品', value: 2 },
  { label: '包装材料', value: 3 },
  { label: '工具', value: 4 }
]

const getStoreName = (id?: number) => storeOptions.find(s => s.value === id)?.label
const getCategoryName = (id?: number) => categoryOptions.find(c => c.value === id)?.label

const queryForm = reactive({
  storeId: undefined as number | undefined,
  dateRange: [] as string[],
  categoryId: undefined as number | undefined,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const exportDialogVisible = ref(false)

const reportList = ref<ConsumptionRow[]>([
  { id: 1, materialCode: 'HC001', materialName: 'A4打印纸', specification: '70g/500张', unit: '包', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 45, avgCost: 25.0, totalCost: 1125, consumeDate: '2026-06-18' },
  { id: 2, materialCode: 'HC002', materialName: '签字笔', specification: '0.5mm黑色', unit: '支', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 120, avgCost: 2.5, totalCost: 300, consumeDate: '2026-06-17' },
  { id: 3, materialCode: 'HC003', materialName: '胶带', specification: '48mm宽', unit: '卷', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 18, avgCost: 8.0, totalCost: 144, consumeDate: '2026-06-15' },
  { id: 4, materialCode: 'HC004', materialName: '文件夹', specification: 'A4双夹', unit: '个', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 22, avgCost: 15.0, totalCost: 330, consumeDate: '2026-06-12' },
  { id: 5, materialCode: 'HC006', materialName: '清洁剂', specification: '500ml', unit: '瓶', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 16, avgCost: 18.0, totalCost: 288, consumeDate: '2026-06-19' },
  { id: 6, materialCode: 'HC007', materialName: '垃圾袋', specification: '大号黑色', unit: '卷', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 30, avgCost: 12.0, totalCost: 360, consumeDate: '2026-06-16' },
  { id: 7, materialCode: 'HC008', materialName: '纸箱', specification: '40x30x20cm', unit: '个', categoryId: 3, categoryName: '包装材料', storeId: 1, storeName: '总店', quantity: 240, avgCost: 5.0, totalCost: 1200, consumeDate: '2026-06-20' },
  { id: 8, materialCode: 'HC009', materialName: '螺丝刀套装', specification: '6件套', unit: '套', categoryId: 4, categoryName: '工具', storeId: 3, storeName: '分店B', quantity: 2, avgCost: 45.0, totalCost: 90, consumeDate: '2026-06-10' },
  { id: 9, materialCode: 'HC010', materialName: '胶水', specification: '50ml', unit: '瓶', categoryId: 1, categoryName: '办公用品', storeId: 3, storeName: '分店B', quantity: 14, avgCost: 6.0, totalCost: 84, consumeDate: '2026-06-08' },
  { id: 10, materialCode: 'HC011', materialName: '橡皮擦', specification: '白色', unit: '块', categoryId: 1, categoryName: '办公用品', storeId: 2, storeName: '分店A', quantity: 60, avgCost: 1.5, totalCost: 90, consumeDate: '2026-06-14' },
  { id: 11, materialCode: 'HC012', materialName: '剪刀', specification: '不锈钢', unit: '把', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 8, avgCost: 12.0, totalCost: 96, consumeDate: '2026-06-06' },
  { id: 12, materialCode: 'HC013', materialName: '白板笔', specification: '黑色', unit: '支', categoryId: 1, categoryName: '办公用品', storeId: 4, storeName: '分店C', quantity: 25, avgCost: 4.0, totalCost: 100, consumeDate: '2026-06-11' },
  { id: 13, materialCode: 'HC015', materialName: '扫把', specification: '软毛', unit: '把', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 6, avgCost: 22.0, totalCost: 132, consumeDate: '2026-06-05' },
  { id: 14, materialCode: 'HC016', materialName: '封箱胶带', specification: '60mm宽', unit: '卷', categoryId: 3, categoryName: '包装材料', storeId: 1, storeName: '总店', quantity: 50, avgCost: 10.0, totalCost: 500, consumeDate: '2026-06-19' }
])

const filteredList = computed(() => {
  return reportList.value.filter(item => {
    if (queryForm.storeId !== undefined && item.storeId !== queryForm.storeId) return false
    if (queryForm.categoryId !== undefined && item.categoryId !== queryForm.categoryId) return false
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      const [start, end] = queryForm.dateRange
      if (item.consumeDate < start || item.consumeDate > end) return false
    }
    if (queryForm.keyword) {
      const kw = queryForm.keyword.trim().toLowerCase()
      if (!item.materialName.toLowerCase().includes(kw) && !item.materialCode.toLowerCase().includes(kw)) return false
    }
    return true
  })
})

const pagedList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

const totalAmount = computed(() => reportList.value.reduce((s, i) => s + i.totalCost, 0))
const filteredAmount = computed(() => filteredList.value.reduce((s, i) => s + i.totalCost, 0))
const totalQuantity = computed(() => filteredList.value.reduce((s, i) => s + i.quantity, 0))
const materialKindCount = computed(() => new Set(filteredList.value.map(i => i.materialId || i.materialCode)).size)

const scopeItems = computed(() => {
  const items: Array<{ label: string; value: string; tagType?: string }> = []
  items.push({ label: '门店', value: getStoreName(queryForm.storeId) || '全部门店', tagType: queryForm.storeId ? 'primary' : 'info' })
  if (queryForm.dateRange && queryForm.dateRange.length === 2) {
    items.push({ label: '日期范围', value: `${queryForm.dateRange[0]} 至 ${queryForm.dateRange[1]}`, tagType: 'primary' })
  } else {
    items.push({ label: '日期范围', value: '全部日期', tagType: 'info' })
  }
  items.push({ label: '分类', value: getCategoryName(queryForm.categoryId) || '全部分类', tagType: queryForm.categoryId ? 'primary' : 'info' })
  if (queryForm.keyword) {
    items.push({ label: '关键词', value: queryForm.keyword, tagType: 'warning' })
  }
  return items
})

const formatNumber = (num: number) => num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatQuantity = (num: number) => num.toLocaleString('zh-CN')

const getPercentage = (value: number): string => {
  const total = filteredAmount.value
  if (total === 0) return '0'
  return Math.abs((value / total) * 100).toFixed(1)
}

const getProgressColor = (value: number) => {
  const pct = Number(getPercentage(value))
  if (pct >= 20) return '#1E3A8A'
  if (pct >= 10) return '#3B82F6'
  if (pct >= 5) return '#60A5FA'
  return '#93C5FD'
}

const getSummaries = ({ columns, data }: { columns: any[]; data: ConsumptionRow[] }) => {
  const sums: any[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'materialName') {
      sums[index] = `共 ${data.length} 项`
    } else if (column.property === 'quantity') {
      sums[index] = formatQuantity(data.reduce((s, r) => s + r.quantity, 0))
    } else if (column.property === 'totalCost') {
      sums[index] = '¥' + formatNumber(data.reduce((s, r) => s + r.totalCost, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleQuery = () => {
  pagination.page = 1
  ElMessage.success('报表查询完成')
}

const handleReset = () => {
  queryForm.storeId = undefined
  queryForm.dateRange = []
  queryForm.categoryId = undefined
  queryForm.keyword = ''
  pagination.page = 1
  ElMessage.info('已重置筛选条件')
}

const handleExport = () => {
  exportDialogVisible.value = true
}

const confirmExport = () => {
  ElMessage.success(`正在按口径导出消耗报表，共 ${filteredList.value.length} 条数据...`)
  exportDialogVisible.value = false
}

const handlePrint = () => {
  ElMessage.info('正在准备打印...')
}
</script>

<style scoped lang="scss">
.status-stat {
  display: flex;
  align-items: center;
  padding: 20px;
  background: var(--status-bg);
  border-radius: 12px;
  border-left: 4px solid var(--status-color);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .status-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--status-color);
    margin-right: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .status-info {
    .status-count {
      font-size: 26px;
      font-weight: 700;
      color: var(--status-color);
      line-height: 1.1;
      font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    }

    .status-label {
      font-size: 14px;
      color: #6b7280;
      margin-top: 4px;
    }

    .unit {
      font-size: 14px;
      font-weight: 500;
      color: #6b7280;
    }
  }
}

.filter-card,
.table-card {
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
}

.num-text {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-weight: 500;
  color: #1f2937;

  &.text-primary {
    color: #1e3a8a;
    font-weight: 600;
  }
}

.progress-text {
  font-size: 12px;
  color: #6b7280;
  margin-left: 6px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
  margin-top: 8px;
}

:deep(.el-table) {
  .el-table__header-wrapper {
    .el-table__header {
      th {
        background-color: #f3f4f6;
        color: #374151;
        font-weight: 600;
      }
    }
  }
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
