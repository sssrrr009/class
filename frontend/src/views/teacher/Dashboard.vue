<template>
  <el-card class="welcome-card">
    <h2>{{ greeting }}，{{ userStore.realName || userStore.username }}老师！</h2>
    <p>欢迎使用智慧班级管理系统教师端，您可以在左侧菜单管理课程与成绩、发布公告与投票。</p>
  </el-card>
</template>

<script setup>
import { useUserStore } from '../../stores/user'
const userStore = useUserStore()
const hour = new Date().getHours()
const greeting = hour < 9 ? '早上好' : hour < 12 ? '上午好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
</script>

<style scoped>
.welcome-card { text-align: center; padding: 40px 0; }
h2 { color: #1f4e79; margin-bottom: 12px; }
p { color: #666; }
</style>
