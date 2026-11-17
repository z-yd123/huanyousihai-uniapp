<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="层级" prop="level">
        <el-select v-model="queryParams.level" placeholder="全部层级" clearable style="width: 130px">
          <el-option v-for="(item, key) in COMMISSION_LEVEL" :key="key" :label="item.text" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="已入账" :value="1" />
          <el-option label="已回冲" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert type="info" :closable="false" show-icon class="mb8"
      title="佣金拨比恒为订单实付的 200 元（20000 分）：直推第 1~2 单各 100 元，第 3 单起直推人独得 200 元、间推为 0。合计不足 200 元的部分会落入「异常对账」，不会静默丢失。" />

    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">今日发放佣金</div>
          <div class="stat-value">¥{{ yuan(stats.today || 0) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">当前筛选合计</div>
          <div class="stat-value">¥{{ yuan(pageAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">直推 / 间推笔数</div>
          <div class="stat-value">{{ directCount }} / {{ indirectCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">本页记录</div>
          <div class="stat-value">{{ list.length }} 条</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" row-key="commissionId">
      <el-table-column label="佣金ID" prop="commissionId" width="80" />
      <el-table-column label="订单号" prop="orderNo" width="180" />
      <el-table-column label="商品" prop="productTitle" min-width="180" show-overflow-tooltip />
      <el-table-column label="受益人" width="160">
        <template #default="{ row }">
          <div class="cell-title">{{ row.memberName }}</div>
          <div class="cell-sub">{{ maskPhone(row.memberPhone) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="层级" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="(COMMISSION_LEVEL[row.level] || {}).type || 'info'">
            {{ (COMMISSION_LEVEL[row.level] || {}).text || row.level }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="佣金金额" width="120" align="right">
        <template #default="{ row }">
          <span :class="row.status === 2 ? 'money-reversed' : 'money'">¥{{ yuan(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已入账' : '已回冲' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结算时间" width="170">
        <template #default="{ row }">{{ formatTime(row.settleTime) }}</template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="HysCommission">
import { computed, reactive, ref } from 'vue'
import { commissionStats, listCommission } from '@/api/hys'
import { COMMISSION_LEVEL, yuan } from '../shared'

const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const stats = ref({})

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderNo: '', level: '', status: null })

const pageAmount = computed(() => list.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const directCount = computed(() => list.value.filter(item => item.level === 'DIRECT').length)
const indirectCount = computed(() => list.value.filter(item => item.level === 'INDIRECT').length)

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
    const res = await listCommission(queryParams)
    list.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function getStats() {
  const res = await commissionStats()
  stats.value = res.data || {}
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.orderNo = ''
  queryParams.level = ''
  queryParams.status = null
  handleQuery()
}

getList()
getStats()
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}
.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-top: 6px;
}
.money {
  color: #c45656;
  font-weight: 600;
}
.money-reversed {
  color: var(--el-text-color-secondary);
  text-decoration: line-through;
}
.cell-title {
  font-size: 13px;
}
.cell-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
