<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="耗材名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入耗材名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="耗材分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="办公用品" value="办公用品" />
            <el-option label="清洁用品" value="清洁用品" />
            <el-option label="包装材料" value="包装材料" />
            <el-option label="维修工具" value="维修工具" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="预警" value="warning" />
            <el-option label="缺货" value="shortage" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">耗材列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增耗材
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="selectedRows.length > 0" class="batch-actions">
        <span class="selected-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button type="warning" size="small" @click="handleBatchExport">
          <el-icon><Download /></el-icon>
          批量导出
        </el-button>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        stripe
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="code" label="耗材编码" width="120" sortable="custom" />
        <el-table-column prop="name" label="耗材名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right" sortable="custom">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存数量" width="100" align="center" sortable="custom">
          <template #default="{ row }">
            <span :class="getStockClass(row)">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警值" width="80" align="center" />
        <el-table-column label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="160" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="耗材编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入耗材编码" :disabled="isView" />
        </el-form-item>
        <el-form-item label="耗材名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入耗材名称" :disabled="isView" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" :disabled="isView">
            <el-option label="办公用品" value="办公用品" />
            <el-option label="清洁用品" value="清洁用品" />
            <el-option label="包装材料" value="包装材料" />
            <el-option label="维修工具" value="维修工具" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="formData.specification" placeholder="请输入规格" :disabled="isView" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="formData.unit" placeholder="请选择单位" :disabled="isView">
            <el-option label="个" value="个" />
            <el-option label="件" value="件" />
            <el-option label="箱" value="箱" />
            <el-option label="包" value="包" />
            <el-option label="卷" value="卷" />
            <el-option label="套" value="套" />
            <el-option label="瓶" value="瓶" />
            <el-option label="袋" value="袋" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :precision="2"
            placeholder="请输入单价"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item label="预警库存" prop="warningStock">
          <el-input-number
            v-model="formData.warningStock"
            :min="0"
            placeholder="请输入预警库存值"
            :disabled="isView"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            :disabled="isView"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="!isView" type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'

const tableRef = ref()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const selectedRows = ref<any[]>([])

const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 100
})

const sortParams = reactive({
  prop: '',
  order: ''
})

const dialogType = ref<'add' | 'edit' | 'view'>('add')
const isView = computed(() => dialogType.value === 'view')
const dialogTitle = computed(() => {
  const titles = {
    add: '新增耗材',
    edit: '编辑耗材',
    view: '查看耗材详情'
  }
  return titles[dialogType.value]
})

