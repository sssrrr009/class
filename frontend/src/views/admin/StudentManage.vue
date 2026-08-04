<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索学号/姓名/身份证" clearable style="width: 240px" @keyup.enter="load" />
      <el-select v-model="query.classId" placeholder="按班级筛选" clearable style="width: 180px" @change="load">
        <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增学生</el-button>
      <el-upload :show-file-list="false" :http-request="handleImport" accept=".xlsx,.xls">
        <el-button type="warning">Excel 导入</el-button>
      </el-upload>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="110" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="班级" width="160">
        <template #default="{ row }">{{ className(row.classId) }}</template>
      </el-table-column>
      <el-table-column prop="idCard" label="身份证号" width="200" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学号" required>
          <el-input v-model="form.studentNo" placeholder="登录用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input v-model="form.idCard" placeholder="初始密码取身份证后6位" />
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%" clearable>
            <el-option v-for="c in classes" :key="c.id" :label="`${c.classNo} ${c.className}`" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入结果 -->
    <el-dialog v-model="importVisible" title="导入结果" width="560px">
      <p>共 {{ importResult.total }} 条，成功 {{ importResult.success }} 条</p>
      <el-table v-if="importResult.errors?.length" :data="importResult.errors" border max-height="300">
        <el-table-column prop="row" label="Excel行号" width="100" />
        <el-table-column prop="message" label="失败原因" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="importVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { studentApi, importStudents } from '../../api/student'
import { classApi } from '../../api/classInfo'

const rows = ref([])
const classes = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const importResult = ref({})
const query = reactive({ page: 1, size: 10, keyword: '', classId: null })
const form = reactive({ id: null, studentNo: '', name: '', gender: '', phone: '', idCard: '', classId: null })

const classMap = reactive({})
function className(id) { return classMap[id] || `班级#${id }`
}

async function load() {
  loading.value = true
  try {
    const res = await studentApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadClasses() {
  const res = await classApi.list()
  classes.value = res.data
  
  res.data.forEach(c => classMap[c.id] = c.className)
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.studentNo = row?.studentNo ?? ''
  form.name = row?.name ?? ''
  form.gender = row?.gender ?? ''
  form.phone = row?.phone ?? ''
  form.idCard = row?.idCard ?? ''
  form.classId = row?.classId ?? null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.studentNo || !form.name || !form.idCard) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await studentApi.update(form)
  } else {
    await studentApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
  loadClasses()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除学生「${row.name}」吗？`, '提示', { type: 'warning' })
  await studentApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
  loadClasses()
}
async function handleImport({ file }) {
  try {
    const res = await importStudents(file)
    importResult.value = res.data
    importVisible.value = true
    load()
    loadClasses()
  } catch (e) {
    // 拦截器已提示
  }
}
onMounted(() => { load(); loadClasses() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
