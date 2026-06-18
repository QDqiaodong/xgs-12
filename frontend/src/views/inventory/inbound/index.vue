<template>
  <div class="page-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">新增入库单</span>
          <el-tag type="info">入库管理</el-tag>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="inbound-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库单号" prop="orderNo">
              <el-input v-model="formData.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择入库类型">
                <el-option label="采购入库" value="purchase" />
                <el-option label="调拨入库" value="transfer" />
                <el-option label="退货入库" value="return" />
                <el-option label="其他入库" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库日期" prop="date">
              <el-date-picker
                v-model="formData.date"
                type="date"
                placeholder="请选择入库日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="目标门店" prop="storeId">
              <el-select v-model="formData.storeId" placeholder="请选择门店">
                <el-option label="总店" :value="1" />
                <el-option label="分店A" :value="2" />
                <el-option label="分店B" :value="3" />
                <el-option label="分店C" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="formData.supplierId" placeholder="请选择供应商" clearable>
                <el-option label="办公用品供应商A" :value="1" />
                <el-option label="清洁用品供应商B" :value="2" />
                <el-option label="包装材料供应商C" :value="3" />
                <el-option label="综合供应商D" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经办人" prop="operator">
              <el-input v-model="formData.operator" placeholder="请输入经办人" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">
          <span class="divider-title">入库明细</span>
          <el-button type="primary" size="small" @click="addItem">
            <el-icon><Plus /></el-icon>
            添加耗材
          </el-button>
        </el-divider>

        <el-table
          :data="formData.items"
          style="width: 100%"
          stripe
          border
          class="items-table"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="耗材名称" min-width="200">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.consumableId"
                placeholder="请选择耗材"
                filterable
                @change="handleConsumableChange(row, $index)"
              >
                <el-option
                  v-for="item in consumableList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="规格" width="120" align="center">
            <template #default="{ row }">
              {{ row.specification || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="{ row }">
              {{ row.unit || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="入库数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="99999"
                size="small"
                @change="calculateTotal(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价(元)" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.unitPrice"
                :min="0"
                :precision="2"
                size="small"
                @change="calculateTotal(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额(元)" width="120" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.totalPrice.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="货架位置" width="150">
            <template #default="{ row }">
              <el-input v-model="row.location" placeholder="如: A-01-03" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="备注" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                size="small"
                @click="removeItem($index)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="formData.items.length === 0" class="empty-tip">
          <el-empty description="请添加入库耗材明细" :image-size="80" />
        </div>

        <div class="summary-row">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">耗材种类：</span>
                <span class="value">{{ formData.items.length }} 种</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <span class="label">入库总量：</span>
                <span class="value">{{ totalQuantity }} 件</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item highlight">
                <span class="label">入库总金额：</span>
                <span class="value">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="入库备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入入库备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="附件上传" prop="attachments">
              <el-upload
                v-model:file-list="formData.attachments"
                action="/api/upload"
                multiple
                :limit="5"
                :on-exceed="handleExceed"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  上传附件
                </el-button>
                <template #tip>
                  <div class="upload-tip">
                    支持上传采购合同、发票等文件，最多5个文件
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div class="action-bar">
      <el-button @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置表单
      </el-button>
      <el-button type="warning" @click="handleSaveDraft">
        <el-icon><Document /></el-icon>
        保存草稿
      </el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
        <el-icon><Check /></el-icon>
        提交入库
      </el-button>
    </div>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">入库历史记录</span>
          <el-button type="primary" link @click="router.push('/reports/inbound')">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>

      <el-table :data="historyList" style="width: 100%" size="small" stripe>
        <el-table-column prop="orderNo" label="入库单号" width="140" />
        <el-table-column prop="type" label="入库类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="目标门店" width="100" />
        <el-table-column prop="itemCount" label="耗材种类" width="80" align="center" />
        <el-table-column prop="totalQuantity" label="入库数量" width="100" align="center" />
        <el-table-column prop="totalAmount" label="入库金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="经办人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
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
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

const formData = reactive({
  orderNo: `RK${dayjs().format('YYYYMMDDHHmmss')}`,
  type: 'purchase',
  date: dayjs().format('YYYY-MM-DD'),
  storeId: 1,
  supplierId: undefined as number | undefined,
  operator: '',
  remark: '',
  attachments: [] as any[],
  items: [] as InboundItem[]
})

interface InboundItem {
  consumableId: number | undefined
  consumableName: string
  specification: string
  unit: string
  quantity: number
  unitPrice: number
  totalPrice: number
  location: string
  remark: string
}

const formRules: FormRules = {
  type: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  date: [{ required: true, message: '请选择入库日期', trigger: 'change' }],
  storeId: [{ required: true, message: '请选择目标门店', trigger: 'change' }],
  operator: [{ required: true, message: '请输入经办人', trigger: 'blur' }]
}

const consumableList = ref([
  { id: 1, name: 'A4打印纸', specification: '70g/500张', unit: '包', price: 25.00 },
  { id: 2, name: '签字笔', specification: '0.5mm黑色', unit: '支', price: 2.50 },
  { id: 3, name: '胶带', specification: '48mm宽', unit: '卷', price: 8.00 },
  { id: 4, name: '文件夹', specification: 'A4双夹', unit: '个', price: 15.00 },
  { id: 5, name: '订书机', specification: '标准型', unit: '个', price: 35.00 },
  { id: 6, name: '清洁剂', specification: '500ml', unit: '瓶', price: 18.00 },
  { id: 7, name: '垃圾袋', specification: '大号黑色', unit: '卷', price: 12.00 },
  { id: 8, name: '纸箱', specification: '40x30x20cm', unit: '个', price: 5.00 },
  { id: 9, name: '螺丝刀套装', specification: '6件套', unit: '套', price: 45.00 },
  { id: 10, name: '胶水', specification: '50ml', unit: '瓶', price: 6.00 }
])

const historyList = ref([
  { orderNo: 'RK20240115001', type: 'purchase', storeName: '总店', itemCount: 3, totalQuantity: 150, totalAmount: 1250.00, operator: '张三', createdAt: '2024-01-15 10:30:00', status: 'completed' },
  { orderNo: 'RK20240114002', type: 'transfer', storeName: '分店A', itemCount: 2, totalQuantity: 80, totalAmount: 640.00, operator: '李四', createdAt: '2024-01-14 15:20:00', status: 'pending' },
  { orderNo: 'RK20240113003', type: 'purchase', storeName: '分店B', itemCount: 5, totalQuantity: 200, totalAmount: 1800.00, operator: '王五', createdAt: '2024-01-13 09:15:00', status: 'completed' }
])

const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.totalPrice || 0), 0)
})

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    purchase: '采购入库',
    transfer: '调拨入库',
    return: '退货入库',
    other: '其他入库'
  }
  return map[type] || type
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待审核',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const addItem = () => {
  formData.items.push({
    consumableId: undefined,
    consumableName: '',
    specification: '',
    unit: '',
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0,
    location: '',
    remark: ''
  })
}

