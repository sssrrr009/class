<template>
  <el-card v-if="!configOpen" class="closed-card">
    <el-result icon="warning" title="选课暂未开放">
      <template #sub-title>
        <p>当前不在选课时间窗口内，请等待管理员开放选课。</p>
      </template>
    </el-result>
  </el-card>
  <template v-else>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card header="课程类别（本专业计划）">
          <el-menu @select="selectCategory" :default-active="activeCategory">
            <el-menu-item v-for="cat in categories" :key="cat.id" :index="String(cat.id)">
              {{ cat.categoryCode }} {{ cat.categoryName }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card :header="activeCategoryName || '可选课程'">
          <el-table :data="candidates" border stripe v-loading="candLoading" max-height="420">
            <el-table-column prop="courseCode" label="编号" width="90" />
            <el-table-column prop="courseName" label="课程名称" min-width="130" />
            <el-table-column label="任课教师" width="100">
              <template #default="{ row }">{{ row.teacherName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="classTime" label="上课时间" width="140" />
            <el-table-column prop="location" label="地点" width="100" />
            <el-table-column label="学分" width="70">
              <template #default="{ row }">{{ row.credit }}</template>
            </el-table-column>
            <el-table-column label="容量" width="70">
              <template #default="{ row }">{{ row.capacity ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="handleEnroll(row)">选课</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    <el-card header="我的选课" class="my-courses">
      <el-table :data="myCourses" border stripe v-loading="myLoading">
        <el-table-column prop="courseCode" label="编号" width="90" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column label="学分" width="70">
          <template #default="{ row }">{{ row.credit }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ statusName(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDrop(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </template>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEnrollCategories, getCandidates, getMyCourses, enroll, drop } from '../../api/enroll'
import { getEnrollConfig } from '../../api/enrollConfig'

const categories = ref([])
const candidates = ref([])
const myCourses = ref([])
const activeCategory = ref('')
const configOpen = ref(true)
const candLoading = ref(false)
const myLoading = ref(false)

const activeCategoryName = computed(() => {
  const cat = categories.value.find(c => String(c.id) === activeCategory.value)
  return cat ? `${cat.categoryCode} ${cat.categoryName}` : ''
})
function statusName(s) {
  return { NOT_STARTED: '未开课', OPENING: '开课中', FINISHED: '已结课' }[s] || s
}

async function loadCategories() {
  const res = await getEnrollCategories()
  categories.value = res.data
  if (res.data.length) {
    activeCategory.value = String(res.data[0].id)
    loadCandidates()
  }
}
async function loadCandidates() {
  candLoading.value = true
  try {
    const res = await getCandidates(activeCategory.value ? Number(activeCategory.value) : undefined)
    candidates.value = res.data
  } finally {
    candLoading.value = false
  }
}
async function loadMy() {
  myLoading.value = true
  try {
    const res = await getMyCourses()
    myCourses.value = res.data
  } finally {
    myLoading.value = false
  }
}
async function loadConfig() {
  try {
    const res = await getEnrollConfig()
    if (res.data) {
      const now = new Date()
      const start = res.data.startTime ? new Date(res.data.startTime) : null
      const end = res.data.endTime ? new Date(res.data.endTime) : null
      configOpen.value = res.data.isOpen === 1 &&
        (!start || now >= start) && (!end || now <= end)
    } else {
      configOpen.value = false
    }
  } catch (e) {
    configOpen.value = false
  }
}
function selectCategory(index) {
  activeCategory.value = index
  loadCandidates()
}
async function handleEnroll(row) {
  const res = await enroll(row.id)
  ElMessage.success(res.data.message || '选课成功')
  loadMy()
  loadCandidates()
}
async function handleDrop(row) {
  await ElMessageBox.confirm(`确定退选「${row.courseName}」吗？`, '提示', { type: 'warning' })
  await drop(row.id)
  ElMessage.success('已退课')
  loadMy()
}
onMounted(() => {
  loadConfig()
  loadCategories()
  loadMy()
})
</script>

<style scoped>
.closed-card { max-width: 600px; margin: 40px auto; }
.my-courses { margin-top: 16px; }
</style>
