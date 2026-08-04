<template>
  <el-row :gutter="16">
    <el-col :span="16">
      <el-card>
        <el-table :data="rows" border stripe v-loading="loading">
          <el-table-column prop="title" label="投票主题" min-width="180" />
          <el-table-column label="每人票数" width="90">
            <template #default="{ row }">{{ row.maxVotes }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '进行中' : '已结束' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="creatorName" label="发起人" width="100" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openVote(row)" :disabled="row.status !== 1">投票</el-button>
              <el-button size="small" @click="showResult(row)" :disabled="row.showResult !== 1">结果</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          layout="total, prev, pager, next" @current-change="load" class="pager" />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card header="我的投票记录">
        <el-table :data="myVotes" border stripe max-height="480" size="small">
          <el-table-column prop="voteTitle" label="活动" min-width="120" show-overflow-tooltip />
          <el-table-column prop="optionText" label="所选" width="100" show-overflow-tooltip />
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="handleDeleteMy(row)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="voteVisible" :title="currentVote?.title" width="480px">
    <p class="vote-desc">{{ currentVote?.content }}</p>
    <p class="vote-tip">每人最多可投 {{ currentVote?.maxVotes }} 票</p>
    <el-checkbox-group v-model="selectedOptions">
      <el-checkbox v-for="(opt, idx) in currentVote?.options" :key="idx" :value="currentVote?.optionIds?.[idx]" class="vote-opt">
        {{ opt }}
      </el-checkbox>
    </el-checkbox-group>
    <template #footer>
      <el-button @click="voteVisible = false">取消</el-button>
      <el-button type="primary" :disabled="selectedOptions.length === 0" @click="handleCast">提交投票</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="resultVisible" :title="resultTitle" width="480px">
    <div ref="resultChartRef" style="height: 300px"></div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVotes, getVoteDetail, castVote, getMyVotes, deleteMyVote, getVoteResult } from '../../api/vote'
import * as echarts from 'echarts'

const rows = ref([])
const myVotes = ref([])
const total = ref(0)
const loading = ref(false)
const voteVisible = ref(false)
const resultVisible = ref(false)
const resultTitle = ref('')
const currentVote = ref(null)
const selectedOptions = ref([])
const resultChartRef = ref(null)
const query = reactive({ page: 1, size: 10 })
let chart = null

async function load() {
  loading.value = true
  try {
    const [v, m] = await Promise.all([getVotes(query), getMyVotes()])
    rows.value = v.data.list
    total.value = v.data.total
    myVotes.value = m.data
  } finally {
    loading.value = false
  }
}
async function openVote(row) {
  try {
    const res = await getVoteDetail(row.id)
    currentVote.value = res.data
  } catch (e) {
    currentVote.value = row
  }
  selectedOptions.value = []
  voteVisible.value = true
}
async function handleCast() {
  if (selectedOptions.value.length > currentVote.value.maxVotes) {
    return ElMessage.warning(`最多只能投 ${currentVote.value.maxVotes} 票`)
  }
  await castVote({ voteId: currentVote.value.id, optionIds: selectedOptions.value })
  ElMessage.success('投票成功')
  voteVisible.value = false
  load()
}
async function handleDeleteMy(row) {
  await ElMessageBox.confirm('确定删除该投票记录吗？', '提示', { type: 'warning' })
  await deleteMyVote(row.id)
  ElMessage.success('已删除')
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
    series: [{ type: 'pie', radius: '60%', data: res.data, label: { formatter: '{b}: {c} 票' } }]
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
.pager { margin-top: 14px; justify-content: flex-end; }
.vote-desc { color: #666; margin-bottom: 6px; }
.vote-tip { color: #e6a23c; font-size: 13px; margin-bottom: 12px; }
.vote-opt { display: block; margin-bottom: 10px; }
</style>
