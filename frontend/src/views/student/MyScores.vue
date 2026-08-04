<template>
  <el-card>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column label="分数" width="100">
        <template #default="{ row }">{{ row.score ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="gradeType(row.gradeLevel)">{{ row.gradeLevel || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ row.updateTime || '-' }}</template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyScores } from '../../api/score'

const rows = ref([])
const loading = ref(false)
function gradeType(g) {
  return { 优秀: 'success', 良好: 'primary', 及格: 'warning', 不及格: 'danger' }[g] || 'info'
}
async function load() {
  loading.value = true
  try {
    const res = await getMyScores()
    rows.value = res.data
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>
