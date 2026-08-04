<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索课程名称/编号" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增课程</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="courseCode" label="课程编号" width="110" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column label="类别" width="130">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="capacity" label="容量" width="70" />
      <el-table-column prop="courseHours" label="课时" width="70" />
      <el-table-column prop="credit" label="学分" width="70" />
      <el-table-column label="任课教师" width="130">
        <template #default="{ row }">{{ teacherName(row.teacherId) }}</template>
      </el-table-column>
      <el-table-column prop="classTime" label="上课时间" width="140" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="课程编号" required>
          <el-input v-model="form.courseCode" />
        </el-form-item>
        <el-form-item label="课程名称" required>
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课时">
          <el-input-number v-model="form.courseHours" :min="0" />
        </el-form-item>
        <el-form-item label="学分">
          <el-input-number v-model="form.credit" :min="0" :step="0.5" />
        </el-form-item>
        <el-form-item label="课程类别">
          <el-select v-model="form.categoryId" placeholder="选择类别" style="width: 100%" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :label="`${cat.categoryCode} ${cat.categoryName}`" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
        </el-form-item>
        <el-form-item label="任课教师">
          <el-select v-model="form.teacherId" placeholder="选择教师" style="width: 100%" clearable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
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
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi } from '../../api/course'
import { teacherApi } from '../../api/teacher'
import { courseCategoryApi } from '../../api/courseCategory'

const rows = ref([])
const teachers = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })
const form = reactive({ id: null, courseCode: '', courseName: '', categoryId: null, courseHours: 0, credit: 0, capacity: 50, teacherId: null, classTime: '', location: '', status: 'NOT_STARTED' })

const teacherMap = reactive({})
const categoryMap = reactive({})
function teacherName(id) { return teacherMap[id] || `教师#${id }` }
function categoryName(id) { return categoryMap[id] || '-' }
function statusName(s) { return { NOT_STARTED: '未开课', OPENING: '开课中', FINISHED: '已结课' }[s] || s }
function statusType(s) { return { NOT_STARTED: 'info', OPENING: 'success', FINISHED: 'warning' }[s] || 'info' }

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
async function loadTeachers() {
  const [t, cat] = await Promise.all([teacherApi.list(), courseCategoryApi.list()])
  teachers.value = t.data
  
  
  t.data.forEach(x => teacherMap[x.id] = `${x.teacherNo} ${x.name}`)
  cat.data.forEach(x => categoryMap[x.id] = x.categoryName)
}
function openDialog(row) {
  Object.assign(form, row ? { ...row } : { id: null, courseCode: '', courseName: '', categoryId: null, courseHours: 0, credit: 0, capacity: 50, teacherId: null, classTime: '', location: '', status: 'NOT_STARTED' })
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.courseCode || !form.courseName) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await courseApi.update(form)
  } else {
    await courseApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除课程「${row.courseName}」吗？`, '提示', { type: 'warning' })
  await courseApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(() => { load(); loadTeachers() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
