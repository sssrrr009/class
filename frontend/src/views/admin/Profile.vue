<template>
  <el-row :gutter="20">
    <el-col :span="12">
      <el-card header="基本信息">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="角色">{{ roleName }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ info.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ info.realName }}</el-descriptions-item>
          <el-descriptions-item v-if="info.gender" label="性别">{{ info.gender }}</el-descriptions-item>
          <el-descriptions-item v-if="info.phone" label="联系电话">{{ info.phone }}</el-descriptions-item>
          <el-descriptions-item v-if="info.title" label="职称">{{ info.title }}</el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" style="margin-top: 14px" @click="profileVisible = true">修改资料</el-button>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card header="修改密码">
        <el-form :model="pwdForm" label-width="80px" style="max-width: 360px">
          <el-form-item label="原密码" required>
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" required>
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="danger" @click="handleChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="profileVisible" title="修改资料" width="420px">
    <el-form :model="profileForm" label-width="80px">
      <el-form-item v-if="userStore.role === 'ADMIN'" label="用户名">
        <el-input v-model="profileForm.username" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="profileForm.realName" />
      </el-form-item>
      <el-form-item v-if="['STUDENT','CADRE'].includes(userStore.role)" label="性别">
        <el-radio-group v-model="profileForm.gender">
          <el-radio value="男">男</el-radio>
          <el-radio value="女">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="profileForm.phone" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="profileVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSaveProfile">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, changePassword, updateProfile } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const info = ref({})
const profileVisible = ref(false)
const roleName = { ADMIN: '管理员', TEACHER: '教职工', STUDENT: '学生', CADRE: '干部' }[userStore.role] || userStore.role
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
const profileForm = reactive({ username: '', realName: '', gender: '', phone: '' })

async function load() {
  const res = await getUserInfo()
  info.value = res.data
  profileForm.username = res.data.username || ''
  profileForm.realName = res.data.realName || ''
  profileForm.gender = res.data.gender || ''
  profileForm.phone = res.data.phone || ''
}
async function handleChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return ElMessage.warning('请输入完整信息')
  await changePassword(pwdForm)
  ElMessage.success('密码修改成功，请重新登录')
  userStore.logout()
  window.location.href = '/login'
}
async function handleSaveProfile() {
  await updateProfile(profileForm)
  ElMessage.success('资料修改成功')
  profileVisible.value = false
  load()
}
onMounted(load)
</script>
