<template>
  <div class="schedule-wrap">
    <table class="schedule-table" border="1" cellspacing="0" cellpadding="0">
      <thead>
        <tr>
          <th class="sched-corner">节次/星期</th>
          <th v-for="d in dayNames" :key="d">{{ d }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in scheduleRows" :key="row.period">
          <td class="sched-period">{{ row.period }}</td>
          <template v-for="(cell, di) in row.cells" :key="di">
            <!-- 已被前续课程跨行覆盖的格子: 渲染占位td以维持表格列结构 -->
            <td v-if="cell.covered" style="display:none"></td>
            <!-- 课程开始格: 带 rowspan 跨行 -->
            <td v-else-if="cell.course" :rowspan="cell.course.span" class="sched-course-cell"
                :style="{ backgroundColor: colorFor(cell.course.courseCode) }">
              <div class="course-name">{{ cell.course.courseName }}</div>
              <div class="course-loc">{{ cell.course.location }}</div>
            </td>
            <!-- 空格 -->
            <td v-else class="sched-empty-cell"></td>
          </template>
        </tr>
      </tbody>
    </table>
    <el-alert type="info" :closable="false" title="日程表按上课时间自动排布，连续节次(如1-2节)自动跨行" class="sched-tip" />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  courses: { type: Array, default: () => [] }
})

const DAY_MAP = { '周一': 1, '周二': 2, '周三': 3, '周四': 4, '周五': 5, '周六': 6, '周日': 7 }
const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

function colorFor(code) {
  const colors = ['#2e74b5', '#e6a23c', '#67c23a', '#f56c6c', '#909399', '#409eff']
  let h = 0
  for (const ch of code) h = (h * 31 + ch.charCodeAt(0)) % colors.length
  return colors[h]
}

function parseTime(t) {
  if (!t) return null
  for (const [name, day] of Object.entries(DAY_MAP)) {
    if (t.includes(name)) {
      const m = t.match(/(\d+)\s*-\s*(\d+)/)
      if (m) return { day, start: +m[1], end: +m[2] }
      const s = t.match(/(\d+)\s*节/)
      if (s) return { day, start: +s[1], end: +s[1] }
    }
  }
  return null
}

// 构建日程表: grid[period][day]
const scheduleRows = computed(() => {
  const PERIODS = 10
  const grid = []
  for (let p = 1; p <= PERIODS; p++) {
    const cells = []
    for (let d = 1; d <= 7; d++) {
      cells.push({ course: null, covered: false })
    }
    grid.push({ period: p, cells })
  }
  // 填入课程
  for (const c of props.courses) {
    const parsed = parseTime(c.classTime)
    if (!parsed) continue
    const span = parsed.end - parsed.start + 1
    // 标记起始格
    if (parsed.start >= 1 && parsed.start <= PERIODS) {
      grid[parsed.start - 1].cells[parsed.day - 1].course = {
        courseCode: c.courseCode,
        courseName: c.courseName,
        location: c.location,
        span
      }
      // 标记被覆盖的后续格子
      for (let p = parsed.start + 1; p <= parsed.end && p <= PERIODS; p++) {
        grid[p - 1].cells[parsed.day - 1].covered = true
      }
    }
  }
  return grid
})
</script>

<style scoped>
.schedule-wrap { overflow-x: auto; }
.schedule-table {
  border-collapse: collapse;
  width: 100%;
  min-width: 860px;
  table-layout: fixed;
}
.schedule-table th, .schedule-table td {
  border: 1px solid #d9d9d9;
  padding: 4px;
  vertical-align: middle;
}
.sched-corner, .sched-day-header, .sched-period {
  background: #f5f7fa;
  text-align: center;
  font-weight: bold;
  color: #1f4e79;
  font-size: 13px;
}
.sched-period { width: 60px; color: #666; }
.sched-course-cell {
  text-align: center;
  color: #fff;
  font-size: 12px;
}
.course-name { font-weight: bold; font-size: 13px; }
.course-loc { opacity: 0.9; font-size: 11px; }
.sched-empty-cell { background: #fff; }
.sched-tip { margin-top: 12px; }
</style>
