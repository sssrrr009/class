<template>
  <el-card>
    <div class="toolbar">
      <el-radio-group v-model="viewMode">
        <el-radio-button value="table">列表</el-radio-button>
        <el-radio-button value="schedule">日程表</el-radio-button>
      </el-radio-group>
      <el-button type="primary" style="margin-left: 12px" @click="$router.push('/teacher/course-change')">
        提交课程修改申请
      </el-button>
    </div>

    <!-- 列表视图 -->
    <el-table v-if="viewMode === 'table'" :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseCode" label="课程编号" width="110" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column label="类别" width="130">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="credit" label="学分" width="70" />
      <el-table-column prop="classTime" label="上课时间" width="140" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="goApply(row)">申请修改</el-button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyTeachingCourses } from '../../api/course'

const router = useRouter()
const rows = ref([])
const loading = ref(false)
const viewMode = ref('table')


function statusName(s) { return { NOT_STARTED: '未开课', OPENING: '开课中', FINISHED: '已结课' }[s] || s }
function statusType(s) { return { NOT_STARTED: 'info', OPENING: 'success', FINISHED: 'warning' }[s] || 'info' }


function goApply(row) {
  router.push({ path: '/teacher/course-change', query: { courseId: row.id } })
}

async function load() {
  loading.value = true
  try {
    const res = await getMyTeachingCourses()
    rows.value = res.data
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
</style>
