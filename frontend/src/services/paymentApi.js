import api from "./api";

export const createPayment = async ({ productId, quantity, amount, username, method = "MANUAL" }) => {
  const response = await api.post(
    `/orders/api/v1/orders?productId=${productId}&quantity=${quantity}&amount=${amount}&username=${encodeURIComponent(username || "")}&method=${encodeURIComponent(method)}`
  );
  return response.data;
};

export const getPaymentTransaction = async (transactionRef) => {
  const response = await api.get(`/payments/api/v1/payments/${transactionRef}`);
  return response.data;
};
