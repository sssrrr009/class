<template>
  <el-row :gutter="16">
    <el-col :span="16">
      <el-card>
        <div class="toolbar">
          <el-input v-model="query.keyword" placeholder="搜索文件名/说明" clearable style="width: 200px" @keyup.enter="load" />
          <el-select v-model="query.categoryId" placeholder="按分类筛选" clearable style="width: 150px" @change="load">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
          <el-button type="primary" @click="load">查询</el-button>
          <el-upload :show-file-list="false" :http-request="handleUpload" accept="*">
            <el-button type="success">上传文件</el-button>
          </el-upload>
        </div>
        <el-table :data="rows" border stripe v-loading="loading">
          <el-table-column prop="fileName" label="文件名" min-width="160" />
          <el-table-column label="分类" width="110">
            <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
          </el-table-column>
          <el-table-column label="大小" width="90">
            <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="120" show-overflow-tooltip />
          <el-table-column prop="uploader" label="上传人" width="80" />
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="handleDownload(row)">下载</el-button>
              <el-button size="small" @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
          layout="total, prev, pager, next" @current-change="load" class="pager" />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card header="文件类型占比">
        <div ref="chartRef" style="height: 320px"></div>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="uploadVisible" title="上传文件" width="480px">
    <el-form :model="uploadForm" label-width="80px">
      <el-form-item label="文件">
        <el-upload :auto-upload="false" :limit="1" ref="uploadRef">
          <el-button>选择文件</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="uploadForm.categoryId" placeholder="选择分类" style="width: 100%" clearable>
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="uploadForm.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="uploadVisible = false">取消</el-button>
      <el-button type="primary" @click="handleUploadConfirm">上传</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" title="编辑文件信息" width="480px">
    <el-form :model="editForm" label-width="80px">
      <el-form-item label="分类">
        <el-select v-model="editForm.categoryId" style="width: 100%" clearable>
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="editForm.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="handleEditSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFiles, uploadFile, updateFile, deleteFile, getFileStat, downloadFile } from '../../api/fileInfo'
import { fileCategoryApi } from '../../api/fileCategory'
import * as echarts from 'echarts'

const rows = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)
const uploadVisible = ref(false)
const editVisible = ref(false)
const uploadRef = ref(null)
const chartRef = ref(null)
const query = reactive({ page: 1, size: 10, keyword: '', categoryId: null })
const uploadForm = reactive({ categoryId: null, description: '' })
const editForm = reactive({ id: null, categoryId: null, description: '' })
let chart = null

const categoryMap = new Map()
function categoryName(id) { return categoryMap.get(id) || '未分类' }
function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1024 / 1024).toFixed(1) + 'MB'
}

async function load() {
  loading.value = true
  try {
    const res = await getFiles(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadCategories() {
  const res = await fileCategoryApi.list()
  categories.value = res.data
  categoryMap.clear()
  res.data.forEach(c => categoryMap.set(c.id, c.categoryName))
}
async function loadStat() {
  const res = await getFileStat()
  await nextTick()
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '65%',
      data: res.data,
      label: { formatter: '{b}: {c}' }
    }]
  })
}
function handleUpload({ file }) {
  uploadForm.file = file
  uploadVisible.value = true
}
async function handleUploadConfirm() {
  if (!uploadForm.file) return ElMessage.warning('请选择文件')
  const fd = new FormData()
  fd.append('file', uploadForm.file)
  if (uploadForm.categoryId) fd.append('categoryId', uploadForm.categoryId)
  if (uploadForm.description) fd.append('description', uploadForm.description)
  await uploadFile(fd)
  ElMessage.success('上传成功')
  uploadVisible.value = false
  load()
  loadStat()
}
function openDialog(row) {
  editForm.id = row.id
  editForm.categoryId = row.categoryId
  editForm.description = row.description
  editVisible.value = true
}
async function handleEditSave() {
  await updateFile({ id: editForm.id, categoryId: editForm.categoryId, description: editForm.description })
  ElMessage.success('保存成功')
  editVisible.value = false
  load()
  loadStat()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除文件「${row.fileName}」吗？`, '提示', { type: 'warning' })
  await deleteFile(row.id)
  ElMessage.success('删除成功')
  load()
  loadStat()
}
async function handleDownload(row) {
  const res = await downloadFile(row.id)
  const url = URL.createObjectURL(new Blob([res]))
  const a = document.createElement('a')
  a.href = url
  a.download = row.fileName
  a.click()
  URL.revokeObjectURL(url)
}
function handleResize() { chart && chart.resize() }
onMounted(() => {
  load(); loadCategories(); loadStat()
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart && chart.dispose()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
