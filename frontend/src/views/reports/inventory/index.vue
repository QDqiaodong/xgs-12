<template>
  <div class="page-container">
    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="6">
        <div
          class="status-stat"
          :style="{ '--status-color': STOCK_STATUS_CONFIG.normal.color, '--status-bg': STOCK_STATUS_CONFIG.normal.bgColor }"
        >
          <div class="status-icon-wrap">
            <el-icon :size="24"><component :is="STOCK_STATUS_CONFIG.normal.iconName" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ statusCounts.normal }}</div>
            <div class="status-label">{{ STOCK_STATUS_CONFIG.normal.label }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div
          class="status-stat"
          :style="{ '--status-color': STOCK_STATUS_CONFIG.warning.color, '--status-bg': STOCK_STATUS_CONFIG.warning.bgColor }"
        >
          <div class="status-icon-wrap">
            <el-icon :size="24"><component :is="STOCK_STATUS_CONFIG.warning.iconName" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ statusCounts.warning }}</div>
            <div class="status-label">{{ STOCK_STATUS_CONFIG.warning.label }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div
          class="status-stat"
          :style="{ '--status-color': STOCK_STATUS_CONFIG.shortage.color, '--status-bg': STOCK_STATUS_CONFIG.shortage.bgColor }"
        >
          <div class="status-icon-wrap">
            <el-icon :size="24"><component :is="STOCK_STATUS_CONFIG.shortage.iconName" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ statusCounts.shortage }}</div>
            <div class="status-label">{{ STOCK_STATUS_CONFIG.shortage.label }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div
          class="status-stat"
          :style="{ '--status-color': STOCK_STATUS_CONFIG.negative.color, '--status-bg': STOCK_STATUS_CONFIG.negative.bgColor }"
        >
          <div class="status-icon-wrap">
            <el-icon :size="24"><component :is="STOCK_STATUS_CONFIG.negative.iconName" /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ statusCounts.negative }}</div>
            <div class="status-label">{{ STOCK_STATUS_CONFIG.negative.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mb-20">
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><Money /></el-icon>
                库存总值
              </span>
            </div>
          </template>
          <div class="summary-total-value">¥{{ formatNumber(totalStockValue) }}</div>
          <div class="summary-trend up">
            <el-icon><Top /></el-icon>
            <span>较上月增长 3.2%</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><Goods /></el-icon>
                耗材总数
              </span>
            </div>
          </template>
          <div class="summary-total-value">{{ reportList.length }} <span class="unit">种</span></div>
          <div class="summary-detail">
            <span>总库存：{{ totalQuantity }} 件</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card alert">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon class="title-icon"><Warning /></el-icon>
                异常库存
              </span>
            </div>
          </template>
          <div class="summary-total-value text-danger">
            {{ statusCounts.shortage + statusCounts.warning + statusCounts.negative }} <span class="unit">项</span>
          </div>
          <div class="summary-alert-list">
            <span class="alert-item warning">
              <el-icon><Warning /></el-icon>
              预警 {{ statusCounts.warning }}
            </span>
            <span class="alert-item danger">
              <el-icon><WarningFilled /></el-icon>
              短缺 {{ statusCounts.shortage }}
            </span>
            <span class="alert-item negative">
              <el-icon><CircleCloseFilled /></el-icon>
              负库存 {{ statusCounts.negative }}
            </span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><Filter /></el-icon>
            报表筛选
          </span>
        </div>
      </template>
      <el-form :model="queryForm" inline>
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="queryForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="门店">
          <el-select v-model="queryForm.storeId" placeholder="全部门店" clearable style="width: 160px">
            <el-option label="总店" :value="1" />
            <el-option label="分店A" :value="2" />
            <el-option label="分店B" :value="3" />
            <el-option label="分店C" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option
              v-for="config in Object.values(STOCK_STATUS_CONFIG)"
              :key="config.status"
              :label="config.label"
              :value="config.status"
            >
              <div style="display: flex; align-items: center; gap: 8px">
                <el-icon :style="{ color: config.color }">
                  <component :is="config.iconName" />
                </el-icon>
                <span>{{ config.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option label="办公用品" :value="1" />
            <el-option label="清洁用品" :value="2" />
            <el-option label="包装材料" :value="3" />
            <el-option label="工具" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
          <el-button type="warning" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ReportScopeSummary :items="scopeItems" class="mb-20" />

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><DataAnalysis /></el-icon>
            库存报表明细
            <el-tag type="info" size="small" style="margin-left: 8px">
              共 {{ filteredList.length }} 条 | 总金额 ¥{{ formatNumber(filteredTotalValue) }}
            </el-tag>
          </span>
          <div class="card-actions">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="table">表格视图</el-radio-button>
              <el-radio-button label="summary">汇总视图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <template v-if="viewMode === 'table'">
        <el-table
          :data="pagedList"
          style="width: 100%"
          stripe
          border
          height="560"
          :row-class-name="getRowClassName"
          show-summary
          :summary-method="getSummaries"
          @row-dblclick="handleViewDetail"
        >
          <el-table-column type="index" label="序号" width="60" align="center" fixed="left" />
          <el-table-column label="耗材编码" width="120" prop="materialCode" />
          <el-table-column label="耗材名称" min-width="160" prop="materialName" fixed="left" />
          <el-table-column label="规格" width="140" prop="specification" />
          <el-table-column label="单位" width="80" align="center" prop="unit" />
          <el-table-column label="分类" width="120" prop="categoryName" />
          <el-table-column label="门店" width="100" prop="storeName" />
          <el-table-column label="系统库存" width="100" align="right">
            <template #default="{ row }">
              <span class="num-text" :class="{ negative: row.quantity < 0 }">{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="锁定库存" width="100" align="right">
            <template #default="{ row }">
              <span class="num-text text-muted">{{ row.lockedQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="可用库存" width="100" align="right">
            <template #default="{ row }">
              <span class="num-text" :class="{ negative: row.availableQuantity < 0 }">
                {{ row.availableQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="安全库存" width="100" align="right">
            <template #default="{ row }">
              <span class="num-text text-muted">{{ row.safetyStock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均单价" width="120" align="right">
            <template #default="{ row }">
              <span class="num-text">¥{{ formatNumber(row.avgCost) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存金额" width="130" align="right">
            <template #default="{ row }">
              <span class="num-text text-primary">¥{{ formatNumber(row.stockValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存状态" width="120" align="center">
            <template #default="{ row }">
              <div
                class="status-badge"
                :style="{
                  color: getStockStatusColor(row.stockStatus),
                  backgroundColor: getStockStatusBgColor(row.stockStatus)
                }"
              >
                <el-icon style="margin-right: 4px">
                  <component :is="getStockStatusIcon(row.stockStatus)" />
                </el-icon>
                <span>{{ getStockStatusText(row.stockStatus) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="占比" width="100" align="center">
            <template #default="{ row }">
              <el-tooltip :content="`占总库存 ${getPercentage(row.stockValue)}%`">
                <el-progress
                  :percentage="Number(getPercentage(row.stockValue))"
                  :stroke-width="10"
                  :show-text="false"
                  :color="getProgressColor(row.stockValue)"
                />
              </el-tooltip>
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
      </template>

      <template v-else>
        <el-table
          :data="categorySummary"
          style="width: 100%"
          stripe
          border
          height="560"
          show-summary
          :summary-method="getCategorySummaries"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="分类名称" min-width="200" prop="categoryName" />
          <el-table-column label="耗材种类" width="120" align="center" prop="itemCount" />
          <el-table-column label="库存总量" width="140" align="right">
            <template #default="{ row }">
              <span class="num-text">{{ formatQuantity(row.totalQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存总金额" width="160" align="right">
            <template #default="{ row }">
              <span class="num-text text-primary">¥{{ formatNumber(row.totalValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="正常" width="100" align="center">
            <template #default="{ row }">
              <span class="status-count-normal">{{ row.statusCounts.normal }}</span>
            </template>
          </el-table-column>
          <el-table-column label="预警" width="100" align="center">
            <template #default="{ row }">
              <span class="status-count-warning">{{ row.statusCounts.warning }}</span>
            </template>
          </el-table-column>
          <el-table-column label="短缺" width="100" align="center">
            <template #default="{ row }">
              <span class="status-count-danger">{{ row.statusCounts.shortage }}</span>
            </template>
          </el-table-column>
          <el-table-column label="负库存" width="100" align="center">
            <template #default="{ row }">
              <span class="status-count-negative">{{ row.statusCounts.negative }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额占比" width="160">
            <template #default="{ row }">
              <div class="progress-inline">
                <el-progress
                  :percentage="Number(getPercentage(row.totalValue))"
                  :stroke-width="14"
                  :color="getProgressColor(row.totalValue)"
                />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-if="viewMode === 'table'" class="table-footer">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="库存详情"
      width="680px"
      :close-on-click-modal="false"
    >
      <div v-if="currentStock" class="detail-container">
        <div class="detail-header">
          <div class="detail-title">
            <el-icon :size="20" :style="{ color: getStockStatusColor(currentStock.stockStatus) }">
              <component :is="getStockStatusIcon(currentStock.stockStatus)" />
            </el-icon>
            <span class="name">{{ currentStock.materialName }}</span>
            <el-tag size="small" type="info" style="margin-left: 8px">
              {{ currentStock.materialCode }}
            </el-tag>
          </div>
          <div
            class="status-tag"
            :style="{
              color: getStockStatusColor(currentStock.stockStatus),
              backgroundColor: getStockStatusBgColor(currentStock.stockStatus)
            }"
          >
            <el-icon style="margin-right: 4px">
              <component :is="getStockStatusIcon(currentStock.stockStatus)" />
            </el-icon>
            <span>{{ getStockStatusText(currentStock.stockStatus) }}</span>
          </div>
        </div>

        <el-descriptions :column="2" border size="default" class="detail-desc">
          <el-descriptions-item label="耗材名称">{{ currentStock.materialName }}</el-descriptions-item>
          <el-descriptions-item label="耗材编码">{{ currentStock.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ currentStock.specification }}</el-descriptions-item>
          <el-descriptions-item label="计量单位">{{ currentStock.unit }}</el-descriptions-item>
          <el-descriptions-item label="所属分类">{{ currentStock.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="所属门店">{{ currentStock.storeName }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">库存数据</el-divider>

        <div class="stock-stats-row">
          <div class="stock-stat-item">
            <div class="stock-stat-label">系统库存</div>
            <div class="stock-stat-value" :class="{ negative: currentStock.quantity < 0 }">
              {{ currentStock.quantity }} {{ currentStock.unit }}
            </div>
          </div>
          <div class="stock-stat-item">
            <div class="stock-stat-label">锁定库存</div>
            <div class="stock-stat-value text-muted">
              {{ currentStock.lockedQuantity }} {{ currentStock.unit }}
            </div>
          </div>
          <div class="stock-stat-item">
            <div class="stock-stat-label">可用库存</div>
            <div class="stock-stat-value" :class="{ negative: currentStock.availableQuantity < 0 }">
              {{ currentStock.availableQuantity }} {{ currentStock.unit }}
            </div>
          </div>
          <div class="stock-stat-item">
            <div class="stock-stat-label">安全库存</div>
            <div class="stock-stat-value text-muted">
              {{ currentStock.safetyStock }} {{ currentStock.unit }}
            </div>
          </div>
        </div>

        <el-row :gutter="20" class="stock-value-row">
          <el-col :span="12">
            <div class="stock-value-card">
              <div class="label">平均单价</div>
              <div class="value">¥{{ formatNumber(currentStock.avgCost) }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stock-value-card highlight">
              <div class="label">库存总金额</div>
              <div class="value">¥{{ formatNumber(currentStock.stockValue) }}</div>
            </div>
          </el-col>
        </el-row>

        <el-divider content-position="left">状态分析</el-divider>

        <div class="status-analysis" :class="currentStock.stockStatus">
          <el-icon :size="20">
            <component :is="getStockStatusIcon(currentStock.stockStatus)" />
          </el-icon>
          <div class="analysis-content">
            <div class="analysis-title" :style="{ color: getStockStatusColor(currentStock.stockStatus) }">
              {{ getStockStatusText(currentStock.stockStatus) }}
            </div>
            <div class="analysis-desc">{{ getStatusAnalysis(currentStock) }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExportDetail">导出详情</el-button>
      </template>
    </el-dialog>

    <ExportConfirmDialog
      v-model="exportDialogVisible"
      report-type="库存报表"
      :scope-items="scopeItems"
      :data-count="filteredList.length"
      extra-info="按门店/分类/库存状态口径导出当前库存明细与金额。"
      @confirm="confirmExport"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ReportScopeSummary from '@/components/report/ReportScopeSummary.vue'
import ExportConfirmDialog from '@/components/report/ExportConfirmDialog.vue'
import {
  STOCK_STATUS_CONFIG,
  getStockStatusText,
  getStockStatusColor,
  getStockStatusBgColor,
  getStockStatusIcon,
  calcStockStatus,
  type StockStatusType
} from '@/api/report'

interface StockReportRow {
  id: number
  materialCode: string
  materialId: number
  materialName: string
  specification: string
  unit: string
  categoryId: number
  categoryName: string
  storeId: number
  storeName: string
  quantity: number
  lockedQuantity: number
  availableQuantity: number
  safetyStock: number
  avgCost: number
  stockValue: number
  stockStatus: StockStatusType
}

interface CategorySummaryRow {
  categoryName: string
  itemCount: number
  totalQuantity: number
  totalValue: number
  statusCounts: {
    normal: number
    warning: number
    shortage: number
    negative: number
  }
}

const queryForm = reactive({
  date: '',
  storeId: undefined as number | undefined,
  status: undefined as StockStatusType | undefined,
  categoryId: undefined as number | undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const viewMode = ref('table')
const detailDialogVisible = ref(false)
const currentStock = ref<StockReportRow | null>(null)
const exportDialogVisible = ref(false)

const storeNameMap: Record<number, string> = { 1: '总店', 2: '分店A', 3: '分店B', 4: '分店C' }
const categoryNameMap: Record<number, string> = { 1: '办公用品', 2: '清洁用品', 3: '包装材料', 4: '工具' }

const reportList = ref<StockReportRow[]>([
  { id: 1, materialCode: 'HC001', materialId: 1, materialName: 'A4打印纸', specification: '70g/500张', unit: '包', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 200, lockedQuantity: 20, availableQuantity: 180, safetyStock: 100, avgCost: 25.00, stockValue: 4500, stockStatus: 'normal' },
  { id: 2, materialCode: 'HC002', materialId: 2, materialName: '签字笔', specification: '0.5mm黑色', unit: '支', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 500, lockedQuantity: 50, availableQuantity: 450, safetyStock: 500, avgCost: 2.50, stockValue: 1125, stockStatus: 'warning' },
  { id: 3, materialCode: 'HC003', materialId: 3, materialName: '胶带', specification: '48mm宽', unit: '卷', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 0, lockedQuantity: 0, availableQuantity: 0, safetyStock: 50, avgCost: 8.00, stockValue: 0, stockStatus: 'shortage' },
  { id: 4, materialCode: 'HC004', materialId: 4, materialName: '文件夹', specification: 'A4双夹', unit: '个', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: -5, lockedQuantity: 0, availableQuantity: -5, safetyStock: 30, avgCost: 15.00, stockValue: -75, stockStatus: 'negative' },
  { id: 5, materialCode: 'HC005', materialId: 5, materialName: '订书机', specification: '标准型', unit: '个', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 30, lockedQuantity: 2, availableQuantity: 28, safetyStock: 20, avgCost: 35.00, stockValue: 980, stockStatus: 'normal' },
  { id: 6, materialCode: 'HC006', materialId: 6, materialName: '清洁剂', specification: '500ml', unit: '瓶', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 20, lockedQuantity: 0, availableQuantity: 20, safetyStock: 30, avgCost: 18.00, stockValue: 360, stockStatus: 'warning' },
  { id: 7, materialCode: 'HC007', materialId: 7, materialName: '垃圾袋', specification: '大号黑色', unit: '卷', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 0, lockedQuantity: 0, availableQuantity: 0, safetyStock: 50, avgCost: 12.00, stockValue: 0, stockStatus: 'shortage' },
  { id: 8, materialCode: 'HC008', materialId: 8, materialName: '纸箱', specification: '40x30x20cm', unit: '个', categoryId: 3, categoryName: '包装材料', storeId: 1, storeName: '总店', quantity: 500, lockedQuantity: 50, availableQuantity: 450, safetyStock: 200, avgCost: 5.00, stockValue: 2250, stockStatus: 'normal' },
  { id: 9, materialCode: 'HC009', materialId: 9, materialName: '螺丝刀套装', specification: '6件套', unit: '套', categoryId: 4, categoryName: '工具', storeId: 3, storeName: '分店B', quantity: 15, lockedQuantity: 0, availableQuantity: 15, safetyStock: 10, avgCost: 45.00, stockValue: 675, stockStatus: 'normal' },
  { id: 10, materialCode: 'HC010', materialId: 10, materialName: '胶水', specification: '50ml', unit: '瓶', categoryId: 1, categoryName: '办公用品', storeId: 3, storeName: '分店B', quantity: 0, lockedQuantity: 0, availableQuantity: 0, safetyStock: 20, avgCost: 6.00, stockValue: 0, stockStatus: 'shortage' },
  { id: 11, materialCode: 'HC011', materialId: 11, materialName: '橡皮擦', specification: '白色', unit: '块', categoryId: 1, categoryName: '办公用品', storeId: 2, storeName: '分店A', quantity: 300, lockedQuantity: 10, availableQuantity: 290, safetyStock: 100, avgCost: 1.50, stockValue: 435, stockStatus: 'normal' },
  { id: 12, materialCode: 'HC012', materialId: 12, materialName: '剪刀', specification: '不锈钢', unit: '把', categoryId: 1, categoryName: '办公用品', storeId: 1, storeName: '总店', quantity: 60, lockedQuantity: 5, availableQuantity: 55, safetyStock: 20, avgCost: 12.00, stockValue: 660, stockStatus: 'normal' },
  { id: 13, materialCode: 'HC013', materialId: 13, materialName: '白板笔', specification: '黑色', unit: '支', categoryId: 1, categoryName: '办公用品', storeId: 4, storeName: '分店C', quantity: 30, lockedQuantity: 0, availableQuantity: 30, safetyStock: 50, avgCost: 4.00, stockValue: 120, stockStatus: 'warning' },
  { id: 14, materialCode: 'HC014', materialId: 14, materialName: '拖把', specification: '棉头', unit: '把', categoryId: 2, categoryName: '清洁用品', storeId: 1, storeName: '总店', quantity: -2, lockedQuantity: 0, availableQuantity: -2, safetyStock: 10, avgCost: 28.00, stockValue: -56, stockStatus: 'negative' },
  { id: 15, materialCode: 'HC015', materialId: 15, materialName: '扫把', specification: '软毛', unit: '把', categoryId: 2, categoryName: '清洁用品', storeId: 2, storeName: '分店A', quantity: 25, lockedQuantity: 0, availableQuantity: 25, safetyStock: 15, avgCost: 22.00, stockValue: 550, stockStatus: 'normal' },
  { id: 16, materialCode: 'HC016', materialId: 16, materialName: '封箱胶带', specification: '60mm宽', unit: '卷', categoryId: 3, categoryName: '包装材料', storeId: 1, storeName: '总店', quantity: 80, lockedQuantity: 10, availableQuantity: 70, safetyStock: 50, avgCost: 10.00, stockValue: 700, stockStatus: 'normal' }
])

const statusCounts = computed(() => {
  const counts = { normal: 0, warning: 0, shortage: 0, negative: 0 }
  reportList.value.forEach(item => {
    counts[item.stockStatus]++
  })
  return counts
})

const filteredList = computed(() => {
  return reportList.value.filter(item => {
    if (queryForm.storeId !== undefined && item.storeId !== queryForm.storeId) return false
    if (queryForm.status !== undefined && item.stockStatus !== queryForm.status) return false
    if (queryForm.categoryId !== undefined && item.categoryId !== queryForm.categoryId) return false
    return true
  })
})

const pagedList = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredList.value.slice(start, start + pagination.pageSize)
})

const totalStockValue = computed(() => {
  return reportList.value.reduce((sum, item) => sum + item.stockValue, 0)
})

const filteredTotalValue = computed(() => {
  return filteredList.value.reduce((sum, item) => sum + item.stockValue, 0)
})

const totalQuantity = computed(() => {
  return reportList.value.reduce((sum, item) => sum + item.quantity, 0)
})

const categorySummary = computed<CategorySummaryRow[]>(() => {
  const map = new Map<number, CategorySummaryRow>()
  filteredList.value.forEach(item => {
    if (!map.has(item.categoryId)) {
      map.set(item.categoryId, {
        categoryName: item.categoryName,
        itemCount: 0,
        totalQuantity: 0,
        totalValue: 0,
        statusCounts: { normal: 0, warning: 0, shortage: 0, negative: 0 }
      })
    }
    const row = map.get(item.categoryId)!
    row.itemCount++
    row.totalQuantity += item.quantity
    row.totalValue += item.stockValue
    row.statusCounts[item.stockStatus]++
  })
  return Array.from(map.values())
})

const scopeItems = computed(() => {
  const items: Array<{ label: string; value: string; tagType?: string }> = []
  items.push({
    label: '门店',
    value: (queryForm.storeId !== undefined && storeNameMap[queryForm.storeId]) || '全部门店',
    tagType: queryForm.storeId !== undefined ? 'primary' : 'info'
  })
  items.push({
    label: '统计日期',
    value: queryForm.date || '默认（当前）',
    tagType: queryForm.date ? 'primary' : 'info'
  })
  items.push({
    label: '分类',
    value: (queryForm.categoryId !== undefined && categoryNameMap[queryForm.categoryId]) || '全部分类',
    tagType: queryForm.categoryId !== undefined ? 'primary' : 'info'
  })
  items.push({
    label: '库存状态',
    value: queryForm.status !== undefined ? getStockStatusText(queryForm.status) : '全部状态',
    tagType: queryForm.status !== undefined ? 'warning' : 'info'
  })
  return items
})

const formatNumber = (num: number) => {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatQuantity = (num: number) => {
  return num.toLocaleString('zh-CN')
}

const getPercentage = (value: number): string => {
  const total = viewMode.value === 'table' ? filteredTotalValue.value : categorySummary.value.reduce((s, r) => s + r.totalValue, 0)
  if (total === 0) return '0'
  return Math.abs((value / total) * 100).toFixed(1)
}

const getProgressColor = (value: number) => {
  const pct = Number(getPercentage(value))
  if (pct >= 20) return '#1E3A8A'
  if (pct >= 10) return '#3B82F6'
  if (pct >= 5) return '#60A5FA'
  return '#93C5FD'
}

const getRowClassName = ({ row }: { row: StockReportRow }) => {
  return `row-status-${row.stockStatus}`
}

const getSummaries = ({ columns, data }: { columns: any[], data: StockReportRow[] }) => {
  const sums: any[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    const values = data.map(item => Number(item[column.property as keyof StockReportRow]))
    if (['quantity', 'lockedQuantity', 'availableQuantity', 'safetyStock'].includes(column.property)) {
      sums[index] = values.reduce((prev, curr) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        } else {
          return prev
        }
      }, 0)
    } else if (['avgCost', 'stockValue'].includes(column.property)) {
      const total = values.reduce((prev, curr) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        } else {
          return prev
        }
      }, 0)
      sums[index] = '¥' + formatNumber(total)
    } else if (column.property === 'materialName') {
      sums[index] = `共 ${data.length} 项`
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getCategorySummaries = ({ columns, data }: { columns: any[], data: CategorySummaryRow[] }) => {
  const sums: any[] = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'categoryName') {
      sums[index] = `共 ${data.length} 个分类`
    } else if (column.property === 'itemCount') {
      sums[index] = data.reduce((s, r) => s + r.itemCount, 0)
    } else if (column.property === 'totalQuantity') {
      sums[index] = formatQuantity(data.reduce((s, r) => s + r.totalQuantity, 0))
    } else if (column.property === 'totalValue') {
      sums[index] = '¥' + formatNumber(data.reduce((s, r) => s + r.totalValue, 0))
    } else if (column.label === '正常') {
      sums[index] = data.reduce((s, r) => s + r.statusCounts.normal, 0)
    } else if (column.label === '预警') {
      sums[index] = data.reduce((s, r) => s + r.statusCounts.warning, 0)
    } else if (column.label === '短缺') {
      sums[index] = data.reduce((s, r) => s + r.statusCounts.shortage, 0)
    } else if (column.label === '负库存') {
      sums[index] = data.reduce((s, r) => s + r.statusCounts.negative, 0)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleQuery = () => {
  pagination.page = 1
  ElMessage.success('报表查询完成')
}

const handleReset = () => {
  queryForm.date = ''
  queryForm.storeId = undefined
  queryForm.status = undefined
  queryForm.categoryId = undefined
  pagination.page = 1
  ElMessage.info('已重置筛选条件')
}

const handleExport = () => {
  exportDialogVisible.value = true
}

const confirmExport = () => {
  ElMessage.success(`正在按口径导出库存报表，共 ${filteredList.value.length} 条数据...`)
  exportDialogVisible.value = false
}

const handlePrint = () => {
  ElMessage.info('正在准备打印...')
}

const handleViewDetail = (row: StockReportRow) => {
  currentStock.value = { ...row }
  detailDialogVisible.value = true
}

const handleExportDetail = () => {
  ElMessage.success('正在导出详情...')
}

const getStatusAnalysis = (row: StockReportRow): string => {
  switch (row.stockStatus) {
    case 'normal':
      return `当前库存充足，可用库存 ${row.availableQuantity} ${row.unit}，高于安全库存 ${row.safetyStock} ${row.unit}，库存状态良好。`
    case 'warning':
      return `库存即将低于安全库存，当前可用库存 ${row.availableQuantity} ${row.unit}，安全库存 ${row.safetyStock} ${row.unit}，建议及时补货。`
    case 'shortage':
      return `库存已为零！当前可用库存 ${row.availableQuantity} ${row.unit}，安全库存 ${row.safetyStock} ${row.unit}，请立即补货。`
    case 'negative':
      return `库存异常！当前可用库存为负值（${row.availableQuantity} ${row.unit}），请立即核查并调整库存数据。`
    default:
      return ''
  }
}

onMounted(() => {
  reportList.value = reportList.value.map(item => ({
    ...item,
    stockStatus: calcStockStatus(item.quantity, item.safetyStock, item.availableQuantity)
  }))
})
</script>

<style scoped lang="scss">
.status-stat {
  display: flex;
  align-items: center;
  padding: 20px;
  background: var(--status-bg);
  border-radius: 12px;
  border-left: 4px solid var(--status-color);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .status-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--status-color);
    margin-right: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  .status-info {
    .status-count {
      font-size: 28px;
      font-weight: 700;
      color: var(--status-color);
      line-height: 1.1;
    }

    .status-label {
      font-size: 14px;
      color: #6B7280;
      margin-top: 4px;
    }
  }
}

.summary-card {
  height: 100%;

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

  .summary-total-value {
    font-size: 32px;
    font-weight: 700;
    color: #1E3A8A;
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    margin: 8px 0;

    .unit {
      font-size: 16px;
      font-weight: 500;
      color: #6B7280;
      margin-left: 4px;
    }
  }

  &.alert .summary-total-value {
    color: #DC2626;
  }

  .summary-trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    padding: 4px 10px;
    border-radius: 12px;
    display: inline-flex;

    &.up {
      color: #059669;
      background: #ECFDF5;
    }

    &.down {
      color: #DC2626;
      background: #FEF2F2;
    }
  }

  .summary-detail {
    font-size: 14px;
    color: #6B7280;
  }

  .summary-alert-list {
    display: flex;
    gap: 12px;
    margin-top: 8px;
    flex-wrap: wrap;

    .alert-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 10px;

      &.warning {
        color: #F59E0B;
        background: #FFFBEB;
      }

      &.danger {
        color: #DC2626;
        background: #FEF2F2;
      }

      &.negative {
        color: #7C3AED;
        background: #F5F3FF;
      }
    }
  }
}

.filter-card,
.table-card {
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

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.num-text {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-weight: 500;
  color: #1F2937;

  &.negative {
    color: #DC2626;
    font-weight: 600;
  }

  &.text-muted {
    color: #6B7280;
    font-weight: 400;
  }

  &.text-primary {
    color: #1E3A8A;
    font-weight: 600;
  }

  &.text-danger {
    color: #DC2626;
    font-weight: 700;
  }
}

.status-count-normal {
  color: #059669;
  font-weight: 600;
}

.status-count-warning {
  color: #F59E0B;
  font-weight: 600;
}

.status-count-danger {
  color: #DC2626;
  font-weight: 600;
}

.status-count-negative {
  color: #7C3AED;
  font-weight: 600;
}

.progress-inline {
  padding: 0 8px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #F3F4F6;
  margin-top: 8px;
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

  .el-table__row {
    &.row-status-warning {
      background-color: rgba(245, 158, 11, 0.04);
    }

    &.row-status-shortage {
      background-color: rgba(220, 38, 38, 0.04);
    }

    &.row-status-negative {
      background-color: rgba(124, 58, 237, 0.04);
    }

    &:hover {
      &.row-status-warning > td {
        background-color: rgba(245, 158, 11, 0.08) !important;
      }

      &.row-status-shortage > td {
        background-color: rgba(220, 38, 38, 0.08) !important;
      }

      &.row-status-negative > td {
        background-color: rgba(124, 58, 237, 0.08) !important;
      }
    }
  }
}

.detail-container {
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16px;
    margin-bottom: 16px;
    border-bottom: 1px solid #F3F4F6;

    .detail-title {
      display: flex;
      align-items: center;

      .name {
        font-size: 20px;
        font-weight: 600;
        color: #1F2937;
        margin-left: 8px;
      }
    }

    .status-tag {
      display: inline-flex;
      align-items: center;
      padding: 6px 14px;
      border-radius: 16px;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .detail-desc {
    margin-bottom: 8px;
  }

  .stock-stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 16px;

    .stock-stat-item {
      background: #F9FAFB;
      padding: 16px;
      border-radius: 8px;
      text-align: center;

      .stock-stat-label {
        font-size: 13px;
        color: #6B7280;
        margin-bottom: 6px;
      }

      .stock-stat-value {
        font-size: 20px;
        font-weight: 600;
        color: #1F2937;
        font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;

        &.negative {
          color: #DC2626;
        }

        &.text-muted {
          color: #6B7280;
          font-weight: 500;
        }
      }
    }
  }

  .stock-value-row {
    margin-bottom: 8px;

    .stock-value-card {
      background: #F3F4F6;
      padding: 16px 20px;
      border-radius: 8px;
      text-align: center;

      .label {
        font-size: 13px;
        color: #6B7280;
        margin-bottom: 6px;
      }

      .value {
        font-size: 22px;
        font-weight: 700;
        color: #1F2937;
        font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
      }

      &.highlight {
        background: linear-gradient(135deg, #EFF6FF, #DBEAFE);

        .value {
          color: #1E3A8A;
        }
      }
    }
  }

  .status-analysis {
    display: flex;
    gap: 16px;
    padding: 20px;
    border-radius: 12px;
    background: #F9FAFB;
    border-left: 4px solid;

    .analysis-content {
      flex: 1;

      .analysis-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 8px;
      }

      .analysis-desc {
        font-size: 14px;
        color: #374151;
        line-height: 1.6;
      }
    }

    &.normal {
      border-color: #059669;
      background: #ECFDF5;

      .el-icon {
        color: #059669;
      }
    }

    &.warning {
      border-color: #F59E0B;
      background: #FFFBEB;

      .el-icon {
        color: #F59E0B;
      }
    }

    &.shortage {
      border-color: #DC2626;
      background: #FEF2F2;

      .el-icon {
        color: #DC2626;
      }
    }

    &.negative {
      border-color: #7C3AED;
      background: #F5F3FF;

      .el-icon {
        color: #7C3AED;
      }
    }
  }
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #F3F4F6;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-radio-button__inner) {
  border-radius: 0;
}

:deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 6px 0 0 6px;
}

:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 6px 6px 0;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #1E3A8A;
  border-color: #1E3A8A;
}
</style>
