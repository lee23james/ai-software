<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 数据看板 -->
      <el-tab-pane label="数据看板" name="dashboard">
        <el-row :gutter="20">
          <el-col :span="6" v-for="(stat, index) in statsList" :key="index">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card class="content-section" shadow="never">
          <template #header>后台管理</template>
          <el-table :data="items" style="width: 100%">
            <el-table-column prop="module" label="模块" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === '已完成' ? 'success' : 'info'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="owner" label="负责人" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 岗位管理 -->
      <el-tab-pane label="岗位管理" name="jobs">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>岗位数据管理</span>
              <div class="header-actions">
                <el-button type="primary" @click="openAddDialog">新增岗位</el-button>
                <el-upload
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleFileChange"
                  accept=".csv"
                  style="display: inline-block; margin-left: 10px;"
                >
                  <el-button type="success">CSV 导入</el-button>
                </el-upload>
              </div>
            </div>
          </template>

          <!-- 筛选表单 -->
          <el-form :model="queryForm" inline class="filter-form">
            <el-form-item label="岗位名称">
              <el-input v-model="queryForm.keyword" placeholder="请输入岗位名称" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="公司名称">
              <el-input v-model="queryForm.companyName" placeholder="请输入公司名称" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="城市">
              <el-input v-model="queryForm.city" placeholder="请输入城市" clearable style="width: 120px" />
            </el-form-item>
            <el-form-item label="学历要求">
              <el-select v-model="queryForm.education" placeholder="请选择" clearable style="width: 120px">
                <el-option label="不限" value="不限" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="上线" :value="1" />
                <el-option label="下线" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 数据表格 -->
          <el-table :data="jobList" style="width: 100%" v-loading="loading" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="jobName" label="岗位名称" min-width="150" />
            <el-table-column prop="companyName" label="公司名称" min-width="120" />
            <el-table-column prop="city" label="城市" width="100" />
            <el-table-column label="薪资范围" width="120">
              <template #default="scope">
                {{ scope.row.salaryMin }}-{{ scope.row.salaryMax }}
              </template>
            </el-table-column>
            <el-table-column prop="education" label="学历要求" width="100" />
            <el-table-column prop="experience" label="经验要求" width="100" />
            <el-table-column prop="skillTags" label="技能标签" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="(val) => handleStatusChange(scope.row.id, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="queryForm.pageNum"
            v-model:page-size="queryForm.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </el-card>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>用户数据管理</span>
            </div>
          </template>

          <!-- 筛选表单 -->
          <el-form :model="userQueryForm" inline class="filter-form">
            <el-form-item label="关键词">
              <el-input v-model="userQueryForm.keyword" placeholder="用户名/姓名/邮箱" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="userQueryForm.role" placeholder="请选择" clearable style="width: 120px">
                <el-option label="学生" value="student" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="userQueryForm.status" placeholder="请选择" clearable style="width: 120px">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUserSearch">查询</el-button>
              <el-button @click="handleUserReset">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 数据表格 -->
          <el-table :data="userList" style="width: 100%" v-loading="userLoading" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="realName" label="真实姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column prop="email" label="邮箱" min-width="150" />
            <el-table-column prop="role" label="角色" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
                  {{ scope.row.role === 'admin' ? '管理员' : '学生' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="(val) => handleUserStatusChange(scope.row.id, val)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginAt" label="最后登录" width="160" />
            <el-table-column prop="createdAt" label="注册时间" width="160" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" type="danger" @click="handleUserDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="userQueryForm.pageNum"
            v-model:page-size="userQueryForm.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="userTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleUserSizeChange"
            @current-change="handleUserPageChange"
            class="pagination"
          />
        </el-card>
      </el-tab-pane>

      <!-- 系统日志 -->
      <el-tab-pane label="系统日志" name="logs">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>操作日志</span>
            </div>
          </template>

          <!-- 筛选表单 -->
          <el-form :model="logQueryForm" inline class="filter-form">
            <el-form-item label="模块">
              <el-input v-model="logQueryForm.moduleName" placeholder="模块名称" clearable style="width: 150px" />
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="logQueryForm.operationType" placeholder="请选择" clearable style="width: 120px">
                <el-option label="新增" value="新增" />
                <el-option label="修改" value="修改" />
                <el-option label="删除" value="删除" />
                <el-option label="查询" value="查询" />
                <el-option label="登录" value="登录" />
              </el-select>
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="logQueryForm.resultStatus" placeholder="请选择" clearable style="width: 120px">
                <el-option label="成功" value="success" />
                <el-option label="失败" value="fail" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogSearch">查询</el-button>
              <el-button @click="handleLogReset">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 数据表格 -->
          <el-table :data="logList" style="width: 100%" v-loading="logLoading" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="userId" label="用户ID" width="80" />
            <el-table-column prop="moduleName" label="模块" width="120" />
            <el-table-column prop="operationType" label="操作类型" width="100" />
            <el-table-column prop="operationDesc" label="操作描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="requestPath" label="请求路径" min-width="150" show-overflow-tooltip />
            <el-table-column prop="requestMethod" label="请求方式" width="80" />
            <el-table-column prop="ipAddress" label="IP地址" width="120" />
            <el-table-column label="结果" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.resultStatus === 'success' ? 'success' : 'danger'">
                  {{ scope.row.resultStatus === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="操作时间" width="160" />
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="logQueryForm.pageNum"
            v-model:page-size="logQueryForm.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="logTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleLogSizeChange"
            @current-change="handleLogPageChange"
            class="pagination"
          />
        </el-card>
      </el-tab-pane>

      <!-- 数据清理 -->
      <el-tab-pane label="数据清理" name="clean">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据清理与重置</span>
            </div>
          </template>

          <el-alert
            title="警告：数据清理操作不可恢复，请谨慎操作！"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />

          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="clean-card">
                <template #header>清理岗位数据</template>
                <div class="clean-desc">删除所有岗位数据，保留用户和日志</div>
                <el-button type="danger" @click="handleClean('jobs')">执行清理</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="clean-card">
                <template #header>清理用户数据</template>
                <div class="clean-desc">删除所有用户数据，保留岗位和日志</div>
                <el-button type="danger" @click="handleClean('users')">执行清理</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="clean-card">
                <template #header>清理简历数据</template>
                <div class="clean-desc">删除所有简历数据</div>
                <el-button type="danger" @click="handleClean('resumes')">执行清理</el-button>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <el-card shadow="hover" class="clean-card">
                <template #header>清理系统日志</template>
                <div class="clean-desc">删除所有操作日志记录</div>
                <el-button type="danger" @click="handleClean('logs')">执行清理</el-button>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="clean-card">
                <template #header>重置所有数据</template>
                <div class="clean-desc">清空所有数据，恢复到初始状态</div>
                <el-button type="danger" @click="handleClean('all')">执行重置</el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      class="job-dialog"
    >
      <el-form :model="formData" label-width="100px" :rules="formRules" ref="formRef" class="job-form">
        <el-form-item label="岗位名称" prop="jobName">
          <el-input v-model="formData.jobName" style="width: 100%" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="formData.companyName" style="width: 100%" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="formData.city" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资" prop="salaryMin">
              <el-input-number v-model="formData.salaryMin" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资" prop="salaryMax" label-width="100px">
              <el-input-number v-model="formData.salaryMax" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学历要求" prop="education">
          <el-select v-model="formData.education" style="width: 100%">
            <el-option label="不限" value="不限" />
            <el-option label="大专" value="大专" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
            <el-option label="博士" value="博士" />
          </el-select>
        </el-form-item>
        <el-form-item label="经验要求" prop="experience">
          <el-input v-model="formData.experience" placeholder="例如：1-3年" style="width: 100%" />
        </el-form-item>
        <el-form-item label="技能标签" prop="skillTags">
          <el-input v-model="formData.skillTags" placeholder="逗号分隔，例如：Java,Spring Boot,MySQL" style="width: 100%" />
        </el-form-item>
        <el-form-item label="岗位描述" prop="jobDescription">
          <el-input v-model="formData.jobDescription" type="textarea" :rows="4" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上线"
            inactive-text="下线"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchAdminDashboard,
  fetchAdminJobList,
  addAdminJob,
  updateAdminJob,
  deleteAdminJob,
  toggleJobStatus,
  importJobs,
  fetchAdminUserList,
  toggleUserStatus,
  deleteAdminUser,
  fetchAdminLogList,
  cleanAdminData
} from '../api/admin'

const activeTab = ref('dashboard')
const loading = ref(false)
const userLoading = ref(false)
const logLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const formRef = ref(null)
const isEdit = ref(false)
const editingId = ref(null)

// 看板数据
const statsList = ref([
  { label: '总岗位数', value: 0 },
  { label: '用户数', value: 0 },
  { label: '简历数', value: 0 },
  { label: '今日新增', value: 0 }
])

const items = ref([
  { module: '岗位数据管理', status: '已完成', owner: '后端负责人' },
  { module: '用户管理', status: '已完成', owner: '后端负责人' },
  { module: '数据清理', status: '已完成', owner: '数据负责人' }
])

// 岗位列表查询
const queryForm = reactive({
  keyword: '',
  companyName: '',
  city: '',
  education: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const jobList = ref([])
const total = ref(0)

// 用户列表查询
const userQueryForm = reactive({
  keyword: '',
  role: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const userList = ref([])
const userTotal = ref(0)

// 日志列表查询
const logQueryForm = reactive({
  moduleName: '',
  operationType: '',
  resultStatus: '',
  pageNum: 1,
  pageSize: 10
})

const logList = ref([])
const logTotal = ref(0)

// 表单数据
const formData = reactive({
  jobName: '',
  companyName: '',
  city: '',
  salaryMin: 0,
  salaryMax: 0,
  education: '本科',
  experience: '不限',
  skillTags: '',
  jobDescription: '',
  status: 1
})

const formRules = {
  jobName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }]
}

// Tab 切换时加载数据
watch(activeTab, (newTab) => {
  if (newTab === 'users') {
    loadUserList()
  } else if (newTab === 'logs') {
    loadLogList()
  }
})

// 加载看板数据
async function loadDashboard() {
  try {
    const res = await fetchAdminDashboard()
    if (res.code === 200) {
      const data = res.data
      statsList.value = [
        { label: '总岗位数', value: data.totalJobs || 0 },
        { label: '用户数', value: data.totalUsers || 0 },
        { label: '简历数', value: data.totalResumes || 0 },
        { label: '今日新增', value: data.todayNew || 0 }
      ]
    }
  } catch (e) {
    console.error('加载看板失败', e)
  }
}

// 加载岗位列表
async function loadJobList() {
  loading.value = true
  try {
    const res = await fetchAdminJobList(queryForm)
    if (res.code === 200) {
      jobList.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载岗位列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.pageNum = 1
  loadJobList()
}

function handleReset() {
  queryForm.keyword = ''
  queryForm.companyName = ''
  queryForm.city = ''
  queryForm.education = ''
  queryForm.status = null
  queryForm.pageNum = 1
  loadJobList()
}

function handleSizeChange(size) {
  queryForm.pageSize = size
  loadJobList()
}

function handlePageChange(page) {
  queryForm.pageNum = page
  loadJobList()
}

// 加载用户列表
async function loadUserList() {
  userLoading.value = true
  try {
    const res = await fetchAdminUserList(userQueryForm)
    if (res.code === 200) {
      userList.value = res.data.records
      userTotal.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载用户列表失败')
    console.error(e)
  } finally {
    userLoading.value = false
  }
}

function handleUserSearch() {
  userQueryForm.pageNum = 1
  loadUserList()
}

function handleUserReset() {
  userQueryForm.keyword = ''
  userQueryForm.role = ''
  userQueryForm.status = null
  userQueryForm.pageNum = 1
  loadUserList()
}

function handleUserSizeChange(size) {
  userQueryForm.pageSize = size
  loadUserList()
}

function handleUserPageChange(page) {
  userQueryForm.pageNum = page
  loadUserList()
}

async function handleUserStatusChange(id, status) {
  try {
    await toggleUserStatus(id, status)
    ElMessage.success('用户状态更新成功')
  } catch (e) {
    ElMessage.error('用户状态更新失败')
    console.error(e)
    loadUserList()
  }
}

async function handleUserDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteAdminUser(row.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }
}

// 加载日志列表
async function loadLogList() {
  logLoading.value = true
  try {
    const res = await fetchAdminLogList(logQueryForm)
    if (res.code === 200) {
      logList.value = res.data.records
      logTotal.value = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载日志列表失败')
    console.error(e)
  } finally {
    logLoading.value = false
  }
}

function handleLogSearch() {
  logQueryForm.pageNum = 1
  loadLogList()
}

function handleLogReset() {
  logQueryForm.moduleName = ''
  logQueryForm.operationType = ''
  logQueryForm.resultStatus = ''
  logQueryForm.pageNum = 1
  loadLogList()
}

function handleLogSizeChange(size) {
  logQueryForm.pageSize = size
  loadLogList()
}

function handleLogPageChange(page) {
  logQueryForm.pageNum = page
  loadLogList()
}

// 数据清理
async function handleClean(type) {
  const typeMap = {
    jobs: '岗位数据',
    users: '用户数据',
    resumes: '简历数据',
    logs: '系统日志',
    all: '所有数据'
  }
  try {
    await ElMessageBox.confirm(
      `确定要清理${typeMap[type]}吗？此操作不可恢复！`,
      '危险操作确认',
      { type: 'error', confirmButtonClass: 'el-button--danger' }
    )
    await cleanAdminData(type)
    ElMessage.success('清理成功')
    loadDashboard()
    if (type === 'jobs' || type === 'all') loadJobList()
    if (type === 'users' || type === 'all') loadUserList()
    if (type === 'logs' || type === 'all') loadLogList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('清理失败')
      console.error(e)
    }
  }
}

// 打开新增弹窗
function openAddDialog() {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增岗位'
  Object.assign(formData, {
    jobName: '',
    companyName: '',
    city: '',
    salaryMin: 0,
    salaryMax: 0,
    education: '本科',
    experience: '不限',
    skillTags: '',
    jobDescription: '',
    status: 1
  })
  dialogVisible.value = true
}

// 打开编辑弹窗
function openEditDialog(row) {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑岗位'
  Object.assign(formData, {
    jobName: row.jobName,
    companyName: row.companyName,
    city: row.city,
    salaryMin: row.salaryMin,
    salaryMax: row.salaryMax,
    education: row.education,
    experience: row.experience,
    skillTags: row.skillTags,
    jobDescription: row.jobDescription,
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value) {
        await updateAdminJob(editingId.value, formData)
        ElMessage.success('修改成功')
      } else {
        await addAdminJob(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadJobList()
    } catch (e) {
      ElMessage.error(isEdit.value ? '修改失败' : '新增失败')
      console.error(e)
    }
  })
}

// 删除岗位
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除岗位 "${row.jobName}" 吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteAdminJob(row.id)
    ElMessage.success('删除成功')
    loadJobList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(e)
    }
  }
}

// 岗位状态切换
async function handleStatusChange(id, status) {
  try {
    await toggleJobStatus(id, status)
    ElMessage.success('状态更新成功')
  } catch (e) {
    ElMessage.error('状态更新失败')
    console.error(e)
    loadJobList()
  }
}

// CSV 导入
async function handleFileChange(file) {
  try {
    const res = await importJobs(file.raw)
    if (res.code === 200) {
      ElMessage.success(`成功导入 ${res.data} 条岗位数据`)
      loadJobList()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (e) {
    ElMessage.error('导入失败')
    console.error(e)
  }
}

onMounted(() => {
  loadDashboard()
  loadJobList()
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.stat-card {
  text-align: center;
  margin-bottom: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  margin-top: 8px;
  color: #666;
}

.content-section {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.job-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.job-form .el-form-item {
  margin-bottom: 18px;
}

.job-form .el-input,
.job-form .el-input-number,
.job-form .el-select,
.job-form .el-textarea {
  width: 100%;
}

.job-form .el-input-number :deep(.el-input__inner) {
  text-align: left;
}

.clean-card {
  text-align: center;
}

.clean-card .el-card__header {
  font-weight: bold;
}

.clean-desc {
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
  min-height: 40px;
}
</style>
