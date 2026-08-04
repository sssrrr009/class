<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索工号/姓名" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增教师</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="teacherNo" label="工号" width="100" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="所属学院" width="150">
        <template #default="{ row }">{{ collegeName(row.collegeId) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="职称" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑教师' : '新增教师'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="工号" required>
          <el-input v-model="form.teacherNo" placeholder="登录用户名" :disabled="!!form.id" />
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
        <el-form-item label="所属学院">
          <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%" clearable>
            <el-option v-for="c in colleges" :key="c.id" :label="c.collegeName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="form.title" placeholder="请选择职称" style="width: 100%" clearable>
            <el-option v-for="t in ['助教','讲师','副教授','教授']" :key="t" :label="t" :value="t" />
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
import { teacherApi } from '../../api/teacher'
import { collegeApi } from '../../api/college'

const rows = ref([])
const colleges = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })
const form = reactive({ id: null, teacherNo: '', name: '', gender: '', phone: '', collegeId: null, title: '' })

const collegeMap = reactive({})
function collegeName(id) { return collegeMap[id] || `学院#${id }`
}

async function load() {
  loading.value = true
  try {
    const res = await teacherApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadColleges() {
  const res = await collegeApi.list()
  colleges.value = res.data
  
  res.data.forEach(c => collegeMap[c.id] = c.collegeName)
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.teacherNo = row?.teacherNo ?? ''
  form.name = row?.name ?? ''
  form.gender = row?.gender ?? ''
  form.phone = row?.phone ?? ''
  form.collegeId = row?.collegeId ?? null
  form.title = row?.title ?? ''
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.teacherNo || !form.name) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await teacherApi.update(form)
  } else {
    await teacherApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除教师「${row.name}」吗？`, '提示', { type: 'warning' })
  await teacherApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(() => { load(); loadColleges() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
