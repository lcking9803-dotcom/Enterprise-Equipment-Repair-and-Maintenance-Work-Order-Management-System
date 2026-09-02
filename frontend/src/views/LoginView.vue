<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter(); const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Admin@123' })
const accounts = [
  ['管理员', 'admin', 'Admin@123'], ['报修人员', 'reporter', 'Reporter@123'],
  ['调度人员', 'dispatcher', 'Dispatcher@123'], ['维修人员', 'maintainer', 'Maintainer@123'],
  ['验收人员', 'acceptor', 'Acceptor@123']
]
const fill = a => { form.username = a[1]; form.password = a[2] }
const submit = async () => {
  loading.value = true
  try {
    const { data } = await authApi.login(form); const user = data.data
    Object.entries(user).forEach(([k, v]) => localStorage.setItem(k, v))
    localStorage.setItem('roleCode', user.roleCode); ElMessage.success('登录成功')
    router.push(['ADMIN', 'DISPATCHER', 'ACCEPTOR'].includes(user.roleCode) ? '/dashboard' : '/work-orders')
  } finally { loading.value = false }
}
</script>

<template>
  <main class="login-page">
    <section class="login-intro"><div class="eyebrow">ENTERPRISE MAINTENANCE</div><h1>让每一次设备故障<br>都有闭环、有证据。</h1><p>设备台账、工单流转、权限控制、维修验收和运维指标集中管理。</p><div class="feature-row"><span>状态机</span><span>全程审计</span><span>SLA指标</span></div></section>
    <section class="login-card"><h2>登录运维中心</h2><p>选择演示角色或输入账号</p><el-form @submit.prevent="submit"><el-form-item><el-input v-model="form.username" size="large" placeholder="用户名" prefix-icon="User" /></el-form-item><el-form-item><el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" prefix-icon="Lock" @keyup.enter="submit" /></el-form-item><el-button type="primary" size="large" :loading="loading" class="login-button" @click="submit">进入系统</el-button></el-form><div class="demo-accounts"><small>演示账号</small><button v-for="a in accounts" :key="a[1]" @click="fill(a)">{{ a[0] }}</button></div></section>
  </main>
</template>

