<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.majorId" placeholder="选择专业" clearable style="width: 200px" @change="load">
        <el-option v-for="m in majors" :key="m.id" :label="m.majorName" :value="m.id" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">添加计划类别</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column label="专业" width="170">
        <template #default="{ row }">{{ row.majorName }}</template>
      </el-table-column>
      <el-table-column label="类别编号" width="110">
        <template #default="{ row }">{{ row.categoryCode }}</template>
      </el-table-column>
      <el-table-column label="课程类别">
        <template #default="{ row }">{{ row.categoryName }}</template>
      </el-table-column>
      <el-table-column label="学年" width="80">
        <template #default="{ row }">{{ row.yearLevel }}</template>
      </el-table-column>
      <el-table-column label="学期" width="80">
        <template #default="{ row }">{{ row.semester || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" title="添加计划课程类别" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="专业" required>
          <el-select v-model="form.majorId" placeholder="选择专业" style="width: 100%">
            <el-option v-for="m in majors" :key="m.id" :label="m.majorName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类别" required>
          <el-select v-model="form.categoryId" placeholder="选择课程类别" style="width: 100%" filterable>
            <el-option v-for="cat in categories" :key="cat.id" :label="`${cat.categoryCode} ${cat.categoryName}`" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-input-number v-model="form.yearLevel" :min="1" />
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.semester" placeholder="不限" clearable style="width: 100%">
            <el-option :value="1" label="第一学期" />
            <el-option :value="2" label="第二学期" />
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
import { planApi } from '../../api/plan'
import { majorApi } from '../../api/major'
import { courseCategoryApi } from '../../api/courseCategory'

const rows = ref([])
const majors = ref([])
const categories = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, majorId: null })
const form = reactive({ id: null, majorId: null, categoryId: null, yearLevel: 1, semester: null })

async function load() {
  loading.value = true
  try {
    const res = await planApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadOptions() {
  const [m, c] = await Promise.all([majorApi.list(), courseCategoryApi.list()])
  majors.value = m.data
  categories.value = c.data
}
function openDialog() {
  form.id = null
  form.majorId = query.majorId
  form.categoryId = null
  form.yearLevel = 1
  form.semester = null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.majorId || !form.categoryId) return ElMessage.warning('请选择专业和课程类别')
  await planApi.add(form)
  ElMessage.success('添加成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定从专业计划中移除该课程类别吗？', '提示', { type: 'warning' })
  await planApi.remove(row.id)
  ElMessage.success('已移除')
  load()
}
onMounted(() => { load(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
