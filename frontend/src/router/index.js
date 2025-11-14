import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import TextWorkspace from "@/views/TextWorkspace.vue";

const routes = [
  {
    path: "/",
    name: "dashboard",
    component: DashboardView
  },
  {
    path: "/texts/:id",
    name: "text-workspace",
    component: TextWorkspace,
    props: true
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
