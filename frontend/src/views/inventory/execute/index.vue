<template>
  <div class="page-container">
    <el-card class="plan-select-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">盘点计划选择</span>
        </div>
      </template>
      <el-form :model="queryForm" inline>
        <el-form-item label="盘点计划">
          <el-select
            v-model="queryForm.planId"
            placeholder="请选择盘点计划"
            style="width: 320px"
            filterable
            @change="handlePlanChange"
          >
            <el-option
              v-for="plan in planList"
              :key="plan.id"
              :label="`${plan.planNo} - ${plan.planName}`"
              :value="plan.id"
            >
              <span>{{ plan.planNo }} - {{ plan.planName }}</span>
              <el-tag
                :type="getInventoryStatusType(plan.status) as any"
                size="small"
                style="margin-left: 8px"
              >
                {{ getInventoryStatusText(plan.status) }}
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="mb-20">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="28"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalItems }}</div>
            <div class="stat-label">计划明细总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card entered">
          <div class="stat-icon">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.enteredItems }}</div>
            <div class="stat-label">已录入数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card pending">
          <div class="stat-icon">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.unenteredItems }}</div>
            <div class="stat-label">未录入数量</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card diff">
          <div class="stat-icon">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.diffItems }}</div>
            <div class="stat-label">存在差异数量</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :xs="24" :lg="12">
        <el-card class="progress-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><DataLine /></el-icon>
                盘点完成进度
              </span>
              <span class="progress-percentage">{{ progressPercent }}%</span>
            </div>
          </template>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="24"
            :color="progressColor"
            :text-inside="true"
            status="success"
          />
          <div class="progress-detail">
            <div class="detail-item">
              <span class="dot entered"></span>
              <span>已录入：{{ stats.enteredItems }} 项</span>
            </div>
            <div class="detail-item">
              <span class="dot pending"></span>
              <span>未录入：{{ stats.unenteredItems }} 项</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="amount-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><Money /></el-icon>
                差异金额统计
              </span>
            </div>
          </template>
          <div class="amount-stats">
            <div class="amount-row profit">
              <div class="amount-label">
                <el-icon><Top /></el-icon>
                盘盈金额
              </div>
              <div class="amount-value">+¥{{ formatNumber(stats.profitAmount) }}</div>
            </div>
            <div class="amount-row loss">
              <div class="amount-label">
                <el-icon><Bottom /></el-icon>
                盘亏金额
              </div>
              <div class="amount-value">-¥{{ formatNumber(stats.lossAmount) }}</div>
            </div>
            <el-divider />
            <div class="amount-row total-diff" :class="{ positive: stats.totalDiffAmount >= 0, negative: stats.totalDiffAmount < 0 }">
              <div class="amount-label">
                <el-icon><Scale /></el-icon>
                净差异金额
              </div>
              <div class="amount-value">
                {{ stats.totalDiffAmount >= 0 ? '+' : '' }}¥{{ formatNumber(stats.totalDiffAmount) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><List /></el-icon>
            盘点明细
          </span>
          <div class="filter-tabs">
            <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="entered">已录入</el-radio-button>
              <el-radio-button label="unentered">未录入</el-radio-button>
              <el-radio-button label="diff">存在差异</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredItems"
        style="width: 100%"
        stripe
        border
        height="500"
      >
        <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
        <el-table-column prop="consumableName" label="耗材名称" min-width="160" fixed="left" />
        <el-table-column prop="specification" label="规格" width="140" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="systemQuantity" label="系统库存" width="110" align="right">
          <template #default="{ row }">
            <span class="num-text">{{ row.systemQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-if="row.entered"
              v-model="row.actualQuantity"
              :min="0"
              :max="99999"
              size="small"
              :controls="false"
              @change="handleQuantityChange(row as InventoryExecuteItem)"
            />
            <span v-else class="unentered-text">待录入</span>
          </template>
        </el-table-column>
        <el-table-column label="差异数量" width="110" align="right">
          <template #default="{ row }">
            <span
              v-if="row.entered"
              class="diff-qty"
              :class="{
                positive: row.differenceQuantity > 0,
                negative: row.differenceQuantity < 0,
                zero: row.differenceQuantity === 0
              }"
            >
              {{ row.differenceQuantity > 0 ? '+' : '' }}{{ row.differenceQuantity }}
            </span>
            <span v-else class="unentered-text">-</span>
          </template>
        </el-table-column>
        <el-table-column label="差异金额" width="120" align="right">
          <template #default="{ row }">
            <span
              v-if="row.entered"
              class="diff-amount"
              :class="{
                positive: row.differenceAmount > 0,
                negative: row.differenceAmount < 0,
                zero: row.differenceAmount === 0
              }"
            >
              {{ row.differenceAmount > 0 ? '+' : '' }}¥{{ formatNumber(row.differenceAmount) }}
            </span>
            <span v-else class="unentered-text">-</span>
          </template>
        </el-table-column>
        <el-table-column label="录入状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.entered" type="success" size="small" effect="light">
              <el-icon style="margin-right: 2px"><CircleCheck /></el-icon>
              已录入
            </el-tag>
            <el-tag v-else type="info" size="small" effect="light">
              <el-icon style="margin-right: 2px"><Clock /></el-icon>
              未录入
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异状态" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.entered">
              <el-tag v-if="row.differenceQuantity === 0" type="success" size="small" effect="plain">
                一致
              </el-tag>
              <el-tag v-else-if="row.differenceQuantity > 0" type="warning" size="small" effect="plain">
                盘盈
              </el-tag>
              <el-tag v-else type="danger" size="small" effect="plain">
                盘亏
              </el-tag>
            </template>
            <span v-else class="unentered-text">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.entered"
              type="primary"
              link
              size="small"
              @click="openEntryDialog(row as InventoryExecuteItem)"
            >
              <el-icon><EditPen /></el-icon>
              录入
            </el-button>
            <el-button
              v-else
              type="primary"
              link
              size="small"
              @click="openEntryDialog(row as InventoryExecuteItem)"
            >
              <el-icon><Edit /></el-icon>
              修改
            </el-button>
            <el-button
              v-if="row.entered"
              type="danger"
              link
              size="small"
              @click="clearEntry(row as InventoryExecuteItem)"
            >
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredItems.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          small
        />
      </div>
    </el-card>

    <el-dialog
      v-model="entryDialogVisible"
      title="录入实盘数量"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="currentItem!" :label-width="100" v-if="currentItem">
        <el-form-item label="耗材名称">
          <span>{{ currentItem!.consumableName }}</span>
        </el-form-item>
        <el-form-item label="规格">
          <span>{{ currentItem!.specification }} / {{ currentItem!.unit }}</span>
        </el-form-item>
        <el-form-item label="系统库存">
          <span class="num-text">{{ currentItem!.systemQuantity }} {{ currentItem!.unit }}</span>
        </el-form-item>
        <el-form-item label="系统单价">
          <span>¥{{ formatNumber(currentItem!.unitPrice) }} / {{ currentItem!.unit }}</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="实盘数量" required>
          <el-input-number
            v-model="currentItem!.actualQuantity"
            :min="0"
            :max="99999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="差异原因">
          <el-input
            v-model="currentItem!.differenceReason"
            type="textarea"
            :rows="3"
            placeholder="请输入差异原因（如有差异）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="entryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEntry">确认录入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInventoryStatusText,
  getInventoryStatusType,
  type InventoryPlan,
  type InventoryPlanItem
} from '@/api/inventory'

interface InventoryExecuteItem extends InventoryPlanItem {
  systemQuantity: number
  unitPrice: number
  actualQuantity: number
  differenceQuantity: number
  differenceAmount: number
  differenceReason?: string
  entered: boolean
}

const queryForm = reactive({
  planId: undefined as number | undefined
})

const planList = ref<InventoryPlan[]>([
  {
    id: 1,
    planNo: 'PD202401001',
    planName: '2024年1月总店全盘',
    storeId: 1,
    storeName: '总店',
    inventoryType: 'full',
    status: 'in_progress',
    planDate: '2024-01-15',
    executor: '张三',
    operator: 'admin',
    createdAt: '2024-01-10 09:00:00',
    updatedAt: '2024-01-15 10:30:00',
    items: []
  },
  {
    id: 2,
    planNo: 'PD202401002',
    planName: '分店A耗材抽盘',
    storeId: 2,
    storeName: '分店A',
    inventoryType: 'partial',
    status: 'pending',
    planDate: '2024-01-20',
    executor: '李四',
    operator: 'admin',
    createdAt: '2024-01-12 14:00:00',
    updatedAt: '2024-01-12 14:00:00',
    items: []
  }
])

const executeItems = ref<InventoryExecuteItem[]>([])
const filterStatus = ref('all')
const pagination = reactive({
  page: 1,
  pageSize: 20
})

const entryDialogVisible = ref(false)
const currentItem = ref<InventoryExecuteItem | null>(null)

const mockPlanItems: InventoryExecuteItem[] = [
  { id: 1, consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', unit: '包', categoryId: 1, categoryName: '办公用品', systemQuantity: 200, unitPrice: 25.00, actualQuantity: 195, differenceQuantity: -5, differenceAmount: -125.00, differenceReason: '自然损耗', entered: true },
  { id: 2, consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', unit: '支', categoryId: 1, categoryName: '办公用品', systemQuantity: 500, unitPrice: 2.50, actualQuantity: 512, differenceQuantity: 12, differenceAmount: 30.00, differenceReason: '之前盘点少计', entered: true },
  { id: 3, consumableId: 3, consumableName: '胶带', specification: '48mm宽', unit: '卷', categoryId: 1, categoryName: '办公用品', systemQuantity: 100, unitPrice: 8.00, actualQuantity: 100, differenceQuantity: 0, differenceAmount: 0, entered: true },
  { id: 4, consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', unit: '个', categoryId: 1, categoryName: '办公用品', systemQuantity: 80, unitPrice: 15.00, actualQuantity: 0, differenceQuantity: 0, differenceAmount: 0, entered: false },
  { id: 5, consumableId: 5, consumableName: '订书机', specification: '标准型', unit: '个', categoryId: 1, categoryName: '办公用品', systemQuantity: 30, unitPrice: 35.00, actualQuantity: 0, differenceQuantity: 0, differenceAmount: 0, entered: false },
  { id: 6, consumableId: 6, consumableName: '清洁剂', specification: '500ml', unit: '瓶', categoryId: 2, categoryName: '清洁用品', systemQuantity: 60, unitPrice: 18.00, actualQuantity: 55, differenceQuantity: -5, differenceAmount: -90.00, differenceReason: '已使用未入账', entered: true },
  { id: 7, consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', unit: '卷', categoryId: 2, categoryName: '清洁用品', systemQuantity: 150, unitPrice: 12.00, actualQuantity: 0, differenceQuantity: 0, differenceAmount: 0, entered: false },
  { id: 8, consumableId: 8, consumableName: '纸箱', specification: '40x30x20cm', unit: '个', categoryId: 3, categoryName: '包装材料', systemQuantity: 300, unitPrice: 5.00, actualQuantity: 298, differenceQuantity: -2, differenceAmount: -10.00, entered: true },
  { id: 9, consumableId: 9, consumableName: '螺丝刀套装', specification: '6件套', unit: '套', categoryId: 4, categoryName: '工具', systemQuantity: 20, unitPrice: 45.00, actualQuantity: 0, differenceQuantity: 0, differenceAmount: 0, entered: false },
  { id: 10, consumableId: 10, consumableName: '胶水', specification: '50ml', unit: '瓶', categoryId: 1, categoryName: '办公用品', systemQuantity: 120, unitPrice: 6.00, actualQuantity: 0, differenceQuantity: 0, differenceAmount: 0, entered: false },
  { id: 11, consumableId: 11, consumableName: '橡皮擦', specification: '白色', unit: '块', categoryId: 1, categoryName: '办公用品', systemQuantity: 200, unitPrice: 1.50, actualQuantity: 200, differenceQuantity: 0, differenceAmount: 0, entered: true },
  { id: 12, consumableId: 12, consumableName: '剪刀', specification: '不锈钢', unit: '把', categoryId: 1, categoryName: '办公用品', systemQuantity: 40, unitPrice: 12.00, actualQuantity: 38, differenceQuantity: -2, differenceAmount: -24.00, differenceReason: '遗失', entered: true }
]

const stats = computed(() => {
  const items = executeItems.value
  const totalItems = items.length
  const enteredItems = items.filter(i => i.entered).length
  const unenteredItems = totalItems - enteredItems
  const diffItems = items.filter(i => i.entered && i.differenceQuantity !== 0).length

  const profitAmount = items
    .filter(i => i.entered && i.differenceAmount > 0)
    .reduce((sum, i) => sum + i.differenceAmount, 0)
  const lossAmount = Math.abs(
    items
      .filter(i => i.entered && i.differenceAmount < 0)
      .reduce((sum, i) => sum + i.differenceAmount, 0)
  )
  const totalDiffAmount = profitAmount - lossAmount

  return {
    totalItems,
    enteredItems,
    unenteredItems,
    diffItems,
    profitAmount,
    lossAmount,
    totalDiffAmount
  }
})

const progressPercent = computed(() => {
  if (stats.value.totalItems === 0) return 0
  return Math.round((stats.value.enteredItems / stats.value.totalItems) * 100)
})

const progressColor = computed(() => {
  const pct = progressPercent.value
  if (pct >= 80) return '#059669'
  if (pct >= 50) return '#F59E0B'
  return '#DC2626'
})

const filteredItems = computed(() => {
  switch (filterStatus.value) {
    case 'entered':
      return executeItems.value.filter(i => i.entered)
    case 'unentered':
      return executeItems.value.filter(i => !i.entered)
    case 'diff':
      return executeItems.value.filter(i => i.entered && i.differenceQuantity !== 0)
    default:
      return executeItems.value
  }
})

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const handleQuantityChange = (row: InventoryExecuteItem) => {
  row.differenceQuantity = row.actualQuantity - row.systemQuantity
  row.differenceAmount = row.differenceQuantity * row.unitPrice
  if (row.actualQuantity !== undefined && row.actualQuantity !== null) {
    row.entered = true
  }
}

const handlePlanChange = () => {
  loadData()
}

const loadData = () => {
  executeItems.value = JSON.parse(JSON.stringify(mockPlanItems))
  if (!queryForm.planId && planList.value.length > 0) {
    queryForm.planId = planList.value[0].id
  }
  ElMessage.success('数据加载成功')
}

const refreshData = () => {
  loadData()
}

const handleFilterChange = () => {
  pagination.page = 1
}

const openEntryDialog = (row: InventoryExecuteItem) => {
  currentItem.value = { ...row }
  entryDialogVisible.value = true
}

const confirmEntry = () => {
  if (!currentItem.value) return

  const idx = executeItems.value.findIndex(i => i.id === currentItem.value!.id)
  if (idx !== -1) {
    const item = executeItems.value[idx]
    item.actualQuantity = currentItem.value.actualQuantity
    item.differenceReason = currentItem.value.differenceReason
    item.differenceQuantity = item.actualQuantity - item.systemQuantity
    item.differenceAmount = item.differenceQuantity * item.unitPrice
    item.entered = true

    ElMessage.success('录入成功')
  }
  entryDialogVisible.value = false
}

const clearEntry = (row: InventoryExecuteItem) => {
  ElMessageBox.confirm(
    `确定要清空「${row.consumableName}」的录入数据吗？`,
    '清空确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.actualQuantity = 0
    row.differenceQuantity = 0
    row.differenceAmount = 0
    row.differenceReason = undefined
    row.entered = false
    ElMessage.success('已清空录入数据')
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.plan-select-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
  }
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
    font-size: 28px;
    font-weight: 700;
    color: #1F2937;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 14px;
    color: #6B7280;
    margin-top: 4px;
  }

  &.total .stat-icon {
    background: linear-gradient(135deg, #1E3A8A, #3B82F6);
  }

  &.entered .stat-icon {
    background: linear-gradient(135deg, #059669, #10B981);
  }

  &.pending .stat-icon {
    background: linear-gradient(135deg, #6B7280, #9CA3AF);
  }

  &.diff .stat-icon {
    background: linear-gradient(135deg, #F59E0B, #FBBF24);
  }
}

.progress-card,
.amount-card {
  height: 100%;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-icon {
    color: #1E3A8A;
  }
}

.progress-card {
  .progress-percentage {
    font-size: 24px;
    font-weight: 700;
    color: #1E3A8A;
  }

  .progress-detail {
    margin-top: 20px;
    display: flex;
    gap: 24px;

    .detail-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #374151;

      .dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;

        &.entered {
          background: #059669;
        }

        &.pending {
          background: #9CA3AF;
        }
      }
    }
  }
}

.amount-card {
  .amount-stats {
    padding: 8px 0;

    .amount-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-radius: 8px;
      margin-bottom: 8px;

      .amount-label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #6B7280;
      }

      .amount-value {
        font-size: 20px;
        font-weight: 700;
      }

      &.profit {
        background: #ECFDF5;

        .amount-label {
          color: #059669;
        }

        .amount-value {
          color: #059669;
        }
      }

      &.loss {
        background: #FEF2F2;

        .amount-label {
          color: #DC2626;
        }

        .amount-value {
          color: #DC2626;
        }
      }

      &.total-diff {
        background: #EFF6FF;
        margin-top: 8px;

        .amount-label {
          color: #1E3A8A;
          font-weight: 600;
        }

        .amount-value {
          color: #1E3A8A;
          font-size: 22px;
        }

        &.positive .amount-value {
          color: #059669;
        }

        &.negative .amount-value {
          color: #DC2626;
        }
      }
    }
  }
}

.detail-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-icon {
    color: #1E3A8A;
  }

  .filter-tabs {
    display: flex;
    align-items: center;
  }

  .table-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
    border-top: 1px solid #F3F4F6;
    margin-top: 8px;
  }
}

.num-text {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-weight: 500;
  color: #1F2937;
}

.unentered-text {
  color: #9CA3AF;
  font-style: italic;
}

.diff-qty,
.diff-amount {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-weight: 600;

  &.positive {
    color: #059669;
  }

  &.negative {
    color: #DC2626;
  }

  &.zero {
    color: #6B7280;
    font-weight: 400;
  }
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  .el-table__header-wrapper {
    .el-table__header {
      th {
        background-color: #F3F4F6;
        color: #374151;
        font-weight: 600;
      }
    }
  }
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

:deep(.el-input-number) {
  width: 100%;
}
</style>
