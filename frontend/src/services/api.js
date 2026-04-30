import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 8000
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const getProducts = async () => {
  const response = await api.get("/products/api/v1/products");
  return response.data;
};

export const submitContactForm = async (payload) => {
  try {
    const response = await api.post("/users/api/v1/contact", payload);
    return response.data;
  } catch (error) {
    await new Promise((resolve) => setTimeout(resolve, 500));
    return { message: "Mock contact request submitted successfully." };
  }
};

export default api;
