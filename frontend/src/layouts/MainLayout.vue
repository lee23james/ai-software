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
        <el-menu-item index="/admin">后台管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>招聘数据分析与智能求职辅助平台</p>
        </div>
        <el-button type="primary" plain @click="$router.push('/login')">登录入口</el-button>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentTitle = computed(() => route.meta.title || '首页看板')
</script>