const removeItem = (index: number) => {
  formData.items.splice(index, 1)
}

const handleConsumableChange = (row: InboundItem, index: number) => {
  const consumable = consumableList.value.find(item => item.id === row.consumableId)
  if (consumable) {
    row.consumableName = consumable.name
    row.specification = consumable.specification
    row.unit = consumable.unit
    row.unitPrice = consumable.price
    calculateTotal(row)
  }
}

const calculateTotal = (row: InboundItem) => {
  row.totalPrice = (row.quantity || 0) * (row.unitPrice || 0)
}

const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning('最多只能上传5个文件')
}

const handleReset = () => {
  ElMessageBox.confirm(
    '确定要重置表单吗？已填写的内容将被清空',
    '重置确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    formRef.value?.resetFields()
    formData.items = []
    formData.attachments = []
    formData.orderNo = `RK${dayjs().format('YYYYMMDDHHmmss')}`
    ElMessage.success('表单已重置')
  }).catch(() => {})
}

const handleSaveDraft = () => {
  ElMessage.success('草稿保存成功')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      if (formData.items.length === 0) {
        ElMessage.warning('请添加至少一条入库耗材明细')
        return
      }
      
      const invalidItems = formData.items.filter(item => !item.consumableId)
      if (invalidItems.length > 0) {
        ElMessage.warning('请选择所有耗材明细的耗材名称')
        return
      }
      
      submitLoading.value = true
      setTimeout(() => {
        submitLoading.value = false
        ElMessage.success('入库单提交成功')
        
        historyList.value.unshift({
          orderNo: formData.orderNo,
          type: formData.type,
          storeName: formData.storeId === 1 ? '总店' : `分店${String.fromCharCode(64 + formData.storeId)}`,
          itemCount: formData.items.length,
          totalQuantity: totalQuantity.value,
          totalAmount: totalAmount.value,
          operator: formData.operator,
          createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
          status: 'pending'
        })
        
        formRef.value?.resetFields()
        formData.items = []
        formData.attachments = []
        formData.orderNo = `RK${dayjs().format('YYYYMMDDHHmmss')}`
      }, 1000)
    }
  })
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.form-card {
  margin-bottom: 20px;
  
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

.divider-title {
  font-weight: 600;
  color: #1E3A8A;
}

.items-table {
  margin-bottom: 20px;
}

.empty-tip {
  padding: 20px 0;
}

.summary-row {
  background: #F3F4F6;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  
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

.amount {
  font-weight: 600;
  color: #1E3A8A;
}

.upload-tip {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 8px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0;
  margin-bottom: 20px;
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

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  gap: 12px;
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

:deep(.el-select) {
  width: 100%;
}

:deep(.el-date-picker) {
  width: 100%;
}
</style>