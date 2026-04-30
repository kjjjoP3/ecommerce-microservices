<script setup>
import api from "../services/api";
const pay = async () => {
  const cart = JSON.parse(localStorage.getItem("cart") || "[]");
  if (!cart.length) return;
  const item = cart[0];
  const res = await api.post(`/orders/api/v1/orders?productId=${item.productId}&quantity=${item.quantity}&amount=${item.amount}`);
  alert("Order Status: " + res.data.orderStatus);
};
</script>
<template><button @click="pay">Pay Now</button></template>
