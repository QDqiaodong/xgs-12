<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">领用登记</span>
            </div>
          </template>
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
          >
            <el-form-item label="领用门店" prop="storeId">
              <el-select v-model="formData.storeId" placeholder="请选择门店" style="width: 100%">
                <el-option v-for="store in storeList" :key="store.id" :label="store.name" :value="store.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="耗材名称" prop="consumableId">
              <el-select
                v-model="formData.consumableId"
                placeholder="请选择耗材"
                style="width: 100%"
                filterable
                @change="handleConsumableChange"
              >
                <el-option v-for="item in consumableList" :key="item.id" :label="item.name" :value="item.id">
                  <span>{{ item.name }} (库存: {{ item.stock }})</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="规格型号">
              <el-input v-model="formData.specification" disabled placeholder="自动获取" />
            </el-form-item>
            <el-form-item label="单位">
              <el-input v-model="formData.unit" disabled placeholder="自动获取" />
            </el-form-item>
            <el-form-item label="领用数量" prop="quantity">
              <el-input-number
                v-model="formData.quantity"
                :min="1"
                :max="maxQuantity"
                placeholder="请输入数量"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="领用用途" prop="purpose">
              <el-select v-model="formData.purpose" placeholder="请选择用途" style="width: 100%">
                <el-option label="日常办公" value="daily" />
                <el-option label="门店运营" value="operation" />
                <el-option label="客户服务" value="service" />
                <el-option label="设备维护" value="maintenance" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注说明">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
                <el-icon><Check /></el-icon>
                提交领用
              </el-button>
              <el-button @click="handleResetForm">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">领用记录</span>
              <div class="header-actions">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索耗材名称"
                  clearable
                  style="width: 200px"
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 240px"
                  @change="handleSearch"
                />
              </div>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="recordList"
            style="width: 100%"
            stripe
          >
            <el-table-column prop="orderNo" label="领用单号" width="140" />
            <el-table-column prop="storeName" label="领用门店" width="120" />
            <el-table-column prop="consumableName" label="耗材名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="100" />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="unit" label="单位" width="60" align="center" />
            <el-table-column prop="purpose" label="用途" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ getPurposeText(row.purpose) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="领用人" width="100" />
            <el-table-column prop="createdAt" label="领用时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
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

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="detailVisible"
      title="领用详情"
      width="500px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="领用单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="领用门店">{{ detailData.storeName }}</el-descriptions-item>
        <el-descriptions-item label="耗材名称">{{ detailData.consumableName }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detailData.specification }}</el-descriptions-item>
        <el-descriptions-item label="领用数量">{{ detailData.quantity }} {{ detailData.unit }}</el-descriptions-item>
        <el-descriptions-item label="领用用途">{{ getPurposeText(detailData.purpose) }}</el-descriptions-item>
        <el-descriptions-item label="领用人">{{ detailData.operator }}</el-descriptions-item>
        <el-descriptions-item label="领用时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const searchKeyword = ref('')
const dateRange = ref<string[]>([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 50
})

const formData = reactive({
  storeId: undefined as number | undefined,
  consumableId: undefined as number | undefined,
  specification: '',
  unit: '',
  quantity: 1,
  purpose: '',
  remark: ''
})

const detailData = reactive({
  orderNo: '',
  storeName: '',
  consumableName: '',
  specification: '',
  quantity: 0,
  unit: '',
  purpose: '',
  operator: '',
  createdAt: '',
  remark: ''
})

