// Marketplace page with category filters and product search

import { useState, useEffect } from 'react';
import { Product, Category } from '../types';
import productService from '../services/productService';
import ProductGrid from '../components/marketplace/ProductGrid';
import ProductForm from '../components/marketplace/ProductForm';
import WholesalerList from '../components/marketplace/WholesalerList';

const Marketplace = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [wholesalers, setWholesalers] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'products' | 'myProducts' | 'wholesalers'>('products');
  const [showProductForm, setShowProductForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | undefined>(undefined);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    if (activeTab === 'products') {
      loadMarketplaceProducts();
    } else if (activeTab === 'myProducts') {
      loadMyProducts();
    } else if (activeTab === 'wholesalers') {
      loadWholesalers();
    }
  }, [activeTab, selectedCategory, searchQuery, currentPage]);

  const loadCategories = async () => {
    try {
      const data = await productService.getCategories();
      setCategories(data);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const loadMarketplaceProducts = async () => {
    setLoading(true);
    try {
      const params: any = {
        page: currentPage,
        size: 20,
      };

      if (selectedCategory) {
        params.categoryId = selectedCategory;
      }

      let response;
      if (searchQuery.trim()) {
        response = await productService.searchMarketplace(searchQuery, params);
      } else {
        response = await productService.getMarketplaceProducts(params);
      }

      setProducts(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Failed to load marketplace products:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMyProducts = async () => {
    setLoading(true);
    try {
      const params: any = {
        page: currentPage,
        size: 20,
      };

      if (selectedCategory) {
        params.categoryId = selectedCategory;
      }

      if (searchQuery.trim()) {
        params.search = searchQuery;
      }

      const response = await productService.getProducts(params);
      setProducts(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('Failed to load my products:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadWholesalers = async () => {
    setLoading(true);
    try {
      const data = await productService.getWholesalers();
      setWholesalers(data);
    } catch (error) {
      console.error('Failed to load wholesalers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryChange = (categoryId: number | null) => {
    setSelectedCategory(categoryId);
    setCurrentPage(0);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
  };

  const handleAddProduct = () => {
    setEditingProduct(undefined);
    setShowProductForm(true);
  };

  const handleEditProduct = (product: Product) => {
    setEditingProduct(product);
    setShowProductForm(true);
  };

  const handleProductSubmit = async (data: any) => {
    try {
      if (editingProduct) {
        await productService.updateProduct(editingProduct.id, data);
      } else {
        await productService.createProduct(data);
      }
      setShowProductForm(false);
      setEditingProduct(undefined);
      loadMyProducts();
    } catch (error) {
      console.error('Failed to save product:', error);
    }
  };

  const handleProductFormCancel = () => {
    setShowProductForm(false);
    setEditingProduct(undefined);
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Marketplace</h1>
        {activeTab === 'myProducts' && (
          <button
            onClick={handleAddProduct}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Add Product
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('products')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'products'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Browse Marketplace
          </button>
          <button
            onClick={() => setActiveTab('myProducts')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'myProducts'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            My Products
          </button>
          <button
            onClick={() => setActiveTab('wholesalers')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'wholesalers'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Nearby Wholesalers
          </button>
        </nav>
      </div>

      {/* Product Form Modal */}
      {showProductForm && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-full max-w-2xl shadow-lg rounded-md bg-white">
            <div className="mb-4">
              <h3 className="text-lg font-medium text-gray-900">
                {editingProduct ? 'Edit Product' : 'Add New Product'}
              </h3>
            </div>
            <ProductForm
              product={editingProduct}
              categories={categories}
              onSubmit={handleProductSubmit}
              onCancel={handleProductFormCancel}
            />
          </div>
        </div>
      )}

      {/* Filters and Search */}
      {activeTab !== 'wholesalers' && (
        <div className="bg-white p-4 rounded-lg shadow space-y-4">
          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search products..."
              className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              Search
            </button>
          </form>

          {/* Category Filter */}
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => handleCategoryChange(null)}
              className={`px-4 py-2 rounded-full text-sm font-medium ${
                selectedCategory === null
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              All Categories
            </button>
            {categories.map((category) => (
              <button
                key={category.id}
                onClick={() => handleCategoryChange(category.id)}
                className={`px-4 py-2 rounded-full text-sm font-medium ${
                  selectedCategory === category.id
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {category.name}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Content */}
      {activeTab === 'wholesalers' ? (
        <div className="bg-white p-6 rounded-lg shadow">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">
            Nearby Wholesalers
          </h2>
          <WholesalerList wholesalers={wholesalers} loading={loading} />
        </div>
      ) : (
        <>
          <ProductGrid
            products={products}
            loading={loading}
            onProductClick={activeTab === 'myProducts' ? handleEditProduct : undefined}
            emptyMessage={
              activeTab === 'myProducts'
                ? 'No products found. Add your first product to get started.'
                : 'No products available in the marketplace.'
            }
          />

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="text-sm text-gray-700">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Marketplace;
