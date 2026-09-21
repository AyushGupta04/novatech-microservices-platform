import { api } from './api';
import { ApiResponse, Cart } from '../types';

export const cartService = {
  async getCart(): Promise<Cart> {
    const response = await api.get<ApiResponse<Cart>>('/api/v1/cart');
    return response.data.data;
  },

  async addToCart(productId: number, quantity: number): Promise<Cart> {
    const response = await api.post<ApiResponse<Cart>>('/api/v1/cart/items', {
      productId,
      quantity,
    });
    return response.data.data;
  },

  async updateItemQuantity(itemId: number, quantity: number): Promise<Cart> {
    const response = await api.put<ApiResponse<Cart>>(`/api/v1/cart/items/${itemId}`, {
      quantity,
    });
    return response.data.data;
  },

  async removeItem(itemId: number): Promise<Cart> {
    const response = await api.delete<ApiResponse<Cart>>(`/api/v1/cart/items/${itemId}`);
    return response.data.data;
  },

  async clearCart(): Promise<void> {
    await api.delete('/api/v1/cart');
  },
};
