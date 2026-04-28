<template>
  <section class="page-grid two-columns">
    <el-card class="content-section" shadow="never">
      <template #header>城市岗位分布</template>
      <div ref="cityChartRef" class="chart-box"></div>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>分析指标</template>
      <el-table :data="metrics" style="width: 100%">
        <el-table-column prop="name" label="指标" />
        <el-table-column prop="value" label="数值" />
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'

const cityChartRef = ref(null)
const metrics = [
  { name: '平均薪资', value: '12.6K' },
  { name: '岗位数量最高城市', value: '上海' },
  { name: '热门技能', value: 'Java' }
]

onMounted(() => {
  const chart = echarts.init(cityChartRef.value)
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: ['上海', '杭州', '深圳', '北京'] },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: [320, 260, 240, 210],
        itemStyle: { color: '#2563eb' }
      }
    ]
  })
})
</script>

