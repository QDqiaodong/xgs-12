<template>
  <div class="page-container">
    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#1E3A8A', '--status-bg': '#EFF6FF' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Money /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">¥{{ formatNumber(monthAmount) }}</div>
            <div class="status-label">月度消耗金额</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#059669', '--status-bg': '#ECFDF5' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Calendar /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ entryDays }} <span class="unit">天</span></div>
            <div class="status-label">录入天数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#F59E0B', '--status-bg': '#FFFBEB' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ totalEntries }} <span class="unit">笔</span></div>
            <div class="status-label">消耗记录数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="status-stat" :style="{ '--status-color': '#7C3AED', '--status-bg': '#F5F3FF' }">
          <div class="status-icon-wrap">
            <el-icon :size="24"><Goods /></el-icon>
          </div>
          <div class="status-info">
            <div class="status-count">{{ distinctMaterialCount }} <span class="unit">种</span></div>
            <div class="status-label">涉及耗材数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="filter-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><Filter /></el-icon>
            门店与口径
          </span>
          <div class="legend">
            <span class="legend-item"><i class="dot dot-entered" />已录入</span>
            <span class="legend-item"><i class="dot dot-empty" />未录入</span>
            <span class="legend-item"><i class="dot dot-today" />今日</span>
          </div>
        </div>
      </template>
      <el-form :model="queryForm" inline>
        <el-form-item label="门店">
          <el-select v-model="queryForm.storeId" placeholder="全部门店" clearable style="width: 160px" @change="handleStoreChange">
            <el-option v-for="s in storeOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY 年 MM 月"
            value-format="YYYY-MM"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="goToday">
            <el-icon><Calendar /></el-icon>
            回到本月
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="calendar-card mb-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon class="title-icon"><DataLine /></el-icon>
            {{ viewYear }} 年 {{ viewMonth }} 月消耗日历
            <el-tag type="info" size="small" style="margin-left: 8px">
              日均消耗 ¥{{ formatNumber(avgPerDay) }}
            </el-tag>
          </span>
          <span class="store-hint">当前门店：{{ currentStoreName }}</span>
        </div>
      </template>

      <el-calendar v-model="calendarDate" class="consumption-calendar">
        <template #date-cell="{ data }">
          <div
            class="cal-cell"
            :class="getCellClass(data)"
            @click="openDay(data)"
          >
            <div class="cal-head">
              <span class="cal-day">{{ Number(data.day.split('-')[2]) }}</span>
              <span v-if="isToday(data.day)" class="cal-today-tag">今日</span>
            </div>
            <template v-if="getDayInfo(data)">
              <div class="cal-amount">¥{{ formatNumber(getDayInfo(data)!.amount) }}</div>
              <div class="cal-meta">
                <el-icon><Goods /></el-icon>
                <span>{{ getDayInfo(data)!.consumableCount }} 种耗材</span>
              </div>
            </template>
            <div v-else-if="data.type === 'current-month'" class="cal-empty">未录入</div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-dialog v-model="dayDialogVisible" :title="dayDialogTitle" width="720px" :close-on-click-modal="false">
      <div v-if="currentDayInfo" class="day-detail">
        <el-descriptions :column="3" border size="default" class="mb-16">
          <el-descriptions-item label="所属门店">{{ currentStoreName }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ currentDay }}</el-descriptions-item>
          <el-descriptions-item label="录入状态">
            <el-tag type="success" size="small">已录入</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消耗金额">
            <span class="num-highlight">¥{{ formatNumber(currentDayInfo.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="耗材种类">{{ currentDayInfo.consumableCount }} 种</el-descriptions-item>
          <el-descriptions-item label="消耗笔数">{{ currentDayInfo.items.length }} 笔</el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentDayInfo.items" border stripe size="small" show-summary :summary-method="getDaySummaries">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="耗材名称" prop="materialName" min-width="140" />
          <el-table-column label="规格" prop="specification" width="130" />
          <el-table-column label="单位" prop="unit" width="70" align="center" />
          <el-table-column label="数量" prop="quantity" width="90" align="right" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ formatNumber(row.cost) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <span class="num-text text-primary">¥{{ formatNumber(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="day-detail-empty">
        <el-empty description="当日暂无消耗录入" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="dayDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

interface DayItem {
  materialName: string
  specification: string
  unit: string
  quantity: number
  cost: number
  amount: number
}

interface DayData {
  hasEntry: boolean
  amount: number
  consumableCount: number
  items: DayItem[]
}

const storeOptions = [
  { label: '总店', value: 1 },
  { label: '分店A', value: 2 },
  { label: '分店B', value: 3 },
  { label: '分店C', value: 4 }
]

const CONSUMABLE_POOL = [
  { name: 'A4打印纸', specification: '70g/500张', unit: '包', price: 25 },
  { name: '签字笔', specification: '0.5mm黑色', unit: '支', price: 2.5 },
  { name: '清洁剂', specification: '500ml', unit: '瓶', price: 18 },
  { name: '纸箱', specification: '40x30x20cm', unit: '个', price: 5 },
  { name: '橡皮擦', specification: '白色', unit: '块', price: 1.5 },
  { name: '封箱胶带', specification: '60mm宽', unit: '卷', price: 10 },
  { name: '垃圾袋', specification: '大号黑色', unit: '卷', price: 12 },
  { name: '白板笔', specification: '黑色', unit: '支', price: 4 }
]

const queryForm = reactive({
  storeId: undefined as number | undefined
})

const calendarDate = ref<Date>(new Date())
const dayDialogVisible = ref(false)
const currentDay = ref('')
const currentDayInfo = ref<DayData | null>(null)

const viewYear = computed(() => dayjs(calendarDate.value).year())
const viewMonth = computed(() => dayjs(calendarDate.value).month() + 1)

const selectedMonth = computed<string>({
  get: () => dayjs(calendarDate.value).format('YYYY-MM'),
  set: (val: string) => {
    const [y, m] = val.split('-').map(Number)
    calendarDate.value = dayjs(`${y}-${pad(m)}-01`).toDate()
    ElMessage.success(`已切换至 ${y} 年 ${m} 月`)
  }
})

const currentStoreName = computed(() => {
  if (queryForm.storeId === undefined) return '全部门店'
  return storeOptions.find(s => s.value === queryForm.storeId)?.label || '全部门店'
})

const pad = (n: number) => String(n).padStart(2, '0')

const buildMonthData = (year: number, month: number, storeId: number | undefined): Map<number, DayData> => {
  const map = new Map<number, DayData>()
  const storeSeed = storeId ?? 0
  const daysInMonth = dayjs(`${year}-${pad(month)}-01`).daysInMonth()
  for (let d = 1; d <= daysInMonth; d++) {
    const date = dayjs(`${year}-${pad(month)}-${pad(d)}`)
    const dow = date.day()
    const isWeekend = dow === 0 || dow === 6
    const seed = year * 10000 + month * 100 + d + storeSeed * 97
    const hasEntry = !isWeekend && seed % 5 !== 0
    if (!hasEntry) {
      map.set(d, { hasEntry: false, amount: 0, consumableCount: 0, items: [] })
      continue
    }
    const itemCount = 1 + (seed % 3)
    const items: DayItem[] = []
    let amount = 0
    for (let k = 0; k < itemCount; k++) {
      const c = CONSUMABLE_POOL[(d + k + storeSeed) % CONSUMABLE_POOL.length]
      const qty = 5 + ((seed + k * 7) % 30)
      const lineAmount = qty * c.price
      amount += lineAmount
      items.push({
        materialName: c.name,
        specification: c.specification,
        unit: c.unit,
        quantity: qty,
        cost: c.price,
        amount: lineAmount
      })
    }
    map.set(d, { hasEntry: true, amount, consumableCount: itemCount, items })
  }
  return map
}

const monthData = computed(() => buildMonthData(viewYear.value, viewMonth.value, queryForm.storeId))

const getDayInfo = (data: { type: string; day: string }): DayData | null => {
  if (data.type !== 'current-month') return null
  const day = Number(data.day.split('-')[2])
  return monthData.value.get(day) || null
}

const isToday = (day: string) => day === dayjs().format('YYYY-MM-DD')

const getCellClass = (data: { type: string; day: string }) => {
  const info = getDayInfo(data)
  return {
    'is-other-month': data.type !== 'current-month',
    'is-entered': !!info?.hasEntry,
    'is-empty': data.type === 'current-month' && !info?.hasEntry,
    'is-today': isToday(data.day)
  }
}

const monthAmount = computed(() => {
  let sum = 0
  monthData.value.forEach(d => { sum += d.amount })
  return sum
})
const entryDays = computed(() => {
  let count = 0
  monthData.value.forEach(d => { if (d.hasEntry) count++ })
  return count
})
const totalEntries = computed(() => {
  let count = 0
  monthData.value.forEach(d => { count += d.items.length })
  return count
})
const distinctMaterialCount = computed(() => {
  const set = new Set<string>()
  monthData.value.forEach(d => d.items.forEach(i => set.add(i.materialName)))
  return set.size
})
const avgPerDay = computed(() => entryDays.value ? monthAmount.value / entryDays.value : 0)

const dayDialogTitle = computed(() => `${currentDay.value} 消耗明细`)

const openDay = (data: { type: string; day: string }) => {
  if (data.type !== 'current-month') return
  currentDay.value = data.day
  currentDayInfo.value = getDayInfo(data)
  dayDialogVisible.value = true
}

const formatNumber = (num: number) => num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const getDaySummaries = ({ columns, data }: { columns: any[]; data: DayItem[] }) => {
  const sums: any[] = []
  columns.forEach((column, index) => {
    if (index === 0) { sums[index] = '合计'; return }
    if (column.property === 'quantity') {
      sums[index] = data.reduce((s, r) => s + r.quantity, 0)
    } else if (index === columns.length - 1) {
      sums[index] = '¥' + formatNumber(data.reduce((s, r) => s + r.amount, 0))
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleStoreChange = () => {
  ElMessage.success(`已切换至：${currentStoreName.value}`)
}

const goToday = () => {
  calendarDate.value = new Date()
  ElMessage.info('已回到本月')
}
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
      font-size: 26px;
      font-weight: 700;
      color: var(--status-color);
      line-height: 1.1;
      font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    }

    .status-label {
      font-size: 14px;
      color: #6b7280;
      margin-top: 4px;
    }

    .unit {
      font-size: 14px;
      font-weight: 500;
      color: #6b7280;
    }
  }
}

.filter-card,
.calendar-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .title-icon {
    color: #1e3a8a;
  }

  .legend {
    display: flex;
    gap: 16px;

    .legend-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #6b7280;
    }

    .dot {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 50%;

      &.dot-entered {
        background: #1e3a8a;
      }

      &.dot-empty {
        background: #e5e7eb;
      }

      &.dot-today {
        background: #f59e0b;
      }
    }
  }

  .store-hint {
    font-size: 13px;
    color: #6b7280;
  }
}

.consumption-calendar {
  :deep(.el-calendar__header) {
    padding: 4px 12px 12px;
  }

  :deep(.el-calendar-table) {
    th {
      padding: 8px 0;
      color: #374151;
      font-weight: 600;
    }

    .el-calendar-day {
      height: 96px;
      padding: 4px;

      &:hover {
        background: #f9fafb;
      }
    }
  }
}

.cal-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 4px;
  cursor: pointer;
  border-radius: 8px;
  padding: 2px 4px;
  transition: background 0.2s;

  .cal-head {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .cal-day {
      font-size: 14px;
      font-weight: 600;
      color: #1f2937;
    }

    .cal-today-tag {
      font-size: 11px;
      color: #fff;
      background: #f59e0b;
      border-radius: 8px;
      padding: 1px 6px;
    }
  }

  .cal-amount {
    font-size: 15px;
    font-weight: 700;
    color: #1e3a8a;
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  }

  .cal-meta {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #6b7280;
  }

  .cal-empty {
    font-size: 12px;
    color: #cbd5e1;
  }

  &.is-other-month {
    opacity: 0.45;

    .cal-day {
      color: #9ca3af;
    }
  }

  &.is-entered {
    background: linear-gradient(135deg, #eff6ff, #dbeafe);

    &:hover {
      background: linear-gradient(135deg, #dbeafe, #bfdbfe);
    }
  }

  &.is-empty {
    background: #fafafa;
  }

  &.is-today {
    box-shadow: inset 0 0 0 2px #f59e0b;
  }
}

.day-detail {
  .num-highlight {
    font-weight: 700;
    color: #1e3a8a;
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  }

  .num-text {
    font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
    font-weight: 500;

    &.text-primary {
      color: #1e3a8a;
      font-weight: 600;
    }
  }

  .mb-16 {
    margin-bottom: 16px;
  }
}

.day-detail-empty {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
