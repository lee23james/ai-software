<template>
  <div class="page-grid two-columns">
    <el-card class="content-section" shadow="never">
      <template #header>1) 账号求职意向</template>
      <p class="section-hint">
        保存在个人档案（可多选），表示你整体想投的岗位方向，与下面「本份简历」独立。
        当前匹配仍会结合技能与岗位 JD 在全库在招岗位中打分，不会仅按此名单过滤。
      </p>
      <el-form label-position="top">
        <el-form-item label="用户ID">
          <el-input :model-value="userIdText" disabled />
        </el-form-item>
        <el-form-item label="意向岗位（逗号分隔，越靠前优先级越高）">
          <el-input v-model="interestText" placeholder="例如：数据分析师,Java后端开发,算法工程师" />
        </el-form-item>
        <el-form-item label="意向月薪（左右拖动区间，元/月）">
          <div class="salary-slider-wrap">
            <el-slider
              v-model="salaryRange"
              range
              :min="SALARY_SLIDER_MIN"
              :max="SALARY_SLIDER_MAX"
              :step="SALARY_STEP"
            />
            <p class="salary-range-text">
              {{ salaryRange[0].toLocaleString() }} — {{ salaryRange[1].toLocaleString() }} 元/月
            </p>
          </div>
        </el-form-item>
        <el-button type="primary" :loading="savingInterest" @click="handleSaveInterest">保存到个人档案</el-button>
      </el-form>
      <el-divider />
      <p v-if="interestList.length === 0">暂无兴趣岗位</p>
      <el-tag
        v-for="(item, idx) in interestList"
        :key="`${item.jobName}-${idx}`"
        class="interest-tag"
      >
        {{ item.jobName }} (P{{ item.priority }})
      </el-tag>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>2) 本份简历：上传与匹配</template>
      <el-form label-position="top">
        <el-form-item label="简历名称">
          <el-input v-model="resumeForm.resumeName" placeholder="例如：数据分析师简历-2026" />
        </el-form-item>
        <el-form-item label="本简历适配岗位（选填）">
          <div class="target-job-row">
            <el-input
              v-model="resumeForm.targetJobName"
              placeholder="例如：数据分析师，或与上方首选意向保持一致"
            />
            <el-button :disabled="!firstInterestName" plain @click="fillTargetFromInterest">
              填入首选求职意向
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="技能（逗号分隔）">
          <el-input v-model="skillsText" placeholder="例如：SQL,Python,Tableau,Java" />
        </el-form-item>
        <el-form-item label="简历文件">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :show-file-list="true"
            accept=".pdf,.txt,application/pdf,text/plain"
          >
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <div class="upload-actions">
          <el-button type="primary" :loading="matching" @click="handleUploadAndMatch">上传并匹配</el-button>
          <el-button
            v-if="latestResumeId"
            type="success"
            plain
            :disabled="matching"
            @click="goHistoryDetail"
          >
            查看本条历史详情
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>

  <el-card class="content-section" shadow="never">
    <template #header>3) 匹配结果</template>
    <p v-if="latestResumeId" class="section-hint">
      以下为刚完成匹配的结果；也可点击上方「查看本条历史详情」打开完整解析与原文。薪资分按已保存的意向月薪与岗位月薪区间的重叠比例计算；未保存意向或岗位无有效月薪时该项为中性分
      60。
    </p>
    <el-table :data="matchList" style="width: 100%">
      <el-table-column prop="jobName" label="岗位" />
      <el-table-column prop="companyName" label="公司" />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="skillScore" label="技能分" width="100" />
      <el-table-column prop="experienceScore" label="经验分" width="100" />
      <el-table-column prop="educationScore" label="学历分" width="100" />
      <el-table-column prop="salaryScore" label="薪资分" width="100" />
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { fetchResumeMatches, triggerResumeMatch, uploadResume } from '@/api/resume'
import { fetchInterestJobs, saveInterestJobs } from '@/api/interest'

const userStore = useUserStore()
const router = useRouter()
const savingInterest = ref(false)
const matching = ref(false)
const interestText = ref('')
const skillsText = ref('SQL,Python')
const interestList = ref([])
const matchList = ref([])
const latestResumeId = ref(null)
const selectedFile = ref(null)

const resumeForm = reactive({
  resumeName: '默认简历',
  targetJobName: ''
})

