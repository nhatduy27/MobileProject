# Hướng dẫn Repository Pattern

## 📋 Tổng quan

Tài liệu này giải thích chi tiết về Repository Pattern trong kiến trúc Clean-lite của chúng ta, cách sử dụng Ports & Adapters, và best practices khi làm việc với data access layer.

## 🎯 Repository Pattern là gì?

**Repository Pattern** là một design pattern tách biệt logic truy cập dữ liệu khỏi business logic. Nó cung cấp một interface (abstraction) để tương tác với data source mà không cần biết chi tiết implementation.

### Lợi ích

✅ **Testability**: Dễ dàng mock repository trong unit tests  
✅ **Flexibility**: Dễ thay đổi data source (Firebase → PostgreSQL → MongoDB)  
✅ **Maintainability**: Business logic không bị ảnh hưởng khi đổi database  
✅ **Clean Code**: Tách biệt rõ ràng concerns giữa các layer  

## 🏗️ Kiến trúc Repository trong Clean-lite

### Dependency Inversion Principle

```
┌──────────────────────────────────────┐
│     Application Layer (Service)      │
│  Depends on abstraction (Port)       │
└───────────────┬──────────────────────┘
                │ depends on
                ↓
┌──────────────────────────────────────┐
│   Domain Layer (Repository Port)     │
│   Abstract class định nghĩa contract │
└───────────────┬──────────────────────┘
                │ implemented by
                ↓
┌──────────────────────────────────────┐
│ Infrastructure Layer (Adapter)        │
│ Concrete implementation (Firebase)    │
└──────────────────────────────────────┘
```

**Key Point**: Service phụ thuộc vào Port (abstraction), không phụ thuộc vào Adapter (concrete implementation).

## 📐 Cấu trúc Repository

### 1. Domain Layer - Repository Port (Abstract)

**Vị trí:** `src/modules/{module}/domain/{entity}.repository.ts`

Repository Port là một **abstract class** (không phải interface) định nghĩa các methods để truy cập dữ liệu.

**Tại sao dùng abstract class thay vì interface?**
- NestJS Dependency Injection yêu cầu token là class
- Abstract class có thể chứa implementation methods nếu cần
- TypeScript interface bị xóa sau compile, không dùng được làm DI token

**Ví dụ: OrderRepository Port**

```typescript
import { Order } from './order.entity';

/**
 * Type định nghĩa data cần thiết để tạo Order mới
 */
export type CreateOrderDto = {
  customerId: string;
  sellerId: string;
  items: OrderItem[];
  status: OrderStatus;
  totalAmount: number;
};

/**
 * OrderRepository Port (Abstraction)
 * 
 * Abstract class định nghĩa contract cho order data access.
 * Mọi implementation (Firebase, PostgreSQL, MongoDB, etc.) 
 * phải extend class này và implement tất cả methods.
 */
export abstract class OrderRepository {
  /**
   * Tạo order mới trong database
   * @param order - Dữ liệu order cần tạo
   * @returns Promise với Order đã được tạo (có id, timestamps)
   */
  abstract create(order: CreateOrderDto): Promise<Order>;

  /**
   * Tìm order theo ID
   * @param id - ID của order
   * @returns Promise với Order hoặc null nếu không tìm thấy
   */
  abstract findById(id: string): Promise<Order | null>;

  /**
   * Tìm tất cả orders của customer
   * @param customerId - ID của customer
   * @returns Promise với mảng Orders
   */
  abstract findByCustomer(customerId: string): Promise<Order[]>;

  /**
   * Tìm tất cả orders của seller
   * @param sellerId - ID của seller
   * @returns Promise với mảng Orders
   */
  abstract findBySeller(sellerId: string): Promise<Order[]>;

  /**
   * Cập nhật order
   * @param id - ID của order cần update
   * @param order - Partial order data để update
   * @returns Promise với Order đã được update
   */
  abstract update(id: string, order: Partial<Order>): Promise<Order>;

  /**
   * Xóa order
   * @param id - ID của order cần xóa
   * @returns Promise<void>
   */
  abstract delete(id: string): Promise<void>;
}
```

**Best Practices cho Repository Port:**

