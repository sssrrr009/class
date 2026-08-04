<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索专业名称/编号" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增专业</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="majorName" label="专业名称" />
      <el-table-column prop="majorCode" label="专业编号" width="120" />
      <el-table-column label="所属学院" width="180">
        <template #default="{ row }">{{ collegeName(row.collegeId) }}</template>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专业' : '新增专业'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="专业名称" required>
          <el-input v-model="form.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="专业编号">
          <el-input v-model="form.majorCode" placeholder="请输入专业编号" />
        </el-form-item>
        <el-form-item label="所属学院" required>
          <el-select v-model="form.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.collegeName" :value="c.id" />
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
import { majorApi } from '../../api/major'
import { collegeApi } from '../../api/college'

const rows = ref([])
const colleges = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })
const form = reactive({ id: null, majorName: '', majorCode: '', collegeId: null })

const collegeMap = reactive({})
function collegeName(id) { return collegeMap[id] || `学院#${id }`
}

async function load() {
  loading.value = true
  try {
    const res = await majorApi.page(query)
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
  form.majorName = row?.majorName ?? ''
  form.majorCode = row?.majorCode ?? ''
  form.collegeId = row?.collegeId ?? null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.majorName || !form.collegeId) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await majorApi.update(form)
  } else {
    await majorApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除专业「${row.majorName}」吗？`, '提示', { type: 'warning' })
  await majorApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(() => { load(); loadColleges() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
