import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import WorkspacePage from '../views/WorkspacePage.vue'

const routes = [
  { path: '/', name: 'Home', component: HomePage },
  { path: '/novel/:id', name: 'Workspace', component: WorkspacePage, props: true }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
