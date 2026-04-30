<script setup>
import { RouterLink } from "vue-router";
</script>

<template>
  <header class="navbar">
    <div class="container nav-wrap">
      <RouterLink class="brand" to="/">Ecom Platform</RouterLink>
      <nav class="nav-links">
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/products">Products</RouterLink>
        <RouterLink to="/blog">Blog</RouterLink>
        <RouterLink to="/about">About</RouterLink>
        <RouterLink to="/contact">Contact</RouterLink>
      </nav>
    </div>
  </header>
</template>
