<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">创建投票活动</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="title" label="主题" min-width="180" />
      <el-table-column label="创建人" width="110">
        <template #default="{ row }">{{ row.creatorName }}</template>
      </el-table-column>
      <el-table-column label="每人票数" width="90">
        <template #default="{ row }">{{ row.maxVotes }}</template>
      </el-table-column>
      <el-table-column label="结果公开" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.showResult === 1 ? 'success' : 'info'">{{ row.showResult === 1 ? '公开' : '不公开' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '进行中' : '已结束' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showResult(row)">结果</el-button>
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="row.status === 1" size="small" type="warning" @click="handleToggleEnd(row, false)">结束</el-button>
          <el-button v-else size="small" type="success" @click="handleToggleEnd(row, true)">开启</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑投票' : '创建投票活动'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="主题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="每人票数" required>
          <el-input-number v-model="form.maxVotes" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="结果公开">
          <el-switch v-model="form.showResult" :active-value="1" :inactive-value="0"
            active-text="公开" inactive-text="不公开" />
        </el-form-item>
        <el-form-item label="选项" required>
          <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
            <el-input v-model="form.options[idx]" :placeholder="`选项 ${idx + 1}`" />
            <el-button type="danger" size="small" @click="form.options.splice(idx, 1)">删</el-button>
          </div>
          <el-button size="small" @click="form.options.push('')">+ 添加选项</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultVisible" :title="resultTitle" width="480px">
      <div ref="resultChartRef" style="height: 300px"></div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVotes, createVote, updateVote, deleteVote, getVoteResult } from '../../api/vote'
import * as echarts from 'echarts'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const resultVisible = ref(false)
const resultTitle = ref('')
const resultChartRef = ref(null)
const query = reactive({ page: 1, size: 10 })
const form = reactive({ id: null, title: '', content: '', maxVotes: 1, showResult: 1, options: ['', ''] })
let chart = null

async function load() {
  loading.value = true
  try {
    const res = await getVotes(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
function openDialog(row) {
  if (row) {
    form.id = row.id
    form.title = row.title
    form.content = row.content
    form.maxVotes = row.maxVotes
    form.showResult = row.showResult ?? 1
    form.options = row.options?.length ? [...row.options] : ['', '']
  } else {
    form.id = null
    form.title = ''
    form.content = ''
    form.maxVotes = 1
    form.showResult = 1
    form.options = ['', '']
  }
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.title) return ElMessage.warning('请输入主题')
  const opts = form.options.filter(o => o && o.trim())
  if (opts.length < 1) return ElMessage.warning('至少需要一个选项')
  if (form.id) {
    await updateVote({ id: form.id, title: form.title, content: form.content, maxVotes: form.maxVotes, showResult: form.showResult })
  } else {
    await createVote({ title: form.title, content: form.content, maxVotes: form.maxVotes, showResult: form.showResult, options: opts })
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleToggleEnd(row, end) {
  await updateVote({ id: row.id, status: end ? 0 : 1 })
  ElMessage.success(end ? '投票已结束' : '投票已开启')
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除投票「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteVote(row.id)
  ElMessage.success('删除成功')
  load()
}
async function showResult(row) {
  resultTitle.value = `投票结果：${row.title}`
  resultVisible.value = true
  const res = await getVoteResult(row.id)
  await nextTick()
  if (!resultChartRef.value) return
  if (!chart) chart = echarts.init(resultChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: res.data,
      label: { formatter: '{b}: {c} 票' }
    }]
  })
}
function handleResize() { chart && chart.resize() }
onMounted(() => { load(); window.addEventListener('resize', handleResize) })
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart && chart.dispose()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
.option-row { display: flex; gap: 8px; margin-bottom: 8px; }
</style>
