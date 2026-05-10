<template>
  <main class="login-page">
    <section class="login-panel">
      <h1>就业分析平台</h1>
      <p>先简单跑通：支持手机号/邮箱注册并登录。</p>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form label-position="top">
        <el-form-item label="手机号或邮箱">
          <el-input v-model="form.identifier" placeholder="输入手机号或邮箱" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <template v-if="activeTab === 'register'">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="可选" />
          </el-form-item>
        </template>
        <el-button :loading="submitting" type="primary" class="full-button" @click="handleSubmit">
          {{ activeTab === 'login' ? '登录并进入系统' : '注册账号' }}
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { checkSession, login, register } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const submitting = ref(false)
const form = reactive({
  identifier: '',
  username: '',
  password: ''
})

function splitIdentifier(identifier) {
  const value = identifier.trim()
  if (value.includes('@')) {
    return { email: value, phone: null }
  }
  return { phone: value, email: null }
}

onMounted(async () => {
  const snapshotId = userStore.userId
  if (!snapshotId) {
    return
  }
  try {
    const result = await checkSession(snapshotId)
    // 校验期间若用户已注册/登录为新账号，不得用旧请求结果覆盖或清空会话
    if (userStore.userId !== snapshotId) {
      return
    }
    if (result?.data) {
      userStore.setUser(result.data)
      ElMessage.success('已登录，正在进入系统')
      await router.replace('/resume')
    }
  } catch {
    if (userStore.userId !== snapshotId) {
      return
    }
    // 仅清理与本次校验一致的过期会话，避免误杀刚登录的新账号
    userStore.logout()
  }
})

async function handleSubmit() {
  if (!form.identifier.trim() || !form.password.trim()) {
    ElMessage.warning('请先填写手机号/邮箱和密码')
    return
  }
  submitting.value = true
  try {
    if (activeTab.value === 'register') {
      const payload = {
        username: form.username.trim() || null,
        ...splitIdentifier(form.identifier),
        password: form.password
      }
      const result = await register(payload)
      userStore.setUser(result.data)
      ElMessage.success('注册成功，已自动登录')
      await router.push('/resume')
      return
    }
    const result = await login({
      identifier: form.identifier.trim(),
      password: form.password
    })
    userStore.setUser(result.data)
    ElMessage.success('登录成功')
    await router.push('/resume')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