const formData = reactive({
  id: 0,
  code: '',
  name: '',
  category: '',
  specification: '',
  unit: '',
  price: 0,
  warningStock: 0,
  remark: ''
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入耗材编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '编码只能包含字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入耗材名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  warningStock: [{ required: true, message: '请输入预警库存值', trigger: 'blur' }]
}

const tableData = ref([
  { id: 1, code: 'HC001', name: 'A4打印纸', category: '办公用品', specification: '70g/500张', unit: '包', price: 25.00, stock: 50, warningStock: 100, updatedAt: '2024-01-15 10:30:00' },
  { id: 2, code: 'HC002', name: '签字笔', category: '办公用品', specification: '0.5mm黑色', unit: '支', price: 2.50, stock: 200, warningStock: 500, updatedAt: '2024-01-14 15:20:00' },
  { id: 3, code: 'HC003', name: '胶带', category: '包装材料', specification: '48mm宽', unit: '卷', price: 8.00, stock: 30, warningStock: 50, updatedAt: '2024-01-13 09:15:00' },
  { id: 4, code: 'HC004', name: '文件夹', category: '办公用品', specification: 'A4双夹', unit: '个', price: 15.00, stock: 15, warningStock: 30, updatedAt: '2024-01-12 14:45:00' },
  { id: 5, code: 'HC005', name: '订书机', category: '办公用品', specification: '标准型', unit: '个', price: 35.00, stock: 8, warningStock: 20, updatedAt: '2024-01-11 11:30:00' },
  { id: 6, code: 'HC006', name: '清洁剂', category: '清洁用品', specification: '500ml', unit: '瓶', price: 18.00, stock: 45, warningStock: 30, updatedAt: '2024-01-10 16:20:00' },
  { id: 7, code: 'HC007', name: '垃圾袋', category: '清洁用品', specification: '大号黑色', unit: '卷', price: 12.00, stock: 80, warningStock: 50, updatedAt: '2024-01-09 08:40:00' },
  { id: 8, code: 'HC008', name: '纸箱', category: '包装材料', specification: '40x30x20cm', unit: '个', price: 5.00, stock: 150, warningStock: 100, updatedAt: '2024-01-08 13:10:00' },
  { id: 9, code: 'HC009', name: '螺丝刀套装', category: '维修工具', specification: '6件套', unit: '套', price: 45.00, stock: 5, warningStock: 10, updatedAt: '2024-01-07 10:50:00' },
  { id: 10, code: 'HC010', name: '胶水', category: '办公用品', specification: '50ml', unit: '瓶', price: 6.00, stock: 60, warningStock: 40, updatedAt: '2024-01-06 15:35:00' }
])

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStockClass = (row: any) => {
  if (row.stock <= 0) return 'stock-shortage'
  if (row.stock <= row.warningStock) return 'stock-warning'
  return 'stock-normal'
}

const getStatusType = (row: any) => {
  if (row.stock <= 0) return 'danger'
  if (row.stock <= row.warningStock) return 'warning'
  return 'success'
}

const getStatusText = (row: any) => {
  if (row.stock <= 0) return '缺货'
  if (row.stock <= row.warningStock) return '预警'
  return '正常'
}

const handleSearch = () => {
  pagination.pageNum = 1
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.category = ''
  searchForm.status = ''
  pagination.pageNum = 1
  handleSearch()
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handleSortChange = ({ prop, order }: any) => {
  sortParams.prop = prop
  sortParams.order = order
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  handleSearch()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  handleSearch()
}

const resetForm = () => {
  formData.id = 0
  formData.code = ''
  formData.name = ''
  formData.category = ''
  formData.specification = ''
  formData.unit = ''
  formData.price = 0
  formData.warningStock = 0
  formData.remark = ''
  formRef.value?.resetFields()
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleView = (row: any) => {
  dialogType.value = 'view'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除耗材"${row.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
      pagination.total -= 1
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 项耗材吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const ids = selectedRows.value.map(item => item.id)
    tableData.value = tableData.value.filter(item => !ids.includes(item.id))
    pagination.total -= ids.length
    selectedRows.value = []
    ElMessage.success('批量删除成功')
  }).catch(() => {})
}

const handleBatchExport = () => {
  ElMessage.success('批量导出功能开发中')
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      setTimeout(() => {
        if (dialogType.value === 'add') {
          const newItem = {
            ...formData,
            id: tableData.value.length + 1,
            stock: 0,
            updatedAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
          }
          tableData.value.unshift(newItem)
          pagination.total += 1
          ElMessage.success('新增成功')
        } else {
          const index = tableData.value.findIndex(item => item.id === formData.id)
          if (index > -1) {
            tableData.value[index] = {
              ...tableData.value[index],
              ...formData,
              updatedAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
            }
          }
          ElMessage.success('编辑成功')
        }
        submitLoading.value = false
        dialogVisible.value = false
      }, 500)
    }
  })
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
  background-color: #F3F4F6;
}

.search-card {
  margin-bottom: 20px;
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    
    .el-form-item {
      margin-bottom: 0;
    }
  }
}

.table-card {
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
  
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #EEF2FF;
  border-radius: 8px;
  margin-bottom: 16px;
  
  .selected-info {
    font-size: 14px;
    color: #1E3A8A;
    font-weight: 500;
  }
}

.stock-shortage {
  color: #DC2626;
  font-weight: 600;
}

.stock-warning {
  color: #F59E0B;
  font-weight: 600;
}

.stock-normal {
  color: #059669;
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

:deep(.el-dialog__header) {
  border-bottom: 1px solid #F3F4F6;
  padding: 16px 20px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #F3F4F6;
  padding: 16px 20px;
}
</style>