<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getBlogPostById } from "../services/blogService";

const route = useRoute();
const post = ref(null);

onMounted(async () => {
  post.value = await getBlogPostById(route.params.id);
});
</script>

<template>
  <main class="container page">
    <article v-if="post" class="card">
      <h1>{{ post.title }}</h1>
      <small>{{ post.date }}</small>
      <p>{{ post.content }}</p>
      <RouterLink class="btn-secondary" to="/blog">Back to Blog</RouterLink>
    </article>
    <p v-else>Post not found.</p>
  </main>
</template>
