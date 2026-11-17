<template>
  <div class="app-container">
    <el-alert type="error" :closable="false" show-icon class="mb8"
      title="合规红线（修改会被服务端直接拒绝）：单笔订单拨比总额恒为 20000 分（200 元）；分销层级固定两级，payout.max_depth 不提供修改入口；提现手续费率不得高于 0.06。" />

    <el-tabs v-model="activeGroup">
      <el-tab-pane v-for="group in groups" :key="group.key" :label="group.label" :name="group.key">
        <el-table v-loading="loading" :data="group.rows" row-key="configKey">
          <el-table-column label="参数键" prop="configKey" width="240">
            <template #default="{ row }">
              <span class="mono">{{ row.configKey }}</span>
              <el-tag v-if="isProtected(row.configKey)" size="small" type="danger" effect="plain" class="ml4">只读</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="参数名称" prop="configName" width="200" show-overflow-tooltip />
          <el-table-column label="当前值" min-width="240">
            <template #default="{ row }">
              <template v-if="editingKey === row.configKey">
                <el-input v-model="editingValue" size="small" style="width: 220px" @keyup.enter="save(row)" />
              </template>
              <template v-else>
                <span class="mono">{{ row.configValue }}</span>
                <span v-if="unitOf(row.configKey)" class="unit">{{ displayValue(row) }}</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="remark" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <template v-if="editingKey === row.configKey">
                <el-button link type="success" :loading="saving" @click="save(row)">保存</el-button>
                <el-button link @click="cancel">取消</el-button>
              </template>
              <el-button v-else link type="primary" :disabled="isProtected(row.configKey)"
                v-hasPermi="['hys:config:edit']" @click="startEdit(row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="HysConfig">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listConfig, updateConfig } from '@/api/hys'
import { yuan } from '../shared'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const activeGroup = ref('payout')
const editingKey = ref('')
const editingValue = ref('')

/** 只读参数：层级上限属于监管红线，不允许在界面上改。 */
const PROTECTED = ['payout.max_depth']

const GROUPS = [
  { key: 'payout', label: '佣金拨比', match: key => key.startsWith('payout.') },
  { key: 'wallet', label: '钱包风控', match: key => key.startsWith('wallet.') },
  { key: 'vip', label: '会员权益', match: key => key.startsWith('vip.') },
  { key: 'other', label: '展示与文案', match: key => !/^(payout|wallet|vip)\./.test(key) }
]

const groups = computed(() =>
  GROUPS.map(group => ({
    ...group,
    rows: rows.value.filter(item => group.match(item.configKey))
  })).filter(group => group.rows.length > 0)
)

/** 以「分」存储的参数，展示时补一份元值，避免运营按分误填。 */
function unitOf(key) {
  return /price|amount|limit|total|first_two|_after$|recharge_limit|transfer_daily_limit/.test(key) ? 'cent' : ''
}

function displayValue(row) {
  const value = Number(row.configValue)
  if (Number.isNaN(value)) return ''
  return unitOf(row.configKey) ? `（= ¥${yuan(value)}）` : ''
}

function isProtected(key) {
  return PROTECTED.includes(key)
}

async function getList() {
  loading.value = true
  try {
    const res = await listConfig({ pageNum: 1, pageSize: 100 })
    rows.value = res.rows || []
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  editingKey.value = row.configKey
  editingValue.value = row.configValue
}

function cancel() {
  editingKey.value = ''
  editingValue.value = ''
}

async function save(row) {
  const value = String(editingValue.value || '').trim()
  if (!value) {
    ElMessage.warning('参数值不能为空')
    return
  }
  saving.value = true
  try {
    await updateConfig(row.configKey, value)
    ElMessage.success('已保存')
    cancel()
    getList()
  } finally {
    saving.value = false
  }
}

onMounted(getList)
</script>

<style scoped>
.mono {
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
.unit {
  margin-left: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.ml4 {
  margin-left: 4px;
}
</style>
