<template>
  <section class="order-manage">
    <div class="page-head">
      <div>
        <h2 class="page-title">订单管理</h2>
        <p class="page-subtitle">查看所有订单、发货、取消订单、处理退款，并支持导出 Excel</p>
      </div>
      <el-button :icon="Download" type="success" plain @click="exportExcel">导出 Excel</el-button>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="订单号">
          <el-input
            v-model="filter.orderNo"
            placeholder="精确或模糊匹配"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="收件人">
          <el-input
            v-model="filter.userKeyword"
            placeholder="姓名 / 手机号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="全部" :value="null" />
            <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="orders" stripe empty-text="暂无订单数据" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column label="收件人" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div>{{ row.receiverName || '—' }}</div>
              <div class="muted">{{ row.receiverPhone || '' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="120" align="right">
          <template #default="{ row }">¥{{ Number(row.payAmount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.orderStatus)" size="small">
              {{ statusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.orderStatus === 1"
              text
              type="success"
              size="small"
              @click="openShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="canCancel(row.orderStatus)"
              text
              type="warning"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.orderStatus === 6"
              text
              type="primary"
              size="small"
              @click="openRefund(row)"
            >
              处理退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @change="loadList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" :title="`订单详情 - ${active?.orderNo || ''}`" width="720px">
      <el-descriptions v-if="active" :column="2" border>
        <el-descriptions-item label="订单号">{{ active.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(active.orderStatus)" size="small">{{ statusLabel(active.orderStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ active.userId }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ paymentLabel(active.paymentType) }}</el-descriptions-item>
        <el-descriptions-item label="商品金额">¥{{ Number(active.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ Number(active.freightAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="优惠">¥{{ Number(active.discountAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付">
          <span class="strong-amount">¥{{ Number(active.payAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收件人">{{ active.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ active.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">
          {{ [active.receiverProvince, active.receiverCity, active.receiverDistrict, active.receiverAddress].filter(Boolean).join(' ') }}
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(active.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(active.payTime) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatTime(active.deliveryTime) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="收货时间">{{ formatTime(active.receiveTime) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <div class="content-block">{{ active.remark || '—' }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <h4 v-if="detailItems.length" class="items-title">商品明细</h4>
      <el-table v-if="detailItems.length" :data="detailItems" size="small" border style="width: 100%">
        <el-table-column prop="productName" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="specInfo" label="规格" width="160" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="{ row }">¥{{ Number(row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">
            ¥{{ (Number(row.price || 0) * Number(row.quantity || 0)).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="shipVisible" title="订单发货" width="480px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="订单号">
          <span class="readonly-text">{{ active?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="shipForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可填写物流单号 / 物流公司 / 其他说明"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="处理退款" width="480px">
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="订单号">
          <span class="readonly-text">{{ active?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-radio-group v-model="refundForm.approved">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="refundForm.remark"
            type="textarea"
            :rows="3"
            placeholder="退款说明 / 驳回原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRefund">确认提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Download } from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import {
  adminListOrders,
  adminGetOrderDetail,
  adminShipOrder,
  adminCancelOrder,
  adminProcessRefund,
  adminExportOrdersUrl
} from '@/api/admin'

const STATUS_OPTIONS = [
  { value: 0, label: '待支付' },
  { value: 1, label: '待发货' },
  { value: 2, label: '待收货' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已关闭' },
  { value: 5, label: '已取消' },
  { value: 6, label: '退款中' },
  { value: 7, label: '已退款' }
]

const STATUS_TAG_TYPE = {
  0: 'warning',
  1: 'primary',
  2: '',
  3: 'success',
  4: 'info',
  5: 'info',
  6: 'danger',
  7: 'info'
}

const loading = ref(false)
const submitting = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filter = reactive({
  orderNo: '',
  userKeyword: '',
  status: null
})

const detailVisible = ref(false)
const shipVisible = ref(false)
const refundVisible = ref(false)
const active = ref(null)
const detailItems = ref([])

const shipForm = reactive({ remark: '' })
const refundForm = reactive({ approved: true, remark: '' })

function statusLabel(code) {
  return STATUS_OPTIONS.find((s) => s.value === code)?.label || `状态${code}`
}

function statusTag(code) {
  return STATUS_TAG_TYPE[code] ?? ''
}

function paymentLabel(code) {
  switch (code) {
    case 1: return '支付宝'
    case 2: return '微信'
    case 3: return '银行卡'
    default: return '未支付'
  }
}

function canCancel(status) {
  return [0, 1, 2, 6].includes(status)
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderNo: filter.orderNo || undefined,
      userKeyword: filter.userKeyword || undefined,
      status: filter.status ?? undefined
    }
    const { data } = await adminListOrders(params)
    orders.value = data?.list || []
    total.value = data?.total || 0
  } catch (err) {
    ElMessage.error(err?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  filter.orderNo = ''
  filter.userKeyword = ''
  filter.status = null
  pageNum.value = 1
  loadList()
}

async function openDetail(row) {
  active.value = row
  detailItems.value = []
  detailVisible.value = true
  try {
    const { data } = await adminGetOrderDetail(row.id)
    if (data?.order) Object.assign(active.value, data.order)
    detailItems.value = data?.items || []
  } catch (err) {
    ElMessage.error(err?.message || '加载订单详情失败')
  }
}

function openShip(row) {
  active.value = row
  shipForm.remark = ''
  shipVisible.value = true
}

async function submitShip() {
  if (!active.value) return
  submitting.value = true
  try {
    await adminShipOrder(active.value.id, { remark: shipForm.remark })
    ElMessage.success('订单已发货')
    shipVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '发货失败')
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(`确定取消订单「${row.orderNo}」？`, '取消确认', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await adminCancelOrder(row.id)
    ElMessage.success('订单已取消')
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '取消失败')
  }
}

function openRefund(row) {
  active.value = row
  refundForm.approved = true
  refundForm.remark = ''
  refundVisible.value = true
}

async function submitRefund() {
  if (!active.value) return
  submitting.value = true
  try {
    await adminProcessRefund(active.value.id, {
      approved: refundForm.approved,
      remark: refundForm.remark
    })
    ElMessage.success(refundForm.approved ? '已同意退款' : '已驳回退款')
    refundVisible.value = false
    await loadList()
  } catch (err) {
    ElMessage.error(err?.message || '处理失败')
  } finally {
    submitting.value = false
  }
}

function exportExcel() {
  const url = adminExportOrdersUrl(filter.status)
  const a = document.createElement('a')
  a.href = url
  a.target = '_blank'
  a.rel = 'noopener'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function formatTime(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 19).replace('T', ' ') : value
}

onMounted(loadList)
</script>

<style scoped lang="scss">
.order-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.page-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px;
}

.user-cell {
  line-height: 1.4;
}

.muted {
  color: #9ca3af;
  font-size: 12px;
}

.readonly-text {
  color: #1f2937;
  font-weight: 500;
}

.strong-amount {
  color: #ef4444;
  font-weight: 700;
}

.items-title {
  margin: 20px 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.content-block {
  white-space: pre-wrap;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
