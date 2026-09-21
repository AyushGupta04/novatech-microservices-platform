import { api } from './api';
import { ApiResponse, InventoryItem } from '../types';

export const inventoryService = {
  async getStockBySku(sku: string): Promise<InventoryItem> {
    const response = await api.get<ApiResponse<InventoryItem>>(`/api/v1/inventory/${sku}`);
    return response.data.data;
  },

  async updateStock(sku: string, quantity: number): Promise<InventoryItem> {
    const response = await api.put<ApiResponse<InventoryItem>>('/api/v1/inventory/stock', {
      sku,
      quantity,
    });
    return response.data.data;
  },

  async createInventory(sku: string, initialStock: number = 0): Promise<InventoryItem> {
    const response = await api.post<ApiResponse<InventoryItem>>('/api/v1/inventory', null, {
      params: { sku, initialStock },
    });
    return response.data.data;
  },
};
