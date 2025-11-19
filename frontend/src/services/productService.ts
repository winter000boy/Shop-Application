// Product service - API calls for products and marketplace

import apiService from './api';
import { API_ENDPOINTS } from '../utils/constants';
import { Product, ProductRequest, Category, PaginatedResponse, PaginationParams } from '../types';

class ProductService {
  async getProducts(params?: PaginationParams): Promise<PaginatedResponse<Product>> {
    return await apiService.get<PaginatedResponse<Product>>(
      API_ENDPOINTS.PRODUCTS.BASE,
      { params }
    );
  }

  async getProductById(id: number): Promise<Product> {
    return await apiService.get<Product>(API_ENDPOINTS.PRODUCTS.BY_ID(id));
  }

  async createProduct(data: ProductRequest): Promise<Product> {
    return await apiService.post<Product>(API_ENDPOINTS.PRODUCTS.BASE, data);
  }

  async updateProduct(id: number, data: ProductRequest): Promise<Product> {
    return await apiService.put<Product>(API_ENDPOINTS.PRODUCTS.BY_ID(id), data);
  }

  async deleteProduct(id: number): Promise<void> {
    return await apiService.delete<void>(API_ENDPOINTS.PRODUCTS.BY_ID(id));
  }

  async uploadProductImages(id: number, files: File[]): Promise<Product> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('images', file);
    });
    return await apiService.uploadFile<Product>(API_ENDPOINTS.PRODUCTS.IMAGES(id), formData);
  }

  async getMarketplaceProducts(params?: PaginationParams): Promise<PaginatedResponse<Product>> {
    return await apiService.get<PaginatedResponse<Product>>(
      API_ENDPOINTS.MARKETPLACE.PRODUCTS,
      { params }
    );
  }

  async getCategories(): Promise<Category[]> {
    return await apiService.get<Category[]>(API_ENDPOINTS.MARKETPLACE.CATEGORIES);
  }
}

export const productService = new ProductService();
export default productService;
