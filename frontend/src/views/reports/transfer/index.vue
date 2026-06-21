<template>
  <div class="page-container">
    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#1E3A8A', '--status-bg': '#EFF6FF' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Sort /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ filteredList.length }} <span class="unit">单</span></div>
            <div class="status-label">调拨单数</div>
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
            <div class="status-label">调拨总数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#F59E0B', '--status-bg': '#FFFBEB' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Money /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">¥{{ formatNumber(totalAmount) }}</div>
            <div class="status-label">调拨总金额</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#DC2626', '--status-bg': '#FEF2F2' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Clock /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ pendingCount }} <span class="unit">单</span></div>
            <div class="status-label">待签收</div>
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
        <el-form-item label="调出门店">
          <el-select v-model="queryForm.fromStoreId" placeholder="全部门店" clearable style="width: 160px">
            <el-option v-for="s in storeOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入门店">
          <el-select v-model="queryForm.toStoreId" placeholder="全部门店" clearable style="width: 160px">
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
        <el-form-item label="单据状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="c in Object.values(TRANSFER_STATUS_CONFIG)"
              :key="c.status"
              :label="c.label"
              :value="c.status"
            />
          </el-select>
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
            调拨报表明细
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
        <el-table-column label="调拨单号" width="160" prop="orderNo" fixed="left" />
        <el-table-column label="调出门店" width="110" prop="fromStoreName" />
        <el-table-column label="调入门店" width="110" prop="toStoreName" />
        <el-table-column label="耗材种类" width="100" align="center" prop="itemKindCount" />
        <el-table-column label="调拨数量" width="110" align="right">
          <template #default="{ row }">
            <span class="num-text">{{ formatQuantity(row.totalQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="调拨金额" width="130" align="right">
          <template #default="{ row }">
            <span class="num-text text-primary">¥{{ formatNumber(row.totalCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单据状态" width="120" align="center">
          <template #default="{ row }">
            <div
              class="status-badge"
              :style="{
                color: getStatusColor(row.status),
                backgroundColor: getStatusBgColor(row.status)
              }"
            >
              <span>{{ getStatusLabel(row.status) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160" align="center" prop="applyTime" />
        <el-table-column label="签收时间" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.receiveTime">{{ row.receiveTime }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="detailDialogVisible" title="调拨单详情" width="720px" :close-on-click-modal="false">
      <div v-if="currentOrder" class="detail-container">
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="调拨单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="单据状态">
            <el-tag :type="getStatusTagType(currentOrder.status)" size="small">{{ getStatusLabel(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调出门店">{{ currentOrder.fromStoreName }}</el-descriptions-item>
          <el-descriptions-item label="调入门店">{{ currentOrder.toStoreName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentOrder.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">{{ currentOrder.receiveTime || '未签收' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">调拨明细</el-divider>
        <el-table :data="currentOrder.items" border stripe size="small">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="耗材名称" prop="materialName" min-width="140" />
          <el-table-column label="规格" prop="specification" width="120" />
          <el-table-column label="单位" prop="unit" width="70" align="center" />
          <el-table-column label="数量" prop="quantity" width="90" align="right" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.cost) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.quantity * row.cost) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <ExportConfirmDialog
      v-model="exportDialogVisible"
      report-type="调拨报表"
      :scope-items="scopeItems"
      :data-count="filteredList.length"
      extra-info="按调拨单维度汇总门店间调拨的数量、金额与签收状态。"
      @confirm="confirmExport"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import ReportScopeSummary from '@/components/report/ReportScopeSummary.vue'
import ExportConfirmDialog from '@/components/report/ExportConfirmDialog.vue'

type TransferStatus = 'pending' | 'outbound' | 'receiving' | 'completed' | 'rejected'

interface TransferStatusConfig {
  status: TransferStatus
  label: string
  color: string
  bgColor: string
  tagType: '' | 'success' | 'warning' | 'danger' | 'info'
}

const TRANSFER_STATUS_CONFIG: Record<TransferStatus, TransferStatusConfig> = {
  pending: { status: 'pending', label: '待审核', color: '#6B7280', bgColor: '#F3F4F6', tagType: 'info' },
  outbound: { status: 'outbound', label: '待出库', color: '#F59E0B', bgColor: '#FFFBEB', tagType: 'warning' },
  receiving: { status: 'receiving', label: '待签收', color: '#1E3A8A', bgColor: '#EFF6FF', tagType: '' },
  completed: { status: 'completed', label: '已签收', color: '#059669', bgColor: '#ECFDF5', tagType: 'success' },
  rejected: { status: 'rejected', label: '已驳回', color: '#DC2626', bgColor: '#FEF2F2', tagType: 'danger' }
}

interface TransferItem {
  materialName: string
  specification: string
  unit: string
  quantity: number
  cost: number
}

interface TransferRow {
  id: number
  orderNo: string
  fromStoreId: number
  fromStoreName: string
  toStoreId: number
  toStoreName: string
  itemKindCount: number
  totalQuantity: number
  totalCost: number
  status: TransferStatus
  applyTime: string
  receiveTime: string
  items: TransferItem[]
}

const storeOptions = [
  { label: '总店', value: 1 },
  { label: '分店A', value: 2 },
  { label: '分店B', value: 3 },
  { label: '分店C', value: 4 }
]

const getStoreName = (id?: number) => storeOptions.find(s => s.value === id)?.label

const getStatusLabel = (s: TransferStatus) => TRANSFER_STATUS_CONFIG[s].label
const getStatusColor = (s: TransferStatus) => TRANSFER_STATUS_CONFIG[s].color
const getStatusBgColor = (s: TransferStatus) => TRANSFER_STATUS_CONFIG[s].bgColor
const getStatusTagType = (s: TransferStatus) => TRANSFER_STATUS_CONFIG[s].tagType

const queryForm = reactive({
  fromStoreId: undefined as number | undefined,
  toStoreId: undefined as number | undefined,
  dateRange: [] as string[],
  status: undefined as TransferStatus | undefined
})

const pagination = reactive({ page: 1, pageSize: 20 })
const exportDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentOrder = ref<TransferRow | null>(null)

const reportList = ref<TransferRow[]>([
  {
    id: 1, orderNo: 'DB20260620001', fromStoreId: 1, fromStoreName: '总店', toStoreId: 2, toStoreName: '分店A',
    itemKindCount: 2, totalQuantity: 60, totalCost: 660, status: 'completed',
    applyTime: '2026-06-18 09:20', receiveTime: '2026-06-19 14:30',
    items: [
      { materialName: 'A4打印纸', specification: '70g/500张', unit: '包', quantity: 20, cost: 25 },
      { materialName: '签字笔', specification: '0.5mm黑色', unit: '支', quantity: 40, cost: 0.4 }
    ]
  },
  {
    id: 2, orderNo: 'DB20260620002', fromStoreId: 1, fromStoreName: '总店', toStoreId: 3, toStoreName: '分店B',
    itemKindCount: 1, totalQuantity: 30, totalCost: 540, status: 'receiving',
    applyTime: '2026-06-19 10:05', receiveTime: '',
    items: [
      { materialName: '清洁剂', specification: '500ml', unit: '瓶', quantity: 30, cost: 18 }
    ]
  },
  {
    id: 3, orderNo: 'DB20260620003', fromStoreId: 2, fromStoreName: '分店A', toStoreId: 4, toStoreName: '分店C',
    itemKindCount: 3, totalQuantity: 80, totalCost: 760, status: 'outbound',
    applyTime: '2026-06-17 15:40', receiveTime: '',
    items: [
      { materialName: '垃圾袋', specification: '大号黑色', unit: '卷', quantity: 30, cost: 12 },
      { materialName: '白板笔', specification: '黑色', unit: '支', quantity: 30, cost: 4 },
      { materialName: '橡皮擦', specification: '白色', unit: '块', quantity: 20, cost: 5 }
    ]
  },
  {
    id: 4, orderNo: 'DB20260620004', fromStoreId: 3, fromStoreName: '分店B', toStoreId: 1, toStoreName: '总店',
    itemKindCount: 1, totalQuantity: 5, totalCost: 225, status: 'pending',
    applyTime: '2026-06-20 08:30', receiveTime: '',
    items: [
      { materialName: '螺丝刀套装', specification: '6件套', unit: '套', quantity: 5, cost: 45 }
    ]
  },
  {
    id: 5, orderNo: 'DB20260620005', fromStoreId: 1, fromStoreName: '总店', toStoreId: 4, toStoreName: '分店C',
    itemKindCount: 2, totalQuantity: 120, totalCost: 1100, status: 'completed',
    applyTime: '2026-06-15 11:00', receiveTime: '2026-06-16 16:20',
    items: [
      { materialName: '纸箱', specification: '40x30x20cm', unit: '个', quantity: 100, cost: 5 },
      { materialName: '封箱胶带', specification: '60mm宽', unit: '卷', quantity: 20, cost: 30 }
    ]
  },
  {
    id: 6, orderNo: 'DB20260620006', fromStoreId: 2, fromStoreName: '分店A', toStoreId: 3, toStoreName: '分店B',
    itemKindCount: 2, totalQuantity: 45, totalCost: 540, status: 'rejected',
    applyTime: '2026-06-14 13:25', receiveTime: '',
    items: [
      { materialName: '扫把', specification: '软毛', unit: '把', quantity: 15, cost: 22 },
      { materialName: '拖把', specification: '棉头', unit: '把', quantity: 10, cost: 21 }
    ]
  },
  {
    id: 7, orderNo: 'DB20260620007', fromStoreId: 4, fromStoreName: '分店C', toStoreId: 1, toStoreName: '总店',
    itemKindCount: 1, totalQuantity: 50, totalCost: 200, status: 'completed',
    applyTime: '2026-06-12 09:00', receiveTime: '2026-06-13 10:10',
    items: [
      { materialName: '文件夹', specification: 'A4双夹', unit: '个', quantity: 50, cost: 4 }
    ]
  },
  {
    id: 8, orderNo: 'DB20260620008', fromStoreId: 1, fromStoreName: '总店', toStoreId: 2, toStoreName: '分店A',
    itemKindCount: 2, totalQuantity: 70, totalCost: 580, status: 'receiving',
    applyTime: '2026-06-20 16:45', receiveTime: '',
    items: [
      { materialName: '胶带', specification: '48mm宽', unit: '卷', quantity: 30, cost: 8 },
      { materialName: '剪刀', specification: '不锈钢', unit: '把', quantity: 20, cost: 17 }
    ]
  }
])

const filteredList = computed(() => {
  return reportList.value.filter(item => {
    if (queryForm.fromStoreId !== undefined && item.fromStoreId !== queryForm.fromStoreId) return false
    if (queryForm.toStoreId !== undefined && item.toStoreId !== queryForm.toStoreId) return false
    if (queryForm.status !== undefined && item.status !== queryForm.status) return false
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      const [start, end] = queryForm.dateRange
      const applyDay = item.applyTime.slice(0, 10)
      if (applyDay < start || applyDay > end) return false
    }
    return true
  })
})

const pagedList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

const totalAmount = computed(() => filteredList.value.reduce((s, i) => s + i.totalCost, 0))
const filteredAmount = computed(() => filteredList.value.reduce((s, i) => s + i.totalCost, 0))
const totalQuantity = computed(() => filteredList.value.reduce((s, i) => s + i.totalQuantity, 0))
const pendingCount = computed(() => filteredList.value.filter(i => i.status === 'receiving' || i.status === 'outbound').length)

const scopeItems = computed(() => {
  const items: Array<{ label: string; value: string; tagType?: string }> = []
  items.push({ label: '调出门店', value: getStoreName(queryForm.fromStoreId) || '全部门店', tagType: queryForm.fromStoreId ? 'primary' : 'info' })
  items.push({ label: '调入门店', value: getStoreName(queryForm.toStoreId) || '全部门店', tagType: queryForm.toStoreId ? 'primary' : 'info' })
  if (queryForm.dateRange && queryForm.dateRange.length === 2) {
    items.push({ label: '日期范围', value: `${queryForm.dateRange[0]} 至 ${queryForm.dateRange[1]}`, tagType: 'primary' })
  } else {
    items.push({ label: '日期范围', value: '全部日期', tagType: 'info' })
  }
  items.push({ label: '单据状态', value: queryForm.status ? getStatusLabel(queryForm.status) : '全部状态', tagType: queryForm.status ? 'warning' : 'info' })
  return items
})

const formatNumber = (num: number) => num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatQuantity = (num: number) => num.toLocaleString('zh-CN')

const getSummaries = ({ columns, data }: { columns: any[]; data: TransferRow[] }) => {
  const sums: any[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'orderNo') {
      sums[index] = `共 ${data.length} 单`
    } else if (column.property === 'totalQuantity') {
      sums[index] = formatQuantity(data.reduce((s, r) => s + r.totalQuantity, 0))
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
  queryForm.fromStoreId = undefined
  queryForm.toStoreId = undefined
  queryForm.dateRange = []
  queryForm.status = undefined
  pagination.page = 1
  ElMessage.info('已重置筛选条件')
}

const handleExport = () => {
  exportDialogVisible.value = true
}

const confirmExport = () => {
  ElMessage.success(`正在按口径导出调拨报表，共 ${filteredList.value.length} 条数据...`)
  exportDialogVisible.value = false
}

const handlePrint = () => {
  ElMessage.info('正在准备打印...')
}

const handleViewDetail = (row: TransferRow) => {
  currentOrder.value = { ...row, items: [...row.items] }
  detailDialogVisible.value = true
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

.text-muted {
  color: #9ca3af;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
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
