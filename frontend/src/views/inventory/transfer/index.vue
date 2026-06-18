<template>
  <div class="page-container">
    <el-card class="step-card">
      <el-steps :active="currentStep" align-center finish-status="success">
        <el-step title="选择门店" description="选择调拨目标门店" />
        <el-step title="添加耗材" description="选择调拨耗材明细" />
        <el-step title="确认提交" description="确认并提交申请" />
      </el-steps>
    </el-card>

    <el-card class="content-card">
      <div v-show="currentStep === 0" class="step-content">
        <div class="step-title">选择目标门店</div>
        <el-form
          ref="storeFormRef"
          :model="storeForm"
          :rules="storeRules"
          label-width="120px"
          class="store-form"
        >
          <el-form-item label="调出门店" prop="sourceStoreId">
            <el-select v-model="storeForm.sourceStoreId" placeholder="请选择调出门店" disabled>
              <el-option
                v-for="store in storeList"
                :key="store.id"
                :label="store.name"
                :value="store.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调入门店" prop="targetStoreId">
            <el-select
              v-model="storeForm.targetStoreId"
              placeholder="请选择调入门店"
              filterable
            >
              <el-option
                v-for="store in targetStoreList"
                :key="store.id"
                :label="store.name"
                :value="store.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="调拨原因" prop="reason">
            <el-input
              v-model="storeForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入调拨原因"
            />
          </el-form-item>
        </el-form>
      </div>

      <div v-show="currentStep === 1" class="step-content">
        <div class="step-title">添加调拨耗材</div>
        <div class="toolbar">
          <el-button type="primary" @click="openConsumableDialog">
            <el-icon><Plus /></el-icon>
            选择耗材
          </el-button>
          <el-button type="danger" :disabled="selectedItems.length === 0" @click="batchRemove">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <el-table
          ref="tableRef"
          :data="transferItems"
          stripe
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="120" align="center" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="stock" label="当前库存" width="100" align="center">
            <template #default="{ row }">
              <span :class="{ 'low-stock': row.stock < 10 }">{{ row.stock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调拨数量" width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.stock"
                size="small"
                @change="calculateItemTotal(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="单价(元)" width="100" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额(元)" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.totalPrice.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeItem($index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="transferItems.length === 0" class="empty-tip">
          <el-empty description="请添加调拨耗材" :image-size="80" />
        </div>
        <div v-if="transferItems.length > 0" class="summary-row">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">耗材种类：</span>
                <span class="value">{{ transferItems.length }} 种</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">调拨总量：</span>
                <span class="value">{{ totalQuantity }} 件</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item highlight">
                <span class="label">调拨总成本：</span>
                <span class="value">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <div v-show="currentStep === 2" class="step-content">
        <div class="step-title">确认调拨信息</div>
        <el-descriptions :column="2" border class="confirm-descriptions">
          <el-descriptions-item label="调出门店">
            {{ getStoreName(storeForm.sourceStoreId) }}
          </el-descriptions-item>
          <el-descriptions-item label="调入门店">
            {{ getStoreName(storeForm.targetStoreId) }}
          </el-descriptions-item>
          <el-descriptions-item label="调拨原因" :span="2">
            {{ storeForm.reason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="耗材种类">
            {{ transferItems.length }} 种
          </el-descriptions-item>
          <el-descriptions-item label="调拨总量">
            {{ totalQuantity }} 件
          </el-descriptions-item>
          <el-descriptions-item label="调拨总成本" :span="2">
            <span class="highlight-amount">¥{{ totalAmount.toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="confirm-items">
          <div class="confirm-items-title">调拨耗材明细</div>
          <el-table :data="transferItems" stripe border size="small">
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
      </div>

      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="prevStep">
          <el-icon><ArrowLeft /></el-icon>
          上一步
        </el-button>
        <el-button v-if="currentStep < 2" type="primary" @click="nextStep">
          下一步
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button
          v-if="currentStep === 2"
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>
          提交申请
        </el-button>
      </div>
    </el-card>

    <el-dialog
      v-model="consumableDialogVisible"
      title="选择耗材"
      width="800px"
      destroy-on-close
    >
      <div class="dialog-toolbar">
        <el-input
          v-model="consumableSearch"
          placeholder="搜索耗材名称/编码"
          clearable
          style="width: 200px"
          @keyup.enter="searchConsumables"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="categoryFilter" placeholder="选择分类" clearable style="width: 150px; margin-left: 12px">
          <el-option
            v-for="cat in categoryList"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="searchConsumables">
          搜索
        </el-button>
      </div>
      <el-table
        ref="consumableTableRef"
        :data="filteredConsumableList"
        stripe
        border
        max-height="400px"
        @selection-change="handleConsumableSelection"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="name" label="耗材名称" min-width="150" />
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="consumableDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedConsumables.length === 0" @click="confirmConsumables">
          确定添加 ({{ selectedConsumables.length }})
        </el-button>
      </template>
    </el-dialog>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">调拨申请记录</span>
        </div>
      </template>
      <el-table :data="historyList" stripe size="small">
        <el-table-column prop="orderNo" label="调拨单号" width="150" />
        <el-table-column prop="targetStoreName" label="调入门店" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="调拨数量" width="100" align="center" />
        <el-table-column label="调拨成本" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { getStatusText, getStatusType, type TransferStatus } from '@/api/transfer'

const router = useRouter()

interface Store {
  id: number
  name: string
  code: string
}

interface Consumable {
  id: number
  name: string
  code: string
  category: string
  specification: string
  unit: string
  price: number
  stock: number
}

interface TransferItem {
  consumableId: number
  consumableName: string
  code: string
  category: string
  specification: string
  unit: string
  price: number
  stock: number
  quantity: number
  unitPrice: number
  totalPrice: number
}

const currentStep = ref(0)
const storeFormRef = ref<FormInstance>()
const tableRef = ref()
const consumableTableRef = ref()
const submitLoading = ref(false)

const storeForm = reactive({
  sourceStoreId: 1,
  targetStoreId: undefined as number | undefined,
  reason: ''
})

const storeRules: FormRules = {
  targetStoreId: [
    { required: true, message: '请选择调入门店', trigger: 'change' }
  ]
}

const storeList = ref<Store[]>([
  { id: 1, name: '总店', code: 'ZD001' },
  { id: 2, name: '分店A', code: 'FDA001' },
  { id: 3, name: '分店B', code: 'FDB001' },
  { id: 4, name: '分店C', code: 'FDC001' },
  { id: 5, name: '分店D', code: 'FDD001' }
])

const targetStoreList = computed(() => {
  return storeList.value.filter(s => s.id !== storeForm.sourceStoreId)
})

const transferItems = ref<TransferItem[]>([])
const selectedItems = ref<TransferItem[]>([])

const consumableDialogVisible = ref(false)
const consumableSearch = ref('')
const categoryFilter = ref('')
const selectedConsumables = ref<Consumable[]>([])

const categoryList = ref(['办公用品', '清洁用品', '包装材料', '维修工具', '其他'])

const consumableList = ref<Consumable[]>([
  { id: 1, name: 'A4打印纸', code: 'C001', category: '办公用品', specification: '70g/500张', unit: '包', price: 25.00, stock: 150 },
  { id: 2, name: '签字笔', code: 'C002', category: '办公用品', specification: '0.5mm黑色', unit: '支', price: 2.50, stock: 500 },
  { id: 3, name: '胶带', code: 'C003', category: '包装材料', specification: '48mm宽', unit: '卷', price: 8.00, stock: 80 },
  { id: 4, name: '文件夹', code: 'C004', category: '办公用品', specification: 'A4双夹', unit: '个', price: 15.00, stock: 60 },
  { id: 5, name: '订书机', code: 'C005', category: '办公用品', specification: '标准型', unit: '个', price: 35.00, stock: 25 },
  { id: 6, name: '清洁剂', code: 'C006', category: '清洁用品', specification: '500ml', unit: '瓶', price: 18.00, stock: 40 },
  { id: 7, name: '垃圾袋', code: 'C007', category: '清洁用品', specification: '大号黑色', unit: '卷', price: 12.00, stock: 100 },
  { id: 8, name: '纸箱', code: 'C008', category: '包装材料', specification: '40x30x20cm', unit: '个', price: 5.00, stock: 200 },
  { id: 9, name: '螺丝刀套装', code: 'C009', category: '维修工具', specification: '6件套', unit: '套', price: 45.00, stock: 15 },
  { id: 10, name: '胶水', code: 'C010', category: '办公用品', specification: '50ml', unit: '瓶', price: 6.00, stock: 80 }
])

const filteredConsumableList = computed(() => {
  let list = consumableList.value
  if (consumableSearch.value) {
    const search = consumableSearch.value.toLowerCase()
    list = list.filter(item => 
      item.name.toLowerCase().includes(search) || 
      item.code.toLowerCase().includes(search)
    )
  }
  if (categoryFilter.value) {
    list = list.filter(item => item.category === categoryFilter.value)
  }
  return list
})

const historyList = ref([
  { orderNo: 'DB20240115001', targetStoreName: '分店A', status: 'pending', totalQuantity: 50, totalAmount: 680.00, createdAt: '2024-01-15 10:30:00' },
  { orderNo: 'DB20240114002', targetStoreName: '分店B', status: 'approved', totalQuantity: 30, totalAmount: 420.00, createdAt: '2024-01-14 15:20:00' },
  { orderNo: 'DB20240113003', targetStoreName: '分店C', status: 'rejected', totalQuantity: 20, totalAmount: 280.00, createdAt: '2024-01-13 09:15:00' }
])

const totalQuantity = computed(() => {
  return transferItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalAmount = computed(() => {
  return transferItems.value.reduce((sum, item) => sum + item.totalPrice, 0)
})

const getStoreName = (storeId: number | undefined) => {
  if (!storeId) return '-'
  const store = storeList.value.find(s => s.id === storeId)
  return store ? store.name : '-'
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!storeFormRef.value) return
    await storeFormRef.value.validate((valid) => {
      if (valid) {
        currentStep.value++
      }
    })
  } else if (currentStep.value === 1) {
    if (transferItems.value.length === 0) {
      ElMessage.warning('请添加至少一条调拨耗材')
      return
    }
    currentStep.value++
  }
}

const openConsumableDialog = () => {
  selectedConsumables.value = []
  consumableDialogVisible.value = true
}

const searchConsumables = () => {
  // 搜索逻辑已在computed中实现
}

const handleConsumableSelection = (selection: Consumable[]) => {
  selectedConsumables.value = selection
}

const confirmConsumables = () => {
  const existingIds = transferItems.value.map(item => item.consumableId)
  const newItems = selectedConsumables.value
    .filter(c => !existingIds.includes(c.id))
    .map(c => ({
      consumableId: c.id,
      consumableName: c.name,
      code: c.code,
      category: c.category,
      specification: c.specification,
      unit: c.unit,
      price: c.price,
      stock: c.stock,
      quantity: 1,
      unitPrice: c.price,
      totalPrice: c.price
    }))
  
  if (newItems.length === 0) {
    ElMessage.warning('所选耗材已存在于列表中')
    return
  }
  
  transferItems.value.push(...newItems)
  consumableDialogVisible.value = false
  ElMessage.success(`已添加 ${newItems.length} 种耗材`)
}

const handleSelectionChange = (selection: TransferItem[]) => {
  selectedItems.value = selection
}

const batchRemove = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedItems.value.length} 条耗材吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const ids = selectedItems.value.map(item => item.consumableId)
    transferItems.value = transferItems.value.filter(item => !ids.includes(item.consumableId))
    selectedItems.value = []
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const removeItem = (index: number) => {
  transferItems.value.splice(index, 1)
}

const calculateItemTotal = (row: TransferItem) => {
  row.totalPrice = row.quantity * row.unitPrice
}

const handleSubmit = async () => {
  ElMessageBox.confirm(
    '确定要提交调拨申请吗？',
    '提交确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    submitLoading.value = true
    setTimeout(() => {
      submitLoading.value = false
      ElMessage.success('调拨申请提交成功')
      
      historyList.value.unshift({
        orderNo: `DB${dayjs().format('YYYYMMDDHHmmss')}`,
        targetStoreName: getStoreName(storeForm.targetStoreId),
        status: 'pending' as TransferStatus,
        totalQuantity: totalQuantity.value,
        totalAmount: totalAmount.value,
        createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
      })
      
      resetForm()
    }, 1000)
  }).catch(() => {})
}

const resetForm = () => {
  currentStep.value = 0
  storeForm.targetStoreId = undefined
  storeForm.reason = ''
  transferItems.value = []
  storeFormRef.value?.resetFields()
}

const viewDetail = (row: any) => {
  ElMessage.info(`查看调拨单 ${row.orderNo} 详情`)
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.step-card {
  margin-bottom: 20px;
  
  :deep(.el-steps) {
    padding: 20px 60px;
  }
  
  :deep(.el-step__title) {
    font-size: 14px;
    font-weight: 600;
  }
  
  :deep(.el-step__description) {
    font-size: 12px;
  }
}

.content-card {
  margin-bottom: 20px;
}

.step-content {
  min-height: 400px;
  padding: 20px 0;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E3A8A;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #1E3A8A;
}

.store-form {
  max-width: 600px;
  
  :deep(.el-select) {
    width: 100%;
  }
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.empty-tip {
  padding: 40px 0;
}

.summary-row {
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

.low-stock {
  color: #DC2626;
  font-weight: 600;
}

.amount {
  font-weight: 600;
  color: #1E3A8A;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #E5E7EB;
  margin-top: 20px;
}

.confirm-descriptions {
  margin-bottom: 24px;
  
  :deep(.el-descriptions__label) {
    width: 120px;
    font-weight: 600;
  }
}

.highlight-amount {
  font-size: 18px;
  font-weight: 700;
  color: #1E3A8A;
}

.confirm-items {
  margin-top: 24px;
  
  .confirm-items-title {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }
}

.dialog-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
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

:deep(.el-input-number) {
  width: 100%;
}
</style>