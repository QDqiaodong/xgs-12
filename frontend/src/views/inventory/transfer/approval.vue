<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="调拨单号">
          <el-input v-model="filterForm.orderNo" placeholder="请输入调拨单号" clearable />
        </el-form-item>
        <el-form-item label="调入门店">
          <el-select v-model="filterForm.targetStoreId" placeholder="请选择门店" clearable>
            <el-option label="总店" :value="1" />
            <el-option label="分店A" :value="2" />
            <el-option label="分店B" :value="3" />
            <el-option label="分店C" :value="4" />
          </el-select>
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
            <el-tag type="warning" size="small">待审批</el-tag>
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
                <span class="label">申请人：</span>
                <span class="value">{{ order.operator }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="label">调拨原因：</span>
                <span class="value">{{ order.reason || '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="card-footer">
          <el-button type="primary" size="small" @click="viewDetail(order)">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <el-button type="success" size="small" @click="openApproveDialog(order, true)">
            <el-icon><Check /></el-icon>
            通过
          </el-button>
          <el-button type="danger" size="small" @click="openApproveDialog(order, false)">
            <el-icon><Close /></el-icon>
            驳回
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="pendingList.length === 0" description="暂无待审批调拨单" :image-size="120" />
    </div>

    <el-dialog
      v-model="detailDialogVisible"
      title="调拨单详情"
      width="700px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="调拨单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="warning" size="small">待审批</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="调出门店">{{ currentOrder?.sourceStoreName }}</el-descriptions-item>
        <el-descriptions-item label="调入门店">{{ currentOrder?.targetStoreName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentOrder?.operator }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentOrder?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="调拨原因" :span="2">{{ currentOrder?.reason || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-items">
        <div class="detail-items-title">调拨耗材明细</div>
        <el-table :data="currentOrder?.items" stripe border size="small">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="调拨数量" width="100" align="center" />
          <el-table-column label="单价(元)" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额(元)" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.totalPrice.toFixed(2) }}
            </template>
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
      </template>
    </el-dialog>

    <el-dialog
      v-model="approveDialogVisible"
      :title="approveAction === 'approve' ? '审批通过' : '审批驳回'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="approveFormRef"
        :model="approveForm"
        :rules="approveRules"
        label-width="100px"
      >
        <el-form-item label="调拨单号">
          <el-input :value="currentOrder?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="审批结果">
          <el-tag :type="approveAction === 'approve' ? 'success' : 'danger'" size="large">
            {{ approveAction === 'approve' ? '通过' : '驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批意见" prop="remark">
          <el-input
            v-model="approveForm.remark"
            type="textarea"
            :rows="4"
            :placeholder="approveAction === 'approve' ? '请输入审批意见（可选）' : '请输入驳回原因'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button
          :type="approveAction === 'approve' ? 'success' : 'danger'"
          :loading="approveLoading"
          @click="handleApprove"
        >
          确认{{ approveAction === 'approve' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">审批历史记录</span>
        </div>
      </template>

      <el-table :data="historyList" stripe size="small">
        <el-table-column prop="orderNo" label="调拨单号" width="150" />
        <el-table-column prop="targetStoreName" label="调入门店" width="120" />
        <el-table-column label="审批结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.approved ? 'success' : 'danger'" size="small">
              {{ row.approved ? '已通过' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="100" />
        <el-table-column prop="approveRemark" label="审批意见" min-width="150" />
        <el-table-column prop="approveTime" label="审批时间" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
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
  operator: string
  reason?: string
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
  unitPrice: number
  totalPrice: number
}

const filterForm = reactive({
  orderNo: '',
  targetStoreId: undefined as number | undefined
})

const approveFormRef = ref<FormInstance>()
const approveForm = reactive({
  remark: ''
})

const approveRules: FormRules = {
  remark: [
    { required: false, message: '请输入审批意见', trigger: 'blur' }
  ]
}

const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const approveAction = ref<'approve' | 'reject'>('approve')
const approveLoading = ref(false)
const currentOrder = ref<TransferOrder | null>(null)

const pendingList = ref<TransferOrder[]>([
  {
    id: 1,
    orderNo: 'DB20240115001',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 2,
    targetStoreName: '分店A',
    status: 'pending',
    totalAmount: 680.00,
    totalQuantity: 50,
    operator: '张三',
    reason: '分店A办公用品库存不足，需要补充',
    createdAt: '2024-01-15 10:30:00',
    items: [
      { id: 1, consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', unit: '包', quantity: 20, unitPrice: 25.00, totalPrice: 500.00 },
      { id: 2, consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', unit: '支', quantity: 30, unitPrice: 2.50, totalPrice: 75.00 },
      { id: 3, consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', unit: '个', quantity: 5, unitPrice: 15.00, totalPrice: 75.00 }
    ]
  },
  {
    id: 2,
    orderNo: 'DB20240114002',
    sourceStoreId: 1,
    sourceStoreName: '总店',
    targetStoreId: 3,
    targetStoreName: '分店B',
    status: 'pending',
    totalAmount: 420.00,
    totalQuantity: 30,
    operator: '李四',
    reason: '分店B清洁用品短缺',
    createdAt: '2024-01-14 15:20:00',
    items: [
      { id: 1, consumableId: 6, consumableName: '清洁剂', specification: '500ml', unit: '瓶', quantity: 10, unitPrice: 18.00, totalPrice: 180.00 },
      { id: 2, consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', unit: '卷', quantity: 20, unitPrice: 12.00, totalPrice: 240.00 }
    ]
  },
  {
    id: 3,
    orderNo: 'DB20240113003',
    sourceStoreId: 2,
    sourceStoreName: '分店A',
    targetStoreId: 4,
    targetStoreName: '分店C',
    status: 'pending',
    totalAmount: 280.00,
    totalQuantity: 20,
    operator: '王五',
    createdAt: '2024-01-13 09:15:00',
    items: [
      { id: 1, consumableId: 3, consumableName: '胶带', specification: '48mm宽', unit: '卷', quantity: 10, unitPrice: 8.00, totalPrice: 80.00 },
      { id: 2, consumableId: 8, consumableName: '纸箱', specification: '40x30x20cm', unit: '个', quantity: 10, unitPrice: 5.00, totalPrice: 50.00 }
    ]
  }
])

const historyList = ref([
  { orderNo: 'DB20240112001', targetStoreName: '分店A', approved: true, approver: '管理员', approveRemark: '同意调拨', approveTime: '2024-01-12 14:30:00' },
  { orderNo: 'DB20240111002', targetStoreName: '分店B', approved: false, approver: '管理员', approveRemark: '调拨数量过大，请重新评估', approveTime: '2024-01-11 16:45:00' },
  { orderNo: 'DB20240110003', targetStoreName: '分店C', approved: true, approver: '管理员', approveRemark: '同意', approveTime: '2024-01-10 10:20:00' }
])

const handleSearch = () => {
  ElMessage.info('搜索功能已触发')
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.targetStoreId = undefined
}

const viewDetail = (order: TransferOrder) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const openApproveDialog = (order: TransferOrder, approved: boolean) => {
  currentOrder.value = order
  approveAction.value = approved ? 'approve' : 'reject'
  approveForm.remark = ''
  approveDialogVisible.value = true
  
  if (!approved) {
    approveRules.remark = [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
  } else {
    approveRules.remark = [{ required: false, message: '请输入审批意见', trigger: 'blur' }]
  }
}

const handleApprove = async () => {
  if (!approveFormRef.value) return
  
  if (approveAction.value === 'reject') {
    await approveFormRef.value.validate()
  }
  
  ElMessageBox.confirm(
    `确定要${approveAction.value === 'approve' ? '通过' : '驳回'}调拨单 ${currentOrder.value?.orderNo} 吗？`,
    '审批确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: approveAction.value === 'approve' ? 'info' : 'warning'
    }
  ).then(() => {
    approveLoading.value = true
    setTimeout(() => {
      approveLoading.value = false
      approveDialogVisible.value = false
      
      const approved = approveAction.value === 'approve'
      ElMessage.success(approved ? '审批通过成功' : '审批驳回成功')
      
      historyList.value.unshift({
        orderNo: currentOrder.value!.orderNo,
        targetStoreName: currentOrder.value!.targetStoreName,
        approved,
        approver: '当前用户',
        approveRemark: approveForm.remark || (approved ? '同意' : '驳回'),
        approveTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
      
      pendingList.value = pendingList.value.filter(o => o.id !== currentOrder.value!.id)
      currentOrder.value = null
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
  margin-bottom: 20px;
  
  .filter-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
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
</style>