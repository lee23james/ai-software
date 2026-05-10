<template>
  <el-container class="app-shell">
    <el-aside class="app-sidebar" width="232px">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <strong>就业分析平台</strong>
          <span>Job Analytics</span>
        </div>
      </div>

      <el-menu router :default-active="$route.path" class="side-menu">
        <el-menu-item index="/dashboard">首页看板</el-menu-item>
        <el-menu-item index="/jobs">岗位列表</el-menu-item>
        <el-menu-item index="/analysis">数据分析</el-menu-item>
        <el-menu-item index="/resume">简历辅助</el-menu-item>
        <el-menu-item index="/resume/history">简历历史</el-menu-item>
        <el-menu-item index="/admin">后台管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>招聘数据分析与智能求职辅助平台</p>
        </div>
        <div class="header-actions">
          <el-button v-if="!isLoggedIn" type="primary" plain @click="$router.push('/login')">登录入口</el-button>
          <template v-else>
            <el-tag type="success" effect="plain">已登录：{{ displayName }}</el-tag>
            <el-button plain @click="handleLogout">退出登录</el-button>
          </template>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { checkSession } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentTitle = computed(() => route.meta.title || '首页看板')
const isLoggedIn = computed(() => !!userStore.userId)
const displayName = computed(() => userStore.profile.username || userStore.profile.phone || userStore.profile.email)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function shouldTreatAsStaleSession(error) {
  const status = error?.response?.status
  if (status === 400 || status === 401 || status === 403) {
    return true
  }
  // 业务包装：HTTP 200 但 body.code≠200 时，拦截器已 reject，无 response.status
  const msg = String(error?.response?.data?.message || error?.message || '')
  return msg.includes('登录状态已失效') || msg.includes('用户不存在')
}

onMounted(async () => {
  const snapshotId = userStore.userId
  if (!snapshotId) {
    return
  }
  try {
    const result = await checkSession(snapshotId)
    if (userStore.userId !== snapshotId) {
      return
    }
    if (result?.data) {
      userStore.setUser(result.data)
    }
  } catch (error) {
    if (userStore.userId !== snapshotId) {
      return
    }
    if (!shouldTreatAsStaleSession(error)) {
      return
    }
    userStore.logout()
    ElMessage.warning('登录已失效，请重新注册或登录（常见于数据库被清空或重建后）')
    await router.push('/login')
  }
})
</script>
