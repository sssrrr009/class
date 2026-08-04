<template>
  <el-card>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="范围" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ scopeName(row.scope) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ row.publishTime || row.createTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="detailVisible" :title="detail.title" width="520px">
      <div class="detail-meta">
        <el-tag size="small">{{ scopeName(detail.scope) }}</el-tag>
        <span>发布人：{{ detail.publisherName }}</span>
        <span>{{ detail.publishTime || detail.createTime }}</span>
      </div>
      <div class="detail-content">{{ detail.content }}</div>
    </el-dialog>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { noticeApi } from '../../api/notice'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })
const detailVisible = ref(false)
const detail = ref({})
function scopeName(s) {
  return { SCHOOL: '学校', COLLEGE: '学院', MAJOR: '专业', CLASS: '班级', PERSONAL: '个人' }[s] || s
}
function viewDetail(row) {
  detail.value = row
  detailVisible.value = true
}
async function load() {
  loading.value = true
  try {
    const res = await noticeApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
.detail-meta { margin-bottom: 14px; display: flex; align-items: center; gap: 12px; color: #666; font-size: 13px; }
.detail-content { white-space: pre-wrap; line-height: 1.7; color: #333; }
</style>
