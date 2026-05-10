<template>
  <div class="detail-stack" v-loading="loading">
    <el-card class="content-section" shadow="never">
      <template #header>
        <div class="history-header">
          <span>简历详情</span>
          <el-space>
            <el-button plain @click="$router.push('/resume/history')">返回历史</el-button>
            <el-button type="primary" plain @click="$router.push('/resume')">继续上传</el-button>
          </el-space>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="简历名称">{{ detail.resumeName }}</el-descriptions-item>
        <el-descriptions-item label="目标岗位">{{ detail.targetJobName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">{{ detail.fileType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="解析状态">{{ detail.parseStatus === 0 ? '待解析' : '已保存' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <div class="page-grid two-columns">
      <el-card class="content-section" shadow="never">
        <template #header>解析文本</template>
        <pre class="text-panel">{{ detail.resumeText || '暂无解析文本' }}</pre>
      </el-card>

      <el-card class="content-section" shadow="never">
        <template #header>解析摘要</template>
        <div v-if="detail.parseResult">
          <p><strong>摘要说明：</strong>{{ detail.parseResult.suggestions || '-' }}</p>
          <p><strong>学校：</strong>{{ detail.parseResult.parsedSchool || '-' }}</p>
          <p><strong>专业：</strong>{{ detail.parseResult.parsedMajor || '-' }}</p>
          <p><strong>学历：</strong>{{ detail.parseResult.parsedEducation || '-' }}</p>
        </div>
        <p v-else>暂无解析摘要</p>
      </el-card>
    </div>

    <div class="page-grid two-columns">
      <el-card class="content-section" shadow="never">
        <template #header>已保存技能</template>
        <el-space wrap>
          <el-tag v-for="skill in detail.skills" :key="skill.id" type="success">
            {{ skill.skillName }}
          </el-tag>
        </el-space>
        <p v-if="detail.skills.length === 0">暂无技能记录</p>
      </el-card>

      <el-card class="content-section" shadow="never">
        <template #header>匹配结果</template>
        <el-table :data="detail.matches" style="width: 100%">
          <el-table-column prop="jobName" label="岗位" />
          <el-table-column prop="companyName" label="公司" />
          <el-table-column prop="totalScore" label="总分" width="90" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchResumeHistoryDetail } from '@/api/resume'

const route = useRoute()
const loading = ref(false)
const detail = reactive({
  resumeId: null,
  resumeName: '',
  fileUrl: '',
  fileType: '',
  targetJobName: '',
  parseStatus: 0,
  resumeText: '',
  skills: [],
  parseResult: null,
  matches: []
})

async function loadDetail() {
  loading.value = true
  try {
    const result = await fetchResumeHistoryDetail(route.params.id)
    Object.assign(detail, result.data || {})
    detail.skills = result.data?.skills || []
    detail.matches = result.data?.matches || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>