✅ **DO:**
- Dùng abstract class, không dùng interface
- Đặt tên methods rõ ràng, mô tả hành động (create, find, update, delete)
- Document đầy đủ JSDoc cho mỗi method
- Return Promise cho tất cả async operations
- Return entity hoặc null, không throw exceptions trong signature

❌ **DON'T:**
- Không chứa implementation logic trong Port
- Không có dependencies khác (database, http client, etc.)
- Không tham chiếu đến infrastructure concerns (Firestore, SQL queries)

### 2. Infrastructure Layer - Repository Adapter (Concrete)

**Vị trí:** `src/modules/{module}/infra/{adapter-name}.repository.ts`

Repository Adapter là concrete class **extends** Repository Port và implement tất cả abstract methods.

**Ví dụ: FirebaseOrderRepository Adapter**

```typescript
import { Injectable, Logger } from '@nestjs/common';
import { OrderRepository, CreateOrderDto } from '../domain/order.repository';
import { Order, OrderItem, OrderStatus } from '../domain/order.entity';

/**
 * Firebase Order Repository Adapter
 * 
 * Implementation cụ thể cho OrderRepository sử dụng Firebase Firestore.
 * Adapter này có thể thay thế bằng PostgresOrderRepository, 
 * MongoOrderRepository, etc. mà không ảnh hưởng Service.
 */
@Injectable()
export class FirebaseOrderRepository extends OrderRepository {
  private readonly logger = new Logger(FirebaseOrderRepository.name);

  // Temporary in-memory storage (stub cho demo)
  private orders: Map<string, Order> = new Map();
  private orderIdCounter = 1;

  /**
   * Tạo order mới trong Firestore
   */
  async create(orderData: CreateOrderDto): Promise<Order> {
    // TODO: Tích hợp Firebase Admin SDK
    // const docRef = await admin.firestore().collection('orders').add({
    //   customerId: orderData.customerId,
    //   sellerId: orderData.sellerId,
    //   items: orderData.items.map(item => ({
    //     productId: item.productId,
    //     productName: item.productName,
    //     quantity: item.quantity,
    //     unitPrice: item.unitPrice,
    //     totalPrice: item.totalPrice,
    //   })),
    //   status: orderData.status,
    //   totalAmount: orderData.totalAmount,
    //   createdAt: admin.firestore.FieldValue.serverTimestamp(),
    //   updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    // });
    // 
    // const snapshot = await docRef.get();
    // return this.mapFirestoreDocToOrder(snapshot);

    this.logger.log(`[STUB] Creating order for customer: ${orderData.customerId}`);

    const newOrder = new Order({
      id: `order_${this.orderIdCounter++}`,
      ...orderData,
      createdAt: new Date(),
      updatedAt: new Date(),
    });

    this.orders.set(newOrder.id, newOrder);
    return newOrder;
  }

  /**
   * Tìm order theo ID trong Firestore
   */
  async findById(id: string): Promise<Order | null> {
    // TODO: const doc = await admin.firestore()
    //   .collection('orders')
    //   .doc(id)
    //   .get();
    // 
    // if (!doc.exists) return null;
    // return this.mapFirestoreDocToOrder(doc);

    this.logger.log(`[STUB] Finding order by ID: ${id}`);
    return this.orders.get(id) || null;
  }

  /**
   * Tìm orders của customer trong Firestore
   */
  async findByCustomer(customerId: string): Promise<Order[]> {
    // TODO: const snapshot = await admin.firestore()
    //   .collection('orders')
    //   .where('customerId', '==', customerId)
    //   .orderBy('createdAt', 'desc')
    //   .get();
    // 
    // return snapshot.docs.map(doc => this.mapFirestoreDocToOrder(doc));

    this.logger.log(`[STUB] Finding orders for customer: ${customerId}`);
    return Array.from(this.orders.values())
      .filter(order => order.customerId === customerId)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
  }

  /**
   * Tìm orders của seller trong Firestore
   */
  async findBySeller(sellerId: string): Promise<Order[]> {
    // TODO: Firestore query with sellerId filter
    this.logger.log(`[STUB] Finding orders for seller: ${sellerId}`);
    return Array.from(this.orders.values())
      .filter(order => order.sellerId === sellerId);
  }

  /**
   * Cập nhật order trong Firestore
   */
  async update(id: string, orderData: Partial<Order>): Promise<Order> {
    // TODO: await admin.firestore()
    //   .collection('orders')
    //   .doc(id)
    //   .update({
    //     ...orderData,
    //     updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    //   });
    // 
    // const doc = await admin.firestore().collection('orders').doc(id).get();
    // return this.mapFirestoreDocToOrder(doc);

    this.logger.log(`[STUB] Updating order: ${id}`);

    const existingOrder = this.orders.get(id);
    if (!existingOrder) {
      throw new Error(`Order not found: ${id}`);
    }

    const updatedOrder = new Order({
      ...existingOrder,
      ...orderData,
      updatedAt: new Date(),
    });

    this.orders.set(id, updatedOrder);
    return updatedOrder;
  }

  /**
   * Xóa order trong Firestore
   */
  async delete(id: string): Promise<void> {
    // TODO: await admin.firestore().collection('orders').doc(id).delete();
    this.logger.log(`[STUB] Deleting order: ${id}`);
    this.orders.delete(id);
  }

  /**
   * Helper: Map Firestore document sang Order entity
   * (Sẽ dùng khi tích hợp Firebase thật)
   */
  // private mapFirestoreDocToOrder(doc: FirebaseFirestore.DocumentSnapshot): Order {
  //   const data = doc.data();
  //   return new Order({
  //     id: doc.id,
  //     customerId: data.customerId,
  //     sellerId: data.sellerId,
  //     items: data.items.map(item => new OrderItem(item)),
  //     status: data.status as OrderStatus,
  //     totalAmount: data.totalAmount,
  //     createdAt: data.createdAt.toDate(),
  //     updatedAt: data.updatedAt.toDate(),
  //   });
  // }
}
```

