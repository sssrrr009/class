<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">录入成绩</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="110" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column label="分数" width="100">
        <template #default="{ row }">{{ row.score ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="gradeType(row.gradeLevel)">{{ row.gradeLevel || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">修改</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '修改成绩' : '录入成绩'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学生" required>
          <el-select v-model="form.studentId" placeholder="选择学生" style="width: 100%" filterable>
            <el-option v-for="s in students" :key="s.id" :label="`${s.studentNo} ${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" required>
          <el-select v-model="form.courseId" placeholder="选择课程" style="width: 100%" filterable>
            <el-option v-for="c in courses" :key="c.id" :label="`${c.courseCode} ${c.courseName}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分数" required>
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="2" style="width: 100%" />
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
import { scoreApi } from '../../api/score'
import { studentApi } from '../../api/student'
import { courseApi, getMyTeachingCourses } from '../../api/course'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const rows = ref([])
const students = ref([])
const courses = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10 })
const form = reactive({ id: null, studentId: null, courseId: null, score: null })

function gradeType(g) {
  return { 优秀: 'success', 良好: 'primary', 及格: 'warning', 不及格: 'danger' }[g] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await scoreApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadOptions() {
  const s = await studentApi.page({ page: 1, size: 1000 })
  students.value = s.data.list
  // 管理员加载全部课程, 教师只加载自己的
  if (userStore.role === 'TEACHER') {
    const c = await getMyTeachingCourses()
    courses.value = c.data
  } else {
    const c = await courseApi.list()
    courses.value = c.data
  }
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.studentId = row?.studentId ?? null
  form.courseId = row?.courseId ?? null
  form.score = row?.score ?? null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.studentId || !form.courseId || form.score === null) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await scoreApi.update({ id: form.id, score: form.score })
  } else {
    await scoreApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该成绩吗？', '提示', { type: 'warning' })
  await scoreApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
