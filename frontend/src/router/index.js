import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AppLayout from '../components/AppLayout.vue'

const routes = [
  { path: '/login', component: LoginView },
  {
    path: '/', component: AppLayout, redirect: '/dashboard', children: [
      { path: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { roles: ['ADMIN', 'DISPATCHER', 'ACCEPTOR'] } },
      { path: 'equipment', component: () => import('../views/EquipmentView.vue') },
      { path: 'work-orders', component: () => import('../views/WorkOrdersView.vue') },
      { path: 'work-orders/:id', component: () => import('../views/WorkOrderDetailView.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(to => {
  if (to.path === '/login') return true
  if (!localStorage.getItem('token')) return '/login'
  const roles = to.meta.roles
  const role = localStorage.getItem('roleCode')
  if (roles && !roles.includes(role)) return '/work-orders'
  return true
})

export default router