const SALARY_SLIDER_MIN = 5000
const SALARY_SLIDER_MAX = 80000
const SALARY_STEP = 1000
/** 双端滑块：[下限, 上限]，与岗位 salary_min/max（月薪，元）一致 */
const salaryRange = ref([12000, 35000])

const DRAFT_PREFIX = 'jobPlatform_resumeAssistant_'

function draftStorageKey() {
  return `${DRAFT_PREFIX}${userStore.userId || 0}`
}

function readDraft() {
  try {
    const raw = localStorage.getItem(draftStorageKey())
    if (!raw) {
      return null
    }
    return JSON.parse(raw)
  } catch {
    return null
  }
}

let saveDraftTimer
function scheduleSaveDraft() {
  clearTimeout(saveDraftTimer)
  saveDraftTimer = setTimeout(() => {
    try {
      const payload = {
        interestText: interestText.value,
        salaryRange: [...salaryRange.value],
        resumeName: resumeForm.resumeName,
        targetJobName: resumeForm.targetJobName,
        skillsText: skillsText.value,
        matchList: matchList.value,
        latestResumeId: latestResumeId.value
      }
      localStorage.setItem(draftStorageKey(), JSON.stringify(payload))
    } catch {
      /* 存储配额等 */
    }
  }, 400)
}

function applyDraft(draft) {
  if (!draft || typeof draft !== 'object') {
    return
  }
  if (typeof draft.interestText === 'string') {
    interestText.value = draft.interestText
  }
  if (Array.isArray(draft.salaryRange) && draft.salaryRange.length === 2) {
    const a = Number(draft.salaryRange[0])
    const b = Number(draft.salaryRange[1])
    if (Number.isFinite(a) && Number.isFinite(b) && a <= b) {
      salaryRange.value = [a, b]
    }
  }
  if (typeof draft.resumeName === 'string') {
    resumeForm.resumeName = draft.resumeName
  }
  if (typeof draft.targetJobName === 'string') {
    resumeForm.targetJobName = draft.targetJobName
  }
  if (typeof draft.skillsText === 'string') {
    skillsText.value = draft.skillsText
  }
  if (Array.isArray(draft.matchList)) {
    matchList.value = draft.matchList
  }
  if (draft.latestResumeId != null && draft.latestResumeId !== '') {
    const n = Number(draft.latestResumeId)
    if (Number.isFinite(n) && n > 0) {
      latestResumeId.value = n
    }
  }
}

watch(
  [
    interestText,
    skillsText,
    latestResumeId,
    matchList,
    salaryRange,
    () => resumeForm.resumeName,
    () => resumeForm.targetJobName
  ],
  () => scheduleSaveDraft(),
  { deep: true, flush: 'post' }
)

watch(
  () => userStore.userId,
  async (id) => {
    if (!id) {
      interestList.value = []
      interestText.value = ''
      return
    }
    await loadInterest()
    applyDraftThenSyncTagsIfNeeded()
  }
)

const firstInterestName = computed(() => interestList.value[0]?.jobName || '')

const userIdText = computed(() => String(userStore.userId || '未登录'))

function ensureLogin() {
  if (!userStore.userId) {
    ElMessage.warning('请先去登录页注册/登录')
    throw new Error('NO_LOGIN')
  }
}

