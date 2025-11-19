// Product and marketplace related types

export interface Product {
  id: number;
  shopId: number;
  name: string;
  description?: string;
  price: number;
  category: Category;
  imageUrls: string[];
  stockQuantity: number;
  available: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  price: number;
  categoryId: number;
  stockQuantity: number;
  available?: boolean;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}
