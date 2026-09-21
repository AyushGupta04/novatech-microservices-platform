import { api } from './api';
import { ApiResponse, CreateOrderRequest, Order, OrderStatus, PageResponse } from '../types';

export const orderService = {
  async createOrder(data: CreateOrderRequest): Promise<Order> {
    const response = await api.post<ApiResponse<Order>>('/api/v1/orders', data);
    return response.data.data;
  },

  async getMyOrders(): Promise<Order[]> {
    const response = await api.get<ApiResponse<PageResponse<Order> | Order[]>>('/api/v1/orders');
    const data = response.data.data;
    if (Array.isArray(data)) {
      return data;
    }
    return data?.content || [];
  },

  async getOrderById(id: number): Promise<Order> {
    const response = await api.get<ApiResponse<Order>>(`/api/v1/orders/${id}`);
    return response.data.data;
  },

  async cancelOrder(id: number): Promise<Order> {
    const response = await api.post<ApiResponse<Order>>(`/api/v1/orders/${id}/cancel`);
    return response.data.data;
  },

  async getAllOrders(): Promise<Order[]> {
    const response = await api.get<ApiResponse<PageResponse<Order> | Order[]>>('/api/v1/orders/admin');
    const data = response.data.data;
    if (Array.isArray(data)) {
      return data;
    }
    return data?.content || [];
  },

  async updateOrderStatus(id: number, status: OrderStatus): Promise<Order> {
    const response = await api.put<ApiResponse<Order>>(`/api/v1/orders/admin/${id}/status`, {
      status,
    });
    return response.data.data;
  },
};