**Best Practices cho Repository Adapter:**

✅ **DO:**
- Extend Repository Port
- Implement tất cả abstract methods
- Xử lý database/external service errors
- Log operations quan trọng
- Convert giữa database models và domain entities
- Đặt TODO comments cho các tích hợp thật

❌ **DON'T:**
- Không chứa business logic
- Không expose database implementation details ra ngoài
- Không throw business exceptions (NotFoundException, etc.) - để Service xử lý

### 3. Application Layer - Service sử dụng Repository

**Vị dụ: OrdersService inject OrderRepository Port**

```typescript
import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { OrderRepository } from './domain/order.repository'; // Import Port
import { Order, OrderStatus } from './domain/order.entity';
import { CreateOrderDto, OrderResponseDto } from './dto/order.dto';

/**
 * Orders Service (Application Layer)
 * 
 * Service inject OrderRepository Port (abstraction).
 * NestJS DI sẽ tự động inject FirebaseOrderRepository (adapter).
 */
@Injectable()
export class OrdersService {
  private readonly logger = new Logger(OrdersService.name);

  constructor(
    // ✅ Inject Port (abstract class), không inject Adapter
    private readonly orderRepository: OrderRepository,
  ) {}

  /**
   * Business logic: Tạo order mới
   */
  async createOrder(dto: CreateOrderDto): Promise<OrderResponseDto> {
    this.logger.log(`Creating order for customer: ${dto.customerId}`);

    // Service chỉ gọi Port methods, không biết Adapter là gì
    const order = await this.orderRepository.create({
      customerId: dto.customerId,
      sellerId: dto.sellerId,
      items: dto.items,
      status: OrderStatus.PENDING,
      totalAmount: 0, // Entity sẽ tự tính
    });

    return this.mapToResponseDto(order);
  }

  /**
   * Business logic: Lấy order theo ID
   */
  async getOrderById(id: string): Promise<OrderResponseDto> {
    this.logger.log(`Getting order by ID: ${id}`);

    // Service gọi Port method
    const order = await this.orderRepository.findById(id);

    // Service xử lý business logic (NotFoundException)
    if (!order) {
      throw new NotFoundException(`Không tìm thấy đơn hàng: ${id}`);
    }

    return this.mapToResponseDto(order);
  }

  /**
   * Business logic: Hủy order
   */
  async cancelOrder(id: string, customerId: string): Promise<OrderResponseDto> {
    this.logger.log(`Cancelling order: ${id}`);

    const order = await this.orderRepository.findById(id);
    if (!order) {
      throw new NotFoundException(`Không tìm thấy đơn hàng: ${id}`);
    }

    // Business rule: Chỉ customer sở hữu order mới có thể hủy
    if (order.customerId !== customerId) {
      throw new ForbiddenException('Bạn không có quyền hủy đơn hàng này');
    }

    // Domain logic: Entity tự kiểm tra có thể hủy không
    order.cancel(); // Throws error nếu không thể hủy

    // Update thông qua repository
    const updatedOrder = await this.orderRepository.update(id, {
      status: order.status,
      updatedAt: order.updatedAt,
    });

    return this.mapToResponseDto(updatedOrder);
  }

  private mapToResponseDto(order: Order): OrderResponseDto {
    return {
      id: order.id,
      customerId: order.customerId,
      sellerId: order.sellerId,
      items: order.items,
      status: order.status,
      totalAmount: order.totalAmount,
      createdAt: order.createdAt,
      updatedAt: order.updatedAt,
    };
  }
}
```

