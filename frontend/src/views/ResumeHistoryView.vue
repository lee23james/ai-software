<template>
  <el-card class="content-section" shadow="never">
    <template #header>
      <div class="history-header">
        <span>简历历史</span>
        <el-button type="primary" plain @click="$router.push('/resume')">返回上传</el-button>
      </div>
    </template>

    <el-table :data="historyList" v-loading="loading" style="width: 100%">
      <el-table-column prop="resumeName" label="简历名称" min-width="160" />
      <el-table-column prop="fileType" label="类型" width="90" />
      <el-table-column prop="targetJobName" label="目标岗位" min-width="140" />
      <el-table-column prop="parseStatus" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.parseStatus === 0 ? 'warning' : 'success'">
            {{ row.parseStatus === 0 ? '待解析' : '已保存' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="resumeTextPreview" label="解析预览" min-width="260" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="上传时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="goDetail(row.resumeId)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { fetchResumeHistory } from '@/api/resume'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const historyList = ref([])

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 19)
}

async function loadHistory() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录后查看历史记录')
    await router.push('/login')
    return
  }
  loading.value = true
  try {
    const result = await fetchResumeHistory(userStore.userId)
    historyList.value = result.data || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '获取历史记录失败')
  } finally {
    loading.value = false
  }
}

function goDetail(resumeId) {
  router.push(`/resume/history/${resumeId}`)
}

onMounted(loadHistory)
</script>
