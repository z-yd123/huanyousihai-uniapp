<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="(item, key) in ORDER_STATUS" :key="key" :label="item.text" :value="Number(key)" />
        </el-select>
      </el-form-item>
      <el-form-item label="售后" prop="afterSale">
        <el-select v-model="queryParams.afterSale" placeholder="全部" clearable style="width: 120px">
          <el-option label="售后中" :value="1" />
          <el-option label="已退款" :value="2" />
          <el-option label="已驳回" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Money" :disabled="!refundTarget" @click="handleRefund">退款</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" row-key="orderId" @selection-change="onSelect" highlight-current-row>
      <el-table-column type="selection" width="46" :selectable="row => row.status === 1 || row.status === 3" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="商品" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="cell-title">{{ row.productTitle }}</div>
          <div class="cell-sub">{{ PRODUCT_TYPE[row.productType] || row.productType }}</div>
        </template>
      </el-table-column>
      <el-table-column label="下单人" width="150">
        <template #default="{ row }">
          <div class="cell-title">{{ row.memberName }}</div>
          <div class="cell-sub">{{ maskPhone(row.memberPhone) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="单价×数量" width="130" align="right">
        <template #default="{ row }">¥{{ yuan(row.unitPrice) }} × {{ row.quantity }}</template>
      </el-table-column>
      <el-table-column label="应付金额" width="110" align="right">
        <template #default="{ row }">
          <span class="money">¥{{ yuan(row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTag(row.status).type">{{ statusTag(row.status).text }}</el-tag>
          <el-tag v-if="row.afterSale" size="small" type="danger" class="ml4" effect="plain">
            {{ AFTER_SALE_STATUS[row.afterSale] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="160">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 1 && row.productType !== 'VIP_RIGHTS'" link type="success"
            v-hasPermi="['hys:order:ship']" @click="handleShip(row)">发货</el-button>
          <el-button v-if="row.status === 3" link type="danger" v-hasPermi="['hys:order:refund']"
            @click="handleReject(row)">驳回售后</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="720px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag size="small" :type="statusTag(detail.status).type">{{ statusTag(detail.status).text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品" :span="2">{{ detail.productTitle }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ yuan(detail.unitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ yuan(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="money">¥{{ yuan(detail.payAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.payType || '余额支付' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(detail.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="出行日期">{{ detail.travelDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }} / {{ maskPhone(detail.contactPhone) }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatTime(detail.shipTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatTime(detail.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="售后状态">{{ AFTER_SALE_STATUS[detail.afterSale] || '无' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ detail.refundReason || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="dialog.visible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HysOrder">
import { getCurrentInstance, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrder, listOrder, refundOrder, rejectAfterSale, shipOrder } from '@/api/hys'
import { AFTER_SALE_STATUS, ORDER_STATUS, PRODUCT_TYPE, statusTag, yuan } from '../shared'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const refundTarget = ref(null)
const detail = ref(null)

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderNo: '', status: null, afterSale: null })
const dialog = reactive({ visible: false, title: '' })

/** 列表接口只回传打码后的手机号，避免前端拿到明文（后端已脱敏，这里再兜底一层）。 */
function maskPhone(phone) {
  if (!phone) return '—'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : '—'
}

async function getList() {
  loading.value = true
  try {
    const res = await listOrder(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.orderNo = ''
  queryParams.status = null
  queryParams.afterSale = null
  handleQuery()
}

function onSelect(rows) {
  // 只允许对「待收货 / 售后中」订单发起退款，避免误退已完成订单
  refundTarget.value = rows.length === 1 ? rows[0] : null
  if (rows.length > 1) {
    ElMessage.warning('一次只能处理一笔订单')
  }
}

async function handleDetail(row) {
  const res = await getOrder(row.orderId)
  detail.value = res.data
  dialog.title = '订单详情 ' + row.orderNo
  dialog.visible = true
}

async function handleShip(row) {
  await ElMessageBox.confirm(`确认对订单 ${row.orderNo} 执行发货？`, '发货确认', { type: 'warning' })
  await shipOrder(row.orderId)
  ElMessage.success('已发货')
  getList()
}

async function handleReject(row) {
  await ElMessageBox.confirm(
    `驳回后订单 ${row.orderNo} 将回到「待收货」，用户可再次联系客服处理。确认驳回？`,
    '驳回售后',
    { type: 'warning' }
  )
  await rejectAfterSale(row.orderId)
  ElMessage.success('已驳回售后')
  getList()
}

async function handleRefund() {
  const row = refundTarget.value
  if (!row) {
    ElMessage.warning('请先选择一笔订单')
    return
  }
  const { value } = await ElMessageBox.prompt(
    `退款将原路退回用户余额，并自动回冲已发放佣金（订单 ${row.orderNo}）`,
    '退款确认',
    {
      inputPlaceholder: '请填写退款原因',
      inputValidator: v => (v && v.trim().length > 0) || '退款原因不能为空'
    }
  )
  await refundOrder(row.orderId, value)
  ElMessage.success('退款完成，佣金已回冲')
  getList()
}

getList()
</script>

<style scoped>
.money {
  color: #c45656;
  font-weight: 600;
}
.cell-title {
  font-size: 13px;
  line-height: 1.4;
}
.cell-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.ml4 {
  margin-left: 4px;
}
</style>
