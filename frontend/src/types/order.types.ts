// Order related types

export interface RepairOrder {
  id: number;
  shopId: number;
  customer: Customer;
  deviceModel: string;
  problemDescription: string;
  estimatedPrice: number;
  paidAmount: number;
  lockCode?: string;
  repairDate: string;
  accessories?: string;
  serialNumber?: string;
  assignedStaff?: Staff;
  status: OrderStatus;
  cashbackEnabled: boolean;
  cashbackAmount?: number;
  warrantyDays?: number;
  expenses?: string;
  images?: OrderImage[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderRequest {
  customerId: number;
  deviceModel: string;
  problemDescription: string;
  estimatedPrice: number;
  paidAmount: number;
  lockCode?: string;
  repairDate: string;
  accessories?: string;
  serialNumber?: string;
  assignedStaffId?: number;
  cashbackEnabled?: boolean;
  cashbackAmount?: number;
  warrantyDays?: number;
  expenses?: string;
}

export interface OrderStatusUpdateRequest {
  status: OrderStatus;
}

export interface OrderImage {
  id: number;
  imageUrl: string;
  createdAt: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  DELIVERED = 'DELIVERED',
}

export interface Customer {
  id: number;
  name: string;
  phoneNumber: string;
  email?: string;
  address?: string;
}

export interface Staff {
  id: number;
  name: string;
  phoneNumber: string;
  email: string;
}
