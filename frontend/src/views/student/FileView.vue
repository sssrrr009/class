<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索文件名/说明" clearable style="width: 220px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="fileName" label="文件名" min-width="180" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="uploader" label="上传人" width="90" />
      <el-table-column prop="createTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleDownload(row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFiles, downloadFile } from '../../api/fileInfo'
import { fileCategoryApi } from '../../api/fileCategory'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })
const categoryMap = new Map()
function categoryName(id) { return categoryMap.get(id) || '未分类' }

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
  res.data.forEach(c => categoryMap.set(c.id, c.categoryName))
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
onMounted(() => { load(); loadCategories() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
