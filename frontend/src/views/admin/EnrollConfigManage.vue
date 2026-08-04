<template>
  <el-card header="选课窗口设置">
    <el-form :model="form" label-width="110px" style="max-width: 520px">
      <el-form-item label="是否开放选课">
        <el-switch v-model="form.isOpen" :active-value="1" :inactive-value="0"
          active-text="开放" inactive-text="关闭" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="form.startTime" type="datetime" placeholder="选课开始时间"
          style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="form.endTime" type="datetime" placeholder="选课结束时间"
          style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </el-form-item>
    </el-form>
    <el-alert type="info" :closable="false" title="说明：选课仅在设置的时间窗口内开放。选课结束时按课程容量随机分配名额。" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnrollConfig, updateEnrollConfig } from '../../api/enrollConfig'

const form = reactive({ isOpen: 0, startTime: null, endTime: null })

async function load() {
  const res = await getEnrollConfig()
  if (res.data) {
    form.isOpen = res.data.isOpen
    form.startTime = res.data.startTime
    form.endTime = res.data.endTime
  }
}
async function handleSave() {
  await updateEnrollConfig(form)
  ElMessage.success('选课设置已保存')
}
onMounted(load)
</script>
