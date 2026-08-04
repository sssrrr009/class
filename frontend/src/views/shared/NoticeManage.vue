<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">发布公告</el-button>
    </div>
    <el-table :data="rows" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="范围" width="90">
        <template #default="{ row }">
          <el-tag size="small">{{ scopeName(row.scope) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="90" />
      <el-table-column label="发送时间" width="170">
        <template #default="{ row }">{{ row.publishTime || row.createTime }}</template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看</el-button>
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
      layout="total, prev, pager, next" @current-change="load" class="pager" />

    <!-- 发布/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="580px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="范围" required>
          <el-select v-model="form.scope" style="width: 100%">
            <el-option value="SCHOOL" label="学校" />
            <el-option value="COLLEGE" label="学院" />
            <el-option value="MAJOR" label="专业" />
            <el-option value="CLASS" label="班级" />
            <el-option value="PERSONAL" label="个人" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scope === 'CLASS'" label="目标班级">
          <el-select v-model="form.targetClassId" style="width: 100%" clearable>
            <el-option v-for="c in classes" :key="c.id" :label="`${c.classNo} ${c.className}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scope === 'PERSONAL'" label="目标学生">
          <el-select v-model="form.targetStudentIds" multiple filterable placeholder="可选择多个学生" style="width: 100%">
            <el-option v-for="s in targetStudents" :key="s.id" :label="`${s.studentNo} ${s.name}`" :value="String(s.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="定时发送">
          <el-switch v-model="form.scheduled" active-text="定时" inactive-text="立即" />
        </el-form-item>
        <el-form-item v-if="form.scheduled" label="发送时间">
          <el-date-picker v-model="form.publishTime" type="datetime" placeholder="选择发送时间"
            style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">发布</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" :title="detail.title" width="520px">
      <div class="detail-meta">
        <el-tag size="small">{{ scopeName(detail.scope) }}</el-tag>
        <span class="detail-publisher">发布人：{{ detail.publisherName }}</span>
        <span class="detail-time">{{ detail.publishTime || detail.createTime }}</span>
      </div>
      <div class="detail-content">{{ detail.content }}</div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeApi } from '../../api/notice'
import { classApi } from '../../api/classInfo'
import { studentApi } from '../../api/student'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const rows = ref([])
const classes = ref([])
const targetStudents = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const query = reactive({ page: 1, size: 10 })
const form = reactive({
  id: null, title: '', content: '', scope: 'CLASS',
  targetClassId: null, targetStudentIds: [], scheduled: false, publishTime: null
})

function scopeName(s) {
  return { SCHOOL: '学校', COLLEGE: '学院', MAJOR: '专业', CLASS: '班级', PERSONAL: '个人' }[s] || s
}

async function load() {
  loading.value = true
  try {
    const res = await noticeApi.page(query)
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
async function loadClasses() {
  const res = await classApi.list()
  classes.value = res.data
}
async function loadStudents() {
  const res = await studentApi.page({ page: 1, size: 1000 })
  targetStudents.value = res.data.list
}
function openDialog(row) {
  form.id = row?.id ?? null
  form.title = row?.title ?? ''
  form.content = row?.content ?? ''
  form.scope = row?.scope ?? 'CLASS'
  form.targetClassId = row?.targetClassId ?? null
  form.targetStudentIds = row?.targetStudentIds ? row.targetStudentIds.split(',').map(s => s.trim()).filter(Boolean) : []
  form.scheduled = !!row?.publishTime
  form.publishTime = row?.publishTime ?? null
  dialogVisible.value = true
}
async function handleSave() {
  if (!form.title) return ElMessage.warning('请输入标题')
  const data = {
    id: form.id,
    title: form.title,
    content: form.content,
    scope: form.scope,
    targetClassId: form.targetClassId,
    targetStudentIds: form.targetStudentIds.length ? form.targetStudentIds.join(',') : null,
    publishTime: form.scheduled ? form.publishTime : null
  }
  await noticeApi.add(data)
  ElMessage.success('发布成功')
  dialogVisible.value = false
  load()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', { type: 'warning' })
  await noticeApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}
function viewDetail(row) {
  detail.value = row
  detailVisible.value = true
}
watch(() => form.scope, (s) => {
  if (s === 'PERSONAL') loadStudents()
})
onMounted(() => { load(); loadClasses(); if (userStore.role === 'CADRE') loadStudents() })
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.pager { margin-top: 14px; justify-content: flex-end; }
.detail-meta { margin-bottom: 14px; display: flex; align-items: center; gap: 12px; }
.detail-publisher { color: #666; font-size: 13px; }
.detail-time { color: #999; font-size: 13px; }
.detail-content { white-space: pre-wrap; line-height: 1.7; color: #333; }
</style>
