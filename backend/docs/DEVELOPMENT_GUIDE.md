# Hướng dẫn Phát triển Backend

## 📋 Tổng quan

Tài liệu này mô tả các quy ước lập trình, cấu trúc code và best practices khi phát triển backend NestJS cho dự án FoodApp.

## 🎯 Nguyên tắc Thiết kế

Backend này tuân theo:
- **Kiến trúc Phân tầng (Layered Architecture)**: Tách biệt rõ ràng các tầng
- **Kiến trúc Sạch (Clean Architecture)**: Logic nghiệp vụ độc lập với framework
- **Đảo ngược Phụ thuộc (Dependency Inversion)**: Phụ thuộc vào abstraction, không phải implementation
- **Mô hình Cổng & Bộ chuyển đổi (Ports & Adapters)**: Dễ dàng thay đổi implementation

## 🏗️ Ánh xạ Tầng và File

### 1. Tầng Trình bày (Presentation Layer) - Controllers

**Vị trí:** `src/modules/{module-name}/{module-name}.controller.ts`

**Trách nhiệm:**
- Xử lý HTTP requests/responses
- Validate input thông qua DTOs
- Gọi Service để xử lý logic
- Trả về response cho client

**Ví dụ:**

```typescript
// ✅ ĐÚNG: Controller chỉ xử lý HTTP
@Controller('orders')
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  @Post()
  async createOrder(@Body() dto: CreateOrderDto) {
    return this.ordersService.createOrder(dto);
  }

  @Get(':id')
  async getOrder(@Param('id') id: string) {
    return this.ordersService.getOrderById(id);
  }
}
```

```typescript
// ❌ SAI: Controller không nên chứa logic nghiệp vụ
@Controller('orders')
export class OrdersController {
  constructor(private readonly orderRepository: OrderRepository) {}

  @Post()
  async createOrder(@Body() dto: CreateOrderDto) {
    // ❌ Logic nghiệp vụ không thuộc Controller
    const order = new Order({
      ...dto,
      status: OrderStatus.PENDING,
    });
    
    // ❌ Controller không nên gọi repository trực tiếp
    return this.orderRepository.create(order);
  }
}
```

### 2. Tầng Ứng dụng (Application Layer) - Services

**Vị trí:** `src/modules/{module-name}/{module-name}.service.ts`

**Trách nhiệm:**
- Chứa logic nghiệp vụ chính
- Điều phối giữa Repository và các Service khác
- Xử lý transactions và business rules
- Sử dụng các Shared Services (cache, notifications, events)

**Ví dụ:**

