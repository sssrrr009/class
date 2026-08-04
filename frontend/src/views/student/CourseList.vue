<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索课程" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-radio-group v-model="viewMode" style="margin-left: 12px">
        <el-radio-button value="table">列表</el-radio-button>
        <el-radio-button value="schedule">日程表</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 列表视图 -->
    <el-table v-if="viewMode === 'table'" :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseCode" label="课程编号" width="110" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column label="类别" width="130">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="credit" label="学分" width="70" />
      <el-table-column label="任课教师" width="110">
        <template #default="{ row }">{{ row.teacherName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="classTime" label="上课时间" width="140" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    
            <!-- 日程表视图 -->
    <div v-else v-loading="loading">
      <ScheduleTable :courses="rows" />
    </div>



    </el-card>
</template>


<script setup>
import { ref, reactive, onMounted } from 'vue'
import { courseApi } from '../../api/course'
import ScheduleTable from '../../components/ScheduleTable.vue'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const viewMode = ref('table')
const query = reactive({ page: 1, size: 10, keyword: '' })


function statusName(s) { return { NOT_STARTED: '未开课', OPENING: '开课中', FINISHED: '已结课' }[s] || s }
function statusType(s) { return { NOT_STARTED: 'info', OPENING: 'success', FINISHED: 'warning' }[s] || 'info' }



// 生成日程表网格 (10节课)




async function load() {
  loading.value = true
  try {
    const res = await courseApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
</style>
