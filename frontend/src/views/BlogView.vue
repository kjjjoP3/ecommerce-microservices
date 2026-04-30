<script setup>
import { onMounted, ref } from "vue";
import { getBlogPosts } from "../services/blogService";

const posts = ref([]);

onMounted(async () => {
  posts.value = await getBlogPosts();
});
</script>

<template>
  <main class="container page">
    <h1>Blog</h1>
    <article v-for="post in posts" :key="post.id" class="card">
      <h2>{{ post.title }}</h2>
      <p>{{ post.description }}</p>
      <small>{{ post.date }}</small>
      <div class="blog-action">
        <RouterLink class="btn-secondary" :to="`/blog/${post.id}`">Read More</RouterLink>
      </div>
    </article>
  </main>
</template>
