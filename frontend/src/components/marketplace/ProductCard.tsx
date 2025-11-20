// Product card component for displaying individual products

import { Product } from '../../types';

interface ProductCardProps {
  product: Product;
  onClick?: (product: Product) => void;
}

const ProductCard = ({ product, onClick }: ProductCardProps) => {
  const handleClick = () => {
    if (onClick) {
      onClick(product);
    }
  };

  return (
    <div
      onClick={handleClick}
      className={`bg-white rounded-lg shadow hover:shadow-lg transition-shadow duration-200 overflow-hidden ${
        onClick ? 'cursor-pointer' : ''
      }`}
    >
      {/* Product Image */}
      <div className="relative h-48 bg-gray-200">
        {product.imageUrls && product.imageUrls.length > 0 ? (
          <img
            src={product.imageUrls[0]}
            alt={product.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg
              className="w-16 h-16"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>
        )}
        {!product.available && (
          <div className="absolute top-2 right-2 bg-red-500 text-white text-xs px-2 py-1 rounded">
            Out of Stock
          </div>
        )}
      </div>

      {/* Product Details */}
      <div className="p-4">
        <h3 className="text-lg font-semibold text-gray-900 truncate">
          {product.name}
        </h3>
        
        <p className="text-sm text-gray-500 mt-1 truncate">
          {product.category.name}
        </p>

        {product.description && (
          <p className="text-sm text-gray-600 mt-2 line-clamp-2">
            {product.description}
          </p>
        )}

        <div className="mt-4 flex items-center justify-between">
          <div>
            <span className="text-2xl font-bold text-blue-600">
              ₹{product.price.toFixed(2)}
            </span>
          </div>
          <div className="text-sm text-gray-500">
            Stock: {product.stockQuantity}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
