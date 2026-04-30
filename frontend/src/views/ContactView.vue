<script setup>
import { reactive, ref } from "vue";
import { submitContactForm } from "../services/api";

const form = reactive({
  name: "",
  email: "",
  message: ""
});

const errors = reactive({
  name: "",
  email: "",
  message: ""
});

const statusMessage = ref("");
const isError = ref(false);

const validate = () => {
  errors.name = form.name.trim() ? "" : "Name is required.";
  errors.email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) ? "" : "Valid email is required.";
  errors.message = form.message.trim().length >= 10 ? "" : "Message must be at least 10 characters.";
  return !errors.name && !errors.email && !errors.message;
};

const submitForm = async () => {
  statusMessage.value = "";
  isError.value = false;

  if (!validate()) {
    isError.value = true;
    statusMessage.value = "Please fix form validation errors.";
    return;
  }

  try {
    const result = await submitContactForm(form);
    statusMessage.value = result.message || "Contact form submitted successfully.";
    form.name = "";
    form.email = "";
    form.message = "";
  } catch (error) {
    isError.value = true;
    statusMessage.value = "Submission failed. Please try again.";
  }
};
</script>

<template>
  <main class="container page">
    <h1>Contact</h1>
    <form class="card form" @submit.prevent="submitForm">
      <label>
        Name
        <input v-model="form.name" type="text" />
        <small class="error">{{ errors.name }}</small>
      </label>

      <label>
        Email
        <input v-model="form.email" type="email" />
        <small class="error">{{ errors.email }}</small>
      </label>

      <label>
        Message
        <textarea v-model="form.message" rows="5"></textarea>
        <small class="error">{{ errors.message }}</small>
      </label>

      <button class="btn-primary" type="submit">Send Message</button>
      <p :class="isError ? 'status-error' : 'status-success'">{{ statusMessage }}</p>
    </form>
  </main>
</template>
