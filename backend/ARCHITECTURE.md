# FoodApp Backend

NestJS backend for the FoodApp mobile application, built with TypeScript following **Monolithic + Layered Architecture** with **Clean Architecture (Ports & Adapters)** principles.

## 🏗️ Architecture Overview

This backend follows a **Monolithic Layered Architecture** with **Dependency Inversion** through the **Ports & Adapters pattern**:

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│              (Controllers - HTTP/REST API)                  │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                        │
│                  (Services - Business Logic)                │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                           │
│         (Entities, Repository Interfaces/Ports)             │
├─────────────────────────────────────────────────────────────┤
│                  Infrastructure Layer                       │
│      (Repository Implementations/Adapters, Firebase)        │
└─────────────────────────────────────────────────────────────┘

         ┌──────────────────────────────────────┐
         │       Shared Services (Global)       │
         │  Cache, Notifications, Event Bus     │
         │     (Ports & Adapters Pattern)       │
         └──────────────────────────────────────┘
```

### Folder Structure

```
backend/
├── src/
│   ├── main.ts                   # Application entry point
│   ├── app.module.ts             # Root module
│   │
│   ├── config/                   # Configuration
│   │   ├── environment.config.ts # Environment variables
│   │   └── firebase.config.ts    # Firebase initialization (stub)
│   │
│   ├── common/                   # Shared utilities
│   │   ├── decorators/           # Custom decorators (@CurrentUser, @Roles)
│   │   ├── filters/              # Exception filters
│   │   ├── guards/               # Auth guards
│   │   └── interceptors/         # Logging interceptors
│   │
│   ├── shared/                   # Technical services (Ports & Adapters)
│   │   ├── cache/
│   │   │   ├── cache.port.ts               # Abstract cache interface
│   │   │   └── in-memory-cache.adapter.ts  # In-memory implementation
│   │   ├── notifications/
│   │   │   ├── notification.port.ts         # Abstract notification interface
│   │   │   └── fcm-notification.adapter.ts  # Firebase FCM stub
│   │   ├── events/
│   │   │   ├── event-bus.port.ts           # Abstract event bus interface
│   │   │   └── in-memory-event-bus.adapter.ts
│   │   └── shared.module.ts      # Shared services module
│   │
│   └── modules/                  # Feature modules
│       ├── auth/                 # Authentication module
│       │   ├── domain/
│       │   │   ├── auth-user.entity.ts    # User entity
│       │   │   └── auth.repository.ts     # Repository interface (Port)
│       │   ├── infra/
│       │   │   └── firebase-auth.repository.ts  # Repository implementation (Adapter)
│       │   ├── dto/
│       │   │   └── auth.dto.ts
│       │   ├── auth.controller.ts
│       │   ├── auth.service.ts
│       │   └── auth.module.ts
│       │
│       └── orders/               # Orders module
│           ├── domain/
│           │   ├── order.entity.ts         # Order entity
│           │   └── order.repository.ts     # Repository interface (Port)
│           ├── infra/
│           │   └── firebase-order.repository.ts  # Repository implementation (Adapter)
│           ├── dto/
│           │   └── order.dto.ts
│           ├── orders.controller.ts
│           ├── orders.service.ts
│           └── orders.module.ts
│
├── test/                         # E2E tests
├── package.json
├── tsconfig.json
└── README.md
```

## 🎯 Key Design Principles

### 1. **Dependency Inversion (Ports & Adapters)**
- **Ports** (abstract classes) define contracts
- **Adapters** (concrete classes) implement those contracts
- Business logic depends on abstractions, not concrete implementations
- Easy to swap implementations (e.g., in-memory cache → Redis)

### 2. **Layered Architecture**
- **Controllers**: Handle HTTP requests/responses only
- **Services**: Contain business logic
- **Domain**: Define entities and repository interfaces
- **Infrastructure**: Implement repository adapters (Firebase, etc.)

### 3. **Clean Architecture Benefits**
- ✅ Testability: Easy to mock dependencies
- ✅ Maintainability: Clear separation of concerns
- ✅ Flexibility: Swap implementations without changing business logic
- ✅ Scalability: Add new features by extending modules

## 📦 Tech Stack

- **Framework**: NestJS
- **Language**: TypeScript
- **Database**: Firebase Firestore (stub implementation)
- **Authentication**: Firebase Auth (stub implementation)
- **Validation**: class-validator, class-transformer
- **Cache**: In-memory (stub for Redis)
- **Notifications**: Firebase Cloud Messaging (stub)
- **Events**: In-memory event bus (stub for RabbitMQ/Kafka)

## 🚀 Getting Started

### Prerequisites
- Node.js >= 18.x
- npm or yarn

### Installation

```bash
# Install dependencies
npm install
```

### Environment Variables

Create a `.env` file in the backend folder (optional for now):

```env
PORT=3000
NODE_ENV=development

