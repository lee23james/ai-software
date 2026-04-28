<template>
  <section class="page-grid">
    <el-card class="metric-card" shadow="never">
      <span>岗位总数</span>
      <strong>1,280</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>覆盖城市</span>
      <strong>18</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>热门技能</span>
      <strong>Java</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>平均薪资</span>
      <strong>12.6K</strong>
    </el-card>
  </section>

  <el-card class="content-section" shadow="never">
    <template #header>开发入口</template>
    <el-space wrap>
      <el-button type="primary" @click="$router.push('/jobs')">查看岗位</el-button>
      <el-button @click="$router.push('/analysis')">数据分析</el-button>
      <el-button @click="$router.push('/resume')">简历辅助</el-button>
    </el-space>
  </el-card>
</template>

