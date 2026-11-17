<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="线路 / 商品标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="productType">
        <el-select v-model="queryParams.productType" placeholder="全部类型" clearable style="width: 150px">
          <el-option v-for="(label, key) in PRODUCT_TYPE" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="线路类型" prop="routeType">
        <el-select v-model="queryParams.routeType" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(label, key) in ROUTE_TYPE" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="上架" value="0" />
          <el-option label="下架" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hys:product:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" row-key="productId">
      <el-table-column label="ID" prop="productId" width="70" />
      <el-table-column label="封面" width="90">
        <template #default="{ row }">
          <el-image :src="row.cover" fit="cover" style="width: 56px; height: 40px; border-radius: 4px">
            <template #error>
              <div class="img-fallback">无图</div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small">{{ PRODUCT_TYPE[row.productType] || row.productType }}</el-tag>
          <el-tag v-if="row.routeType" size="small" type="info" class="ml4">{{ ROUTE_TYPE[row.routeType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="普通价" width="100" align="right">
        <template #default="{ row }">¥{{ yuan(row.normalPrice) }}</template>
      </el-table-column>
      <el-table-column label="VIP 价" width="100" align="right">
        <template #default="{ row }">¥{{ yuan(row.vipPrice) }}</template>
      </el-table-column>
      <el-table-column label="库存/已售" width="110" align="center">
        <template #default="{ row }">{{ row.stock }} / {{ row.sales }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="0"
            inactive-value="1"
            @change="(val) => handleStatusChange(row, val)"
            v-hasPermi="['hys:product:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['hys:product:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['hys:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增 / 修改 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="900px" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="类型" prop="productType">
              <el-select v-model="form.productType" style="width: 100%" @change="onTypeChange">
                <el-option v-for="(label, key) in PRODUCT_TYPE" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="线路类型" prop="routeType">
              <el-select v-model="form.routeType" clearable :disabled="form.productType !== 'ROUTE'" style="width: 100%">
                <el-option v-for="(label, key) in ROUTE_TYPE" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" maxlength="100" />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="普通价(元)" prop="normalPriceYuan">
              <el-input-number v-model="form.normalPriceYuan" :min="0.01" :precision="2" :step="10" style="width: 100%" :disabled="form.productType === 'VIP_RIGHTS'" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="VIP 价(元)" prop="vipPriceYuan">
              <el-input-number v-model="form.vipPriceYuan" :min="0.01" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="天数"><el-input-number v-model="form.days" :min="0" :max="60" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="晚数"><el-input-number v-model="form.nights" :min="0" :max="60" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图 URL">
          <el-input v-model="form.cover" placeholder="/static/img/route-xishuangbanna.jpg" />
        </el-form-item>
        <el-form-item label="图集 URL">
          <el-input v-model="form.imagesText" type="textarea" :rows="2" placeholder="每行一个图片地址，保存时自动转 JSON 数组" />
        </el-form-item>
        <el-form-item label="视频 URL">
          <el-input v-model="form.videoUrl" placeholder="可选，小视频地址" />
        </el-form-item>
        <el-row v-if="form.productType === 'ROUTE'">
          <el-col :span="8">
            <el-form-item label="集合城市"><el-input v-model="form.departCity" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="集合时间"><el-input v-model="form.gatherTime" placeholder="07:00" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="行程天数说明"><el-input v-model="form.daysText" placeholder="如 6 天 5 晚" disabled /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="集合地点" v-if="form.productType === 'ROUTE'">
          <el-input v-model="form.gatherPlace" maxlength="100" />
        </el-form-item>
        <el-row v-if="form.productType === 'ROUTE'">
          <el-col :span="12">
            <el-form-item label="费用包含"><el-input v-model="form.includes" type="textarea" :rows="2" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用不含"><el-input v-model="form.excludes" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图文详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="支持 HTML 片段" />
        </el-form-item>

        <el-divider v-if="form.productType === 'ROUTE'" content-position="left">每日行程（按天分段）</el-divider>
        <div v-if="form.productType === 'ROUTE'">
          <el-table :data="form.dayList" size="small" border>
            <el-table-column label="天" width="60" align="center">
              <template #default="{ $index }">DAY {{ $index + 1 }}</template>
            </el-table-column>
            <el-table-column label="当日标题" min-width="160">
              <template #default="{ row }"><el-input v-model="row.title" size="small" /></template>
            </el-table-column>
            <el-table-column label="行程文案" min-width="240">
              <template #default="{ row }"><el-input v-model="row.content" size="small" type="textarea" :rows="2" /></template>
            </el-table-column>
            <el-table-column label="集合时间" width="110">
              <template #default="{ row }"><el-input v-model="row.gatherTime" size="small" placeholder="08:00" /></template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="form.dayList.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="mt8" plain icon="Plus" size="small" @click="addDay">添加一天</el-button>
          <div class="tip">提示：保存时会按当前顺序重新编号 DAY 1..N，删除中间某天不会造成编号空洞。</div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HysProduct">
import { getCurrentInstance, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProduct,
  changeProductStatus,
  delProduct,
  getProduct,
  listProduct,
  updateProduct
} from '@/api/hys'
import { PRODUCT_TYPE, ROUTE_TYPE, cents, yuan } from '../shared'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const formRef = ref()

const queryParams = reactive({ pageNum: 1, pageSize: 10, title: '', productType: '', routeType: '', status: '' })
const dialog = reactive({ visible: false, title: '' })

const defaultForm = () => ({
  productId: null,
  productType: 'ROUTE',
  routeType: 'GROUP',
  title: '',
  subtitle: '',
  cover: '',
  imagesText: '',
  videoUrl: '',
  detail: '',
  normalPriceYuan: 0,
  vipPriceYuan: 0,
  stock: 0,
  days: 0,
  nights: 0,
  departCity: '',
  gatherTime: '',
  gatherPlace: '',
  includes: '',
  excludes: '',
  sort: 0,
  status: '0',
  dayList: []
})
const form = reactive(defaultForm())

const rules = {
  productType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  normalPriceYuan: [{ required: true, message: '请填写普通价', trigger: 'blur' }],
  vipPriceYuan: [{ required: true, message: '请填写 VIP 价', trigger: 'blur' }]
}

function getList() {
  loading.value = true
  listProduct(queryParams)
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
  Object.assign(queryParams, { pageNum: 1, title: '', productType: '', routeType: '', status: '' })
  getList()
}

function onTypeChange() {
  if (form.productType === 'VIP_RIGHTS') {
    form.normalPriceYuan = 398
    form.vipPriceYuan = 398
    form.routeType = null
    form.days = 0
    form.nights = 0
  }
}

function addDay() {
  form.dayList.push({ title: '', content: '', gatherTime: '', gatherPlace: '', images: '[]' })
}

function handleAdd() {
  Object.assign(form, defaultForm())
  form.dayList = []
  dialog.title = '新增线路 / 商品'
  dialog.visible = true
  formRef.value?.clearValidate()
}

function handleUpdate(row) {
  getProduct(row.productId).then((res) => {
    const data = res.data || {}
    Object.assign(form, defaultForm(), {
      ...data,
      normalPriceYuan: Number((data.normalPrice || 0) / 100),
      vipPriceYuan: Number((data.vipPrice || 0) / 100),
      imagesText: (data.imageList || []).join('\n'),
      dayList: (data.dayList || []).map((d) => ({ ...d }))
    })
    dialog.title = '修改线路 / 商品'
    dialog.visible = true
  })
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    if (form.vipPriceYuan > form.normalPriceYuan) {
      ElMessage.warning('VIP 价不能高于普通价')
      return
    }
    const payload = {
      productId: form.productId,
      productType: form.productType,
      routeType: form.routeType,
      title: form.title,
      subtitle: form.subtitle,
      cover: form.cover,
      videoUrl: form.videoUrl,
      detail: form.detail,
      normalPrice: cents(form.normalPriceYuan),
      vipPrice: cents(form.vipPriceYuan),
      stock: form.stock,
      days: form.days,
      nights: form.nights,
      departCity: form.departCity,
      gatherTime: form.gatherTime,
      gatherPlace: form.gatherPlace,
      includes: form.includes,
      excludes: form.excludes,
      sort: form.sort,
      status: form.status,
      images: JSON.stringify(
        form.imagesText
          .split('\n')
          .map((s) => s.trim())
          .filter(Boolean)
      ),
      dayList: form.dayList
    }
    submitting.value = true
    const action = form.productId ? updateProduct(payload) : addProduct(payload)
    action
      .then(() => {
        ElMessage.success('保存成功')
        dialog.visible = false
        getList()
      })
      .finally(() => {
        submitting.value = false
      })
  })
}

function handleStatusChange(row, val) {
  const text = val === '0' ? '上架' : '下架'
  changeProductStatus(row.productId, val)
    .then(() => ElMessage.success('已' + text))
    .catch(() => {
      row.status = val === '0' ? '1' : '0'
    })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除「${row.title}」？已被订单引用的商品不允许删除，只能下架。`, '提示', {
    type: 'warning'
  })
    .then(() => delProduct(row.productId))
    .then(() => {
      ElMessage.success('已删除')
      getList()
    })
    .catch(() => {})
}

getList()
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.ml4 {
  margin-left: 4px;
}
.mt8 {
  margin-top: 8px;
}
.img-fallback {
  width: 56px;
  height: 40px;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}
.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
