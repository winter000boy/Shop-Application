// Wholesaler list component with distance display

interface Wholesaler {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  distance: number;
  rating?: number;
}

interface WholesalerListProps {
  wholesalers: Wholesaler[];
  loading?: boolean;
  onWholesalerClick?: (wholesaler: Wholesaler) => void;
}

const WholesalerList = ({
  wholesalers,
  loading = false,
  onWholesalerClick,
}: WholesalerListProps) => {
  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (wholesalers.length === 0) {
    return (
      <div className="text-center py-12">
        <svg
          className="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
          />
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
          />
        </svg>
        <p className="mt-4 text-gray-500">No wholesalers found nearby</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {wholesalers.map((wholesaler) => (
        <div
          key={wholesaler.id}
          onClick={() => onWholesalerClick?.(wholesaler)}
          className={`bg-white rounded-lg shadow p-4 hover:shadow-md transition-shadow duration-200 ${
            onWholesalerClick ? 'cursor-pointer' : ''
          }`}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900">
                {wholesaler.name}
              </h3>
              <p className="text-sm text-gray-600 mt-1">{wholesaler.address}</p>
              <p className="text-sm text-gray-600 mt-1">
                <span className="font-medium">Phone:</span> {wholesaler.phoneNumber}
              </p>
              {wholesaler.rating && (
                <div className="flex items-center mt-2">
                  <svg
                    className="w-5 h-5 text-yellow-400"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  <span className="ml-1 text-sm text-gray-600">
                    {wholesaler.rating.toFixed(1)}
                  </span>
                </div>
              )}
            </div>
            <div className="ml-4 flex flex-col items-end">
              <div className="flex items-center text-blue-600">
                <svg
                  className="w-5 h-5 mr-1"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                <span className="font-semibold">{wholesaler.distance.toFixed(1)} km</span>
              </div>
              <span className="text-xs text-gray-500 mt-1">away</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default WholesalerList;