```typescript
// ✅ ĐÚNG: Service chứa logic nghiệp vụ
@Injectable()
export class OrdersService {
  constructor(
    private readonly orderRepository: OrderRepository, // Port (abstraction)
    private readonly cache: CachePort,
    private readonly notification: NotificationPort,
    private readonly eventBus: EventBusPort,
  ) {}

  async createOrder(dto: CreateOrderDto): Promise<OrderResponseDto> {
    // Logic nghiệp vụ: Tạo entity
    const items = dto.items.map(item => new OrderItem(item));
    
    // Lưu vào database thông qua Repository Port
    const order = await this.orderRepository.create({
      customerId: dto.customerId,
      sellerId: dto.sellerId,
      items,
      status: OrderStatus.PENDING,
      totalAmount: 0,
    });

    // Logic nghiệp vụ: Invalidate cache
    await this.cache.del(`orders:customer:${order.customerId}`);

    // Logic nghiệp vụ: Publish event
    await this.eventBus.publish('order.created', {
      orderId: order.id,
      customerId: order.customerId,
      totalAmount: order.totalAmount,
    });

    // Logic nghiệp vụ: Send notification
    await this.notification.sendToUser(order.customerId, {
      title: 'Đơn hàng đã được tạo',
      body: `Đơn hàng #${order.id} đã được tạo thành công`,
    });

    return this.mapToResponseDto(order);
  }
}
```

```typescript
// ❌ SAI: Service không nên chứa HTTP logic
@Injectable()
export class OrdersService {
  async createOrder(req: Request, res: Response) {
    // ❌ Service không làm việc với Request/Response
    const dto = req.body;
    // ...
    res.status(201).json(order);
  }
}
```

### 3. Tầng Miền (Domain Layer) - Entities & Repository Ports

**Vị trí:** 
- Entities: `src/modules/{module-name}/domain/{entity-name}.entity.ts`
- Repository Ports: `src/modules/{module-name}/domain/{entity-name}.repository.ts`

**Trách nhiệm:**
- **Entities**: Chứa business logic thuần túy, không phụ thuộc framework
- **Repository Ports**: Định nghĩa interface (abstract class) cho data access

**Ví dụ Entity:**

```typescript
// ✅ ĐÚNG: Entity chứa domain logic
export class Order {
  id: string;
  customerId: string;
  status: OrderStatus;
  items: OrderItem[];
  totalAmount: number;
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<Order>) {
    Object.assign(this, partial);
    this.calculateTotal();
  }

  // Domain logic: Tính tổng tiền
  private calculateTotal(): void {
    this.totalAmount = this.items.reduce(
      (sum, item) => sum + item.totalPrice,
      0,
    );
  }

  // Domain logic: Thay đổi trạng thái
  changeStatus(newStatus: OrderStatus): void {
    this.status = newStatus;
    this.updatedAt = new Date();
  }

  // Domain logic: Kiểm tra có thể hủy không
  canBeCancelled(): boolean {
    return [
      OrderStatus.PENDING,
      OrderStatus.CONFIRMED,
    ].includes(this.status);
  }

  // Domain logic: Hủy đơn hàng
  cancel(): void {
    if (!this.canBeCancelled()) {
      throw new Error(`Không thể hủy đơn hàng ở trạng thái ${this.status}`);
    }
    this.changeStatus(OrderStatus.CANCELLED);
  }
}
```

```typescript
// ❌ SAI: Entity không nên phụ thuộc framework hoặc database
export class Order {
  @Column() // ❌ Không dùng decorator của database
  id: string;

  async save() {
    // ❌ Entity không nên biết cách lưu mình
    await database.save(this);
  }

  async sendNotification() {
    // ❌ Entity không nên biết về infrastructure
    await firebase.messaging().send(...);
  }
}
```

**Ví dụ Repository Port:**

```typescript
// ✅ ĐÚNG: Repository Port là abstract class
export abstract class OrderRepository {
  abstract create(order: CreateOrderDto): Promise<Order>;
  abstract findById(id: string): Promise<Order | null>;
  abstract findByCustomer(customerId: string): Promise<Order[]>;
  abstract update(id: string, order: Partial<Order>): Promise<Order>;
  abstract delete(id: string): Promise<void>;
}
```

### 4. Tầng Hạ tầng (Infrastructure Layer) - Repository Adapters

**Vị trí:** `src/modules/{module-name}/infra/{adapter-name}.repository.ts`

**Trách nhiệm:**
- Implement Repository Port (abstract class)
- Xử lý database/external service operations
- Chuyển đổi giữa domain entities và database models

**Ví dụ:**

```typescript
// ✅ ĐÚNG: Adapter implement Port
@Injectable()
export class FirebaseOrderRepository extends OrderRepository {
  private readonly logger = new Logger(FirebaseOrderRepository.name);
  private orders: Map<string, Order> = new Map(); // Stub

