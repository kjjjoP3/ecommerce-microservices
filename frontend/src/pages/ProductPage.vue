<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getProducts } from "../services/api";
const products = ref([]);
const router = useRouter();
onMounted(async () => { products.value = await getProducts(); });
const addCart = (p) => {
  const cart = JSON.parse(localStorage.getItem("cart") || "[]");
  cart.push({ productId: p.id, name: p.name, amount: p.price, quantity: 1 });
  localStorage.setItem("cart", JSON.stringify(cart));
  router.push("/cart");
};
</script>
<template><div><div v-for="p in products" :key="p.id">{{ p.name }} - {{ p.price }} <button @click="addCart(p)">Add</button></div></div></template>
