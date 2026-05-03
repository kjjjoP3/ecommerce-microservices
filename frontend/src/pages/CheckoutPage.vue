<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getCart } from "../services/api";
import { createPayment } from "../services/paymentApi";
import { isAuthenticated, parseJwt, getToken } from "../utils/auth";
import { getGuestCart } from "../utils/cartStore";

const router = useRouter();
const message = ref("");
const loading = ref(false);
const cartItems = ref([]);

const authed = computed(() => isAuthenticated());
const role = computed(() => parseJwt(getToken())?.role || "USER");

const subtotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + Number(item.amount || 0) * Number(item.quantity || 1), 0)
);

onMounted(async () => {
  if (authed.value) {
    try {
      cartItems.value = await getCart();
    } catch {
      cartItems.value = [];
    }
  } else {
    cartItems.value = getGuestCart();
  }
});

const pay = async () => {
  if (!cartItems.value.length) {
    message.value = "Giỏ hàng đang trống.";
    return;
  }

  loading.value = true;
  message.value = "";
  try {
    const firstItem = cartItems.value[0];
    const payment = await createPayment({
      productId: firstItem.productId,
      quantity: firstItem.quantity,
      amount: subtotal.value,
      username: JSON.parse(localStorage.getItem("userProfile") || "null")?.username,
      method: "MANUAL"
    });

    if (payment.status === "PAID") {
      await router.push({ path: "/payment-success", query: { transactionRef: payment.transactionRef } });
      return;
    }

    await router.push({ path: "/payment-failed", query: { transactionRef: payment.transactionRef } });
  } catch (error) {
    message.value = error?.response?.data?.message || "Thanh toán thất bại.";
  } finally {
    loading.value = false;
  }
};

const payWithVnPay = () => {
  if (!cartItems.value.length) {
    message.value = "Giỏ hàng đang trống.";
    return;
  }

  const amount = Math.round(Number(subtotal.value) * 100);
  const txnRef = `TXN${Date.now()}`;
  const vnpUrl = new URL("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
  vnpUrl.searchParams.set("vnp_Version", "2.1.0");
  vnpUrl.searchParams.set("vnp_Command", "pay");
  vnpUrl.searchParams.set("vnp_TmnCode", "DEMO1234");
  vnpUrl.searchParams.set("vnp_Amount", String(amount));
  vnpUrl.searchParams.set("vnp_CurrCode", "VND");
  vnpUrl.searchParams.set("vnp_TxnRef", txnRef);
  vnpUrl.searchParams.set("vnp_OrderInfo", "Thanh toan don hang Ecommerce");
  vnpUrl.searchParams.set("vnp_OrderType", "other");
  vnpUrl.searchParams.set("vnp_Locale", "vn");
  vnpUrl.searchParams.set("vnp_ReturnUrl", `${window.location.origin}/payment-return`);
  window.location.href = vnpUrl.toString();
};
</script>

<template>
  <main class="container page checkout-page">
    <section class="panel checkout-panel">
      <p class="eyebrow">Checkout</p>
      <h1>Thanh toán đơn hàng</h1>

      <p v-if="!authed" class="warn-msg">
        Bạn cần <RouterLink to="/login">đăng nhập</RouterLink> để thanh toán.
      </p>
      <p v-else>Vai trò hiện tại: {{ role }}</p>

      <div class="checkout-summary">
        <div>
          <span>Số món</span>
          <strong>{{ cartItems.length }}</strong>
        </div>
        <div>
          <span>Tổng tiền</span>
          <strong>{{ subtotal.toLocaleString('vi-VN') }} đ</strong>
        </div>
      </div>

      <div v-if="cartItems.length" class="cart-preview">
        <div v-for="item in cartItems" :key="item.productId" class="preview-item">
          <span>{{ item.name }}</span>
          <span>x{{ item.quantity }}</span>
          <strong>{{ (Number(item.amount) * Number(item.quantity)).toLocaleString('vi-VN') }} đ</strong>
        </div>
      </div>
      <p v-else class="empty-msg">Giỏ hàng đang trống.</p>

      <div class="actions" v-if="authed && cartItems.length">
        <button class="btn-primary" :disabled="loading" @click="pay">
          {{ loading ? 'Đang xử lý...' : 'Pay Now' }}
        </button>
        <button class="btn-vnpay" @click="payWithVnPay">Thanh toán VNPAY</button>
      </div>

      <p v-if="message" class="result-msg" :class="{ success: message.includes('thành công') || message.includes('PAID') }">
        {{ message }}
      </p>
    </section>
  </main>
</template>
