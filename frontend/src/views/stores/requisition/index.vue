<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="9">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><ShoppingCart /></el-icon>
                新建补货申请
              </span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
          >
            <el-form-item label="申请门店" prop="storeId">
              <el-select
                v-model="formData.storeId"
                placeholder="请选择申请门店"
                style="width: 100%"
                filterable
                @change="handleStoreChange"
              >
                <el-option
                  v-for="store in storeList"
                  :key="store.id"
                  :label="store.storeName"
                  :value="store.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="申请人">
              <el-input v-model="formData.applicant" placeholder="请输入申请人姓名" />
            </el-form-item>

            <el-form-item label="联系电话">
              <el-input v-model="formData.applicantPhone" placeholder="请输入联系电话" />
            </el-form-item>

            <el-divider content-position="left">
              <span class="divider-title">
                <el-icon><Box /></el-icon>
                申请耗材明细
              </span>
            </el-divider>

            <el-form-item label="添加耗材">
              <el-select
                v-model="selectedConsumableId"
                placeholder="搜索并选择耗材"
                style="width: 100%"
                filterable
                clearable
                @change="handleAddConsumable"
              >
                <el-option
                  v-for="item in availableConsumables"
                  :key="item.id"
                  :label="`${item.name} - ${item.specification}`"
                  :value="item.id"
                >
                  <div class="consumable-option">
                    <span class="option-name">{{ item.name }}</span>
                    <span class="option-spec">{{ item.specification }}</span>
                    <el-tag
                      v-if="item.stock <= item.warningStock"
                      type="danger"
                      size="small"
                      effect="light"
                    >
                      库存不足
                    </el-tag>
                    <el-tag
                      v-else
                      type="success"
                      size="small"
                      effect="plain"
                    >
                      库存: {{ item.stock }}
                    </el-tag>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>

            <div v-if="formData.items.length > 0" class="items-table-wrapper">
              <el-table
                :data="formData.items"
                border
                size="small"
                :row-class-name="getRowClassName"
              >
                <el-table-column label="耗材名称" min-width="140">
                  <template #default="{ row }">
                    <div class="cell-name">
                      <div class="name">{{ row.consumableName }}</div>
                      <div class="spec">{{ row.specification }}</div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="单位" width="60" align="center">
                  <template #default="{ row }">
                    {{ row.unit }}
                  </template>
                </el-table-column>
                <el-table-column label="申请数量" width="120" align="center">
                  <template #default="{ row, $index }">
                    <el-input-number
                      v-model="row.quantity"
                      :min="1"
                      :max="9999"
                      size="small"
                      :controls="false"
                      style="width: 100%"
                      @change="handleQuantityChange(row, $index)"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="期望到货日" width="140" align="center">
                  <template #default="{ row }">
                    <el-date-picker
                      v-model="row.expectedArrivalDate"
                      type="date"
                      placeholder="选择日期"
                      size="small"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      :disabled-date="disabledPastDate"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="用途" width="100" align="center">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.purpose"
                      placeholder="选择"
                      size="small"
                      style="width: 100%"
                    >
                      <el-option label="日常办公" value="daily" />
                      <el-option label="门店运营" value="operation" />
                      <el-option label="客户服务" value="service" />
                      <el-option label="设备维护" value="maintenance" />
                      <el-option label="促销活动" value="promotion" />
                      <el-option label="其他" value="other" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" align="center">
                  <template #default="{ $index }">
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleRemoveItem($index)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <el-empty
              v-else
              description="请从上方选择需要申请的耗材"
              :image-size="80"
              class="empty-items"
            />

            <el-divider content-position="left" v-if="formData.items.length > 0">
              <span class="divider-title">
                <el-icon><DataAnalysis /></el-icon>
                库存与消耗分析（提交前参考）
              </span>
            </el-divider>

            <div v-if="formData.items.length > 0" class="analysis-section">
              <div
                v-for="(item, index) in formData.items"
                :key="index"
                class="analysis-card"
                :class="getStockStatusClass(item)"
              >
                <div class="analysis-header">
                  <span class="analysis-name">{{ item.consumableName }}</span>
                  <el-tag
                    v-if="item.currentStock <= item.warningStock"
                    type="danger"
                    effect="dark"
                    size="small"
                  >
                    <el-icon><Warning /></el-icon>
                    库存预警
                  </el-tag>
                  <el-tag
                    v-else-if="item.currentStock - item.quantity < item.warningStock"
                    type="warning"
                    effect="light"
                    size="small"
                  >
                    申请后将预警
                  </el-tag>
                  <el-tag
                    v-else
                    type="success"
                    effect="plain"
                    size="small"
                  >
                    库存充足
                  </el-tag>
                </div>
                <el-row :gutter="12">
                  <el-col :span="6">
                    <div class="stat-block">
                      <div class="stat-label">
                        <el-icon><Box /></el-icon>
                        当前库存
                      </div>
                      <div
                        class="stat-value"
                        :class="{ danger: item.currentStock <= item.warningStock }"
                      >
                        {{ item.currentStock }}
                        <span class="stat-unit">{{ item.unit }}</span>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-block">
                      <div class="stat-label">
                        <el-icon><Warning /></el-icon>
                        预警库存
                      </div>
                      <div class="stat-value warning">
                        {{ item.warningStock }}
                        <span class="stat-unit">{{ item.unit }}</span>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-block">
                      <div class="stat-label">
                        <el-icon><TrendCharts /></el-icon>
                        近月消耗
                      </div>
                      <div class="stat-value info">
                        {{ item.monthlyConsumption }}
                        <span class="stat-unit">{{ item.unit }}</span>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="stat-block">
                      <div class="stat-label">
                        <el-icon><ShoppingCart /></el-icon>
                        申请数量
                      </div>
                      <div class="stat-value primary">
                        {{ item.quantity }}
                        <span class="stat-unit">{{ item.unit }}</span>
                      </div>
                    </div>
                  </el-col>
                </el-row>
                <div class="suggestion-bar">
                  <el-progress
                    :percentage="getStockPercentage(item)"
                    :stroke-width="8"
                    :color="getProgressColor(item)"
                  />
                  <div class="suggestion-text">
                    <el-icon v-if="isQuantityReasonable(item)"><CircleCheck /></el-icon>
                    <el-icon v-else><InfoFilled /></el-icon>
                    <span :class="{ 'text-success': isQuantityReasonable(item), 'text-warning': !isQuantityReasonable(item) }">
                      {{ getSuggestionText(item) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <el-divider v-if="formData.items.length > 0" />

            <el-form-item label="合计" v-if="formData.items.length > 0">
              <div class="summary-row">
                <span>共 <b class="primary">{{ formData.items.length }}</b> 种耗材，</span>
                <span>合计 <b class="primary">{{ totalQuantity }}</b> {{ totalQuantity > 1 ? '件' : '件' }}，</span>
                <span>预估金额 <b class="primary">¥{{ formatNumber(totalAmount) }}</b></span>
              </div>
            </el-form-item>

            <el-form-item label="整体用途说明" prop="purpose">
              <el-input
                v-model="formData.purpose"
                type="textarea"
                :rows="3"
                placeholder="请输入本次补货的整体用途说明（可选）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="submitLoading"
                :disabled="formData.items.length === 0"
                @click="handlePreview"
              >
                <el-icon><View /></el-icon>
                预览并提交
              </el-button>
              <el-button
                :disabled="formData.items.length === 0"
                @click="handleSaveDraft"
              >
                <el-icon><Document /></el-icon>
                保存草稿
              </el-button>
              <el-button @click="handleResetForm">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><List /></el-icon>
                补货申请记录
              </span>
              <div class="header-actions">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索单号/耗材"
                  clearable
                  style="width: 180px"
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="queryForm.status"
                  placeholder="状态"
                  clearable
                  style="width: 140px"
                  @change="handleSearch"
                >
                  <el-option label="草稿" value="draft" />
                  <el-option label="待审批" value="submitted" />
                  <el-option label="已审批" value="approved" />
                  <el-option label="已驳回" value="rejected" />
                  <el-option label="配送中" value="in_transit" />
                  <el-option label="已签收" value="received" />
                  <el-option label="已取消" value="cancelled" />
                </el-select>
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
            <el-table-column prop="orderNo" label="申请单号" width="140" fixed="left" />
            <el-table-column prop="storeName" label="申请门店" width="100" />
            <el-table-column label="申请明细" min-width="200">
              <template #default="{ row }">
                <div class="items-preview">
                  <el-tag
                    v-for="(item, idx) in row.items.slice(0, 2)"
                    :key="idx"
                    size="small"
                    type="info"
                    effect="plain"
                    style="margin-right: 4px; margin-bottom: 4px"
                  >
                    {{ item.consumableName }} x{{ item.quantity }}
                  </el-tag>
                  <el-tag
                    v-if="row.items.length > 2"
                    size="small"
                    type="info"
                    effect="plain"
                  >
                    +{{ row.items.length - 2 }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="总数量" width="80" align="center">
              <template #default="{ row }">
                {{ row.totalQuantity }}
              </template>
            </el-table-column>
            <el-table-column label="预估金额" width="110" align="right">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatNumber(row.totalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getRequisitionStatusType(row.status) as any" size="small" effect="light">
                  {{ getRequisitionStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicant" label="申请人" width="80" />
            <el-table-column label="申请时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleViewDetail(row)">
                  详情
                </el-button>
                <el-button
                  v-if="row.status === 'draft'"
                  type="success"
                  link
                  size="small"
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="row.status === 'draft' || row.status === 'rejected'"
                  type="warning"
                  link
                  size="small"
                  @click="handleResubmit(row)"
                >
                  提交
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
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
              @size-change="handleSearch"
              @current-change="handleSearch"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="previewVisible"
      title="补货申请预览"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border class="preview-desc">
        <el-descriptions-item label="申请单号">
          {{ previewData.orderNo || '系统自动生成' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请门店">
          {{ previewData.storeName }}
        </el-descriptions-item>
        <el-descriptions-item label="申请人">
          {{ previewData.applicant }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ previewData.applicantPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="耗材种类">
          {{ previewData.items.length }} 种
        </el-descriptions-item>
        <el-descriptions-item label="总数量/金额">
          {{ totalQuantity }} 件 / ¥{{ formatNumber(totalAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="整体用途" :span="2">
          {{ previewData.purpose || '未填写' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4 class="preview-subtitle">耗材明细</h4>
      <el-table :data="previewData.items" border size="small">
        <el-table-column label="耗材名称" min-width="140">
          <template #default="{ row }">
            <div>{{ row.consumableName }}</div>
            <div style="color: #9CA3AF; font-size: 12px">{{ row.specification }}</div>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="60" align="center">
          <template #default="{ row }">{{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="申请数量" width="90" align="center">
          <template #default="{ row }">{{ row.quantity }}</template>
        </el-table-column>
        <el-table-column label="当前库存" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.currentStock <= row.warningStock }">
              {{ row.currentStock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="近月消耗" width="90" align="center">
          <template #default="{ row }">{{ row.monthlyConsumption }}</template>
        </el-table-column>
        <el-table-column label="期望到货" width="110" align="center">
          <template #default="{ row }">{{ row.expectedArrivalDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="用途" width="90" align="center">
          <template #default="{ row }">{{ getPurposeText(row.purpose || '') || '-' }}</template>
        </el-table-column>
        <el-table-column label="预估金额" width="100" align="right">
          <template #default="{ row }">¥{{ formatNumber(row.totalPrice) }}</template>
        </el-table-column>
      </el-table>

      <div class="preview-footer">
        <div class="suggestion-summary" :class="{ warn: hasUnreasonableItems }">
          <el-icon v-if="hasUnreasonableItems"><WarningFilled /></el-icon>
          <el-icon v-else><CircleCheck /></el-icon>
          <span>
            {{ hasUnreasonableItems ? '存在申请数量不合理的耗材，请确认是否继续提交' : '所有耗材申请数量参考合理范围，可安全提交' }}
          </span>
        </div>
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">返回修改</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleConfirmSubmit">
          确认提交申请
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="补货申请详情"
      width="700px"
    >
      <el-descriptions :column="2" border class="preview-desc">
        <el-descriptions-item label="申请单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getRequisitionStatusType(detailData.status) as any" size="small">
            {{ getRequisitionStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请门店">{{ detailData.storeName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.applicant }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.applicantPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDate(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="整体用途" :span="2">{{ detailData.purpose || '未填写' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.approver" label="审批人">{{ detailData.approver }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.approveTime" label="审批时间">{{ formatDate(detailData.approveTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="detailData.approveRemark" label="审批备注" :span="2">
          {{ detailData.approveRemark }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4 class="preview-subtitle">耗材明细</h4>
      <el-table :data="detailData.items" border size="small">
        <el-table-column label="耗材名称" min-width="140">
          <template #default="{ row }">
            <div>{{ row.consumableName }}</div>
            <div style="color: #9CA3AF; font-size: 12px">{{ row.specification }}</div>
          </template>
        </el-table-column>
        <el-table-column label="单位" width="60" align="center">
          <template #default="{ row }">{{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="申请数量" width="90" align="center">
          <template #default="{ row }">{{ row.quantity }}</template>
        </el-table-column>
        <el-table-column label="期望到货" width="110" align="center">
          <template #default="{ row }">{{ row.expectedArrivalDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="用途" width="90" align="center">
          <template #default="{ row }">{{ getPurposeText(row.purpose || '') || '-' }}</template>
        </el-table-column>
        <el-table-column label="预估金额" width="100" align="right">
          <template #default="{ row }">¥{{ formatNumber(row.totalPrice) }}</template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'
import type { Store } from '@/api/store'
import type { Consumable } from '@/api/consumable'
import {
  getRequisitionStatusText,
  getRequisitionStatusType,
  getPurposeText,
  type RequisitionOrder,
  type RequisitionItem
} from '@/api/requisition'

interface FormItem {
  consumableId: number
  consumableName: string
  specification: string
  unit: string
  unitPrice: number
  quantity: number
  totalPrice: number
  currentStock: number
  warningStock: number
  monthlyConsumption: number
  expectedArrivalDate?: string
  purpose?: string
}

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const previewVisible = ref(false)
const detailVisible = ref(false)
const searchKeyword = ref('')
const dateRange = ref<string[]>([])
const selectedConsumableId = ref<number | undefined>(undefined)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const queryForm = reactive({
  status: '' as string
})

const formData = reactive({
  storeId: undefined as number | undefined,
  applicant: '',
  applicantPhone: '',
  purpose: '',
  items: [] as FormItem[]
})

const previewData = reactive({
  orderNo: '',
  storeId: undefined as number | undefined,
  storeName: '',
  applicant: '',
  applicantPhone: '',
  purpose: '',
  items: [] as FormItem[]
})

const detailData = reactive<Partial<RequisitionOrder>>({
  orderNo: '',
  storeName: '',
  status: 'draft',
  applicant: '',
  applicantPhone: '',
  purpose: '',
  approver: '',
  approveTime: '',
  approveRemark: '',
  createdAt: '',
  items: []
})

const formRules: FormRules = {
  storeId: [{ required: true, message: '请选择申请门店', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人姓名', trigger: 'blur' }]
}

const storeList = ref<Store[]>([
  { id: 1, storeCode: 'ST001', storeName: '总店', storeType: 'main', address: '北京市朝阳区建国路88号', contactPerson: '王经理', contactPhone: '13800138001', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' },
  { id: 2, storeCode: 'ST002', storeName: '朝阳分店', storeType: 'branch', address: '北京市朝阳区望京SOHO', contactPerson: '李店长', contactPhone: '13800138002', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' },
  { id: 3, storeCode: 'ST003', storeName: '海淀分店', storeType: 'branch', address: '北京市海淀区中关村大街1号', contactPerson: '张店长', contactPhone: '13800138003', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' },
  { id: 4, storeCode: 'ST004', storeName: '西城分店', storeType: 'branch', address: '北京市西城区金融街', contactPerson: '刘店长', contactPhone: '13800138004', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' },
  { id: 5, storeCode: 'ST005', storeName: '东城分店', storeType: 'branch', address: '北京市东城区王府井大街', contactPerson: '陈店长', contactPhone: '13800138005', status: 1, createTime: '2024-01-01 00:00:00', updateTime: '2024-01-01 00:00:00' }
] as Store[])

const consumableList = ref<Consumable[]>([
  { id: 1, name: 'A4打印纸', code: 'MAT001', category: '办公用品', unit: '包', specification: '70g/500张', price: 25.00, stock: 50, warningStock: 30, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 2, name: '签字笔', code: 'MAT002', category: '办公用品', unit: '支', specification: '0.5mm黑色', price: 2.50, stock: 200, warningStock: 100, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 3, name: '胶带', code: 'MAT003', category: '办公用品', unit: '卷', specification: '48mm宽', price: 8.00, stock: 15, warningStock: 20, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 4, name: '文件夹', code: 'MAT004', category: '办公用品', unit: '个', specification: 'A4双夹', price: 15.00, stock: 8, warningStock: 15, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 5, name: '订书机', code: 'MAT005', category: '办公用品', unit: '个', specification: '标准型', price: 35.00, stock: 12, warningStock: 10, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 6, name: '清洁剂', code: 'MAT006', category: '清洁用品', unit: '瓶', specification: '500ml', price: 18.00, stock: 45, warningStock: 20, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 7, name: '垃圾袋', code: 'MAT007', category: '清洁用品', unit: '卷', specification: '大号黑色', price: 12.00, stock: 25, warningStock: 30, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 8, name: '纸箱', code: 'MAT008', category: '包装材料', unit: '个', specification: '40x30x20cm', price: 5.00, stock: 150, warningStock: 50, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 9, name: '小票打印纸', code: 'MAT009', category: '办公用品', unit: '卷', specification: '80x60mm', price: 6.00, stock: 5, warningStock: 20, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 10, name: '购物袋', code: 'MAT010', category: '包装材料', unit: '个', specification: '大号加厚', price: 1.20, stock: 2000, warningStock: 500, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 11, name: '消毒湿巾', code: 'MAT011', category: '清洁用品', unit: '包', specification: '80抽', price: 8.50, stock: 30, warningStock: 25, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  { id: 12, name: '条码标签纸', code: 'MAT012', category: '办公用品', unit: '卷', specification: '40x30mm', price: 22.00, stock: 8, warningStock: 10, status: 1, createdAt: '2024-01-01', updatedAt: '2024-01-01' }
])

const monthlyConsumptionMap: Record<number, number> = {
  1: 45,
  2: 180,
  3: 25,
  4: 18,
  5: 5,
  6: 20,
  7: 35,
  8: 120,
  9: 18,
  10: 600,
  11: 28,
  12: 12
}

const availableConsumables = computed(() => {
  const selectedIds = new Set(formData.items.map(i => i.consumableId))
  return consumableList.value.filter(c => !selectedIds.has(c.id) && c.status === 1)
})

const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + item.quantity, 0)
})

const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + item.totalPrice, 0)
})

const hasUnreasonableItems = computed(() => {
  return formData.items.some(item => !isQuantityReasonable(item))
})

const recordList = ref<RequisitionOrder[]>([
  {
    id: 1,
    orderNo: 'BH202606001',
    storeId: 1,
    storeName: '总店',
    status: 'submitted',
    totalQuantity: 78,
    totalAmount: 1680.00,
    applicant: '张三',
    applicantPhone: '13800138001',
    createdAt: '2026-06-18 10:30:00',
    updatedAt: '2026-06-18 10:30:00',
    items: [
      { id: 1, consumableId: 1, consumableName: 'A4打印纸', specification: '70g/500张', unit: '包', unitPrice: 25, quantity: 20, totalPrice: 500, currentStock: 50, warningStock: 30, monthlyConsumption: 45, purpose: 'daily' },
      { id: 2, consumableId: 2, consumableName: '签字笔', specification: '0.5mm黑色', unit: '支', unitPrice: 2.5, quantity: 50, totalPrice: 125, currentStock: 200, warningStock: 100, monthlyConsumption: 180, purpose: 'daily' },
      { id: 3, consumableId: 8, consumableName: '纸箱', specification: '40x30x20cm', unit: '个', unitPrice: 5, quantity: 8, totalPrice: 40, currentStock: 150, warningStock: 50, monthlyConsumption: 120, purpose: 'operation' }
    ] as RequisitionItem[]
  },
  {
    id: 2,
    orderNo: 'BH202606002',
    storeId: 2,
    storeName: '朝阳分店',
    status: 'approved',
    totalQuantity: 45,
    totalAmount: 890.00,
    applicant: '李四',
    applicantPhone: '13800138002',
    approver: '王经理',
    approveTime: '2026-06-17 14:20:00',
    createdAt: '2026-06-16 09:15:00',
    updatedAt: '2026-06-17 14:20:00',
    items: [
      { id: 4, consumableId: 6, consumableName: '清洁剂', specification: '500ml', unit: '瓶', unitPrice: 18, quantity: 15, totalPrice: 270, currentStock: 45, warningStock: 20, monthlyConsumption: 20, purpose: 'maintenance' },
      { id: 5, consumableId: 7, consumableName: '垃圾袋', specification: '大号黑色', unit: '卷', unitPrice: 12, quantity: 30, totalPrice: 360, currentStock: 25, warningStock: 30, monthlyConsumption: 35, purpose: 'operation' }
    ] as RequisitionItem[]
  },
  {
    id: 3,
    orderNo: 'BH202606003',
    storeId: 3,
    storeName: '海淀分店',
    status: 'received',
    totalQuantity: 200,
    totalAmount: 2400.00,
    applicant: '王五',
    receiver: '王五',
    receiveTime: '2026-06-15 16:00:00',
    createdAt: '2026-06-10 11:00:00',
    updatedAt: '2026-06-15 16:00:00',
    items: [
      { id: 6, consumableId: 10, consumableName: '购物袋', specification: '大号加厚', unit: '个', unitPrice: 1.2, quantity: 2000, totalPrice: 2400, currentStock: 2000, warningStock: 500, monthlyConsumption: 600, purpose: 'service' }
    ] as RequisitionItem[]
  },
  {
    id: 4,
    orderNo: 'BH202606004',
    storeId: 1,
    storeName: '总店',
    status: 'draft',
    totalQuantity: 15,
    totalAmount: 330.00,
    applicant: '张三',
    createdAt: '2026-06-19 14:00:00',
    updatedAt: '2026-06-19 14:00:00',
    items: [
      { id: 7, consumableId: 5, consumableName: '订书机', specification: '标准型', unit: '个', unitPrice: 35, quantity: 5, totalPrice: 175, currentStock: 12, warningStock: 10, monthlyConsumption: 5, purpose: 'daily' },
      { id: 8, consumableId: 4, consumableName: '文件夹', specification: 'A4双夹', unit: '个', unitPrice: 15, quantity: 10, totalPrice: 150, currentStock: 8, warningStock: 15, monthlyConsumption: 18, purpose: 'daily' }
    ] as RequisitionItem[]
  },
  {
    id: 5,
    orderNo: 'BH202606005',
    storeId: 4,
    storeName: '西城分店',
    status: 'rejected',
    totalQuantity: 50,
    totalAmount: 450.00,
    applicant: '刘七',
    approver: '王经理',
    approveTime: '2026-06-18 15:00:00',
    approveRemark: '申请数量过大，请核实实际需求',
    createdAt: '2026-06-17 09:00:00',
    updatedAt: '2026-06-18 15:00:00',
    items: [
      { id: 9, consumableId: 3, consumableName: '胶带', specification: '48mm宽', unit: '卷', unitPrice: 8, quantity: 50, totalPrice: 400, currentStock: 15, warningStock: 20, monthlyConsumption: 25, purpose: 'operation' }
    ] as RequisitionItem[]
  }
])

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const disabledPastDate = (date: Date) => {
  return date.getTime() < dayjs().startOf('day').valueOf()
}

const getRowClassName = ({ row }: { row: FormItem }) => {
  if (row.currentStock <= row.warningStock) return 'stock-warning-row'
  if (row.currentStock - row.quantity < row.warningStock) return 'stock-caution-row'
  return ''
}

const getStockStatusClass = (item: FormItem) => {
  if (item.currentStock <= item.warningStock) return 'status-danger'
  if (item.currentStock - item.quantity < item.warningStock) return 'status-warning'
  return 'status-normal'
}

const getStockPercentage = (item: FormItem) => {
  const afterStock = Math.max(0, item.currentStock - item.quantity)
  const base = Math.max(item.warningStock * 3, item.monthlyConsumption * 2, 1)
  return Math.min(100, Math.round((afterStock / base) * 100))
}

const getProgressColor = (item: FormItem) => {
  const afterStock = item.currentStock - item.quantity
  if (afterStock <= 0) return '#DC2626'
  if (afterStock <= item.warningStock) return '#F59E0B'
  return '#059669'
}

const isQuantityReasonable = (item: FormItem) => {
  const monthly = item.monthlyConsumption || 1
  const maxRecommended = Math.max(monthly * 2, item.warningStock * 1.5)
  return item.quantity <= maxRecommended
}

const getSuggestionText = (item: FormItem) => {
  const monthly = item.monthlyConsumption || 1
  const maxRecommended = Math.max(Math.ceil(monthly * 2), Math.ceil(item.warningStock * 1.5))
  const minRecommended = Math.max(0, item.warningStock - item.currentStock + Math.ceil(monthly * 0.5))

  if (item.quantity > maxRecommended) {
    return `申请数量偏大，建议不超过 ${maxRecommended} ${item.unit}（约2个月用量）`
  }
  if (item.currentStock <= item.warningStock) {
    const need = item.warningStock - item.currentStock + Math.ceil(monthly * 0.5)
    return `库存已预警，建议至少申请 ${Math.max(need, item.quantity)} ${item.unit}`
  }
  if (item.quantity >= minRecommended && item.quantity <= maxRecommended) {
    return `申请数量合理，建议范围 ${minRecommended}-${maxRecommended} ${item.unit}`
  }
  return `建议申请数量范围 ${minRecommended}-${maxRecommended} ${item.unit}`
}

const handleStoreChange = () => {
  formData.items = []
  selectedConsumableId.value = undefined
}

const handleAddConsumable = (id: number) => {
  if (!formData.storeId) {
    ElMessage.warning('请先选择申请门店')
    selectedConsumableId.value = undefined
    return
  }
  const consumable = consumableList.value.find(c => c.id === id)
  if (!consumable) return

  const monthly = monthlyConsumptionMap[id] || Math.ceil(consumable.stock / 3)
  const defaultQty = consumable.stock <= consumable.warningStock
    ? Math.max(consumable.warningStock - consumable.stock + Math.ceil(monthly * 0.5), 1)
    : Math.max(Math.ceil(monthly * 0.5), 1)

  const newItem: FormItem = {
    consumableId: consumable.id,
    consumableName: consumable.name,
    specification: consumable.specification,
    unit: consumable.unit,
    unitPrice: consumable.price,
    quantity: defaultQty,
    totalPrice: consumable.price * defaultQty,
    currentStock: consumable.stock,
    warningStock: consumable.warningStock,
    monthlyConsumption: monthly,
    expectedArrivalDate: dayjs().add(3, 'day').format('YYYY-MM-DD'),
    purpose: ''
  }
  formData.items.push(newItem)
  selectedConsumableId.value = undefined
}

const handleQuantityChange = (row: FormItem) => {
  row.totalPrice = Number((row.unitPrice * row.quantity).toFixed(2))
}

const handleRemoveItem = (index: number) => {
  formData.items.splice(index, 1)
}

const handlePreview = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    if (formData.items.length === 0) {
      ElMessage.warning('请至少添加一种耗材')
      return
    }

    const store = storeList.value.find(s => s.id === formData.storeId)
    previewData.orderNo = ''
    previewData.storeId = formData.storeId
    previewData.storeName = store?.storeName || ''
    previewData.applicant = formData.applicant
    previewData.applicantPhone = formData.applicantPhone
    previewData.purpose = formData.purpose
    previewData.items = JSON.parse(JSON.stringify(formData.items))
    previewVisible.value = true
  })
}

const handleConfirmSubmit = () => {
  submitLoading.value = true
  setTimeout(() => {
    const store = storeList.value.find(s => s.id === formData.storeId)
    const newOrder: RequisitionOrder = {
      id: recordList.value.length + 1,
      orderNo: `BH${dayjs().format('YYYYMM')}${String(recordList.value.length + 1).padStart(3, '0')}`,
      storeId: formData.storeId!,
      storeName: store?.storeName || '',
      status: 'submitted',
      totalQuantity,
      totalAmount,
      expectedArrivalDate: formData.items[0]?.expectedArrivalDate,
      purpose: formData.purpose,
      applicant: formData.applicant,
      applicantPhone: formData.applicantPhone,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      updatedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      items: formData.items.map((item, idx) => ({
        id: idx + 1,
        ...item
      })) as RequisitionItem[]
    }
    recordList.value.unshift(newOrder)
    pagination.total += 1

    submitLoading.value = false
    previewVisible.value = false
    handleResetForm()
    ElMessage.success('补货申请已提交，等待审批')
  }, 800)
}

const handleSaveDraft = () => {
  const store = storeList.value.find(s => s.id === formData.storeId)
  const newOrder: RequisitionOrder = {
    id: recordList.value.length + 1,
    orderNo: `BH${dayjs().format('YYYYMM')}${String(recordList.value.length + 1).padStart(3, '0')}`,
    storeId: formData.storeId || 0,
    storeName: store?.storeName || '未选择',
    status: 'draft',
    totalQuantity,
    totalAmount,
    purpose: formData.purpose,
    applicant: formData.applicant || '未填写',
    applicantPhone: formData.applicantPhone,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    updatedAt: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    items: formData.items.map((item, idx) => ({
      id: idx + 1,
      ...item
    })) as RequisitionItem[]
  }
  recordList.value.unshift(newOrder)
  pagination.total += 1
  handleResetForm()
  ElMessage.success('已保存为草稿')
}

const handleResetForm = () => {
  formRef.value?.resetFields()
  formData.storeId = undefined
  formData.applicant = ''
  formData.applicantPhone = ''
  formData.purpose = ''
  formData.items = []
  selectedConsumableId.value = undefined
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleViewDetail = (row: RequisitionOrder) => {
  Object.assign(detailData, row)
  detailVisible.value = true
}

const handleEdit = (row: RequisitionOrder) => {
  formData.storeId = row.storeId
  formData.applicant = row.applicant
  formData.applicantPhone = row.applicantPhone || ''
  formData.purpose = row.purpose || ''
  formData.items = row.items.map(item => ({
    consumableId: item.consumableId,
    consumableName: item.consumableName,
    specification: item.specification,
    unit: item.unit,
    unitPrice: item.unitPrice,
    quantity: item.quantity,
    totalPrice: item.totalPrice,
    currentStock: item.currentStock,
    warningStock: item.warningStock,
    monthlyConsumption: item.monthlyConsumption,
    expectedArrivalDate: item.expectedArrivalDate,
    purpose: item.purpose
  }))
  ElMessage.info('已载入草稿数据，请修改后提交')
}

const handleResubmit = async (row: RequisitionOrder) => {
  try {
    await ElMessageBox.confirm(
      `确定要重新提交申请「${row.orderNo}」吗？`,
      '提交确认',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const idx = recordList.value.findIndex(r => r.id === row.id)
    if (idx !== -1) {
      recordList.value[idx].status = 'submitted'
      recordList.value[idx].updatedAt = dayjs().format('YYYY-MM-DD HH:mm:ss')
    }
    ElMessage.success('已重新提交审批')
  } catch {
    // 取消操作
  }
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
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-icon {
    color: #1E3A8A;
  }
}

.form-card {
  margin-bottom: 20px;
}

.list-card {
  .header-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.divider-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.consumable-option {
  display: flex;
  align-items: center;
  gap: 8px;

  .option-name {
    font-weight: 500;
    color: #1F2937;
  }

  .option-spec {
    color: #6B7280;
    font-size: 12px;
    flex: 1;
  }
}

.items-table-wrapper {
  margin: 0 -20px 16px;
  padding: 0 20px;
}

.empty-items {
  padding: 32px 0;
}

.cell-name {
  .name {
    font-weight: 500;
    color: #1F2937;
  }
  .spec {
    font-size: 12px;
    color: #6B7280;
  }
}

.analysis-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 8px;
}

.analysis-card {
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  padding: 14px 16px;
  background: #fff;
  transition: all 0.2s;

  &.status-danger {
    border-color: #FECACA;
    background: linear-gradient(to right, #FEF2F2, #fff);
  }

  &.status-warning {
    border-color: #FDE68A;
    background: linear-gradient(to right, #FFFBEB, #fff);
  }

  &.status-normal {
    border-color: #A7F3D0;
    background: linear-gradient(to right, #ECFDF5, #fff);
  }
}

.analysis-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #E5E7EB;

  .analysis-name {
    font-weight: 600;
    color: #1F2937;
    flex: 1;
  }
}

.stat-block {
  .stat-label {
    font-size: 12px;
    color: #6B7280;
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 4px;
  }

  .stat-value {
    font-size: 20px;
    font-weight: 700;
    font-family: 'SF Mono', Monaco, Menlo, monospace;
    color: #1F2937;

    &.danger { color: #DC2626; }
    &.warning { color: #D97706; }
    &.info { color: #2563EB; }
    &.primary { color: #1E3A8A; }
  }

  .stat-unit {
    font-size: 12px;
    font-weight: 400;
    color: #6B7280;
    margin-left: 2px;
  }
}

.suggestion-bar {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #F3F4F6;

  .suggestion-text {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;

    .text-success { color: #059669; }
    .text-warning { color: #D97706; }
  }
}

.summary-row {
  font-size: 14px;
  color: #374151;

  b.primary {
    color: #1E3A8A;
    font-size: 16px;
  }
}

.items-preview {
  display: flex;
  flex-wrap: wrap;
}

.amount-text {
  font-family: 'SF Mono', Monaco, Menlo, monospace;
  font-weight: 600;
  color: #1F2937;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

.preview-desc {
  margin-bottom: 0;
}

.preview-subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #1F2937;
  margin: 0 0 12px;
}

.preview-footer {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #F9FAFB;

  .suggestion-summary {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #059669;

    &.warn {
      color: #D97706;
    }
  }
}

.text-danger {
  color: #DC2626;
  font-weight: 600;
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

  .stock-warning-row {
    background-color: #FEF2F2 !important;
  }

  .stock-caution-row {
    background-color: #FFFBEB !important;
  }
}

:deep(.el-descriptions) {
  .el-descriptions__label {
    width: 100px;
    background: #F9FAFB;
    font-weight: 500;
  }
}
</style>
