import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import AboutView from "../views/AboutView.vue";
import BlogView from "../views/BlogView.vue";
import BlogDetailView from "../views/BlogDetailView.vue";
import ContactView from "../views/ContactView.vue";
import ProductPage from "../pages/ProductPage.vue";

const routes = [
  { path: "/", name: "home", component: HomeView },
  { path: "/products", name: "products", component: ProductPage },
  { path: "/about", name: "about", component: AboutView },
  { path: "/blog", name: "blog", component: BlogView },
  { path: "/blog/:id", name: "blog-detail", component: BlogDetailView, props: true },
  { path: "/contact", name: "contact", component: ContactView }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

export default router;
