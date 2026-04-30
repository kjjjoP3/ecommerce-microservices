<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../services/api";
const username = ref("admin");
const password = ref("password");
const router = useRouter();
const login = async () => {
  const res = await api.post("/auth/api/v1/auth/login", { username: username.value, password: password.value });
  localStorage.setItem("token", res.data.accessToken);
  router.push("/products");
};
</script>
<template>
  <div><input v-model="username" placeholder="username"/><input v-model="password" placeholder="password" type="password"/><button @click="login">Login</button></div>
</template>
