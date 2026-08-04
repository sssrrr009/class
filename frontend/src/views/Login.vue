<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">
          <h1>智慧班级管理系统</h1>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名/学号/工号" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="role">
          <el-radio-group v-model="form.role" class="role-group">
            <el-radio-button value="ADMIN">管理员</el-radio-button>
            <el-radio-button value="TEACHER">教职工</el-radio-button>
            <el-radio-button value="STUDENT">学生</el-radio-button>
            <el-radio-button value="CADRE">干部</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const ROLE_HOME = { ADMIN: '/admin', TEACHER: '/teacher', STUDENT: '/student', CADRE: '/cadre' }

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push(res.data.role ? ROLE_HOME[res.data.role] : '/')
  } catch (e) {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1f4e79 0%, #2e74b5 100%);
}
.login-card {
  width: 420px;
  border-radius: 8px;
}
.login-title {
  text-align: center;
}
.login-title h1 {
  font-size: 22px;
  color: #1f4e79;
  margin: 0;
}
.role-group {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
.login-btn {
  width: 100%;
}
</style>