**Key Points:**
- Service inject `OrderRepository` (Port), không inject `FirebaseOrderRepository` (Adapter)
- Service chỉ gọi methods định nghĩa trong Port
- Service không biết Adapter đang dùng Firebase hay PostgreSQL
- Service xử lý business exceptions (NotFoundException, ForbiddenException)

## 🔌 Dependency Injection Binding

**File: `src/modules/orders/orders.module.ts`**

```typescript
import { Module } from '@nestjs/common';
import { OrdersController } from './orders.controller';
import { OrdersService } from './orders.service';
import { OrderRepository } from './domain/order.repository';
import { FirebaseOrderRepository } from './infra/firebase-order.repository';

/**
 * Orders Module với DI Binding
 * 
 * Bind OrderRepository (Port) với FirebaseOrderRepository (Adapter).
 * Khi Service inject OrderRepository, NestJS sẽ tự động provide 
 * FirebaseOrderRepository instance.
 */
@Module({
  controllers: [OrdersController],
  providers: [
    OrdersService,
    {
      provide: OrderRepository, // Token = Abstract Port
      useClass: FirebaseOrderRepository, // Implementation = Concrete Adapter
    },
  ],
  exports: [OrdersService],
})
export class OrdersModule {}
```

**Cách hoạt động:**
1. Service request `OrderRepository` qua constructor
2. NestJS DI container tìm provider với token `OrderRepository`
3. Container tạo instance của `FirebaseOrderRepository`
4. Inject instance vào Service

**Thay đổi Adapter dễ dàng:**

```typescript
// Chuyển từ Firebase sang PostgreSQL chỉ cần đổi một dòng
{
  provide: OrderRepository,
  useClass: PostgresOrderRepository, // ← Chỉ thay đổi ở đây
}
```

## 🧪 Testing với Repository Pattern

### Unit Test Service với Mock Repository

