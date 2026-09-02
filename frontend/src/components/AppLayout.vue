<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute(); const router = useRouter()
const role = computed(() => localStorage.getItem('roleCode'))
const name = computed(() => localStorage.getItem('displayName') || '用户')
const dashboardRoles = ['ADMIN', 'DISPATCHER', 'ACCEPTOR']
const logout = () => { localStorage.clear(); router.push('/login') }
</script>

<template>
  <el-container class="shell">
    <el-aside width="224px" class="sidebar">
      <div class="brand"><span class="brand-mark">M</span><div><strong>设备运维中心</strong><small>Maintenance Hub</small></div></div>
      <el-menu :default-active="route.path" router class="nav-menu">
        <el-menu-item v-if="dashboardRoles.includes(role)" index="/dashboard"><el-icon><DataAnalysis /></el-icon>运行看板</el-menu-item>
        <el-menu-item index="/equipment"><el-icon><Monitor /></el-icon>设备台账</el-menu-item>
        <el-menu-item index="/work-orders"><el-icon><Tickets /></el-icon>运维工单</el-menu-item>
      </el-menu>
      <div class="aside-note"><span class="dot"></span> 服务运行中<br><small>单体应用 · 可审计</small></div>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div><h1>{{ route.meta.title || '企业设备运维' }}</h1><p>故障闭环、过程留痕、指标可见</p></div>
        <el-dropdown @command="logout"><span class="user-chip"><el-avatar :size="32">{{ name.slice(0, 1) }}</el-avatar>{{ name }}<el-icon><ArrowDown /></el-icon></span><template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown>
      </el-header>
      <el-main class="content"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

