<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="门店编码">
          <el-input
            v-model="searchForm.storeCode"
            placeholder="请输入门店编码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店名称">
          <el-input
            v-model="searchForm.storeName"
            placeholder="请输入门店名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店类型">
          <el-select v-model="searchForm.storeType" placeholder="请选择类型" clearable>
            <el-option label="奶茶店" value="tea_milk" />
            <el-option label="汽修店" value="automotive" />
            <el-option label="零售店" value="retail" />
            <el-option label="仓库" value="warehouse" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="营业中" :value="1" />
            <el-option label="已停业" :value="0" />
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
          <span class="card-title">门店列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增门店
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column prop="storeCode" label="门店编码" width="120" sortable="custom" />
        <el-table-column prop="storeName" label="门店名称" min-width="150" />
        <el-table-column prop="storeType" label="门店类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.storeType)" size="small">
              {{ getTypeText(row.storeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="营业中"
              inactive-text="已停业"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
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
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        :disabled="isView"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门店编码" prop="storeCode">
              <el-input v-model="formData.storeCode" placeholder="请输入门店编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门店名称" prop="storeName">
              <el-input v-model="formData.storeName" placeholder="请输入门店名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门店类型" prop="storeType">
              <el-select v-model="formData.storeType" placeholder="请选择门店类型" style="width: 100%">
                <el-option label="奶茶店" value="tea_milk" />
                <el-option label="汽修店" value="automotive" />
                <el-option label="零售店" value="retail" />
                <el-option label="仓库" value="warehouse" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">营业中</el-radio>
                <el-radio :value="0">已停业</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入门店地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { storeApi, type Store } from '@/api/store'

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)

const searchForm = reactive({
  storeCode: '',
  storeName: '',
  storeType: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogType = ref<'add' | 'edit' | 'view'>('add')
const isView = computed(() => dialogType.value === 'view')
const dialogTitle = computed(() => {
  const titles = {
    add: '新增门店',
    edit: '编辑门店',
    view: '查看门店详情'
  }
  return titles[dialogType.value]
})

const formData = reactive({
  id: 0,
  storeCode: '',
  storeName: '',
  storeType: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  status: 1
})

const formRules: FormRules = {
  storeCode: [
    { required: true, message: '请输入门店编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '编码只能包含字母和数字', trigger: 'blur' }
  ],
  storeName: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  storeType: [{ required: true, message: '请选择门店类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const tableData = ref<Store[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await storeApi.getList({
      storeCode: searchForm.storeCode,
      storeName: searchForm.storeName,
      storeType: searchForm.storeType,
      status: searchForm.status,
      current: pagination.pageNum,
      size: pagination.pageSize
    })
    tableData.value = res.data.records || []
    pagination.total = Number(res.data.total) || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    tea_milk: '奶茶店',
    automotive: '汽修店',
    retail: '零售店',
    warehouse: '仓库'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
    tea_milk: 'primary',
    automotive: 'success',
    retail: 'warning',
    warehouse: 'info'
  }
  return map[type] || 'info' as const
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleReset = () => {
  searchForm.storeCode = ''
  searchForm.storeName = ''
  searchForm.storeType = ''
  searchForm.status = undefined
  pagination.pageNum = 1
  fetchList()
}

const handleSortChange = ({ prop, order }: any) => {
  console.log('sort:', prop, order)
  fetchList()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchList()
}

const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchList()
}

const resetForm = () => {
  formData.id = 0
  formData.storeCode = ''
  formData.storeName = ''
  formData.storeType = ''
  formData.address = ''
  formData.contactPerson = ''
  formData.contactPhone = ''
  formData.status = 1
  formRef.value?.resetFields()
}

const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleView = (row: any) => {
  dialogType.value = 'view'
  Object.assign(formData, row as Store)
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(formData, row as Store)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  const store = row as Store
  ElMessageBox.confirm(
    `确定要删除门店"${store.storeName}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await storeApi.delete(store.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleStatusChange = async (row: any) => {
  const store = row as Store
  try {
    await storeApi.update(store.id, { status: store.status })
    const statusText = store.status === 1 ? '营业中' : '已停业'
    ElMessage.success(`门店"${store.storeName}"状态已更新为"${statusText}"`)
  } catch (e) {
    console.error(e)
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleExport = () => {
  const headers = ['门店编码', '门店名称', '门店类型', '地址', '联系人', '联系电话', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.storeCode,
    item.storeName,
    getTypeText(item.storeType),
    item.address,
    item.contactPerson,
    item.contactPhone,
    item.status === 1 ? '营业中' : '已停业',
    item.createTime
  ])
  
  let csvContent = '\uFEFF'
  csvContent += headers.join(',') + '\n'
  rows.forEach(row => {
    csvContent += row.map(cell => `"${cell}"`).join(',') + '\n'
  })
  
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `门店列表_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  
  ElMessage.success('导出成功')
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await storeApi.create({
            storeCode: formData.storeCode,
            storeName: formData.storeName,
            storeType: formData.storeType,
            address: formData.address,
            contactPerson: formData.contactPerson,
            contactPhone: formData.contactPhone,
            status: formData.status
          })
          ElMessage.success('新增成功')
        } else {
          await storeApi.update(formData.id, {
            storeName: formData.storeName,
            storeType: formData.storeType,
            address: formData.address,
            contactPerson: formData.contactPerson,
            contactPhone: formData.contactPhone,
            status: formData.status
          })
          ElMessage.success('编辑成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (e) {
        console.error(e)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchList()
})
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
