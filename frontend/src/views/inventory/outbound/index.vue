<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="调拨单号">
          <el-input v-model="filterForm.orderNo" placeholder="请输入调拨单号" clearable />
        </el-form-item>
        <el-form-item label="调出门店">
          <el-select v-model="filterForm.sourceStoreId" placeholder="请选择门店" clearable>
            <el-option label="总店" :value="1" />
            <el-option label="分店A" :value="2" />
            <el-option label="分店B" :value="3" />
            <el-option label="分店C" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="action-card">
      <div class="action-bar">
        <div class="batch-actions">
          <el-checkbox v-model="selectAll" :indeterminate="isIndeterminate" @change="handleSelectAll">
            全选
          </el-checkbox>
          <el-button
            type="primary"
            :disabled="selectedIds.length === 0"
            @click="handleBatchOutbound"
          >
            <el-icon><Upload /></el-icon>
            批量出库
          </el-button>
        </div>
        <div class="batch-info">
          <span class="text-muted">已选择 </span>
          <span class="highlight">{{ selectedIds.length }}</span>
          <span class="text-muted"> 项</span>
        </div>
      </div>
    </el-card>

    <div class="card-list">
      <el-card
        v-for="order in pendingList"
        :key="order.id"
        class="order-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="order-info">
            <el-checkbox
              :model-value="selectedIds.includes(order.id)"
              @change="(val: boolean) => handleSelect(order.id, val)"
            />
            <span class="order-no">{{ order.orderNo }}</span>
            <el-tag type="warning" size="small">待出库</el-tag>
          </div>
          <div class="order-time">
            <el-icon><Clock /></el-icon>
            {{ order.createdAt }}
          </div>
        </div>
        
        <el-divider />
        
        <div class="card-body">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">调出门店：</span>
                <span class="value">{{ order.sourceStoreName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">调入门店：</span>
                <span class="value highlight">{{ order.targetStoreName }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 12px">
            <el-col :span="8">
              <div class="info-item">
                <span class="label">耗材种类：</span>
                <span class="value">{{ order.items.length }} 种</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">调拨数量：</span>
                <span class="value">{{ order.totalQuantity }} 件</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <span class="label">调拨成本：</span>
                <span class="value amount">¥{{ order.totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 12px">
            <el-col :span="12">
              <div class="info-item">
                <span class="label">审批人：</span>
                <span class="value">{{ order.approver }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">审批意见：</span>
                <span class="value">{{ order.approveRemark || '同意' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="card-footer">
          <el-button type="primary" size="small" @click="viewDetail(order)">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <el-button type="warning" size="small" @click="openPrintPreview(order)">
            <el-icon><Printer /></el-icon>
            打印预览
          </el-button>
          <el-button type="success" size="small" @click="openOutboundDialog(order)">
            <el-icon><Check /></el-icon>
            确认出库
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="pendingList.length === 0" description="暂无待出库调拨单" :image-size="120" />
    </div>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">出库历史记录</span>
        </div>
      </template>

      <el-table :data="historyList" stripe size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="调拨单号" width="150" />
        <el-table-column prop="sourceStoreName" label="调出门店" width="120" />
        <el-table-column prop="targetStoreName" label="调入门店" width="120" />
        <el-table-column label="出库数量" width="100" align="center">
          <template #default="{ row }">{{ row.totalQuantity }} 件</template>
        </el-table-column>
        <el-table-column label="出库成本" width="120" align="right">
          <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="operator" label="出库人" width="100" />
        <el-table-column prop="outboundTime" label="出库时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">已出库</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="调拨单详情"
      width="700px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="调拨单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="warning" size="small">待出库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调出门店">{{ currentOrder?.sourceStoreName }}</el-descriptions-item>
        <el-descriptions-item label="调入门店">{{ currentOrder?.targetStoreName }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentOrder?.approver }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentOrder?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ currentOrder?.approveRemark || '同意' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-items">
        <div class="detail-items-title">调拨耗材明细</div>
        <el-table :data="currentOrder?.items" stripe border size="small">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="调拨数量" width="100" align="center" />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="{ 'low-stock': row.stock < row.quantity }">{{ row.stock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价(元)" width="100" align="right">
            <template #default="{ row }">¥{{ row.unitPrice.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="金额(元)" width="120" align="right">
            <template #default="{ row }">¥{{ row.totalPrice.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="detail-summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">耗材种类：</span>
              <span class="value">{{ currentOrder?.items?.length || 0 }} 种</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">调拨总量：</span>
              <span class="value">{{ currentOrder?.totalQuantity }} 件</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item highlight">
              <span class="label">调拨总成本：</span>
              <span class="value">¥{{ currentOrder?.totalAmount?.toFixed(2) }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="warning" @click="openPrintPreviewFromDetail">
          <el-icon><Printer /></el-icon>
          打印预览
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="printDialogVisible"
      title="调拨单打印预览"
      width="900px"
      destroy-on-close
      class="print-dialog"
    >
      <div class="print-container" id="printContent">
        <div class="print-header">
          <div class="print-title">耗材调拨单</div>
          <div class="print-subtitle">随货交接单</div>
        </div>

        <el-descriptions :column="2" border size="small" class="print-info">
          <el-descriptions-item label="调拨单号">
            <span class="print-order-no">{{ printOrder?.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="打印日期">
            {{ currentDate }}
          </el-descriptions-item>
          <el-descriptions-item label="调出门店">
            {{ printOrder?.sourceStoreName }}
          </el-descriptions-item>
          <el-descriptions-item label="调入门店">
            {{ printOrder?.targetStoreName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ printOrder?.approver }}
          </el-descriptions-item>
          <el-descriptions-item label="出库人">
            经办人：____________
          </el-descriptions-item>
        </el-descriptions>

        <div class="print-items-title">耗材明细</div>
        <el-table :data="printOrder?.items" border size="small" class="print-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="quantity" label="调拨数量" width="100" align="center" />
          <el-table-column label="实收数量" width="100" align="center">
            <template #default>
              <span class="blank-cell"></span>
            </template>
          </el-table-column>
          <el-table-column label="单价(元)" width="90" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额(元)" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.totalPrice.toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="print-summary">
          <div class="summary-item">
            <span class="label">合计种类：</span>
            <span class="value">{{ printOrder?.items?.length || 0 }} 种</span>
          </div>
          <div class="summary-item">
            <span class="label">合计数量：</span>
            <span class="value">{{ printOrder?.totalQuantity }} 件</span>
          </div>
          <div class="summary-item highlight">
            <span class="label">合计金额：</span>
            <span class="value">¥{{ printOrder?.totalAmount?.toFixed(2) }}</span>
          </div>
        </div>

        <div class="print-signature">
          <div class="signature-item">
            <span class="signature-label">调出门店经办人：</span>
            <span class="signature-line">签字：____________</span>
            <span class="signature-date">日期：____________</span>
          </div>
          <div class="signature-item">
            <span class="signature-label">调入门店签收人：</span>
            <span class="signature-line">签字：____________</span>
            <span class="signature-date">日期：____________</span>
          </div>
        </div>

        <div class="print-remark">
          <div class="remark-title">备注：</div>
          <div class="remark-content">{{ printOrder?.approveRemark || '无' }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="printDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="outboundDialogVisible"
      title="确认出库"
      width="500px"
      destroy-on-close
    >
      <el-alert
        v-if="hasLowStock"
        title="库存警告"
        type="warning"
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #default>
          部分耗材库存不足，请确认是否继续出库。
        </template>
      </el-alert>

      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="调拨单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="调入门店">{{ currentOrder?.targetStoreName }}</el-descriptions-item>
        <el-descriptions-item label="出库数量">{{ currentOrder?.totalQuantity }} 件</el-descriptions-item>
        <el-descriptions-item label="出库成本">¥{{ currentOrder?.totalAmount?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>

      <el-form
        ref="outboundFormRef"
        :model="outboundForm"
        :rules="outboundRules"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="出库备注" prop="remark">
          <el-input
            v-model="outboundForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入出库备注（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="openPrintPreviewFromOutbound">
          <el-icon><Printer /></el-icon>
          打印预览
        </el-button>
        <el-button type="success" :loading="outboundLoading" @click="handleOutbound">
          确认出库
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchDialogVisible"
      title="批量出库确认"
      width="500px"
      destroy-on-close
    >
      <el-alert
        title="批量操作确认"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #default>
          您即将对 <strong>{{ selectedIds.length }}</strong> 张调拨单执行出库操作。
        </template>
      </el-alert>

      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="调拨单号">
          <div class="order-no-list">
            <el-tag v-for="id in selectedIds" :key="id" size="small" style="margin-right: 6px; margin-bottom: 6px">
              {{ getOrderNoById(id) }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="总数量">{{ batchTotalQuantity }} 件</el-descriptions-item>
        <el-descriptions-item label="总成本">¥{{ batchTotalAmount.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="batchLoading" @click="handleBatchConfirm">
          确认批量出库
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

interface TransferOrder {
  id: number
  orderNo: string
  sourceStoreId: number
  sourceStoreName: string
  targetStoreId: number
  targetStoreName: string
  status: string
  totalAmount: number
  totalQuantity: number
  approver: string
  approveRemark?: string
  createdAt: string
  items: TransferItem[]
}

interface TransferItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  quantity: number
  stock: number
  unitPrice: number
  totalPrice: number
}

const filterForm = reactive({
  orderNo: '',
  sourceStoreId: undefined as number | undefined,
  dateRange: [] as string[]
})

const outboundFormRef = ref<FormInstance>()
const outboundForm = reactive({
  remark: ''
})

const outboundRules: FormRules = {
  remark: [{ required: false, message: '请输入出库备注', trigger: 'blur' }]
}

const detailDialogVisible = ref(false)
const printDialogVisible = ref(false)
const outboundDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const outboundLoading = ref(false)
const batchLoading = ref(false)
const currentOrder = ref<TransferOrder | null>(null)
const printOrder = ref<TransferOrder | null>(null)

const currentDate = computed(() => {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
})

const selectedIds = ref<number[]>([])
const selectAll = ref(false)
const isIndeterminate = ref(false)

const pendingList = ref<TransferOrder[]>([
  {
    id: 1,
    orderNo: 'DB20240115001',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 2,
    targetStoreName: '分店A',
    status: 'pending_outbound',
    totalAmount: 680.00,
    totalQuantity: 50,
    approver: '管理员',
    approveRemark: '同意调拨',
    createdAt: '2024-01-15 10:30:00',
    items: [
      { id: 1, consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', unit: '包', quantity: 20, stock: 15, unitPrice: 25.00, totalPrice: 500.00 },
      { id: 2, consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', unit: '支', quantity: 30, stock: 200, unitPrice: 2.50, totalPrice: 75.00 },
      { id: 3, consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', unit: '个', quantity: 5, stock: 50, unitPrice: 15.00, totalPrice: 75.00 }
    ]
  },
  {
    id: 2,
    orderNo: 'DB20240114002',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 3,
    targetStoreName: '分店B',
    status: 'pending_outbound',
    totalAmount: 420.00,
    totalQuantity: 30,
    approver: '管理员',
    createdAt: '2024-01-14 15:20:00',
    items: [
      { id: 1, consumableId: 6, consumableName: '清洁剂', specification: '500ml', unit: '瓶', quantity: 10, stock: 30, unitPrice: 18.00, totalPrice: 180.00 },
      { id: 2, consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', unit: '卷', quantity: 20, stock: 15, unitPrice: 12.00, totalPrice: 240.00 }
    ]
  },
  {
    id: 3,
    orderNo: 'DB20240113003',
    sourceStoreId: 2,
    sourceStoreName: '分店A',
    targetStoreId: 4,
    targetStoreName: '分店C',
    status: 'pending_outbound',
    totalAmount: 280.00,
    totalQuantity: 20,
    approver: '张经理',
    approveRemark: '同意调拨',
    createdAt: '2024-01-13 09:15:00',
    items: [
      { id: 1, consumableId: 3, consumableName: '胶带', specification: '48mm宽', unit: '卷', quantity: 10, stock: 25, unitPrice: 8.00, totalPrice: 80.00 },
      { id: 2, consumableId: 8, consumableName: '纸箱', specification: '40x30x20cm', unit: '个', quantity: 10, stock: 30, unitPrice: 5.00, totalPrice: 50.00 }
    ]
  }
])

const historyList = ref([
  { orderNo: 'DB20240112001', sourceStoreName: '总店', targetStoreName: '分店A', totalQuantity: 45, totalAmount: 520.00, operator: '张三', outboundTime: '2024-01-12 14:30:00' },
  { orderNo: 'DB20240111002', sourceStoreName: '总店', targetStoreName: '分店B', totalQuantity: 28, totalAmount: 380.00, operator: '李四', outboundTime: '2024-01-11 16:45:00' },
  { orderNo: 'DB20240110003', sourceStoreName: '分店A', targetStoreName: '分店C', totalQuantity: 35, totalAmount: 450.00, operator: '王五', outboundTime: '2024-01-10 10:20:00' }
])

const hasLowStock = computed(() => {
  if (!currentOrder.value) return false
  return currentOrder.value.items.some(item => item.stock < item.quantity)
})

const batchTotalQuantity = computed(() => {
  return pendingList.value
    .filter(o => selectedIds.value.includes(o.id))
    .reduce((sum, o) => sum + o.totalQuantity, 0)
})

const batchTotalAmount = computed(() => {
  return pendingList.value
    .filter(o => selectedIds.value.includes(o.id))
    .reduce((sum, o) => sum + o.totalAmount, 0)
})

const getOrderNoById = (id: number) => {
  const order = pendingList.value.find(o => o.id === id)
  return order?.orderNo || ''
}

const handleSearch = () => {
  ElMessage.info('搜索功能已触发')
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.sourceStoreId = undefined
  filterForm.dateRange = []
}

const handleSelect = (id: number, selected: boolean) => {
  if (selected) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value = selectedIds.value.filter(i => i !== id)
  }
  updateSelectAllState()
}

const handleSelectAll = (val: boolean) => {
  if (val) {
    selectedIds.value = pendingList.value.map(o => o.id)
    isIndeterminate.value = false
  } else {
    selectedIds.value = []
    isIndeterminate.value = false
  }
}

const updateSelectAllState = () => {
  const total = pendingList.value.length
  const selected = selectedIds.value.length
  selectAll.value = selected === total && total > 0
  isIndeterminate.value = selected > 0 && selected < total
}

const viewDetail = (order: TransferOrder) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const openPrintPreview = (order: TransferOrder) => {
  printOrder.value = order
  printDialogVisible.value = true
}

const openPrintPreviewFromDetail = () => {
  if (currentOrder.value) {
    printOrder.value = currentOrder.value
    detailDialogVisible.value = false
    printDialogVisible.value = true
  }
}

const openPrintPreviewFromOutbound = () => {
  if (currentOrder.value) {
    printOrder.value = currentOrder.value
    outboundDialogVisible.value = false
    printDialogVisible.value = true
  }
}

const generatePrintHTML = () => {
  if (!printOrder.value) return ''

  const order = printOrder.value
  const items = order.items || []

  let itemsHTML = ''
  items.forEach((item, index) => {
    itemsHTML += `
      <tr>
        <td>${index + 1}</td>
        <td class="text-left">${item.consumableName}</td>
        <td>${item.specification || '-'}</td>
        <td>${item.unit}</td>
        <td>${item.quantity}</td>
        <td></td>
        <td class="text-right">¥${item.unitPrice.toFixed(2)}</td>
        <td class="text-right">¥${item.totalPrice.toFixed(2)}</td>
      </tr>
    `
  })

  return `
    <!DOCTYPE html>
    <html>
    <head>
      <title>耗材调拨单 - ${order.orderNo}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
          font-size: 14px;
          color: #1F2937;
          padding: 30px;
          background: #fff;
        }
        .print-header {
          text-align: center;
          margin-bottom: 24px;
          padding-bottom: 16px;
          border-bottom: 2px solid #1E3A8A;
        }
        .print-title {
          font-size: 26px;
          font-weight: 700;
          color: #1E3A8A;
          margin-bottom: 8px;
          letter-spacing: 4px;
        }
        .print-subtitle {
          font-size: 16px;
          color: #6B7280;
          letter-spacing: 2px;
        }
        .print-info {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 24px;
        }
        .print-info td {
          border: 1px solid #D1D5DB;
          padding: 10px 12px;
        }
        .print-info .label {
          background-color: #F3F4F6;
          font-weight: 600;
          width: 100px;
          color: #374151;
        }
        .print-order-no {
          font-weight: 700;
          color: #1E3A8A;
          font-size: 15px;
        }
        .print-items-title {
          font-size: 16px;
          font-weight: 600;
          color: #1F2937;
          margin-bottom: 12px;
          padding-left: 12px;
          border-left: 4px solid #1E3A8A;
        }
        .print-table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 16px;
        }
        .print-table th {
          background-color: #F3F4F6;
          border: 1px solid #D1D5DB;
          padding: 10px 8px;
          text-align: center;
          font-weight: 600;
          color: #374151;
        }
        .print-table td {
          border: 1px solid #D1D5DB;
          padding: 10px 8px;
          text-align: center;
        }
        .print-table .text-right {
          text-align: right;
        }
        .print-table .text-left {
          text-align: left;
        }
        .print-summary {
          display: flex;
          justify-content: flex-end;
          gap: 40px;
          margin-bottom: 28px;
          padding: 14px 20px 14px 0;
          border-top: 1px solid #D1D5DB;
          border-bottom: 1px solid #D1D5DB;
          background: linear-gradient(135deg, #F9FAFB 0%, #F3F4F6 100%);
        }
        .summary-item {
          display: flex;
          align-items: center;
        }
        .summary-item .label {
          color: #6B7280;
          font-size: 14px;
        }
        .summary-item .value {
          font-size: 16px;
          font-weight: 600;
          color: #1F2937;
          margin-left: 8px;
        }
        .summary-item.highlight .value {
          color: #1E3A8A;
          font-size: 18px;
        }
        .print-signature {
          margin-top: 36px;
          display: flex;
          justify-content: space-between;
        }
        .signature-item {
          display: flex;
          align-items: center;
          gap: 10px;
        }
        .signature-label {
          font-weight: 600;
          color: #374151;
          font-size: 14px;
        }
        .signature-line,
        .signature-date {
          color: #6B7280;
          font-size: 14px;
        }
        .print-remark {
          margin-top: 28px;
          padding: 16px;
          background-color: #F9FAFB;
          border: 1px dashed #D1D5DB;
          border-radius: 6px;
        }
        .remark-title {
          font-weight: 600;
          color: #374151;
          margin-bottom: 8px;
          font-size: 14px;
        }
        .remark-content {
          color: #4B5563;
          font-size: 14px;
          line-height: 1.6;
        }
        @media print {
          body {
            padding: 15px;
          }
          @page {
            size: A4;
            margin: 15mm;
          }
        }
      </style>
    </head>
    <body>
      <div class="print-header">
        <div class="print-title">耗材调拨单</div>
        <div class="print-subtitle">随货交接单</div>
      </div>

      <table class="print-info">
        <tr>
          <td class="label">调拨单号</td>
          <td><span class="print-order-no">${order.orderNo}</span></td>
          <td class="label">打印日期</td>
          <td>${currentDate.value}</td>
        </tr>
        <tr>
          <td class="label">调出门店</td>
          <td>${order.sourceStoreName}</td>
          <td class="label">调入门店</td>
          <td>${order.targetStoreName}</td>
        </tr>
        <tr>
          <td class="label">审批人</td>
          <td>${order.approver || '-'}</td>
          <td class="label">经办人</td>
          <td>____________</td>
        </tr>
      </table>

      <div class="print-items-title">耗材明细</div>
      <table class="print-table">
        <thead>
          <tr>
            <th style="width: 60px">序号</th>
            <th>耗材名称</th>
            <th style="width: 100px">规格</th>
            <th style="width: 70px">单位</th>
            <th style="width: 90px">调拨数量</th>
            <th style="width: 90px">实收数量</th>
            <th style="width: 90px">单价(元)</th>
            <th style="width: 100px">金额(元)</th>
          </tr>
        </thead>
        <tbody>
          ${itemsHTML}
        </tbody>
      </table>

      <div class="print-summary">
        <div class="summary-item">
          <span class="label">合计种类：</span>
          <span class="value">${items.length} 种</span>
        </div>
        <div class="summary-item">
          <span class="label">合计数量：</span>
          <span class="value">${order.totalQuantity} 件</span>
        </div>
        <div class="summary-item highlight">
          <span class="label">合计金额：</span>
          <span class="value">¥${order.totalAmount.toFixed(2)}</span>
        </div>
      </div>

      <div class="print-signature">
        <div class="signature-item">
          <span class="signature-label">调出门店经办人：</span>
          <span class="signature-line">签字：____________</span>
          <span class="signature-date">日期：____________</span>
        </div>
        <div class="signature-item">
          <span class="signature-label">调入门店签收人：</span>
          <span class="signature-line">签字：____________</span>
          <span class="signature-date">日期：____________</span>
        </div>
      </div>

      <div class="print-remark">
        <div class="remark-title">备注：</div>
        <div class="remark-content">${order.approveRemark || '无'}</div>
      </div>
    </body>
    </html>
  `
}

const handlePrint = () => {
  const printHTML = generatePrintHTML()
  if (!printHTML) return

  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.warning('请允许弹出窗口以进行打印')
    return
  }

  printWindow.document.write(printHTML)
  printWindow.document.close()
  printWindow.focus()
  
  setTimeout(() => {
    printWindow.print()
  }, 300)
}

const openOutboundDialog = (order: TransferOrder) => {
  currentOrder.value = order
  outboundForm.remark = ''
  outboundDialogVisible.value = true
}

const handleOutbound = () => {
  ElMessageBox.confirm(
    `确定要对调拨单 ${currentOrder.value?.orderNo} 执行出库操作吗？`,
    '出库确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    outboundLoading.value = true
    setTimeout(() => {
      outboundLoading.value = false
      outboundDialogVisible.value = false
      
      ElMessage.success('出库成功')
      
      historyList.value.unshift({
        orderNo: currentOrder.value!.orderNo,
        sourceStoreName: currentOrder.value!.sourceStoreName,
        targetStoreName: currentOrder.value!.targetStoreName,
        totalQuantity: currentOrder.value!.totalQuantity,
        totalAmount: currentOrder.value!.totalAmount,
        operator: '当前用户',
        outboundTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
      
      pendingList.value = pendingList.value.filter(o => o.id !== currentOrder.value!.id)
      selectedIds.value = selectedIds.value.filter(id => id !== currentOrder.value!.id)
      updateSelectAllState()
      currentOrder.value = null
    }, 1000)
  }).catch(() => {})
}

const handleBatchOutbound = () => {
  batchDialogVisible.value = true
}

const handleBatchConfirm = () => {
  ElMessageBox.confirm(
    `确定要对选中的 ${selectedIds.value.length} 张调拨单执行批量出库吗？`,
    '批量出库确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    batchLoading.value = true
    setTimeout(() => {
      batchLoading.value = false
      batchDialogVisible.value = false
      
      const orders = pendingList.value.filter(o => selectedIds.value.includes(o.id))
      orders.forEach(order => {
        historyList.value.unshift({
          orderNo: order.orderNo,
          sourceStoreName: order.sourceStoreName,
          targetStoreName: order.targetStoreName,
          totalQuantity: order.totalQuantity,
          totalAmount: order.totalAmount,
          operator: '当前用户',
          outboundTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
        })
      })
      
      pendingList.value = pendingList.value.filter(o => !selectedIds.value.includes(o.id))
      selectedIds.value = []
      updateSelectAllState()
      
      ElMessage.success(`批量出库成功，共处理 ${orders.length} 张调拨单`)
    }, 1000)
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.filter-card {
  margin-bottom: 16px;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

.action-card {
  margin-bottom: 16px;
  
  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .batch-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .batch-info {
      .highlight {
        color: #1E3A8A;
        font-weight: 600;
        font-size: 16px;
      }
      
      .text-muted {
        color: #6B7280;
        font-size: 14px;
      }
    }
  }
}

.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.order-card {
  border-radius: 8px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .order-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .order-no {
        font-size: 16px;
        font-weight: 600;
        color: #1E3A8A;
      }
    }
    
    .order-time {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #6B7280;
    }
  }
  
  .card-body {
    padding: 8px 0;
    
    .info-item {
      display: flex;
      align-items: center;
      
      .label {
        font-size: 13px;
        color: #6B7280;
      }
      
      .value {
        font-size: 14px;
        color: #1F2937;
        margin-left: 8px;
        
        &.highlight {
          color: #1E3A8A;
          font-weight: 600;
        }
        
        &.amount {
          color: #1E3A8A;
          font-weight: 600;
        }
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 12px;
    border-top: 1px solid #E5E7EB;
  }
}

.detail-items {
  margin-top: 20px;
  
  .detail-items-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }
  
  .low-stock {
    color: #DC2626;
    font-weight: 600;
  }
}

.detail-summary {
  background: linear-gradient(135deg, #F0F7FF 0%, #E8F4FF 100%);
  padding: 16px 20px;
  border-radius: 8px;
  margin-top: 20px;
  
  .summary-item {
    display: flex;
    align-items: center;
    
    .label {
      font-size: 14px;
      color: #6B7280;
    }
    
    .value {
      font-size: 16px;
      font-weight: 600;
      color: #1F2937;
      margin-left: 8px;
    }
    
    &.highlight .value {
      color: #1E3A8A;
      font-size: 18px;
    }
  }
}

.history-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1F2937;
  }
}

.order-no-list {
  display: flex;
  flex-wrap: wrap;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-divider) {
  margin: 12px 0;
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

:deep(.el-descriptions__label) {
  font-weight: 600;
}

:deep(.print-dialog) {
  .el-dialog__body {
    padding: 20px;
  }
}

.print-container {
  background: #fff;
  padding: 30px;
  border: 1px solid #E5E7EB;
}

.print-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #1E3A8A;
}

.print-title {
  font-size: 26px;
  font-weight: 700;
  color: #1E3A8A;
  margin-bottom: 8px;
  letter-spacing: 4px;
}

.print-subtitle {
  font-size: 16px;
  color: #6B7280;
  letter-spacing: 2px;
}

.print-info {
  margin-bottom: 24px;
  
  :deep(.el-descriptions__label) {
    width: 100px;
    font-weight: 600;
    background-color: #F3F4F6;
  }
}

.print-order-no {
  font-weight: 700;
  color: #1E3A8A;
  font-size: 15px;
}

.print-items-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 4px solid #1E3A8A;
}

.print-table {
  margin-bottom: 16px;
  
  :deep(.el-table__header) {
    th {
      background-color: #F3F4F6;
      color: #374151;
      font-weight: 600;
    }
  }
}

.blank-cell {
  display: inline-block;
  width: 60px;
  height: 20px;
  border-bottom: 1px dashed #9CA3AF;
}

.print-summary {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  margin-bottom: 28px;
  padding: 14px 0;
  border-top: 1px solid #D1D5DB;
  border-bottom: 1px solid #D1D5DB;
  background: linear-gradient(135deg, #F9FAFB 0%, #F3F4F6 100%);
  padding-right: 20px;
  
  .summary-item {
    display: flex;
    align-items: center;
    
    .label {
      font-size: 14px;
      color: #6B7280;
    }
    
    .value {
      font-size: 16px;
      font-weight: 600;
      color: #1F2937;
      margin-left: 8px;
    }
    
    &.highlight .value {
      color: #1E3A8A;
      font-size: 18px;
    }
  }
}

.print-signature {
  margin-top: 36px;
  display: flex;
  justify-content: space-between;
  
  .signature-item {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .signature-label {
      font-weight: 600;
      color: #374151;
      font-size: 14px;
    }
    
    .signature-line,
    .signature-date {
      color: #6B7280;
      font-size: 14px;
    }
  }
}

.print-remark {
  margin-top: 28px;
  padding: 16px;
  background-color: #F9FAFB;
  border: 1px dashed #D1D5DB;
  border-radius: 6px;
  
  .remark-title {
    font-weight: 600;
    color: #374151;
    margin-bottom: 8px;
    font-size: 14px;
  }
  
  .remark-content {
    color: #4B5563;
    font-size: 14px;
    line-height: 1.6;
  }
}
</style>
