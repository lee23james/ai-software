<template>
  <el-card class="content-section" shadow="never">
    <template #header>岗位列表</template>

    <el-form class="filter-bar" inline>
      <el-form-item label="城市">
        <el-input v-model="filters.city" placeholder="例如：上海" />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" placeholder="岗位或技能" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">筛选</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="jobs" style="width: 100%">
      <el-table-column prop="name" label="岗位" />
      <el-table-column prop="company" label="公司" />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="salary" label="薪资" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/jobs/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { reactive } from 'vue'

const filters = reactive({
  city: '',
  keyword: ''
})

const jobs = [
  { id: 1, name: 'Java 后端开发实习生', company: '示例科技', city: '上海', salary: '10K-15K' },
  { id: 2, name: '前端开发实习生', company: '数智未来', city: '杭州', salary: '8K-12K' },
  { id: 3, name: '数据分析助理', company: '云启数据', city: '深圳', salary: '9K-13K' }
]
</script>

