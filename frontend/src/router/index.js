import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import WorkspacePage from '../views/WorkspacePage.vue'
import ConfigPage from '../views/ConfigPage.vue'

const routes = [
  { path: '/', name: 'Home', component: HomePage },
  { path: '/novel/:id', name: 'Workspace', component: WorkspacePage, props: true },
  { path: '/settings', name: 'Settings', component: ConfigPage }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
