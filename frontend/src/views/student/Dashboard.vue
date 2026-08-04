<template>
  <el-row :gutter="20">
    <el-col :span="24">
      <el-card class="welcome-card">
        <h2 class="welcome-title">{{ greeting }}，{{ userStore.realName || userStore.username }}！</h2>
        <p class="welcome-sub">{{ quote }}</p>
      </el-card>
    </el-col>
    <el-col v-if="nextClass.hasNext" :span="24">
      <el-card header="下一节课">
        <div class="next-class">
          <div class="nc-main">
            <span class="nc-course">{{ nextClass.courseName }}</span>
            <span class="nc-loc">{{ nextClass.location }}</span>
          </div>
          <div class="nc-time">
            <span class="nc-label">距离开课还有</span>
            <span class="nc-count">{{ countdownText }}</span>
          </div>
        </div>
      </el-card>
    </el-col>
    <el-col v-else :span="24">
      <el-card>
        <el-empty description="今天没有更多课程了，好好休息！" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { getNextClass } from '../../api/dashboard'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const nextClass = ref({ hasNext: false })
let timer = null

const hour = new Date().getHours()
const greeting = hour < 6 ? '凌晨好' : hour < 9 ? '早上好' : hour < 12 ? '上午好'
  : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'

const quotes = [
  '每一个不曾起舞的日子，都是对生命的辜负。',
  '学习如逆水行舟，不进则退。',
  '今天的努力，是明天的底气。',
  '积土成山，风雨兴焉。',
  '少年易老学难成，一寸光阴不可轻。'
]
const quote = quotes[new Date().getDate() % quotes.length]

const countdownText = computed(() => {
  if (!nextClass.value.hasNext) return ''
  const { dayLeft, hourLeft, minLeft } = nextClass.value
  if (dayLeft > 0) return `${dayLeft}天 ${hourLeft}小时 ${minLeft}分钟`
  if (hourLeft > 0) return `${hourLeft}小时 ${minLeft}分钟`
  return `${minLeft}分钟`
})

async function load() {
  try {
    const res = await getNextClass()
    nextClass.value = res.data
  } catch (e) {
    nextClass.value = { hasNext: false }
  }
}
onMounted(() => {
  load()
  timer = setInterval(load, 60000)
})
onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.welcome-card { text-align: center; padding: 30px 0; }
.welcome-title { color: #1f4e79; margin-bottom: 10px; }
.welcome-sub { color: #909399; }
.next-class { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; }
.nc-course { font-size: 22px; font-weight: bold; color: #1f4e79; margin-right: 16px; }
.nc-loc { color: #666; }
.nc-time { text-align: right; }
.nc-label { color: #909399; font-size: 13px; margin-right: 10px; }
.nc-count { font-size: 24px; font-weight: bold; color: #e6a23c; }
</style>
