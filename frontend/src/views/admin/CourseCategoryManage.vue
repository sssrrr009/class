<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">新增课程类别</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="categoryCode" label="类别编号" width="130" />
      <el-table-column prop="categoryName" label="类别名称" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程类别' : '新增课程类别'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类别编号" required>
          <el-input v-model="form.categoryCode" placeholder="如 080109" />
        </el-form-item>
        <el-form-item label="类别名称" required>
          <el-input v-model="form.categoryName" placeholder="如 大学英语二" />
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
import { courseCategoryApi } from '../../api/courseCategory'

const rows = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, categoryCode: '', categoryName: '' })

async function load() {
  loading.value = true
  try {
    const res = await courseCategoryApi.list()
    rows.value = res.data
  } finally {
    loading.value = false
  }
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.categoryCode = row?.categoryCode ?? ''
  form.categoryName = row?.categoryName ?? ''
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.categoryCode || !form.categoryName) return ElMessage.warning('请填写完整信息')
  if (form.id) {
    await courseCategoryApi.update(form)
  } else {
    await courseCategoryApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除类别「${row.categoryName}」吗？`, '提示', { type: 'warning' })
  await courseCategoryApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
</style>
