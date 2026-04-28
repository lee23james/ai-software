<template>
  <el-card class="content-section" shadow="never">
    <template #header>简历辅助</template>
    <el-upload drag action="#" :auto-upload="false">
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽简历到此处，或点击选择文件</div>
      <template #tip>
        <div class="el-upload__tip">当前为框架页，后续接入后端文件上传和 AI 解析。</div>
      </template>
    </el-upload>
  </el-card>
</template>

<script setup>
import { UploadFilled } from '@element-plus/icons-vue'
</script>

