<template>
  <main class="login-page">
    <section class="login-panel">
      <h1>就业分析平台</h1>
      <p>当前为前端框架页，后续接入真实登录接口。</p>

      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="full-button" @click="$router.push('/dashboard')">
          进入系统
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  username: '',
  password: ''
})
</script>

