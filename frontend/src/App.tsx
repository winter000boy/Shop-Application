import { BrowserRouter } from 'react-router-dom'

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <h1 className="text-3xl font-bold text-primary-600">
            Shop Management System
          </h1>
          <p className="mt-4 text-gray-600">
            Welcome to the Shop Management System
          </p>
        </div>
      </div>
    </BrowserRouter>
  )
}

export default App
