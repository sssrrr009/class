<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">智慧班级管理系统</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f4e79"
        text-color="#cfe0f0"
        active-text-color="#ffffff"
      >
        <el-menu-item :index="homePath"><span>首页</span></el-menu-item>
        <el-sub-menu v-for="menu in menus" :key="menu.title" :index="menu.title">
          <template #title>{{ menu.title }}</template>
          <el-menu-item v-for="item in menu.children" :key="item.path" :index="item.path">
            {{ item.title }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">{{ currentRoleName }}</div>
        <div class="header-right">
          <el-button link type="primary" @click="$router.push('/profile')">个人中心</el-button>
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const ROLE_NAMES = { ADMIN: '管理员', TEACHER: '教职工', STUDENT: '学生', CADRE: '干部' }
const currentRoleName = computed(() => ROLE_NAMES[userStore.role] || '未知角色')
const homePath = computed(() => `/${userStore.role?.toLowerCase()}`)

const MENUS = {
  ADMIN: [
    {
      title: '基础管理',
      children: [
        { path: '/admin/college', title: '学院管理' },
        { path: '/admin/major', title: '专业管理' },
        { path: '/admin/teacher', title: '教师管理' },
        { path: '/admin/student', title: '学生管理' },
        { path: '/admin/class', title: '班级管理' },
        { path: '/admin/cadre', title: '干部管理' }
      ]
    },
    {
      title: '教学管理',
      children: [
        { path: '/admin/course', title: '课程管理' },
        { path: '/admin/course-category', title: '课程类别' },
        { path: '/admin/plan', title: '专业计划' },
        { path: '/admin/enroll-config', title: '选课设置' },
        { path: '/admin/course-change', title: '课程修改审批' },
        { path: '/admin/score', title: '成绩管理' }
      ]
    },
    {
      title: '信息发布',
      children: [
        { path: '/admin/notice', title: '公告管理' },
        { path: '/admin/file-category', title: '文件分类' },
        { path: '/admin/file', title: '文件管理' },
        { path: '/admin/vote', title: '投票管理' }
      ]
    }
  ],
  TEACHER: [
    {
      title: '教学管理',
      children: [
        { path: '/teacher/course', title: '课程管理' },
        { path: '/teacher/course-change', title: '课程修改申请' },
        { path: '/teacher/score', title: '成绩管理' }
      ]
    },
    {
      title: '信息发布',
      children: [
        { path: '/teacher/notice', title: '公告管理' },
        { path: '/teacher/file', title: '文件管理' },
        { path: '/teacher/vote', title: '投票管理' }
      ]
    }
  ],
  STUDENT: [
    {
      title: '学习中心',
      children: [
        { path: '/student/course', title: '课程信息' },
        { path: '/student/enroll', title: '选课管理' },
        { path: '/student/score', title: '我的成绩' }
      ]
    },
    {
      title: '信息中心',
      children: [
        { path: '/student/notice', title: '通知公告' },
        { path: '/student/file', title: '文件下载' },
        { path: '/student/vote', title: '投票评选' }
      ]
    }
  ],
  CADRE: [
    {
      title: '信息中心',
      children: [
        { path: '/cadre/notice', title: '公告管理' },
        { path: '/cadre/vote', title: '投票管理' }
      ]
    },
    {
      title: '学习中心',
      children: [
        { path: '/cadre/course', title: '课程信息' },
        { path: '/cadre/enroll', title: '选课管理' },
        { path: '/cadre/score', title: '我的成绩' },
        { path: '/cadre/file', title: '文件下载' }
      ]
    }
  ]
}

const menus = computed(() => MENUS[userStore.role] || [])

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background-color: #1f4e79; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 16px; font-weight: bold; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; border-bottom: 1px solid #e4e7ed; }
.header-left { font-size: 16px; color: #1f4e79; font-weight: bold; }
.user-name { margin-right: 12px; }
.main { background: #f0f2f5; }
</style>