  async create(orderData: CreateOrderDto): Promise<Order> {
    // TODO: Thay bằng Firebase Firestore
    this.logger.log(`[STUB] Creating order`);
    
    const newOrder = new Order({
      id: `order_${Date.now()}`,
      ...orderData,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
    
    this.orders.set(newOrder.id, newOrder);
    return newOrder;
  }

  async findById(id: string): Promise<Order | null> {
    // TODO: await admin.firestore().collection('orders').doc(id).get()
    return this.orders.get(id) || null;
  }

  async findByCustomer(customerId: string): Promise<Order[]> {
    // TODO: await admin.firestore()
    //   .collection('orders')
    //   .where('customerId', '==', customerId)
    //   .get()
    return Array.from(this.orders.values())
      .filter(order => order.customerId === customerId);
  }
}
```

## 🔌 Dependency Inversion và Ports & Adapters

### Nguyên tắc

**Dependency Inversion Principle:**
> Module cấp cao không nên phụ thuộc vào module cấp thấp. Cả hai nên phụ thuộc vào abstraction.

**Trong dự án này:**
- **Port (Cổng)** = Abstract class định nghĩa contract
- **Adapter (Bộ chuyển đổi)** = Concrete class implement Port
- **Service** inject Port, không biết về Adapter cụ thể

### Ví dụ Đầy đủ

```typescript
// 1. Domain Layer - Port (Abstract)
export abstract class CachePort {
  abstract get<T>(key: string): Promise<T | null>;
  abstract set<T>(key: string, value: T, ttlSeconds?: number): Promise<void>;
  abstract del(key: string): Promise<void>;
}

// 2. Infrastructure Layer - Adapter (Concrete)
@Injectable()
export class InMemoryCacheAdapter extends CachePort {
  private cache = new Map<string, any>();

  async get<T>(key: string): Promise<T | null> {
    return this.cache.get(key) || null;
  }

  async set<T>(key: string, value: T): Promise<void> {
    this.cache.set(key, value);
  }

  async del(key: string): Promise<void> {
    this.cache.delete(key);
  }
}

// 3. Module - Dependency Injection Binding
@Module({
  providers: [
    {
      provide: CachePort, // Token = Port
      useClass: InMemoryCacheAdapter, // Implementation = Adapter
    },
  ],
  exports: [CachePort],
})
export class SharedModule {}

// 4. Service - Inject Port
@Injectable()
export class OrdersService {
  constructor(
    private readonly cache: CachePort, // ✅ Inject Port, không phải Adapter
  ) {}

  async getOrder(id: string) {
    // Service không biết cache là in-memory hay Redis
    const cached = await this.cache.get<Order>(`order:${id}`);
    if (cached) return cached;
    // ...
  }
}
```

### Lợi ích

✅ **Dễ test**: Mock Port thay vì mock concrete class
```typescript
// Test
const mockCache: CachePort = {
  get: jest.fn(),
  set: jest.fn(),
  del: jest.fn(),
};

const service = new OrdersService(mockCache);
```

✅ **Dễ thay đổi implementation**: Redis thay cho In-memory
```typescript
// Chỉ cần thay binding trong Module
@Module({
  providers: [
    {
      provide: CachePort,
      useClass: RedisCacheAdapter, // Thay đổi ở đây thôi
    },
  ],
})
```

✅ **Logic nghiệp vụ độc lập**: Service không bị ảnh hưởng khi đổi database

## 📝 Quy ước Lập trình

### Naming Conventions

| Loại | Quy ước | Ví dụ |
|------|---------|-------|
| Class | PascalCase | `OrdersService`, `AuthController` |
| Interface/Abstract | PascalCase + suffix | `OrderRepository`, `CachePort` |
| File | kebab-case | `orders.service.ts`, `auth.controller.ts` |
| Variable/Function | camelCase | `createOrder`, `userId` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Enum | PascalCase | `OrderStatus`, `UserRole` |

### File Structure

```
src/modules/{module-name}/
├── domain/                    # Domain layer
│   ├── {entity}.entity.ts    # Domain entities
│   └── {entity}.repository.ts # Repository ports
├── infra/                     # Infrastructure layer
│   └── {adapter}.repository.ts # Repository adapters
├── dto/                       # Data Transfer Objects
│   └── {module}.dto.ts
├── {module}.controller.ts     # Presentation layer
├── {module}.service.ts        # Application layer
└── {module}.module.ts         # Module definition
```

### Code Style

**1. Luôn dùng TypeScript types**

```typescript
// ✅ ĐÚNG
function calculateTotal(items: OrderItem[]): number {
  return items.reduce((sum, item) => sum + item.price, 0);
}

// ❌ SAI
function calculateTotal(items) {
  return items.reduce((sum, item) => sum + item.price, 0);
}
```

**2. Dùng async/await thay vì Promise chains**

```typescript
// ✅ ĐÚNG
async createOrder(dto: CreateOrderDto) {
  const order = await this.repository.create(dto);
  await this.cache.del(`orders:customer:${order.customerId}`);
  return order;
}

// ❌ SAI
createOrder(dto: CreateOrderDto) {
  return this.repository.create(dto)
    .then(order => {
      return this.cache.del(`orders:customer:${order.customerId}`)
        .then(() => order);
    });
}
```

**3. Validation với DTOs**

```typescript
// ✅ ĐÚNG: Dùng class-validator
export class CreateOrderDto {
  @IsString()
  @IsNotEmpty()
  customerId: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => OrderItemDto)
  items: OrderItemDto[];
}

// ❌ SAI: Validate thủ công trong Service
if (!dto.customerId || typeof dto.customerId !== 'string') {
  throw new Error('Invalid customerId');
}
```

**4. Error Handling**

```typescript
// ✅ ĐÚNG: Dùng NestJS exceptions
if (!order) {
  throw new NotFoundException(`Order not found: ${id}`);
}

if (!order.canBeCancelled()) {
  throw new BadRequestException('Order cannot be cancelled');
}

// ❌ SAI: Throw generic Error
if (!order) {
  throw new Error('Not found');
}
```

**5. Logging**

```typescript
// ✅ ĐÚNG: Dùng Logger với context
@Injectable()
export class OrdersService {
  private readonly logger = new Logger(OrdersService.name);

