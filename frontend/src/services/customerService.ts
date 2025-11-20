// Customer service for API calls

import { apiService } from './api';
import { API_ENDPOINTS } from '../utils/constants';
import {
  CustomerResponse,
  CustomerRequest,
  RepairOrderSummary,
  PaginatedResponse,
} from '../types';

export interface CustomerSearchParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
  search?: string;
  searchBy?: string;
}

class CustomerService {
  /**
   * Get all customers with pagination and search
   */
  async getCustomers(
    params: CustomerSearchParams = {}
  ): Promise<PaginatedResponse<CustomerResponse>> {
    const queryParams = new URLSearchParams();

    if (params.page !== undefined) queryParams.append('page', params.page.toString());
    if (params.size !== undefined) queryParams.append('size', params.size.toString());
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.sortDir) queryParams.append('sortDir', params.sortDir);
    if (params.search) queryParams.append('search', params.search);
    if (params.searchBy) queryParams.append('searchBy', params.searchBy);

    const url = `${API_ENDPOINTS.CUSTOMERS.BASE}?${queryParams.toString()}`;
    return apiService.get<PaginatedResponse<CustomerResponse>>(url);
  }

  /**
   * Get customer by ID
   */
  async getCustomerById(id: number): Promise<CustomerResponse> {
    return apiService.get<CustomerResponse>(API_ENDPOINTS.CUSTOMERS.BY_ID(id));
  }

  /**
   * Create a new customer
   */
  async createCustomer(data: CustomerRequest): Promise<CustomerResponse> {
    return apiService.post<CustomerResponse>(API_ENDPOINTS.CUSTOMERS.BASE, data);
  }

  /**
   * Update an existing customer
   */
  async updateCustomer(id: number, data: CustomerRequest): Promise<CustomerResponse> {
    return apiService.put<CustomerResponse>(API_ENDPOINTS.CUSTOMERS.BY_ID(id), data);
  }

  /**
   * Delete a customer
   */
  async deleteCustomer(id: number): Promise<void> {
    return apiService.delete<void>(API_ENDPOINTS.CUSTOMERS.BY_ID(id));
  }

  /**
   * Get customer repair order history
   */
  async getCustomerOrders(id: number): Promise<RepairOrderSummary[]> {
    return apiService.get<RepairOrderSummary[]>(
      API_ENDPOINTS.CUSTOMERS.ORDERS(id)
    );
  }
}

export const customerService = new CustomerService();
export default customerService;