function parseTextToList(text) {
  return text
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function clampSalaryRangeToSlider(lo, hi) {
  let a = Number(lo)
  let b = Number(hi)
  if (!Number.isFinite(a) || !Number.isFinite(b)) {
    return null
  }
  if (a > b) {
    ;[a, b] = [b, a]
  }
  a = Math.min(Math.max(a, SALARY_SLIDER_MIN), SALARY_SLIDER_MAX)
  b = Math.min(Math.max(b, SALARY_SLIDER_MIN), SALARY_SLIDER_MAX)
  if (a > b) {
    b = a
  }
  return [a, b]
}

/** 兼容旧接口 data 为岗位数组；新接口为 { jobs, expectedSalaryMin, expectedSalaryMax } */
function normalizeInterestApiData(raw) {
  if (raw == null) {
    return { jobs: [], expectedSalaryMin: null, expectedSalaryMax: null }
  }
  if (Array.isArray(raw)) {
    return { jobs: raw, expectedSalaryMin: null, expectedSalaryMax: null }
  }
  const jobArr = raw.jobs ?? raw.Jobs
  const jobs = Array.isArray(jobArr) ? jobArr : []
  return {
    jobs,
    expectedSalaryMin: raw.expectedSalaryMin ?? raw.expected_salary_min ?? null,
    expectedSalaryMax: raw.expectedSalaryMax ?? raw.expected_salary_max ?? null
  }
}

/** 下方标签与输入框一致：在接口未返回 jobs 但输入里仍有逗号分隔岗位时使用 */
function syncInterestTagsFromInterestText() {
  const names = parseTextToList(interestText.value)
  if (names.length === 0) {
    return
  }
  interestList.value = names.map((jobName, index) => ({
    jobName,
    priority: Math.min(index + 1, 5)
  }))
}

async function loadInterest() {
  if (!userStore.userId) {
    return
  }
  try {
    const result = await fetchInterestJobs(userStore.userId)
    const { jobs, expectedSalaryMin, expectedSalaryMax } = normalizeInterestApiData(result?.data)
    interestList.value = jobs
    interestText.value = jobs.map((item) => item.jobName).join(',')
    const clamped = clampSalaryRangeToSlider(expectedSalaryMin, expectedSalaryMax)
    if (clamped) {
      salaryRange.value = clamped
    }
  } catch {
    /* 请求失败时保留当前界面状态 */
  }
}

function applyDraftThenSyncTagsIfNeeded() {
  const draft = readDraft()
  if (draft) {
    applyDraft(draft)
  }
  // 草稿里曾存过空字符串时，不要用空输入盖住接口刚拉下来的岗位
  if (interestList.value.length > 0 && !interestText.value.trim()) {
    interestText.value = interestList.value.map((item) => item.jobName).join(',')
  }
  if (interestList.value.length === 0) {
    syncInterestTagsFromInterestText()
  }
}

async function handleSaveInterest() {
  ensureLogin()
  const jobs = parseTextToList(interestText.value).map((jobName, index) => ({
    jobName,
    priority: Math.min(index + 1, 5)
  }))
  if (jobs.length === 0) {
    ElMessage.warning('请至少填写一个兴趣岗位')
    return
  }
  savingInterest.value = true
  try {
    await saveInterestJobs({
      userId: userStore.userId,
      jobs,
      expectedSalaryMin: Math.round(salaryRange.value[0]),
      expectedSalaryMax: Math.round(salaryRange.value[1])
    })
    ElMessage.success('兴趣岗位保存成功')
    await loadInterest()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    savingInterest.value = false
  }
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = null
}

function fillTargetFromInterest() {
  const name = firstInterestName.value
  if (!name) {
    ElMessage.warning('请先在上方保存至少一个求职意向')
    return
  }
  resumeForm.targetJobName = name
  ElMessage.success('已填入首选岗位：' + name)
}

function goHistoryDetail() {
  if (latestResumeId.value) {
    router.push(`/resume/history/${latestResumeId.value}`)
  }
}

async function handleUploadAndMatch() {
  ensureLogin()
  if (!resumeForm.resumeName.trim()) {
    ElMessage.warning('请填写简历名称')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先选择简历文件')
    return
  }
  matching.value = true
  try {
    const formData = new FormData()
    formData.append('userId', String(userStore.userId))
    formData.append('resumeName', resumeForm.resumeName.trim())
    formData.append('targetJobName', resumeForm.targetJobName.trim())
    formData.append('skillsText', skillsText.value)
    formData.append('file', selectedFile.value)
    const createResult = await uploadResume(formData)
    latestResumeId.value = createResult.data.resumeId
    await triggerResumeMatch(latestResumeId.value, 20)
    const listResult = await fetchResumeMatches(latestResumeId.value)
    matchList.value = listResult.data || []
    ElMessage.success(`匹配完成，本页下方已展示 ${matchList.value.length} 条结果`)
  } catch (error) {
    const msg =
      error?.response?.data?.message || error?.message || '上传或匹配失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    matching.value = false
  }
}

onMounted(async () => {
  await loadInterest()
  applyDraftThenSyncTagsIfNeeded()
})

onBeforeUnmount(() => {
  clearTimeout(saveDraftTimer)
})
</script>

<style scoped>
.section-hint {
  margin: 0 0 14px;
  font-size: 13px;
  color: #667085;
  line-height: 1.55;
}

.target-job-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.target-job-row .el-input {
  flex: 1;
  min-width: 180px;
}

.upload-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.salary-slider-wrap {
  width: 100%;
  max-width: 520px;
  padding: 4px 0 0;
}

.salary-range-text {
  margin: 8px 0 0;
  font-size: 13px;
  color: #475467;
}
</style>
