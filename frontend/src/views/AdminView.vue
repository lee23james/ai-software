<template>
  <el-card class="content-section" shadow="never">
    <template #header>后台管理</template>
    <el-table :data="items" style="width: 100%">
      <el-table-column prop="module" label="模块" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="owner" label="负责人" />
    </el-table>
  </el-card>
</template>

<script setup>
const items = [
  { module: '岗位数据管理', status: '待开发', owner: '后端负责人' },
  { module: '用户管理', status: '待开发', owner: '后端负责人' },
  { module: '数据清理', status: '待开发', owner: '数据负责人' }
]
</script>

