<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">提交课程修改申请</el-button>
      <el-button type="primary" @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程" min-width="140" />
      <el-table-column label="修改内容" min-width="200">
        <template #default="{ row }">
          <span>{{ summaryText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="拒绝原因" min-width="120" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDiff(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 提交修改(一次性编辑全部字段) -->
    <el-dialog v-model="dialogVisible" :title="`提交修改申请：${currentCourse?.courseName || ''}`" width="560px">
      <el-alert type="info" :closable="false" title="请一次性填写修改后的全部课程信息，提交后等待管理员审批" class="apply-tip" />
      <el-form :model="form" label-width="90px" style="margin-top: 14px">
        <el-form-item label="课程编号">
          <el-input v-model="form.courseCode" disabled />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课时">
          <el-input-number v-model="form.courseHours" :min="0" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="form.credit" :min="0" :step="0.5" />
        </el-form-item>
        <el-form-item label="容纳人数">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.classTime" placeholder="如 周一 1-2节" />
        </el-form-item>
        <el-form-item label="上课地点">
          <el-input v-model="form.location" placeholder="如 教2-301" />
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option value="NOT_STARTED" label="未开课" />
            <el-option value="OPENING" label="开课中" />
            <el-option value="FINISHED" label="已结课" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对比 -->
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyChanges, submitChange } from '../../api/courseChange'
import { getMyTeachingCourses } from '../../api/course'

const route = useRoute()
const rows = ref([])
const myCourses = ref([])
const currentCourse = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const form = reactive({
  courseId: null, courseCode: '', courseName: '', courseHours: 0,
  credit: 0, capacity: 50, classTime: '', location: '', status: 'NOT_STARTED'
})

const FIELD_NAMES = {
  courseName: '课程名称', courseHours: '课时', credit: '学分',
  capacity: '容纳人数', classTime: '上课时间', location: '上课地点', status: '课程状态'
}
function statusName(s) { return { 0: '待审批', 1: '已通过', 2: '已拒绝' }[s] || s }
function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info' }
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

async function load() {
  loading.value = true
  try {
    const res = await getMyChanges()
    rows.value = res.data
  } finally {
    loading.value = false
  }
}
async function loadMyCourses() {
  const res = await getMyTeachingCourses()
  myCourses.value = res.data
  // 支持从我的课程页跳转预选
  if (route.query.courseId) {
    const c = myCourses.value.find(x => x.id === Number(route.query.courseId))
    if (c) openDialog(c)
  }
}
function openDialog(course) {
  if (!course) {
    // 需要选择课程
    if (myCourses.value.length === 0) {
      ElMessage.warning('您还没有授课课程')
      return
    }
    course = myCourses.value[0]
  }
  currentCourse.value = course
  form.courseId = course.id
  form.courseCode = course.courseCode
  form.courseName = course.courseName
  form.courseHours = course.courseHours ?? 0
  form.credit = course.credit ?? 0
  form.capacity = course.capacity ?? 50
  form.classTime = course.classTime ?? ''
  form.location = course.location ?? ''
  form.status = course.status ?? 'NOT_STARTED'
  dialogVisible.value = true
}
async function handleSubmit() {
  if (!form.courseId) return ElMessage.warning('请选择课程')
  const newData = {
    courseName: form.courseName,
    courseHours: form.courseHours,
    credit: form.credit,
    capacity: form.capacity,
    classTime: form.classTime,
    location: form.location,
    status: form.status
  }
  await submitChange({ courseId: form.courseId, newData: JSON.stringify(newData) })
  ElMessage.success('申请已提交，等待管理员审批')
  dialogVisible.value = false
  load()
}
function viewDiff(row) {
  detail.value = row
  detailVisible.value = true
}
onMounted(() => { load(); loadMyCourses() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.apply-tip { margin-bottom: 4px; }
</style>