```typescript
import { Test, TestingModule } from '@nestjs/testing';
import { OrdersService } from './orders.service';
import { OrderRepository } from './domain/order.repository';
import { Order, OrderStatus } from './domain/order.entity';
import { NotFoundException } from '@nestjs/common';

describe('OrdersService', () => {
  let service: OrdersService;
  let mockRepository: jest.Mocked<OrderRepository>;

  beforeEach(async () => {
    // Tạo mock Repository Port
    mockRepository = {
      create: jest.fn(),
      findById: jest.fn(),
      findByCustomer: jest.fn(),
      findBySeller: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
    } as any;

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        OrdersService,
        {
          provide: OrderRepository, // Bind Port
          useValue: mockRepository, // Với mock implementation
        },
      ],
    }).compile();

    service = module.get<OrdersService>(OrdersService);
  });

  describe('getOrderById', () => {
    it('should return order when found', async () => {
      // Arrange
      const mockOrder = new Order({
        id: 'order_1',
        customerId: 'customer_1',
        sellerId: 'seller_1',
        items: [],
        status: OrderStatus.PENDING,
        totalAmount: 100000,
        createdAt: new Date(),
        updatedAt: new Date(),
      });

      mockRepository.findById.mockResolvedValue(mockOrder);

      // Act
      const result = await service.getOrderById('order_1');

      // Assert
      expect(mockRepository.findById).toHaveBeenCalledWith('order_1');
      expect(result.id).toBe('order_1');
      expect(result.customerId).toBe('customer_1');
    });

    it('should throw NotFoundException when order not found', async () => {
      // Arrange
      mockRepository.findById.mockResolvedValue(null);

      // Act & Assert
      await expect(service.getOrderById('invalid_id'))
        .rejects
        .toThrow(NotFoundException);

      expect(mockRepository.findById).toHaveBeenCalledWith('invalid_id');
    });
  });

  describe('createOrder', () => {
    it('should create order successfully', async () => {
      // Arrange
      const dto = {
        customerId: 'customer_1',
        sellerId: 'seller_1',
        items: [],
      };

      const mockCreatedOrder = new Order({
        id: 'order_1',
        ...dto,
        items: [],
        status: OrderStatus.PENDING,
        totalAmount: 0,
        createdAt: new Date(),
        updatedAt: new Date(),
      });

      mockRepository.create.mockResolvedValue(mockCreatedOrder);

      // Act
      const result = await service.createOrder(dto);

      // Assert
      expect(mockRepository.create).toHaveBeenCalledWith(
        expect.objectContaining({
          customerId: dto.customerId,
          sellerId: dto.sellerId,
          status: OrderStatus.PENDING,
        }),
      );
      expect(result.id).toBe('order_1');
    });
  });
});
```

**Lợi ích của mock Repository Port:**
- Không cần database thật để test Service
- Test nhanh và isolated
- Dễ dàng test các edge cases (null, errors)
- Verify Service gọi đúng Repository methods

### Integration Test với Adapter thật

```typescript
describe('FirebaseOrderRepository (Integration)', () => {
  let repository: FirebaseOrderRepository;

  beforeEach(() => {
    repository = new FirebaseOrderRepository();
  });

  it('should create and retrieve order', async () => {
    // Create
    const order = await repository.create({
      customerId: 'customer_1',
      sellerId: 'seller_1',
      items: [],
      status: OrderStatus.PENDING,
      totalAmount: 0,
    });

    expect(order.id).toBeDefined();

    // Retrieve
    const retrieved = await repository.findById(order.id);
    expect(retrieved).not.toBeNull();
    expect(retrieved!.customerId).toBe('customer_1');
  });
});
```

## 🎯 Best Practices Summary

### Repository Port (Abstract)

✅ **DO:**
- Dùng abstract class, không phải interface
- Định nghĩa tất cả data access methods
- Return domain entities, không phải database models
- Document đầy đủ JSDoc
- Đặt tên methods rõ ràng (create, find, update, delete)

❌ **DON'T:**
- Không chứa implementation
- Không reference database-specific types
- Không throw business exceptions trong signature

### Repository Adapter (Concrete)

✅ **DO:**
- Extend Repository Port
- Implement tất cả abstract methods
- Handle database errors gracefully
- Convert database models → domain entities
- Log database operations
- Add TODO comments cho production implementation

❌ **DON'T:**
- Không chứa business logic
- Không expose database details
- Không leak infrastructure concerns

### Service (Application)

✅ **DO:**
- Inject Repository Port (abstraction)
- Xử lý business logic và business exceptions
- Sử dụng domain entities
- Orchestrate multiple repositories nếu cần

❌ **DON'T:**
- Không inject Repository Adapter trực tiếp
- Không làm việc trực tiếp với database
- Không xử lý database-specific errors

### Module (DI)

✅ **DO:**
- Bind Port (abstract) với Adapter (concrete)
- Export Service nếu module khác cần dùng
- Use `provide: Port, useClass: Adapter` syntax

❌ **DON'T:**
- Không bind Adapter trực tiếp vào Service

## 📚 Tài liệu Liên quan

- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Quy ước lập trình tổng thể
- [HOW_TO_ADD_A_NEW_MODULE.md](./HOW_TO_ADD_A_NEW_MODULE.md) - Hướng dẫn thêm module mới với Repository
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc backend
