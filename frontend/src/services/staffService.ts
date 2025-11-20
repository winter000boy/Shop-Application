// Staff service - API calls for staff management

import apiService from './api';
import { API_ENDPOINTS } from '../utils/constants';
import { Staff, PaginatedResponse, PaginationParams } from '../types';

class StaffService {
  async getStaff(params?: PaginationParams): Promise<PaginatedResponse<Staff>> {
    return await apiService.get<PaginatedResponse<Staff>>(
      API_ENDPOINTS.STAFF.BASE,
      { params }
    );
  }

  async getStaffById(id: number): Promise<Staff> {
    return await apiService.get<Staff>(API_ENDPOINTS.STAFF.BY_ID(id));
  }
}

export const staffService = new StaffService();
export default staffService;