const formRules: FormRules = {
  storeId: [{ required: true, message: '请选择领用门店', trigger: 'change' }],
  consumableId: [{ required: true, message: '请选择耗材', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入领用数量', trigger: 'blur' }],
  purpose: [{ required: true, message: '请选择领用用途', trigger: 'change' }]
}

const storeList = ref([
  { id: 1, name: '总店' },
  { id: 2, name: '朝阳分店' },
  { id: 3, name: '海淀分店' },
  { id: 4, name: '西城分店' },
  { id: 5, name: '东城分店' }
])

const consumableList = ref([
  { id: 1, name: 'A4打印纸', specification: '70g/500张', unit: '包', stock: 50 },
  { id: 2, name: '签字笔', specification: '0.5mm黑色', unit: '支', stock: 200 },
  { id: 3, name: '胶带', specification: '48mm宽', unit: '卷', stock: 30 },
  { id: 4, name: '文件夹', specification: 'A4双夹', unit: '个', stock: 15 },
  { id: 5, name: '订书机', specification: '标准型', unit: '个', stock: 8 },
  { id: 6, name: '清洁剂', specification: '500ml', unit: '瓶', stock: 45 },
  { id: 7, name: '垃圾袋', specification: '大号黑色', unit: '卷', stock: 80 },
  { id: 8, name: '纸箱', specification: '40x30x20cm', unit: '个', stock: 150 }
])

const maxQuantity = computed(() => {
  const item = consumableList.value.find(c => c.id === formData.consumableId)
  return item ? item.stock : 100
})

const recordList = ref([
  { id: 1, orderNo: 'LY202401001', storeId: 1, storeName: '总店', consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', quantity: 5, unit: '包', purpose: 'daily', operator: '张三', createdAt: '2024-01-15 10:30:00', remark: '日常办公使用' },
  { id: 2, orderNo: 'LY202401002', storeId: 2, storeName: '朝阳分店', consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', quantity: 20, unit: '支', purpose: 'daily', operator: '李四', createdAt: '2024-01-14 15:20:00', remark: '' },
  { id: 3, orderNo: 'LY202401003', storeId: 3, storeName: '海淀分店', consumableId: 3, consumableName: '胶带', specification: '48mm宽', quantity: 3, unit: '卷', purpose: 'operation', operator: '王五', createdAt: '2024-01-13 09:15:00', remark: '商品包装使用' },
  { id: 4, orderNo: 'LY202401004', storeId: 1, storeName: '总店', consumableId: 6, consumableName: '清洁剂', specification: '500ml', quantity: 2, unit: '瓶', purpose: 'maintenance', operator: '赵六', createdAt: '2024-01-12 14:45:00', remark: '门店清洁' },
  { id: 5, orderNo: 'LY202401005', storeId: 4, storeName: '西城分店', consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', quantity: 10, unit: '卷', purpose: 'operation', operator: '刘七', createdAt: '2024-01-11 11:30:00', remark: '' },
  { id: 6, orderNo: 'LY202401006', storeId: 5, storeName: '东城分店', consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', quantity: 5, unit: '个', purpose: 'daily', operator: '陈八', createdAt: '2024-01-10 16:20:00', remark: '文件整理' },
  { id: 7, orderNo: 'LY202401007', storeId: 2, storeName: '朝阳分店', consumableId: 5, consumableName: '订书机', specification: '标准型', quantity: 2, unit: '个', purpose: 'daily', operator: '周九', createdAt: '2024-01-09 08:40:00', remark: '' },
  { id: 8, orderNo: 'LY202401008', storeId: 3, storeName: '海淀分店', consumableId: 8, consumableName: '纸箱', specification: '40x30x20cm', quantity: 20, unit: '个', purpose: 'operation', operator: '吴十', createdAt: '2024-01-08 13:10:00', remark: '商品配送包装' }
])

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getPurposeText = (purpose: string) => {
  const map: Record<string, string> = {
    daily: '日常办公',
    operation: '门店运营',
    service: '客户服务',
    maintenance: '设备维护',
    other: '其他'
  }
  return map[purpose] || purpose
}

const handleConsumableChange = (id: number) => {
  const item = consumableList.value.find(c => c.id === id)
  if (item) {
    formData.specification = item.specification
    formData.unit = item.unit
    formData.quantity = 1
  }
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  handleSearch()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  handleSearch()
}

const handleResetForm = () => {
  formRef.value?.resetFields()
  formData.specification = ''
  formData.unit = ''
  formData.remark = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      setTimeout(() => {
        const store = storeList.value.find(s => s.id === formData.storeId)
        const consumable = consumableList.value.find(c => c.id === formData.consumableId)
        
        const newRecord = {
          id: recordList.value.length + 1,
          orderNo: `LY${dayjs().format('YYYYMM')}${String(recordList.value.length + 1).padStart(3, '0')}`,
          storeId: formData.storeId!,
          storeName: store?.name || '',
          consumableId: formData.consumableId!,
          consumableName: consumable?.name || '',
          specification: formData.specification,
          quantity: formData.quantity,
          unit: formData.unit,
          purpose: formData.purpose,
          operator: '当前用户',
          createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
          remark: formData.remark
        }
        
        recordList.value.unshift(newRecord)
        pagination.total += 1
        
        if (consumable) {
          consumable.stock -= formData.quantity
        }
        
        submitLoading.value = false
        handleResetForm()
        ElMessage.success('领用登记成功')
      }, 500)
    }
  })
}

const handleViewDetail = (row: any) => {
  Object.assign(detailData, row)
  detailVisible.value = true
}

onMounted(() => {
  pagination.total = recordList.value.length
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.form-card, .list-card {
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

.form-card {
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.list-card {
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
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

:deep(.el-descriptions) {
  .el-descriptions__label {
    width: 100px;
  }
}
</style>