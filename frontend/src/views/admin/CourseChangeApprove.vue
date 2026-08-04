<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="按状态筛选" clearable style="width: 150px" @change="load">
        <el-option :value="0" label="待审批" />
        <el-option :value="1" label="已通过" />
        <el-option :value="2" label="已拒绝" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程" min-width="130" />
      <el-table-column prop="teacherName" label="申请教师" width="100" />
      <el-table-column label="修改内容" min-width="200">
        <template #default="{ row }">
          <span>{{ summaryText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDiff(row)">详情</el-button>
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleApprove(row, true)">通过</el-button>
            <el-button size="small" type="danger" @click="handleApprove(row, false)">拒绝</el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="detailVisible" :title="`修改详情：${detail.courseName}`" width="560px">
      <el-table :data="diffRows" border size="small">
        <el-table-column prop="field" label="字段" width="100" />
        <el-table-column prop="oldValue" label="原值" min-width="140" show-overflow-tooltip />
        <el-table-column prop="newValue" label="新值" min-width="140" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChangePage, approveChange } from '../../api/courseChange'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, status: null })

const FIELD_NAMES = {
  courseName: '课程名称', courseHours: '课时', credit: '学分',
  capacity: '容纳人数', classTime: '上课时间', location: '上课地点', status: '课程状态'
}
function summaryText(row) {
  try {
    const data = JSON.parse(row.newData)
    const parts = []
    for (const [k, v] of Object.entries(data)) {
      if (v !== null && v !== undefined && v !== '') parts.push(`${FIELD_NAMES[k] || k}: ${v}`)
    }
    return parts.join('；') || '（无内容）'
  } catch {
    return row.newValue || '-'
  }
}
const detailVisible = ref(false)
const detail = ref({})
const diffRows = computed(() => {
  if (!detail.value.newData) return []
  const data = JSON.parse(detail.value.newData)
  const oldData = detail.value.oldData ? JSON.parse(detail.value.oldData) : {}
  return Object.entries(data).map(([k, v]) => ({
    field: FIELD_NAMES[k] || k,
    oldValue: oldData[k] ?? '-',
    newValue: v ?? '-'
  }))
})
function viewDiff(row) {
  detail.value = row
  detailVisible.value = true
}
function statusName(s) { return { 0: '待审批', 1: '已通过', 2: '已拒绝' }[s] || s }
function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info' }

async function load() {
  loading.value = true
  try {
    const res = await getChangePage(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function handleApprove(row, pass) {
  if (!pass) {
    await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', { inputPattern: /\S/, inputErrorMessage: '原因不能为空' })
      .then(async ({ value }) => {
        await approveChange(row.id, false, value)
        ElMessage.success('已拒绝')
        load()
      })
      .catch(() => {})
    return
  }
  await ElMessageBox.confirm(`确认通过「${row.courseName}」的 ${fieldName(row.fieldName)} 修改吗？`, '提示', { type: 'warning' })
  await approveChange(row.id, true, '')
  ElMessage.success('已通过，课程信息已更新')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
