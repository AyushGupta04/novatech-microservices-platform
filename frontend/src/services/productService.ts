import { api } from './api';
import { ApiResponse, Category, PageResponse, Product } from '../types';

export interface ProductQueryParams {
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  imageUrl?: string;
  active?: boolean;
}

export const productService = {
  async getProducts(params?: ProductQueryParams): Promise<PageResponse<Product>> {
    const response = await api.get<ApiResponse<PageResponse<Product>>>('/api/v1/products', {
      params,
    });
    return response.data.data;
  },

  async getProductById(id: number): Promise<Product> {
    const response = await api.get<ApiResponse<Product>>(`/api/v1/products/${id}`);
    return response.data.data;
  },

  async getProductBySku(sku: string): Promise<Product> {
    const response = await api.get<ApiResponse<Product>>(`/api/v1/products/sku/${sku}`);
    return response.data.data;
  },

  async createProduct(data: CreateProductRequest): Promise<Product> {
    const response = await api.post<ApiResponse<Product>>('/api/v1/products', data);
    return response.data.data;
  },

  async updateProduct(id: number, data: Partial<CreateProductRequest>): Promise<Product> {
    const response = await api.put<ApiResponse<Product>>(`/api/v1/products/${id}`, data);
    return response.data.data;
  },

  async deleteProduct(id: number): Promise<void> {
    await api.delete(`/api/v1/products/${id}`);
  },

  async getCategories(): Promise<Category[]> {
    const response = await api.get<ApiResponse<Category[]>>('/api/v1/categories');
    return response.data.data;
  },

  async createCategory(data: { name: string; slug: string; description?: string }): Promise<Category> {
    const response = await api.post<ApiResponse<Category>>('/api/v1/categories', data);
    return response.data.data;
  },
};
