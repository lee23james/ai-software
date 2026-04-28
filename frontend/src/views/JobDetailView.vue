<template>
  <el-card class="content-section" shadow="never">
    <template #header>岗位详情</template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="岗位 ID">{{ $route.params.id }}</el-descriptions-item>
      <el-descriptions-item label="岗位名称">Java 后端开发实习生</el-descriptions-item>
      <el-descriptions-item label="城市">上海</el-descriptions-item>
      <el-descriptions-item label="薪资">10K-15K</el-descriptions-item>
      <el-descriptions-item label="技能要求">Java、Spring Boot、MySQL</el-descriptions-item>
      <el-descriptions-item label="学历要求">本科及以上</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

