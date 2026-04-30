<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
const cart = ref(JSON.parse(localStorage.getItem("cart") || "[]"));
</script>
<template><div><div v-for="item in cart" :key="item.productId">{{ item.name }} x {{ item.quantity }}</div><button @click="router.push('/checkout')">Checkout</button></div></template>
