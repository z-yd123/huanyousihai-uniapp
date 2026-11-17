<template>
  <div class="app-container">
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      class="mb12"
      title="推荐关系不可随意修改"
      description="正常会员的推荐关系一经建立即锁定；仅「异常待补绑」账号可补绑推荐人，补绑后系统会自动重算该会员历史未发放的佣金。余额与会员等级不提供直接编辑入口，资金只能通过充值/提现审核流转。"
    />

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="支持模糊查询" clearable style="width: 170px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="名称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="会员名称" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="性质" prop="level">
        <el-select v-model="queryParams.level" placeholder="全部" clearable style="width: 130px">
          <el-option label="普通会员" value="0" />
          <el-option label="VIP 会员" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
          <el-option label="异常待补绑" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" prop="memberId" width="70" />
      <el-table-column label="名称" prop="nickname" width="110" show-overflow-tooltip />
      <el-table-column label="手机号" prop="phone" width="130" />
      <el-table-column label="区域" width="180">
        <template #default="{ row }">{{ row.province }}{{ row.city }}{{ row.district }}</template>
      </el-table-column>
      <el-table-column label="推荐人" width="150">
        <template #default="{ row }">
          <span v-if="row.referrerName">{{ row.referrerName }}（{{ row.referrerPhone }}）</span>
          <el-tag v-else type="danger" size="small">无推荐人</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="性质" width="120">
        <template #default="{ row }">
          <el-tag :type="levelTag(row.level).type" size="small">{{ levelTag(row.level).text }}</el-tag>
          <div v-if="row.vipExpireAt" class="sub">{{ row.vipExpireAt }}</div>
        </template>
      </el-table-column>
      <el-table-column label="余额(元)" width="110" align="right">
        <template #default="{ row }">{{ yuan(row.balance) }}</template>
      </el-table-column>
      <el-table-column label="直推/团队" width="100" align="center">
        <template #default="{ row }">{{ row.directCount || 0 }} / {{ row.teamCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="累计佣金(元)" width="120" align="right">
        <template #default="{ row }">{{ yuan(row.commissionTotal) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="(MEMBER_STATUS[row.status] || {}).type" size="small">{{ (MEMBER_STATUS[row.status] || {}).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)" v-hasPermi="['hys:member:edit']">编辑</el-button>
          <el-button link type="warning" @click="handleRebind(row)" v-hasPermi="['hys:member:rebind']">补绑</el-button>
          <el-button link type="info" @click="handleResetPwd(row)" v-hasPermi="['hys:member:edit']">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑资料 -->
    <el-dialog v-model="edit.visible" title="编辑会员资料" width="560px" append-to-body>
      <el-form ref="editRef" :model="edit.form" :rules="editRules" label-width="90px">
        <el-form-item label="名称" prop="nickname"><el-input v-model="edit.form.nickname" maxlength="20" /></el-form-item>
        <el-form-item label="区域" prop="province">
          <el-row :gutter="8">
            <el-col :span="8"><el-input v-model="edit.form.province" placeholder="省" /></el-col>
            <el-col :span="8"><el-input v-model="edit.form.city" placeholder="市" /></el-col>
            <el-col :span="8"><el-input v-model="edit.form.district" placeholder="区/县" /></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="edit.form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用（立即下线）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="edit.form.remark" type="textarea" :rows="2" maxlength="100" /></el-form-item>
        <el-alert type="info" :closable="false" show-icon title="余额、等级、推荐关系不在此处修改：资金走审核流程，推荐关系走补绑流程。" />
      </el-form>
      <template #footer>
        <el-button @click="edit.visible = false">取消</el-button>
        <el-button type="primary" :loading="edit.loading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 补绑推荐人 -->
    <el-dialog v-model="rebind.visible" title="补绑推荐人" width="520px" append-to-body>
      <el-alert type="error" :closable="false" show-icon class="mb12" title="仅用于推荐关系异常的账号" description="补绑后：该会员的状态恢复为正常，令牌立即失效需重新登录，历史中断计佣的订单会自动重算。" />
      <el-form ref="rebindRef" :model="rebind.form" :rules="rebindRules" label-width="110px">
        <el-form-item label="会员"><el-input :model-value="rebind.memberLabel" disabled /></el-form-item>
        <el-form-item label="新推荐人手机号" prop="referrerPhone">
          <el-input v-model="rebind.form.referrerPhone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="处理说明" prop="remark">
          <el-input v-model="rebind.form.remark" type="textarea" :rows="2" maxlength="100" placeholder="如：历史导入数据，补绑线下登记的推荐人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rebind.visible = false">取消</el-button>
        <el-button type="warning" :loading="rebind.loading" @click="submitRebind">确认补绑并重算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HysMember">
import { getCurrentInstance, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMember, rebindReferrer, resetMemberPassword, updateMember } from '@/api/hys'
import { MEMBER_STATUS, levelTag, yuan } from '../shared'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, phone: '', nickname: '', level: '', status: '' })

const edit = reactive({ visible: false, loading: false, form: {}, memberId: null })
const editRef = ref()
const editRules = {
  nickname: [{ required: true, message: '请填写名称', trigger: 'blur' }],
  province: [{ required: true, message: '请填写省份', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const rebind = reactive({ visible: false, loading: false, form: { referrerPhone: '', remark: '' }, memberId: null, memberLabel: '' })
const rebindRef = ref()
const rebindRules = {
  referrerPhone: [
    { required: true, message: '请填写推荐人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

function getList() {
  loading.value = true
  listMember(queryParams)
    .then((res) => {
      list.value = res.rows || []
      total.value = res.total || 0
    })
    .finally(() => {
      loading.value = false
    })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  Object.assign(queryParams, { pageNum: 1, phone: '', nickname: '', level: '', status: '' })
  getList()
}

function handleEdit(row) {
  edit.memberId = row.memberId
  edit.form = {
    nickname: row.nickname,
    province: row.province,
    city: row.city,
    district: row.district,
    status: row.status === '2' ? '0' : row.status,
    remark: row.remark
  }
  edit.visible = true
}

function submitEdit() {
  editRef.value.validate((valid) => {
    if (!valid) return
    edit.loading = true
    updateMember(edit.memberId, edit.form)
      .then(() => {
        ElMessage.success('已保存')
        edit.visible = false
        getList()
      })
      .finally(() => {
        edit.loading = false
      })
  })
}

function handleRebind(row) {
  rebind.memberId = row.memberId
  rebind.memberLabel = `${row.nickname}（${row.phone}）`
  rebind.form = { referrerPhone: '', remark: '' }
  rebind.visible = true
  if (row.referrerId && row.status !== '2') {
    ElMessage.warning('该会员推荐关系正常，补绑会被服务端拒绝；改绑请走线下审批')
  }
}

function submitRebind() {
  rebindRef.value.validate((valid) => {
    if (!valid) return
    rebind.loading = true
    rebindReferrer(rebind.memberId, rebind.form)
      .then((res) => {
        ElMessage.success(res.msg || '补绑成功')
        rebind.visible = false
        getList()
      })
      .finally(() => {
        rebind.loading = false
      })
  })
}

function handleResetPwd(row) {
  ElMessageBox.prompt(`为「${row.nickname}」设置新密码（6-20 位），重置后该会员需重新登录`, '重置密码', {
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度需为 6-20 位'
  })
    .then(({ value }) => resetMemberPassword(row.memberId, value))
    .then(() => ElMessage.success('密码已重置'))
    .catch(() => {})
}

getList()
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.mb12 {
  margin-bottom: 12px;
}
.sub {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}
</style>
