<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">设置干部</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="110" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column label="干部类型" width="100">
        <template #default="{ row }">{{ row.cadreType === 'MONITOR' ? '班长' : '学生会' }}</template>
      </el-table-column>
      <el-table-column label="关联班级" width="160">
        <template #default="{ row }">{{ className(row.classId) }}</template>
      </el-table-column>
      <el-table-column label="权限范围" width="120">
        <template #default="{ row }">{{ scopeName(row.unionScope) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="设置时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑干部' : '设置干部'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学生" required>
          <el-select v-model="form.studentId" placeholder="选择学生(可为同一学生设置多个干部身份)" style="width: 100%" filterable :disabled="!!form.id">
            <el-option v-for="s in students" :key="s.id" :label="`${s.studentNo} ${s.name}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="干部类型" required>
          <el-radio-group v-model="form.cadreType">
            <el-radio value="MONITOR">班长</el-radio>
            <el-radio value="UNION">学生会</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.cadreType === 'MONITOR'" label="所属班级" required>
          <el-select v-model="form.classId" placeholder="选择班级" style="width: 100%" clearable>
            <el-option v-for="c in classes" :key="c.id" :label="`${c.classNo} ${c.className}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.cadreType === 'UNION'" label="权限范围" required>
          <el-select v-model="form.unionScope" placeholder="选择范围" style="width: 100%">
            <el-option value="SCHOOL" label="学校" />
            <el-option value="COLLEGE" label="学院" />
            <el-option value="MAJOR" label="专业" />
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
import { cadreApi } from '../../api/cadre'
import { studentApi } from '../../api/student'
import { classApi } from '../../api/classInfo'

const rows = ref([])
const students = ref([])
const classes = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10 })
const form = reactive({ id: null, studentId: null, cadreType: 'MONITOR', classId: null, unionScope: '' })

const classMap = reactive({})
function className(id) { return classMap[id] || '-' }
function scopeName(s) {
  return { SCHOOL: '学校', COLLEGE: '学院', MAJOR: '专业' }[s] || '-'
}

async function load() {
  loading.value = true
  try {
    const res = await cadreApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadOptions() {
  const [s, c] = await Promise.all([
    studentApi.page({ page: 1, size: 1000 }),
    classApi.list()
  ])
  students.value = s.data.list
  classes.value = c.data
  
  c.data.forEach(x => classMap[x.id] = `${x.classNo} ${x.className}`)
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.studentId = row?.studentId ?? null
  form.cadreType = row?.cadreType ?? 'MONITOR'
  form.classId = row?.classId ?? null
  form.unionScope = row?.unionScope ?? ''
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.studentId) return ElMessage.warning('请选择学生')
  if (form.cadreType === 'MONITOR' && !form.classId) return ElMessage.warning('班长必须选择班级')
  if (form.cadreType === 'UNION' && !form.unionScope) return ElMessage.warning('请选择权限范围')
  if (form.id) {
    await cadreApi.update(form)
  } else {
    await cadreApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定取消该学生的干部身份吗？`, '提示', { type: 'warning' })
  await cadreApi.remove(row.id)
  ElMessage.success('已取消')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
