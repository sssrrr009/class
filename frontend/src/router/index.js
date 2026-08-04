import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const HOME = { ADMIN: '/admin', TEACHER: '/teacher', STUDENT: '/student', CADRE: '/cadre' }

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    redirect: () => {
      const store = useUserStore()
      return store.role ? HOME[store.role] || '/login' : '/login'
    }
  },
  // 个人中心
  {
    path: '/profile',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT', 'CADRE'], title: '个人中心' },
    children: [
      { path: '', name: 'Profile', component: () => import('../views/admin/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },
  // 管理员
  {
    path: '/admin',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', name: 'AdminHome', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'college', component: () => import('../views/admin/CollegeManage.vue'), meta: { title: '学院管理' } },
      { path: 'major', component: () => import('../views/admin/MajorManage.vue'), meta: { title: '专业管理' } },
      { path: 'teacher', component: () => import('../views/admin/TeacherManage.vue'), meta: { title: '教师管理' } },
      { path: 'student', component: () => import('../views/admin/StudentManage.vue'), meta: { title: '学生管理' } },
      { path: 'class', component: () => import('../views/admin/ClassManage.vue'), meta: { title: '班级管理' } },
      { path: 'cadre', component: () => import('../views/admin/CadreManage.vue'), meta: { title: '干部管理' } },
      { path: 'course', component: () => import('../views/admin/CourseManage.vue'), meta: { title: '课程管理' } },
      { path: 'course-category', component: () => import('../views/admin/CourseCategoryManage.vue'), meta: { title: '课程类别' } },
      { path: 'plan', component: () => import('../views/admin/MajorPlanManage.vue'), meta: { title: '专业计划' } },
      { path: 'enroll-config', component: () => import('../views/admin/EnrollConfigManage.vue'), meta: { title: '选课设置' } },
      { path: 'course-change', component: () => import('../views/admin/CourseChangeApprove.vue'), meta: { title: '课程修改审批' } },
      { path: 'score', component: () => import('../views/teacher/ScoreManage.vue'), meta: { title: '成绩管理' } },
      { path: 'notice', component: () => import('../views/shared/NoticeManage.vue'), meta: { title: '公告管理' } },
      { path: 'file-category', component: () => import('../views/admin/FileCategoryManage.vue'), meta: { title: '文件分类' } },
      { path: 'file', component: () => import('../views/admin/FileManage.vue'), meta: { title: '文件管理' } },
      { path: 'vote', component: () => import('../views/admin/VoteManage.vue'), meta: { title: '投票管理' } }
    ]
  },
  // 教师
  {
    path: '/teacher',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { roles: ['TEACHER'] },
    children: [
      { path: '', name: 'TeacherHome', component: () => import('../views/teacher/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'course', component: () => import('../views/teacher/MyCourses.vue'), meta: { title: '我的课程' } },
      { path: 'course-change', component: () => import('../views/teacher/CourseChangeManage.vue'), meta: { title: '课程修改申请' } },
      { path: 'score', component: () => import('../views/teacher/ScoreManage.vue'), meta: { title: '成绩管理' } },
      { path: 'notice', component: () => import('../views/shared/NoticeManage.vue'), meta: { title: '公告管理' } },
      { path: 'file', component: () => import('../views/admin/FileManage.vue'), meta: { title: '文件管理' } },
      { path: 'vote', component: () => import('../views/admin/VoteManage.vue'), meta: { title: '投票管理' } }
    ]
  },
  // 学生
  {
    path: '/student',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { roles: ['STUDENT'] },
    children: [
      { path: '', name: 'StudentHome', component: () => import('../views/student/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'course', component: () => import('../views/student/CourseList.vue'), meta: { title: '课程信息' } },
      { path: 'enroll', component: () => import('../views/student/Enroll.vue'), meta: { title: '选课管理' } },
      { path: 'score', component: () => import('../views/student/MyScores.vue'), meta: { title: '我的成绩' } },
      { path: 'notice', component: () => import('../views/shared/NoticeView.vue'), meta: { title: '通知公告' } },
      { path: 'file', component: () => import('../views/student/FileView.vue'), meta: { title: '文件下载' } },
      { path: 'vote', component: () => import('../views/student/VoteList.vue'), meta: { title: '投票评选' } }
    ]
  },
  // 干部
  {
    path: '/cadre',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { roles: ['CADRE'] },
    children: [
      { path: '', name: 'CadreHome', component: () => import('../views/cadre/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'notice', component: () => import('../views/shared/NoticeManage.vue'), meta: { title: '公告管理' } },
      { path: 'vote', component: () => import('../views/admin/VoteManage.vue'), meta: { title: '投票管理' } },
      { path: 'course', component: () => import('../views/student/CourseList.vue'), meta: { title: '课程信息' } },
      { path: 'enroll', component: () => import('../views/student/Enroll.vue'), meta: { title: '选课管理' } },
      { path: 'score', component: () => import('../views/student/MyScores.vue'), meta: { title: '我的成绩' } },
      { path: 'file', component: () => import('../views/student/FileView.vue'), meta: { title: '文件下载' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - 智慧班级管理系统` : '智慧班级管理系统'
  if (to.meta.public) {
    next()
    return
  }
  if (!store.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.roles && !to.meta.roles.includes(store.role)) {
    next(HOME[store.role] || '/login')
    return
  }
  next()
})

export default router
