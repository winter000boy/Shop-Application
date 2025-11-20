// Customer related types

import { OrderStatus } from './order.types';

export interface CustomerResponse {
  id: number;
  name: string;
  phoneNumber: string;
  email?: string;
  address?: string;
  repairOrderCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerRequest {
  name: string;
  phoneNumber: string;
  email?: string;
  address?: string;
}

export interface RepairOrderSummary {
  id: number;
  deviceModel: string;
  problemDescription: string;
  estimatedPrice: number;
  paidAmount: number;
  status: OrderStatus;
  repairDate: string;
  assignedStaffName?: string;
  warrantyDays?: number;
  createdAt: string;
  updatedAt: string;
}
