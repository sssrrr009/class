<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.collegeId" placeholder="按学院过滤" clearable style="width: 170px" @change="onCollegeChange">
        <el-option v-for="c in colleges" :key="c.id" :label="c.collegeName" :value="c.id" />
      </el-select>
      <el-select v-model="query.majorId" placeholder="按专业过滤" clearable style="width: 190px" @change="load">
        <el-option v-for="m in filteredMajors" :key="m.id" :label="m.majorName" :value="m.id" />
      </el-select>
      <el-select v-model="query.teacherId" placeholder="按班主任过滤" clearable style="width: 170px" @change="load">
        <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增班级</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="classNo" label="班级号" width="110" />
      <el-table-column prop="className" label="班级名称" />
      <el-table-column label="所属专业" width="170">
        <template #default="{ row }">{{ majorName(row.majorId) }}</template>
      </el-table-column>
      <el-table-column label="班主任" width="130">
        <template #default="{ row }">{{ teacherName(row.teacherId) }}</template>
      </el-table-column>
      <el-table-column prop="studentCount" label="人数" width="80" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="班级号" required>
          <el-input v-model="form.classNo" placeholder="如 2023-01" />
        </el-form-item>
        <el-form-item label="班级名称" required>
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select v-model="form.majorId" placeholder="请选择专业" style="width: 100%" clearable>
            <el-option v-for="m in majors" :key="m.id" :label="m.majorName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任">
          <el-select v-model="form.teacherId" placeholder="请选择教师" style="width: 100%" clearable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classApi } from '../../api/classInfo'
import { majorApi } from '../../api/major'
import { teacherApi } from '../../api/teacher'
import { collegeApi } from '../../api/college'

const rows = ref([])
const majors = ref([])
const teachers = ref([])
const colleges = ref([])
const allMajors = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, collegeId: null, majorId: null, teacherId: null })
const filteredMajors = computed(() => query.collegeId ? allMajors.value.filter(m => m.collegeId === query.collegeId) : allMajors.value)
const form = reactive({ id: null, classNo: '', className: '', majorId: null, teacherId: null })

const majorMap = reactive({})
const teacherMap = reactive({})
function majorName(id) { return majorMap[id] || `专业#${id }` }
function teacherName(id) { return teacherMap[id] || `教师#${id }` }

async function load() {
  loading.value = true
  try {
    const res = await classApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadOptions() {
  const [m, t, co] = await Promise.all([majorApi.list(), teacherApi.list(), collegeApi.list()])
  allMajors.value = m.data
  majors.value = m.data
  teachers.value = t.data
  colleges.value = co.data
  ; 
  m.data.forEach(x => majorMap[x.id] = x.majorName)
  t.data.forEach(x => teacherMap[x.id] = `${x.teacherNo} ${x.name}`)
}
function onCollegeChange() {
  query.majorId = null
  load()
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.classNo = row?.classNo ?? ''
  form.className = row?.className ?? ''
  form.majorId = row?.majorId ?? null
  form.teacherId = row?.teacherId ?? null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.classNo || !form.className) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await classApi.update(form)
  } else {
    await classApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除班级「${row.className}」吗？`, '提示', { type: 'warning' })
  await classApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
