<template>
  <div class="app-container hys-dashboard">
    <el-alert
      v-if="data.gapCount > 0"
      type="error"
      :closable="false"
      show-icon
      class="mb16"
      title="检测到佣金异常对账记录"
      :description="`当前有 ${data.gapCount} 笔订单因推荐链异常中断计佣，请前往「异常对账」补绑推荐人或人工核销。系统不会自动顺延或改派受益人。`"
    />

    <el-row :gutter="16">
      <el-col :xs="12" :sm="8" :md="6" v-for="card in cards" :key="card.label">
        <div class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">
            {{ card.money ? yuan(card.value) : card.value }}
            <span v-if="card.money" class="stat-unit">元</span>
          </div>
          <div class="stat-hint">{{ card.hint }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="mt16" shadow="never">
      <template #header>
        <div class="card-header">
          <span>近 7 日下单趋势</span>
          <span class="muted">柱高按当日下单量，括号内为成交额</span>
        </div>
      </template>
      <div class="trend">
        <div v-for="point in data.trend" :key="point.date" class="trend-col">
          <div class="trend-bar-wrap">
            <div class="trend-bar" :style="{ height: barHeight(point.orders) }" :title="`${point.orders} 单`"></div>
          </div>
          <div class="trend-value">{{ point.orders }}</div>
          <div class="trend-date">{{ point.date }}</div>
          <div class="trend-amount">¥{{ yuan(point.amount) }}</div>
        </div>
      </div>
    </el-card>

    <el-card class="mt16" shadow="never">
      <template #header><span>合规口径速览</span></template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="分销层级">仅两级（直推 + 间推），代码内为常量，不提供第三级开关</el-descriptions-item>
        <el-descriptions-item label="单笔拨比">398 升级订单固定拨出 200 元：前两单 100 + 100，第三单起 200 + 0</el-descriptions-item>
        <el-descriptions-item label="提现手续费">6%，按分向下取整（1000 元 → 60 元，到账 940 元）</el-descriptions-item>
        <el-descriptions-item label="单笔充值上限">5000 元，超出需拆分多次</el-descriptions-item>
        <el-descriptions-item label="注册推荐人">强制必填，无推荐码不允许建档</el-descriptions-item>
        <el-descriptions-item label="异常订单">中断计佣 + 落对账表 + 告警，禁止顺延 / 改派 / 少发</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup name="HysDashboard">
import { computed, getCurrentInstance, onMounted, ref } from 'vue'
import { getDashboard } from '@/api/hys'
import { yuan } from '../shared'

const { proxy } = getCurrentInstance()
const data = ref({ trend: [] })

const cards = computed(() => [
  { label: '会员总数', value: data.value.memberTotal || 0, hint: '不含平台根账号' },
  { label: 'VIP 会员', value: data.value.vipTotal || 0, hint: 'VIP 在有效期内' },
  { label: '订单总数', value: data.value.orderTotal || 0, hint: '商品 + 线路' },
  { label: '今日订单', value: data.value.orderToday || 0, hint: '自然日下单量' },
  { label: '累计成交额', value: data.value.gmvTotal || 0, money: true, hint: '已支付订单实付合计' },
  { label: '累计已发佣金', value: data.value.commissionTotal || 0, money: true, hint: '仅直推 + 间推' },
  { label: '待处理审核', value: data.value.pendingAudit || 0, hint: '充值 / 提现待审核' },
  { label: '异常对账', value: data.value.gapCount || 0, hint: '正常应为 0' }
])

const maxOrders = computed(() => Math.max(1, ...(data.value.trend || []).map((p) => p.orders || 0)))

function barHeight(orders) {
  return Math.max(4, Math.round(((orders || 0) / maxOrders.value) * 120)) + 'px'
}

function load() {
  getDashboard()
    .then((res) => {
      data.value = res.data || {}
    })
    .catch(() => {
      proxy.$modal.msgError('看板数据加载失败')
    })
}

onMounted(load)
</script>

<style scoped>
.hys-dashboard .mb16 {
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 16px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #1f2d3d;
  margin: 6px 0 2px;
  font-variant-numeric: tabular-nums;
}
.stat-unit {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}
.stat-hint {
  font-size: 12px;
  color: #b0b3b8;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.muted {
  color: #909399;
  font-size: 12px;
}
.trend {
  display: flex;
  align-items: flex-end;
  gap: 18px;
  padding: 8px 4px 0;
}
.trend-col {
  flex: 1;
  text-align: center;
}
.trend-bar-wrap {
  height: 128px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.trend-bar {
  width: 26px;
  background: linear-gradient(180deg, #1f5c4b, #4e9c82);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}
.trend-value {
  font-size: 13px;
  font-weight: 600;
  margin-top: 6px;
  color: #1c2b26;
}
.trend-date,
.trend-amount {
  font-size: 12px;
  color: #909399;
}
</style>
