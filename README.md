# Shop Management System

A comprehensive full-stack web application for mobile and hardware repair shops, built with React + TypeScript frontend and Spring Boot backend.

## Features

- Shop registration and authentication
- Repair order management
- Customer management
- Product marketplace
- Wallet and referral system
- Staff management
- Invoicing and sales
- Community forum
- Multi-tenant architecture

## Tech Stack

### Frontend
- React 18 + TypeScript
- Vite
- TailwindCSS
- Redux Toolkit
- React Router
- Axios

### Backend
- Spring Boot 3.2
- Java 17
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Lombok

## Prerequisites

- Node.js 18+ and npm
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Docker and Docker Compose (optional)

## Getting Started

### Using Docker Compose (Recommended)

1. Clone the repository
2. Run the application:
```bash
docker-compose up --build
```

The application will be available at:
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Database: localhost:5432

### Manual Setup

#### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Copy the environment template:
```bash
cp .env.example .env
```

3. Update the `.env` file with your configuration

4. Ensure PostgreSQL is running and create the database:
```sql
CREATE DATABASE shopmanagement;
```

5. Build and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on http://localhost:8080

#### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Copy the environment template:
```bash
cp .env.example .env
```

3. Update the `.env` file with your configuration

4. Install dependencies:
```bash
npm install
```

5. Run the development server:
```bash
npm run dev
```

The frontend will start on http://localhost:3000

## Environment Variables

### Backend (.env)
- `DATABASE_URL` - PostgreSQL connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - Secret key for JWT token generation (minimum 256 bits)
- `FIREBASE_CREDENTIALS` - Path to Firebase credentials JSON
- `STORAGE_BUCKET` - Cloud storage bucket name
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` - Email configuration
- `WHATSAPP_API_URL`, `WHATSAPP_API_KEY` - WhatsApp API configuration

### Frontend (.env)
- `VITE_API_BASE_URL` - Backend API base URL
- `VITE_FIREBASE_API_KEY` - Firebase API key
- `VITE_FIREBASE_STORAGE_BUCKET` - Firebase storage bucket

## Project Structure

```
shop-management-system/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── store/
│   │   ├── hooks/
│   │   ├── types/
│   │   └── utils/
│   ├── Dockerfile
│   └── package.json
└── docker-compose.yml
```

## Building for Production

### Backend
```bash
cd backend
mvn clean package -DskipTests
```

### Frontend
```bash
cd frontend
npm run build
```

## Deployment

The application is designed to be deployed on cloud platforms like Render, Railway, or similar services. Docker configurations are provided for containerized deployment.

## License

MIT