# Firebase Configuration (TODO: Add real credentials)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=your-client-email
FIREBASE_PRIVATE_KEY=your-private-key

# JWT Configuration (TODO)
JWT_SECRET=your-secret-key
JWT_EXPIRES_IN=1d
```

### Running the Application

```bash
# Development mode with hot-reload
npm run start:dev

# Production mode
npm run start:prod

# Debug mode
npm run start:debug
```

The API will be available at: `http://localhost:3000/api`

### Testing

```bash
# Unit tests
npm test

# E2E tests
npm run test:e2e

# Test coverage
npm run test:cov
```

## 📚 API Endpoints

### Authentication

```
POST   /api/auth/register        # Register new user
POST   /api/auth/login           # User login
GET    /api/auth/users/:id       # Get user by ID
```

### Orders

```
POST   /api/orders                      # Create new order
GET    /api/orders/:id                  # Get order by ID
GET    /api/orders/customer/:customerId # Get customer's orders
GET    /api/orders/seller/:sellerId     # Get seller's orders
PATCH  /api/orders/:id/status           # Update order status
DELETE /api/orders/:id                  # Cancel order
```

## 🔧 Stub Implementations

The following features are currently **stubbed** and need Firebase integration:

### 1. **Authentication** (`FirebaseAuthRepository`)
- Currently uses in-memory storage
- TODO: Integrate Firebase Admin SDK for user management

### 2. **Orders** (`FirebaseOrderRepository`)
- Currently uses in-memory storage
- TODO: Integrate Firebase Firestore for data persistence

### 3. **Cache** (`InMemoryCacheAdapter`)
- Currently uses JavaScript Map
- TODO: Integrate Redis for distributed caching

### 4. **Notifications** (`FcmNotificationAdapter`)
- Currently logs to console
- TODO: Integrate Firebase Cloud Messaging for push notifications

### 5. **Events** (`InMemoryEventBusAdapter`)
- Currently logs to console
- TODO: Integrate RabbitMQ, Kafka, or AWS SQS for event-driven architecture

## 🛠️ Next Steps

1. **Integrate Firebase Admin SDK**
   - Add Firebase credentials
   - Replace stub repositories with real Firestore operations
   - Implement Firebase Auth

2. **Add JWT Authentication**
   - Install `@nestjs/jwt` and `@nestjs/passport`
   - Implement JWT token generation and validation
   - Protect routes with AuthGuard

3. **Add More Feature Modules**
   - Products module
   - Users/Sellers module
   - Reviews module
   - Payments module

4. **Add Redis Cache**
   - Install `@nestjs/cache-manager` and `cache-manager-redis-store`
   - Replace `InMemoryCacheAdapter` with `RedisCacheAdapter`

5. **Add Message Queue**
   - Install `@nestjs/microservices` with RabbitMQ or Kafka
   - Replace `InMemoryEventBusAdapter` with real event bus

6. **Add API Documentation**
   - Install `@nestjs/swagger`
   - Add Swagger decorators to controllers

## 📖 Resources

- [NestJS Documentation](https://docs.nestjs.com)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture (Ports & Adapters)](https://alistair.cockburn.us/hexagonal-architecture/)

## 📝 License

This project is part of the FoodApp mobile application.
