// Order service - API calls for repair orders

import apiService from './api';
import { API_ENDPOINTS } from '../utils/constants';
import { RepairOrder, OrderRequest, OrderStatusUpdateRequest, PaginatedResponse, PaginationParams } from '../types';

class OrderService {
  async getOrders(params?: PaginationParams): Promise<PaginatedResponse<RepairOrder>> {
    return await apiService.get<PaginatedResponse<RepairOrder>>(
      API_ENDPOINTS.ORDERS.BASE,
      { params }
    );
  }

  async getOrderById(id: number): Promise<RepairOrder> {
    return await apiService.get<RepairOrder>(API_ENDPOINTS.ORDERS.BY_ID(id));
  }

  async createOrder(data: OrderRequest): Promise<RepairOrder> {
    return await apiService.post<RepairOrder>(API_ENDPOINTS.ORDERS.BASE, data);
  }

  async updateOrder(id: number, data: OrderRequest): Promise<RepairOrder> {
    return await apiService.put<RepairOrder>(API_ENDPOINTS.ORDERS.BY_ID(id), data);
  }

  async deleteOrder(id: number): Promise<void> {
    return await apiService.delete<void>(API_ENDPOINTS.ORDERS.BY_ID(id));
  }

  async updateOrderStatus(id: number, status: OrderStatusUpdateRequest): Promise<RepairOrder> {
    return await apiService.put<RepairOrder>(API_ENDPOINTS.ORDERS.STATUS(id), status);
  }

  async uploadOrderImages(id: number, files: File[]): Promise<RepairOrder> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('images', file);
    });
    return await apiService.uploadFile<RepairOrder>(API_ENDPOINTS.ORDERS.IMAGES(id), formData);
  }

  async notifyCustomer(id: number): Promise<void> {
    return await apiService.post<void>(API_ENDPOINTS.ORDERS.NOTIFY(id));
  }
}

export const orderService = new OrderService();
export default orderService;
