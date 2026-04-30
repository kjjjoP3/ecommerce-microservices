<script setup>
import { onMounted, ref } from "vue";
import { getProducts } from "../services/api";

const featuredProducts = ref([]);

onMounted(async () => {
  try {
    const products = await getProducts();
    featuredProducts.value = products.slice(0, 4);
  } catch (error) {
    featuredProducts.value = [];
  }
});
</script>

<template>
  <main class="container page">
    <section class="hero">
      <h1>Modern Commerce, Built for Speed</h1>
      <p>Discover curated products and a secure checkout experience backed by microservices.</p>
      <RouterLink class="btn-primary" to="/products">Shop Now</RouterLink>
    </section>

    <section>
      <h2>Featured Products</h2>
      <div class="grid">
        <article v-for="product in featuredProducts" :key="product.id" class="card">
          <h3>{{ product.name }}</h3>
          <p class="price">${{ product.price }}</p>
        </article>
      </div>
      <p v-if="featuredProducts.length === 0">Products will appear here when API is available.</p>
    </section>
  </main>
</template>