  async createOrder(dto: CreateOrderDto) {
    this.logger.log(`Creating order for customer: ${dto.customerId}`);
    // ...
  }
}

// ❌ SAI: console.log
console.log('Creating order...');
```

## ✅ DO / ❌ DON'T

### Controllers

| ✅ DO | ❌ DON'T |
|-------|----------|
| Chỉ xử lý HTTP concerns | Chứa logic nghiệp vụ |
| Validate input qua DTOs | Validate thủ công |
| Inject Service, không inject Repository | Inject Repository trực tiếp |
| Return response đơn giản | Xử lý database operations |

### Services

| ✅ DO | ❌ DON'T |
|-------|----------|
| Chứa logic nghiệp vụ | Xử lý HTTP Request/Response |
| Inject Ports (abstractions) | Inject Adapters cụ thể |
| Sử dụng domain entities | Làm việc trực tiếp với database models |
| Handle business rules | Query database trực tiếp |

### Entities

| ✅ DO | ❌ DON'T |
|-------|----------|
| Chứa domain logic thuần túy | Phụ thuộc vào framework |
| Immutable khi có thể | Chứa database decorators |
| Có methods nghiệp vụ | Biết cách save/load mình |
| Validate business rules | Gọi external services |

### Repositories

| ✅ DO | ❌ DON'T |
|-------|----------|
| Port là abstract class | Port là interface (vì DI) |
| Adapter implement Port | Service implement Port |
| Xử lý data persistence | Chứa business logic |
| Convert entities ↔ models | Expose database details |

## 🧪 Testing

### Unit Test Service

```typescript
describe('OrdersService', () => {
  let service: OrdersService;
  let mockRepository: jest.Mocked<OrderRepository>;
  let mockCache: jest.Mocked<CachePort>;

  beforeEach(() => {
    // Mock Ports
    mockRepository = {
      create: jest.fn(),
      findById: jest.fn(),
    } as any;

    mockCache = {
      get: jest.fn(),
      set: jest.fn(),
      del: jest.fn(),
    } as any;

    service = new OrdersService(
      mockRepository,
      mockCache,
      // ... other mocks
    );
  });

  it('should create order and invalidate cache', async () => {
    const dto = { customerId: 'user_1', /* ... */ };
    const order = new Order({ id: 'order_1', ...dto });

    mockRepository.create.mockResolvedValue(order);

    const result = await service.createOrder(dto);

    expect(mockRepository.create).toHaveBeenCalledWith(dto);
    expect(mockCache.del).toHaveBeenCalledWith('orders:customer:user_1');
    expect(result.id).toBe('order_1');
  });
});
```

## 📚 Tài liệu Liên quan

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Tổng quan kiến trúc
- [HOW_TO_ADD_A_NEW_MODULE.md](./HOW_TO_ADD_A_NEW_MODULE.md) - Hướng dẫn thêm module mới
- [REPOSITORY_GUIDE.md](./REPOSITORY_GUIDE.md) - Hướng dẫn Repository pattern
- [API_CONTRACT.md](./API_CONTRACT.md) - Tài liệu API endpoints
