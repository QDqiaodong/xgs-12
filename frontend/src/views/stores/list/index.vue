<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="门店编码">
          <el-input
            v-model="searchForm.code"
            placeholder="请输入门店编码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入门店名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="直营店" value="direct" />
            <el-option label="加盟店" value="franchise" />
            <el-option label="合作店" value="partner" />
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
        <el-table-column prop="code" label="门店编码" width="120" sortable="custom" />
        <el-table-column prop="name" label="门店名称" min-width="150" />
        <el-table-column prop="type" label="门店类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
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
        <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
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
            <el-form-item label="门店编码" prop="code">
              <el-input v-model="formData.code" placeholder="请输入门店编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门店名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入门店名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="门店类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择门店类型" style="width: 100%">
                <el-option label="直营店" value="direct" />
                <el-option label="加盟店" value="franchise" />
                <el-option label="合作店" value="partner" />
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
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="formData.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
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

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)

const searchForm = reactive({
  code: '',
  name: '',
  type: '',
  status: undefined as number | undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 50
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
  code: '',
  name: '',
  type: '',
  address: '',
  contact: '',
  phone: '',
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入门店编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '编码只能包含字母和数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择门店类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const tableData = ref([
  { id: 1, code: 'MD001', name: '总店', type: 'direct', address: '北京市朝阳区建国路88号', contact: '张经理', phone: '13800138001', status: 1, createdAt: '2024-01-01 10:00:00' },
  { id: 2, code: 'MD002', name: '朝阳分店', type: 'direct', address: '北京市朝阳区三里屯路19号', contact: '李经理', phone: '13800138002', status: 1, createdAt: '2024-01-05 14:30:00' },
  { id: 3, code: 'MD003', name: '海淀分店', type: 'franchise', address: '北京市海淀区中关村大街1号', contact: '王经理', phone: '13800138003', status: 1, createdAt: '2024-01-10 09:15:00' },
  { id: 4, code: 'MD004', name: '西城分店', type: 'franchise', address: '北京市西城区西单北大街120号', contact: '赵经理', phone: '13800138004', status: 1, createdAt: '2024-01-15 16:45:00' },
  { id: 5, code: 'MD005', name: '东城分店', type: 'partner', address: '北京市东城区王府井大街255号', contact: '刘经理', phone: '13800138005', status: 0, createdAt: '2024-01-20 11:20:00' },
  { id: 6, code: 'MD006', name: '丰台分店', type: 'direct', address: '北京市丰台区丰台路75号', contact: '陈经理', phone: '13800138006', status: 1, createdAt: '2024-02-01 08:30:00' },
  { id: 7, code: 'MD007', name: '通州分店', type: 'franchise', address: '北京市通州区新华大街88号', contact: '周经理', phone: '13800138007', status: 1, createdAt: '2024-02-10 13:00:00' },
  { id: 8, code: 'MD008', name: '大兴分店', type: 'partner', address: '北京市大兴区黄村镇兴华大街12号', contact: '吴经理', phone: '13800138008', status: 1, createdAt: '2024-02-15 15:40:00' },
  { id: 9, code: 'MD009', name: '昌平分店', type: 'direct', address: '北京市昌平区回龙观东大街108号', contact: '郑经理', phone: '13800138009', status: 1, createdAt: '2024-02-20 10:10:00' },
  { id: 10, code: 'MD010', name: '顺义分店', type: 'franchise', address: '北京市顺义区府前街56号', contact: '孙经理', phone: '13800138010', status: 0, createdAt: '2024-02-25 17:25:00' }
])

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    direct: '直营店',
    franchise: '加盟店',
    partner: '合作店'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    direct: 'primary',
    franchise: 'success',
    partner: 'warning'
  }
  return map[type] || 'info'
}

const handleSearch = () => {
  pagination.pageNum = 1
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchForm.code = ''
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = undefined
  pagination.pageNum = 1
  handleSearch()
}

const handleSortChange = ({ prop, order }: any) => {
  console.log('sort:', prop, order)
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
  formData.type = ''
  formData.address = ''
  formData.contact = ''
  formData.phone = ''
  formData.status = 1
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
    `确定要删除门店"${row.name}"吗？`,
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

const handleStatusChange = (row: any) => {
  const statusText = row.status === 1 ? '营业中' : '已停业'
  ElMessage.success(`门店"${row.name}"状态已更新为"${statusText}"`)
}

const handleExport = () => {
  const headers = ['门店编码', '门店名称', '门店类型', '地址', '联系人', '联系电话', '状态', '创建时间']
  const rows = tableData.value.map(item => [
    item.code,
    item.name,
    getTypeText(item.type),
    item.address,
    item.contact,
    item.phone,
    item.status === 1 ? '营业中' : '已停业',
    item.createdAt
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
  
  await formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      setTimeout(() => {
        if (dialogType.value === 'add') {
          const newItem = {
            ...formData,
            id: tableData.value.length + 1,
            createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
          }
          tableData.value.unshift(newItem)
          pagination.total += 1
          ElMessage.success('新增成功')
        } else {
          const index = tableData.value.findIndex(item => item.id === formData.id)
          if (index > -1) {
            tableData.value[index] = {
              ...tableData.value[index],
              ...formData
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