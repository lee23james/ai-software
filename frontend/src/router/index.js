import { createRouter, createWebHistory } from 'vue-router'

const MainLayout = () => import('@/layouts/MainLayout.vue')
const LoginView = () => import('@/views/LoginView.vue')
const DashboardView = () => import('@/views/DashboardView.vue')
const JobListView = () => import('@/views/JobListView.vue')
const JobDetailView = () => import('@/views/JobDetailView.vue')
const AnalysisView = () => import('@/views/AnalysisView.vue')
const ResumeView = () => import('@/views/ResumeView.vue')
const AdminView = () => import('@/views/AdminView.vue')

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardView,
        meta: { title: '首页看板' }
      },
      {
        path: 'jobs',
        name: 'jobs',
        component: JobListView,
        meta: { title: '岗位列表' }
      },
      {
        path: 'jobs/:id',
        name: 'job-detail',
        component: JobDetailView,
        meta: { title: '岗位详情' }
      },
      {
        path: 'analysis',
        name: 'analysis',
        component: AnalysisView,
        meta: { title: '数据分析' }
      },
      {
        path: 'resume',
        name: 'resume',
        component: ResumeView,
        meta: { title: '简历辅助' }
      },
      {
        path: 'admin',
        name: 'admin',
        component: AdminView,
        meta: { title: '后台管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 招聘数据分析平台`
    : '招聘数据分析平台'
  return true
})

export default router
