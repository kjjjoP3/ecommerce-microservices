<template>
  <div class="app-shell">
    <NavbarMenu />
    <router-view />
    <SiteFooter />
  </div>
</template>

<script setup>
import NavbarMenu from "./components/NavbarMenu.vue";
import SiteFooter from "./components/SiteFooter.vue";
</script>
