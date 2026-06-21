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
        <el-form-item label="出库日期">
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

    <div class="card-list">
      <el-card
        v-for="order in pendingList"
        :key="order.id"
        class="order-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="order-info">
            <span class="order-no">{{ order.orderNo }}</span>
            <el-tag type="primary" size="small">待签收</el-tag>
          </div>
          <div class="order-time">
            <el-icon><Clock /></el-icon>
            出库时间：{{ order.outboundTime }}
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
                <span class="label">出库人：</span>
                <span class="value">{{ order.operator }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">运输方式：</span>
                <span class="value">{{ order.transportType || '自提' }}</span>
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
          <el-button type="success" size="small" @click="openReceiveDialog(order)">
            <el-icon><Check /></el-icon>
            确认签收
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="pendingList.length === 0" description="暂无待签收调拨单" :image-size="120" />
    </div>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">签收历史记录</span>
        </div>
      </template>

      <el-table :data="historyList" stripe size="small">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="调拨单号" width="150" />
        <el-table-column prop="sourceStoreName" label="调出门店" width="120" />
        <el-table-column label="差异情况" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.hasDifference" type="warning" size="small">有差异</el-tag>
            <el-tag v-else type="success" size="small">无差异</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应收数量" width="100" align="center">
          <template #default="{ row }">{{ row.totalQuantity }} 件</template>
        </el-table-column>
        <el-table-column label="实收数量" width="100" align="center">
          <template #default="{ row }">{{ row.receivedQuantity }} 件</template>
        </el-table-column>
        <el-table-column prop="receiver" label="签收人" width="100" />
        <el-table-column prop="receiveTime" label="签收时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">已签收</el-tag>
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
          <el-tag type="primary" size="small">待签收</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调出门店">{{ currentOrder?.sourceStoreName }}</el-descriptions-item>
        <el-descriptions-item label="调入门店">{{ currentOrder?.targetStoreName }}</el-descriptions-item>
        <el-descriptions-item label="出库人">{{ currentOrder?.operator }}</el-descriptions-item>
        <el-descriptions-item label="出库时间">{{ currentOrder?.outboundTime }}</el-descriptions-item>
        <el-descriptions-item label="运输方式" :span="2">{{ currentOrder?.transportType || '自提' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-items">
        <div class="detail-items-title">调拨耗材明细</div>
        <el-table :data="currentOrder?.items" stripe border size="small">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="应收数量" width="100" align="center" />
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
              <span class="label">应收总量：</span>
              <span class="value">{{ currentOrder?.totalQuantity }} 件</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item highlight">
              <span class="label">应收总成本：</span>
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
      <div class="print-container">
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
          <el-descriptions-item label="出库人">
            {{ printOrder?.operator }}
          </el-descriptions-item>
          <el-descriptions-item label="签收人">
            签字：____________
          </el-descriptions-item>
        </el-descriptions>

        <div class="print-items-title">耗材明细</div>
        <el-table :data="printOrder?.items" border size="small" class="print-table">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="quantity" label="应收数量" width="100" align="center" />
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
          <div class="remark-content">{{ printOrder?.transportType || '无' }}</div>
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
      v-model="receiveDialogVisible"
      title="确认签收"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="调拨单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="调出门店">{{ currentOrder?.sourceStoreName }}</el-descriptions-item>
        <el-descriptions-item label="应收数量">{{ currentOrder?.totalQuantity }} 件</el-descriptions-item>
        <el-descriptions-item label="应收成本">¥{{ currentOrder?.totalAmount?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>

      <div class="receive-items">
        <div class="section-title">实收数量录入</div>
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 12px"
        >
          请核对每一项耗材的实收数量，如有差异请填写差异原因。
        </el-alert>
        <el-table :data="receiveItems" stripe border size="small">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="140" />
          <el-table-column prop="specification" label="规格" width="90" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="quantity" label="应收数量" width="90" align="center" />
          <el-table-column label="实收数量" width="120" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.receivedQuantity"
                :min="0"
                :max="9999"
                size="small"
                style="width: 100%"
                @change="calculateDifference(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异数量" width="90" align="center">
            <template #default="{ row }">
              <span :class="{ 'diff-positive': row.difference > 0, 'diff-negative': row.difference < 0 }">
                {{ row.difference > 0 ? '+' : '' }}{{ row.difference }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="差异原因" width="150">
            <template #default="{ row }">
              <el-select
                v-model="row.differenceReason"
                placeholder="请选择"
                size="small"
                :disabled="row.difference === 0"
                clearable
              >
                <el-option label="运输损耗" value="transport_loss" />
                <el-option label="包装破损" value="package_damage" />
                <el-option label="数量短缺" value="shortage" />
                <el-option label="多发" value="overage" />
                <el-option label="其他" value="other" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="receive-summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">应收总量：</span>
              <span class="value">{{ totalExpectedQuantity }} 件</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">实收总量：</span>
              <span class="value highlight-green">{{ totalReceivedQuantity }} 件</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">总差异：</span>
              <span class="value" :class="{ 'diff-positive': totalDifference > 0, 'diff-negative': totalDifference < 0 }">
                {{ totalDifference > 0 ? '+' : '' }}{{ totalDifference }} 件
              </span>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-form
        ref="receiveFormRef"
        :model="receiveForm"
        :rules="receiveRules"
        label-width="100px"
        style="margin-top: 16px"
      >
        <el-form-item label="签收备注" prop="remark">
          <el-input
            v-model="receiveForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入签收备注，存在差异时请补充说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="receiveDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="receiveLoading" @click="handleReceive">
          确认签收
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

interface TransferItem {
  id: number
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  quantity: number
  receivedQuantity: number
  difference: number
  differenceReason?: string
  unitPrice: number
  totalPrice: number
}

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
  operator: string
  outboundTime: string
  transportType?: string
  items: TransferItem[]
}

const filterForm = reactive({
  orderNo: '',
  sourceStoreId: undefined as number | undefined,
  dateRange: [] as string[]
})

const receiveFormRef = ref<FormInstance>()
const receiveForm = reactive({
  remark: ''
})

const receiveRules: FormRules = {
  remark: [
    {
      validator: (_rule, value, callback) => {
        if (hasDifference.value && !value) {
          callback(new Error('存在签收差异时请输入备注'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const detailDialogVisible = ref(false)
const printDialogVisible = ref(false)
const receiveDialogVisible = ref(false)
const receiveLoading = ref(false)
const currentOrder = ref<TransferOrder | null>(null)
const printOrder = ref<TransferOrder | null>(null)

const currentDate = computed(() => {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
})

const pendingList = ref<TransferOrder[]>([
  {
    id: 1,
    orderNo: 'DB20240115001',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 2,
    targetStoreName: '分店A',
    status: 'outbound',
    totalAmount: 680.00,
    totalQuantity: 55,
    operator: '张三',
    outboundTime: '2024-01-15 14:30:00',
    transportType: '物流配送',
    items: [
      { id: 1, consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', unit: '包', quantity: 20, receivedQuantity: 20, difference: 0, unitPrice: 25.00, totalPrice: 500.00 },
      { id: 2, consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', unit: '支', quantity: 30, receivedQuantity: 30, difference: 0, unitPrice: 2.50, totalPrice: 75.00 },
      { id: 3, consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', unit: '个', quantity: 5, receivedQuantity: 5, difference: 0, unitPrice: 15.00, totalPrice: 75.00 }
    ]
  },
  {
    id: 2,
    orderNo: 'DB20240114002',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 3,
    targetStoreName: '分店B',
    status: 'outbound',
    totalAmount: 420.00,
    totalQuantity: 30,
    operator: '李四',
    outboundTime: '2024-01-14 16:20:00',
    transportType: '自提',
    items: [
      { id: 1, consumableId: 6, consumableName: '清洁剂', specification: '500ml', unit: '瓶', quantity: 10, receivedQuantity: 10, difference: 0, unitPrice: 18.00, totalPrice: 180.00 },
      { id: 2, consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', unit: '卷', quantity: 20, receivedQuantity: 20, difference: 0, unitPrice: 12.00, totalPrice: 240.00 }
    ]
  }
])

const historyList = ref([
  { orderNo: 'DB20240112001', sourceStoreName: '总店', totalQuantity: 40, receivedQuantity: 40, hasDifference: false, receiver: '当前用户', receiveTime: '2024-01-12 15:30:00' },
  { orderNo: 'DB20240111002', sourceStoreName: '分店A', totalQuantity: 25, receivedQuantity: 24, hasDifference: true, receiver: '当前用户', receiveTime: '2024-01-11 17:45:00' }
])

const totalExpectedQuantity = computed(() => {
  return currentOrder.value?.items.reduce((sum, item) => sum + item.quantity, 0) || 0
})

const totalReceivedQuantity = computed(() => {
  return currentOrder.value?.items.reduce((sum, item) => sum + item.receivedQuantity, 0) || 0
})

const totalDifference = computed(() => totalReceivedQuantity.value - totalExpectedQuantity.value)
const hasDifference = computed(() => totalDifference.value !== 0 || !!currentOrder.value?.items.some(item => item.difference !== 0))

const handleSearch = () => {
  ElMessage.info('搜索功能已触发')
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.sourceStoreId = undefined
  filterForm.dateRange = []
}

const viewDetail = (order: TransferOrder) => {
  currentOrder.value = cloneOrder(order)
  detailDialogVisible.value = true
}

const openPrintPreview = (order: TransferOrder) => {
  printOrder.value = cloneOrder(order)
  printDialogVisible.value = true
}

const openPrintPreviewFromDetail = () => {
  if (currentOrder.value) {
    printOrder.value = cloneOrder(currentOrder.value)
    detailDialogVisible.value = false
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
          <td class="label">出库人</td>
          <td>${order.operator || '-'}</td>
          <td class="label">签收人</td>
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
            <th style="width: 90px">应收数量</th>
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
        <div class="remark-title">运输方式：</div>
        <div class="remark-content">${order.transportType || '自提'}</div>
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

const openReceiveDialog = (order: TransferOrder) => {
  currentOrder.value = cloneOrder(order)
  receiveForm.remark = ''
  receiveDialogVisible.value = true
}

const calculateDifference = (row: TransferItem) => {
  row.difference = row.receivedQuantity - row.quantity
  if (row.difference === 0) {
    row.differenceReason = undefined
  }
}

const handleReceive = async () => {
  if (!currentOrder.value || !receiveFormRef.value) return

  await receiveFormRef.value.validate()

  const missingReason = currentOrder.value.items.some(item => item.difference !== 0 && !item.differenceReason)
  if (missingReason) {
    ElMessage.warning('存在差异的耗材必须选择差异原因')
    return
  }

  ElMessageBox.confirm(`确定签收调拨单 ${currentOrder.value.orderNo} 吗？`, '签收确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    receiveLoading.value = true
    setTimeout(() => {
      const order = currentOrder.value!
      receiveLoading.value = false
      receiveDialogVisible.value = false
      historyList.value.unshift({
        orderNo: order.orderNo,
        sourceStoreName: order.sourceStoreName,
        totalQuantity: totalExpectedQuantity.value,
        receivedQuantity: totalReceivedQuantity.value,
        hasDifference: hasDifference.value,
        receiver: '当前用户',
        receiveTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
      pendingList.value = pendingList.value.filter(item => item.id !== order.id)
      currentOrder.value = null
      ElMessage.success('签收成功')
    }, 800)
  }).catch(() => {})
}

const cloneOrder = (order: TransferOrder): TransferOrder => {
  return {
    ...order,
    items: order.items.map(item => ({ ...item }))
  }
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.filter-card,
.history-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.order-card {
  border-radius: 8px;
}

.card-header,
.order-info,
.order-time,
.card-footer {
  display: flex;
  align-items: center;
}

.card-header {
  justify-content: space-between;
}

.order-info,
.order-time,
.card-footer {
  gap: 10px;
}

.order-no,
.card-title {
  font-weight: 600;
  color: #1E3A8A;
}

.order-time,
.label {
  color: #6B7280;
  font-size: 13px;
}

.card-body {
  padding: 8px 0;
}

.info-item,
.summary-item {
  display: flex;
  align-items: center;
}

.value {
  color: #1F2937;
  margin-left: 8px;
}

.highlight,
.amount {
  color: #1E3A8A;
  font-weight: 600;
}

.highlight-green {
  color: #059669;
  font-weight: 600;
}

.detail-items,
.receive-items,
.detail-summary,
.receive-summary {
  margin-top: 16px;
}

.detail-items-title,
.section-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: #374151;
}

.diff-positive {
  color: #DC2626;
  font-weight: 600;
}

.diff-negative {
  color: #059669;
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
