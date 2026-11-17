<template>
  <div class="app-container">
    <el-alert type="warning" :closable="false" show-icon class="mb8"
      title="异常对账是佣金拨比的兜底账本：当推荐链缺失、中途断链导致佣金发不满 200 元时，差额会落在这里，必须人工核销。正常情况下本页应为空 —— 有记录说明数据侧出了问题。" />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-select v-model="queryParams.reason" placeholder="全部原因" clearable style="width: 200px">
          <el-option label="推荐人缺失（GAP_REFERRER_MISSING）" value="GAP_REFERRER_MISSING" />
          <el-option label="间推人缺失（GAP_INDIRECT_MISSING）" value="GAP_INDIRECT_MISSING" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="handled">
        <el-select v-model="queryParams.handled" placeholder="全部" clearable style="width: 130px">
          <el-option label="待处理" value="0" />
          <el-option label="已核销" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" row-key="gapId">
      <el-table-column label="ID" prop="gapId" width="70" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="未发放金额" width="120" align="right">
        <template #default="{ row }">
          <span class="money">¥{{ yuan(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="原因码" prop="reason" width="200">
        <template #default="{ row }">
          <el-tag size="small" type="danger" effect="plain">{{ row.reason }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="明细" prop="detail" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.handled === '0' ? 'warning' : 'success'">
            {{ row.handled === '0' ? '待处理' : '已核销' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产生时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="核销说明" prop="handleRemark" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.handleRemark || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.handled === '0'" link type="primary" v-hasPermi="['hys:gap:handle']"
            @click="handleClose(row)">核销</el-button>
          <span v-else class="done-text">已处理</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="HysPayoutGap">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { closePayoutGap, listPayoutGap } from '@/api/hys'
import { yuan } from '../shared'

const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderNo: '', reason: '', handled: '' })

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : '—'
}

async function getList() {
  loading.value = true
  try {
    const res = await listPayoutGap(queryParams)
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
  queryParams.reason = ''
  queryParams.handled = ''
  handleQuery()
}

async function handleClose(row) {
  const { value } = await ElMessageBox.prompt(
    `该记录金额 ¥${yuan(row.amount)} 将不再发放，核销后不可撤销。请填写核销依据。`,
    '人工核销',
    {
      inputPlaceholder: '如：已线下补偿给用户 / 推荐链无法补全',
      inputValidator: v => (v && v.trim().length > 0) || '核销依据不能为空'
    }
  )
  await closePayoutGap(row.gapId, value)
  ElMessage.success('已核销')
  getList()
}

getList()
</script>

<style scoped>
.money {
  color: #c45656;
  font-weight: 600;
}
.done-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
