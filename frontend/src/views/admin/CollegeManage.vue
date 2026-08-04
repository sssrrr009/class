<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索学院名称" clearable style="width: 240px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openDialog()">新增学院</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="collegeName" label="学院名称" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学院' : '新增学院'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学院名称" required>
          <el-input v-model="form.collegeName" placeholder="请输入学院名称" />
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
import { collegeApi } from '../../api/college'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })
const form = reactive({ id: null, collegeName: '' })

async function load() {
  loading.value = true
  try {
    const res = await collegeApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.collegeName = row?.collegeName ?? ''
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.collegeName) return ElMessage.warning('请输入学院名称')
  if (form.id) {
    await collegeApi.update({ id: form.id, collegeName: form.collegeName })
  } else {
    await collegeApi.add({ collegeName: form.collegeName })
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除学院「${row.collegeName}」吗？`, '提示', { type: 'warning' })
  await collegeApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
