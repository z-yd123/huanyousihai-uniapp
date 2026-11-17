<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="申请单号" prop="applyNo">
        <el-input v-model="queryParams.applyNo" placeholder="申请单号" clearable style="width: 190px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="applyType">
        <el-select v-model="queryParams.applyType" placeholder="全部类型" clearable style="width: 140px">
          <el-option label="充值登记" value="RECHARGE" />
          <el-option label="提现申请" value="WITHDRAW" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-tabs v-model="activeTab" @tab-change="onTabChange" class="mb8">
      <el-tab-pane label="待审核" name="pending" />
      <el-tab-pane label="全部申请" name="all" />
    </el-tabs>

    <el-table v-loading="loading" :data="list" row-key="applyId">
      <el-table-column label="申请单号" prop="applyNo" width="190" />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.applyType === 'WITHDRAW' ? 'warning' : 'success'">
            {{ row.applyType === 'WITHDRAW' ? '提现' : '充值' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="会员" width="150">
        <template #default="{ row }">
          <div class="cell-title">{{ row.memberName }}</div>
          <div class="cell-sub">{{ maskPhone(row.memberPhone) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="申请金额" width="110" align="right">
        <template #default="{ row }"><span class="money">¥{{ yuan(row.amount) }}</span></template>
      </el-table-column>
      <el-table-column label="手续费" width="110" align="right">
        <template #default="{ row }">
          <span v-if="row.applyType === 'WITHDRAW'">¥{{ yuan(row.fee) }}</span>
          <span v-else class="cell-sub">—</span>
        </template>
      </el-table-column>
      <el-table-column label="实际到账" width="110" align="right">
        <template #default="{ row }">¥{{ yuan(row.actualAmount) }}</template>
      </el-table-column>
      <el-table-column label="收款信息" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="row.applyType === 'WITHDRAW'">
            {{ row.bankName || '—' }} / {{ row.accountName || '—' }}
            <div class="cell-sub">{{ row.accountNo || '—' }}</div>
          </div>
          <span v-else class="cell-sub">余额充值（无需账户）</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="(APPLY_STATUS[row.status] || {}).type || 'info'">
            {{ (APPLY_STATUS[row.status] || {}).text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="审核意见" prop="auditRemark" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.auditRemark || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" v-hasPermi="['hys:fund:audit']" @click="handleAudit(row, true)">通过</el-button>
            <el-button link type="danger" v-hasPermi="['hys:fund:audit']" @click="handleAudit(row, false)">驳回</el-button>
          </template>
          <span v-else class="cell-sub">{{ row.auditBy || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="HysFund">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditFundApply, listFundApply } from '@/api/hys'
import { APPLY_STATUS, yuan } from '../shared'

const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const activeTab = ref('pending')

const queryParams = reactive({ pageNum: 1, pageSize: 10, applyNo: '', applyType: '', status: 0 })

function maskPhone(phone) {
  if (!phone) return '—'
  return String(phone).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : '—'
}

function onTabChange(name) {
  queryParams.status = name === 'pending' ? 0 : null
  handleQuery()
}

async function getList() {
  loading.value = true
  try {
    const res = await listFundApply(queryParams)
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
  queryParams.applyNo = ''
  queryParams.applyType = ''
  queryParams.status = activeTab.value === 'pending' ? 0 : null
  handleQuery()
}

async function handleAudit(row, pass) {
  const isWithdraw = row.applyType === 'WITHDRAW'
  const tip = pass
    ? isWithdraw
      ? `通过后将从冻结余额扣减 ¥${yuan(row.amount)}，用户实际到账 ¥${yuan(row.actualAmount)}（手续费 ¥${yuan(row.fee)}）`
      : `通过后将为用户余额增加 ¥${yuan(row.amount)}`
    : isWithdraw
      ? `驳回后将解冻 ¥${yuan(row.amount)}，余额恢复到可用状态`
      : '驳回后不会产生任何入账'

  const { value } = await ElMessageBox.prompt(tip, pass ? '审核通过' : '审核驳回', {
    inputPlaceholder: pass ? '审核备注（可留空）' : '请填写驳回原因',
    inputValidator: v => pass || (v && v.trim().length > 0) || '驳回原因不能为空'
  })

  await auditFundApply(row.applyId, pass, value || '')
  ElMessage.success(pass ? '审核通过' : '已驳回')
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
}
.cell-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
