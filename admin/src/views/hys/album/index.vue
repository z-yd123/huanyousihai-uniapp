<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="批次标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="批次标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="可浏览" value="0" />
          <el-option label="已隐藏" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hys:album:edit']">代传相册</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" row-key="albumId">
      <el-table-column label="ID" prop="albumId" width="70" />
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <el-image :src="row.cover" fit="cover" style="width: 64px; height: 44px; border-radius: 4px">
            <template #error><div class="img-fallback">无图</div></template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="批次标题" prop="title" min-width="180" show-overflow-tooltip />
      <el-table-column label="所属会员" width="150">
        <template #default="{ row }">
          <div class="cell-title">{{ row.memberName || '—' }}</div>
          <div class="cell-sub">ID {{ row.memberId }}</div>
        </template>
      </el-table-column>
      <el-table-column label="关联线路" prop="productTitle" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.productTitle || '—' }}</template>
      </el-table-column>
      <el-table-column label="出行日期" width="120" align="center">
        <template #default="{ row }">{{ row.travelDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="素材数" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ (row.mediaList || []).length }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === '0' ? 'success' : 'info'">
            {{ row.status === '0' ? '可浏览' : '已隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="130" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" v-hasPermi="['hys:album:edit']" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="danger" v-hasPermi="['hys:album:edit']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="760px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属会员" prop="memberId">
              <el-select v-model="form.memberId" filterable remote reserve-keyword placeholder="输入手机号/昵称搜索"
                :remote-method="searchMember" :loading="memberLoading" style="width: 100%">
                <el-option v-for="m in memberOptions" :key="m.memberId" :label="`${m.nickname}（${m.phone}）`" :value="m.memberId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联线路" prop="productId">
              <el-select v-model="form.productId" filterable clearable placeholder="可留空" style="width: 100%">
                <el-option v-for="p in productOptions" :key="p.productId" :label="p.title" :value="p.productId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="批次标题" prop="title">
              <el-input v-model="form.title" maxlength="60" placeholder="如：林岚 · 昆明 6 日" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出行日期" prop="travelDate">
              <el-date-picker v-model="form.travelDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面色" prop="cover">
          <el-input v-model="form.cover" placeholder="封面图地址（https:// 或 data URI）" />
        </el-form-item>
        <el-form-item label="回忆文案" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="浏览状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">可浏览</el-radio>
            <el-radio value="1">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">素材清单（category 用于相册内分类展示）</el-divider>
        <el-table :data="form.mediaList" size="small" border>
          <el-table-column label="分类" width="120">
            <template #default="{ row }">
              <el-select v-model="row.category" size="small">
                <el-option label="风景" value="SCENERY" />
                <el-option label="人像" value="PORTRAIT" />
                <el-option label="合影" value="GROUP" />
                <el-option label="视频" value="VIDEO" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="素材地址" min-width="260">
            <template #default="{ row }"><el-input v-model="row.url" size="small" /></template>
          </el-table-column>
          <el-table-column label="标题" min-width="140">
            <template #default="{ row }"><el-input v-model="row.title" size="small" /></template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.mediaList.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button class="mt8" plain icon="Plus" size="small" @click="addMedia">添加素材</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HysAlbum">
import { getCurrentInstance, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addAlbum, delAlbum, getAlbum, listAlbum, listMember, listProduct, updateAlbum } from '@/api/hys'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const submitting = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const formRef = ref()
const memberOptions = ref([])
const memberLoading = ref(false)
const productOptions = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, title: '', status: '' })
const dialog = reactive({ visible: false, title: '' })

const rules = {
  memberId: [{ required: true, message: '请选择所属会员', trigger: 'change' }],
  title: [{ required: true, message: '请填写批次标题', trigger: 'blur' }]
}

const defaultForm = () => ({
  albumId: null,
  memberId: null,
  productId: null,
  title: '',
  travelDate: '',
  cover: '',
  description: '',
  status: '0',
  mediaList: []
})

const form = reactive(defaultForm())

function formatTime(value) {
  return value ? String(value).replace('T', ' ').substring(0, 19) : '—'
}

async function getList() {
  loading.value = true
  try {
    const res = await listAlbum(queryParams)
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
  queryParams.title = ''
  queryParams.status = ''
  handleQuery()
}

async function searchMember(keyword) {
  memberLoading.value = true
  try {
    const res = await listMember({ pageNum: 1, pageSize: 20, phone: keyword || '', nickname: keyword || '' })
    memberOptions.value = res.rows || []
  } finally {
    memberLoading.value = false
  }
}

async function loadProducts() {
  if (productOptions.value.length > 0) return
  const res = await listProduct({ pageNum: 1, pageSize: 100 })
  productOptions.value = res.rows || []
}

function fillForm(data) {
  Object.assign(form, defaultForm(), {
    ...data,
    travelDate: data.travelDate ? String(data.travelDate).substring(0, 10) : '',
    mediaList: (data.mediaList || []).map(item => ({ ...item }))
  })
}

function handleAdd() {
  Object.assign(form, defaultForm())
  dialog.title = '代传相册'
  dialog.visible = true
  searchMember('')
  loadProducts()
}

async function handleUpdate(row) {
  const res = await getAlbum(row.albumId)
  fillForm(res.data)
  dialog.title = '编辑相册 ' + row.title
  dialog.visible = true
  // 编辑时把当前会员塞进下拉，避免回显为空
  memberOptions.value = [{ memberId: res.data.memberId, nickname: res.data.memberName || '当前会员', phone: '' }]
  loadProducts()
}

function addMedia() {
  form.mediaList.push({ category: 'SCENERY', url: '', title: '' })
}

async function submitForm() {
  await formRef.value.validate()
  const invalid = form.mediaList.find(item => !item.url || !item.url.trim())
  if (invalid) {
    ElMessage.warning('素材地址不能为空，请补全或删除空行')
    return
  }
  submitting.value = true
  try {
    const payload = {
      albumId: form.albumId,
      memberId: form.memberId,
      productId: form.productId || null,
      title: form.title,
      travelDate: form.travelDate || null,
      cover: form.cover,
      mediaJson: JSON.stringify(form.mediaList),
      description: form.description,
      status: form.status
    }
    if (form.albumId) {
      await updateAlbum(payload)
    } else {
      await addAlbum(payload)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    getList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除相册「${row.title}」？删除后小程序端将无法查看。`, '删除确认', { type: 'warning' })
  await delAlbum(row.albumId)
  ElMessage.success('已删除')
  getList()
}

getList()
</script>

<style scoped>
.cell-title {
  font-size: 13px;
}
.cell-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.img-fallback {
  width: 64px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-radius: 4px;
}
</style>
