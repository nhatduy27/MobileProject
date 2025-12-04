# Hướng dẫn Thêm Module Mới

## 📋 Tổng quan

Tài liệu này hướng dẫn từng bước cách thêm một feature module mới vào backend, tuân theo kiến trúc Layered + Clean Architecture với Ports & Adapters.

## 🎯 Ví dụ: Tạo Products Module

Chúng ta sẽ tạo một module `Products` để quản lý sản phẩm thực phẩm trong ứng dụng.

## 📁 Bước 1: Tạo Cấu trúc Thư mục

Tạo các thư mục và file sau trong `src/modules/products/`:

```
src/modules/products/
├── domain/
│   ├── product.entity.ts
│   └── product.repository.ts
├── infra/
│   └── firebase-product.repository.ts
├── dto/
│   └── product.dto.ts
├── products.controller.ts
├── products.service.ts
└── products.module.ts
```

**Lệnh tạo thư mục:**

```bash
cd src/modules
mkdir -p products/domain products/infra products/dto
cd products
touch domain/product.entity.ts
touch domain/product.repository.ts
touch infra/firebase-product.repository.ts
touch dto/product.dto.ts
touch products.controller.ts
touch products.service.ts
touch products.module.ts
```

## 📝 Bước 2: Tạo Domain Entity

**File: `domain/product.entity.ts`**

```typescript
/**
 * Product Entity (Domain Model)
 * 
 * Đại diện cho một sản phẩm thực phẩm trong hệ thống.
 * Entity này độc lập với infrastructure và framework.
 */
export enum ProductCategory {
  FOOD = 'FOOD',
  BEVERAGE = 'BEVERAGE',
  DESSERT = 'DESSERT',
  COMBO = 'COMBO',
}

export class Product {
  id: string;
  sellerId: string;
  name: string;
  description: string;
  category: ProductCategory;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<Product>) {
    Object.assign(this, partial);
    this.validatePrice();
  }

  /**
   * Domain logic: Validate giá sản phẩm
   */
  private validatePrice(): void {
    if (this.price < 0) {
      throw new Error('Giá sản phẩm không thể âm');
    }
  }

  /**
   * Domain logic: Đánh dấu sản phẩm hết hàng
   */
  markAsUnavailable(): void {
    this.isAvailable = false;
    this.updatedAt = new Date();
  }

  /**
   * Domain logic: Đánh dấu sản phẩm còn hàng
   */
  markAsAvailable(): void {
    this.isAvailable = true;
    this.updatedAt = new Date();
  }

  /**
   * Domain logic: Cập nhật giá
   */
  updatePrice(newPrice: number): void {
    if (newPrice < 0) {
      throw new Error('Giá sản phẩm không thể âm');
    }
    this.price = newPrice;
    this.updatedAt = new Date();
  }

  /**
   * Domain logic: Kiểm tra sản phẩm có thuộc về seller không
   */
  belongsToSeller(sellerId: string): boolean {
    return this.sellerId === sellerId;
  }
}
```

## 🔌 Bước 3: Tạo Repository Port (Abstract)

**File: `domain/product.repository.ts`**

```typescript
import { Product } from './product.entity';

/**
 * Type cho việc tạo sản phẩm mới
 */
export type CreateProductDto = {
  sellerId: string;
  name: string;
  description: string;
  category: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
};

/**
 * Product Repository Port (Abstraction)
 * 
 * Abstract class định nghĩa contract cho product data access.
 * Implementation có thể dùng Firebase Firestore, PostgreSQL, MongoDB, etc.
 */
export abstract class ProductRepository {
  /**
   * Tạo sản phẩm mới
   */
  abstract create(product: CreateProductDto): Promise<Product>;

  /**
   * Tìm sản phẩm theo ID
   */
  abstract findById(id: string): Promise<Product | null>;

  /**
   * Tìm tất cả sản phẩm của seller
   */
  abstract findBySeller(sellerId: string): Promise<Product[]>;

  /**
   * Tìm sản phẩm theo category
   */
  abstract findByCategory(category: string): Promise<Product[]>;

  /**
   * Tìm sản phẩm còn hàng
   */
  abstract findAvailable(): Promise<Product[]>;

  /**
   * Cập nhật sản phẩm
   */
  abstract update(id: string, product: Partial<Product>): Promise<Product>;

  /**
   * Xóa sản phẩm
   */
  abstract delete(id: string): Promise<void>;
}
```

## 🔧 Bước 4: Tạo Repository Adapter (Implementation)

**File: `infra/firebase-product.repository.ts`**

```typescript
import { Injectable, Logger } from '@nestjs/common';
import { ProductRepository, CreateProductDto } from '../domain/product.repository';
import { Product, ProductCategory } from '../domain/product.entity';

/**
 * Firebase Product Repository Adapter
 * 
 * Stub implementation cho Firebase Firestore.
 * TODO: Tích hợp Firebase Admin SDK cho Firestore operations
 */
@Injectable()
export class FirebaseProductRepository extends ProductRepository {
  private readonly logger = new Logger(FirebaseProductRepository.name);

  // Temporary in-memory storage cho demo
  private products: Map<string, Product> = new Map();
  private productIdCounter = 1;

  async create(productData: CreateProductDto): Promise<Product> {
    // TODO: Thay bằng Firebase Firestore
    // const docRef = await admin.firestore().collection('products').add({
    //   ...productData,
    //   createdAt: admin.firestore.FieldValue.serverTimestamp(),
    //   updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    // });

    this.logger.log(`[STUB] Creating product: ${productData.name}`);

    const newProduct = new Product({
      id: `product_${this.productIdCounter++}`,
      ...productData,
      category: productData.category as ProductCategory,
      createdAt: new Date(),
      updatedAt: new Date(),
    });

    this.products.set(newProduct.id, newProduct);
    return newProduct;
  }

  async findById(id: string): Promise<Product | null> {
    // TODO: await admin.firestore().collection('products').doc(id).get()
    this.logger.log(`[STUB] Finding product by ID: ${id}`);
    return this.products.get(id) || null;
  }

  async findBySeller(sellerId: string): Promise<Product[]> {
    // TODO: await admin.firestore()
    //   .collection('products')
    //   .where('sellerId', '==', sellerId)
    //   .get()
    this.logger.log(`[STUB] Finding products for seller: ${sellerId}`);
    return Array.from(this.products.values())
      .filter(product => product.sellerId === sellerId);
  }

  async findByCategory(category: string): Promise<Product[]> {
    // TODO: Firestore query với category
    this.logger.log(`[STUB] Finding products by category: ${category}`);
    return Array.from(this.products.values())
      .filter(product => product.category === category);
  }

  async findAvailable(): Promise<Product[]> {
    // TODO: Firestore query với isAvailable = true
    this.logger.log(`[STUB] Finding available products`);
    return Array.from(this.products.values())
      .filter(product => product.isAvailable);
  }

  async update(id: string, productData: Partial<Product>): Promise<Product> {
    // TODO: await admin.firestore().collection('products').doc(id).update(...)
    this.logger.log(`[STUB] Updating product: ${id}`);

    const existingProduct = this.products.get(id);
    if (!existingProduct) {
      throw new Error(`Product not found: ${id}`);
    }

    const updatedProduct = new Product({
      ...existingProduct,
      ...productData,
      updatedAt: new Date(),
    });

    this.products.set(id, updatedProduct);
    return updatedProduct;
  }

  async delete(id: string): Promise<void> {
    // TODO: await admin.firestore().collection('products').doc(id).delete()
    this.logger.log(`[STUB] Deleting product: ${id}`);
    this.products.delete(id);
  }
}
```

## 📦 Bước 5: Tạo DTOs

**File: `dto/product.dto.ts`**

```typescript
import {
  IsString,
  IsNotEmpty,
  IsNumber,
  Min,
  IsEnum,
  IsBoolean,
  IsOptional,
  IsUrl,
} from 'class-validator';
import { ProductCategory } from '../domain/product.entity';

/**
 * DTO để tạo sản phẩm mới
 */
export class CreateProductDto {
  @IsString()
  @IsNotEmpty()
  sellerId: string;

  @IsString()
  @IsNotEmpty()
  name: string;

  @IsString()
  @IsNotEmpty()
  description: string;

  @IsEnum(ProductCategory)
  category: ProductCategory;

  @IsNumber()
  @Min(0)
  price: number;

  @IsUrl()
  @IsOptional()
  imageUrl?: string;

  @IsBoolean()
  @IsOptional()
  isAvailable?: boolean = true;
}

/**
 * DTO để cập nhật sản phẩm
 */
export class UpdateProductDto {
  @IsString()
  @IsOptional()
  name?: string;

  @IsString()
  @IsOptional()
  description?: string;

  @IsEnum(ProductCategory)
  @IsOptional()
  category?: ProductCategory;

  @IsNumber()
  @Min(0)
  @IsOptional()
  price?: number;

  @IsUrl()
  @IsOptional()
  imageUrl?: string;

  @IsBoolean()
  @IsOptional()
  isAvailable?: boolean;
}

/**
 * DTO cho response sản phẩm
 */
export class ProductResponseDto {
  id: string;
  sellerId: string;
  name: string;
  description: string;
  category: string;
  price: number;
  imageUrl?: string;
  isAvailable: boolean;
  createdAt: Date;
  updatedAt: Date;
}
```

## 🧩 Bước 6: Tạo Service (Application Logic)

**File: `products.service.ts`**

```typescript
import { Injectable, Logger, NotFoundException, ForbiddenException } from '@nestjs/common';
import { ProductRepository } from './domain/product.repository';
import { Product, ProductCategory } from './domain/product.entity';
import { CreateProductDto, UpdateProductDto, ProductResponseDto } from './dto/product.dto';
import { CachePort } from '../../shared/cache/cache.port';
import { EventBusPort } from '../../shared/events/event-bus.port';

/**
 * Products Service (Application Layer)
 * 
 * Chứa logic nghiệp vụ cho quản lý sản phẩm.
 */
@Injectable()
export class ProductsService {
  private readonly logger = new Logger(ProductsService.name);

  constructor(
    private readonly productRepository: ProductRepository,
    private readonly cache: CachePort,
    private readonly eventBus: EventBusPort,
  ) {}

  /**
   * Tạo sản phẩm mới
   */
  async createProduct(dto: CreateProductDto): Promise<ProductResponseDto> {
    this.logger.log(`Creating product: ${dto.name} for seller: ${dto.sellerId}`);

    // Tạo sản phẩm qua repository
    const product = await this.productRepository.create({
      ...dto,
      isAvailable: dto.isAvailable ?? true,
    });

    // Invalidate cache của seller
    await this.cache.del(`products:seller:${product.sellerId}`);
    await this.cache.del('products:available');

    // Publish event
    await this.eventBus.publish('product.created', {
      productId: product.id,
      sellerId: product.sellerId,
      name: product.name,
      price: product.price,
    });

    return this.mapToResponseDto(product);
  }

  /**
   * Lấy sản phẩm theo ID
   */
  async getProductById(id: string): Promise<ProductResponseDto> {
    this.logger.log(`Getting product by ID: ${id}`);

    // Thử lấy từ cache trước
    const cacheKey = `product:${id}`;
    const cached = await this.cache.get<ProductResponseDto>(cacheKey);
    if (cached) {
      this.logger.log(`Product ${id} found in cache`);
      return cached;
    }

    // Lấy từ database
    const product = await this.productRepository.findById(id);
    if (!product) {
      throw new NotFoundException(`Không tìm thấy sản phẩm: ${id}`);
    }

    const response = this.mapToResponseDto(product);

    // Cache kết quả (5 phút)
    await this.cache.set(cacheKey, response, 300);

    return response;
  }

  /**
   * Lấy tất cả sản phẩm của seller
   */
  async getSellerProducts(sellerId: string): Promise<ProductResponseDto[]> {
    this.logger.log(`Getting products for seller: ${sellerId}`);

    const cacheKey = `products:seller:${sellerId}`;
    const cached = await this.cache.get<ProductResponseDto[]>(cacheKey);
    if (cached) {
      return cached;
    }

    const products = await this.productRepository.findBySeller(sellerId);
    const response = products.map(p => this.mapToResponseDto(p));

    await this.cache.set(cacheKey, response, 120);
    return response;
  }

  /**
   * Lấy sản phẩm theo category
   */
  async getProductsByCategory(category: ProductCategory): Promise<ProductResponseDto[]> {
    this.logger.log(`Getting products by category: ${category}`);

    const products = await this.productRepository.findByCategory(category);
    return products.map(p => this.mapToResponseDto(p));
  }

  /**
   * Lấy tất cả sản phẩm còn hàng
   */
  async getAvailableProducts(): Promise<ProductResponseDto[]> {
    this.logger.log('Getting available products');

    const cacheKey = 'products:available';
    const cached = await this.cache.get<ProductResponseDto[]>(cacheKey);
    if (cached) {
      return cached;
    }

    const products = await this.productRepository.findAvailable();
    const response = products.map(p => this.mapToResponseDto(p));

    await this.cache.set(cacheKey, response, 60);
    return response;
  }

  /**
   * Cập nhật sản phẩm
   */
  async updateProduct(
    id: string,
    dto: UpdateProductDto,
    sellerId: string,
  ): Promise<ProductResponseDto> {
    this.logger.log(`Updating product: ${id}`);

    // Kiểm tra sản phẩm tồn tại
    const product = await this.productRepository.findById(id);
    if (!product) {
      throw new NotFoundException(`Không tìm thấy sản phẩm: ${id}`);
    }

    // Kiểm tra quyền sở hữu
    if (!product.belongsToSeller(sellerId)) {
      throw new ForbiddenException('Bạn không có quyền cập nhật sản phẩm này');
    }

    // Cập nhật
    const updatedProduct = await this.productRepository.update(id, dto);

    // Invalidate caches
    await this.cache.del(`product:${id}`);
    await this.cache.del(`products:seller:${product.sellerId}`);
    await this.cache.del('products:available');

    // Publish event
    await this.eventBus.publish('product.updated', {
      productId: id,
      sellerId: product.sellerId,
      changes: dto,
    });

    return this.mapToResponseDto(updatedProduct);
  }

  /**
   * Xóa sản phẩm
   */
  async deleteProduct(id: string, sellerId: string): Promise<void> {
    this.logger.log(`Deleting product: ${id}`);

    const product = await this.productRepository.findById(id);
    if (!product) {
      throw new NotFoundException(`Không tìm thấy sản phẩm: ${id}`);
    }

    if (!product.belongsToSeller(sellerId)) {
      throw new ForbiddenException('Bạn không có quyền xóa sản phẩm này');
    }

    await this.productRepository.delete(id);

    // Invalidate caches
    await this.cache.del(`product:${id}`);
    await this.cache.del(`products:seller:${product.sellerId}`);
    await this.cache.del('products:available');

    // Publish event
    await this.eventBus.publish('product.deleted', {
      productId: id,
      sellerId: product.sellerId,
    });
  }

  /**
   * Map entity sang response DTO
   */
  private mapToResponseDto(product: Product): ProductResponseDto {
    return {
      id: product.id,
      sellerId: product.sellerId,
      name: product.name,
      description: product.description,
      category: product.category,
      price: product.price,
      imageUrl: product.imageUrl,
      isAvailable: product.isAvailable,
      createdAt: product.createdAt,
      updatedAt: product.updatedAt,
    };
  }
}
```

## 🎮 Bước 7: Tạo Controller (Presentation Layer)

**File: `products.controller.ts`**

```typescript
import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { ProductsService } from './products.service';
import {
  CreateProductDto,
  UpdateProductDto,
  ProductResponseDto,
} from './dto/product.dto';
import { ProductCategory } from './domain/product.entity';

/**
 * Products Controller (Presentation Layer)
 * 
 * Xử lý HTTP requests cho sản phẩm.
 */
@Controller('products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  /**
   * Tạo sản phẩm mới
   * POST /api/products
   */
  @Post()
  async createProduct(@Body() dto: CreateProductDto): Promise<ProductResponseDto> {
    return this.productsService.createProduct(dto);
  }

  /**
   * Lấy sản phẩm theo ID
   * GET /api/products/:id
   */
  @Get(':id')
  async getProductById(@Param('id') id: string): Promise<ProductResponseDto> {
    return this.productsService.getProductById(id);
  }

  /**
   * Lấy sản phẩm của seller
   * GET /api/products/seller/:sellerId
   */
  @Get('seller/:sellerId')
  async getSellerProducts(
    @Param('sellerId') sellerId: string,
  ): Promise<ProductResponseDto[]> {
    return this.productsService.getSellerProducts(sellerId);
  }

  /**
   * Lấy sản phẩm theo category
   * GET /api/products/category/:category
   */
  @Get('category/:category')
  async getProductsByCategory(
    @Param('category') category: ProductCategory,
  ): Promise<ProductResponseDto[]> {
    return this.productsService.getProductsByCategory(category);
  }

  /**
   * Lấy sản phẩm còn hàng
   * GET /api/products/available
   */
  @Get('available')
  async getAvailableProducts(): Promise<ProductResponseDto[]> {
    return this.productsService.getAvailableProducts();
  }

  /**
   * Cập nhật sản phẩm
   * PUT /api/products/:id
   * 
   * Note: Trong thực tế, cần thêm AuthGuard để lấy sellerId từ JWT token
   */
  @Put(':id')
  async updateProduct(
    @Param('id') id: string,
    @Body() dto: UpdateProductDto,
    // TODO: @CurrentUser() user: { id: string, ... }
  ): Promise<ProductResponseDto> {
    // Tạm thời hardcode sellerId, sau này lấy từ JWT token
    const sellerId = 'temp_seller_id';
    return this.productsService.updateProduct(id, dto, sellerId);
  }

  /**
   * Xóa sản phẩm
   * DELETE /api/products/:id
   */
  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async deleteProduct(@Param('id') id: string): Promise<void> {
    // TODO: Lấy sellerId từ JWT token
    const sellerId = 'temp_seller_id';
    return this.productsService.deleteProduct(id, sellerId);
  }
}
```

## 🔗 Bước 8: Tạo Module với Dependency Injection

**File: `products.module.ts`**

```typescript
import { Module } from '@nestjs/common';
import { ProductsController } from './products.controller';
import { ProductsService } from './products.service';
import { ProductRepository } from './domain/product.repository';
import { FirebaseProductRepository } from './infra/firebase-product.repository';

/**
 * Products Module
 * 
 * Feature module cho quản lý sản phẩm.
 * Sử dụng Dependency Inversion bằng cách bind ProductRepository (Port)
 * với FirebaseProductRepository (Adapter).
 * 
 * SharedModule (cache, events, notifications) được import tự động
 * vì nó là Global module.
 */
@Module({
  controllers: [ProductsController],
  providers: [
    ProductsService,
    // Dependency Injection: Bind Port với Adapter
    {
      provide: ProductRepository, // Token = Abstract Port
      useClass: FirebaseProductRepository, // Implementation = Concrete Adapter
    },
  ],
  exports: [ProductsService], // Export nếu module khác cần dùng
})
export class ProductsModule {}
```

## 🔌 Bước 9: Import Module vào App Module

**File: `src/app.module.ts`**

Mở file `app.module.ts` và thêm `ProductsModule` vào imports:

```typescript
import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { SharedModule } from './shared/shared.module';
import { AuthModule } from './modules/auth/auth.module';
import { OrdersModule } from './modules/orders/orders.module';
import { ProductsModule } from './modules/products/products.module'; // ← Thêm import

/**
 * App Module (Root Module)
 */
@Module({
  imports: [
    SharedModule,
    AuthModule,
    OrdersModule,
    ProductsModule, // ← Thêm vào đây
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
```

## ✅ Bước 10: Test Module

### 1. Build và chạy server

```bash
npm run start:dev
```

### 2. Test tạo sản phẩm

```bash
curl -X POST http://localhost:3000/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sellerId": "seller_1",
    "name": "Phở Bò",
    "description": "Phở bò truyền thống Hà Nội",
    "category": "FOOD",
    "price": 50000,
    "imageUrl": "https://example.com/pho-bo.jpg"
  }'
```

### 3. Test lấy sản phẩm

```bash
# Lấy theo ID
curl http://localhost:3000/api/products/product_1

# Lấy theo seller
curl http://localhost:3000/api/products/seller/seller_1

# Lấy sản phẩm còn hàng
curl http://localhost:3000/api/products/available
```

## 📋 Checklist Hoàn thành

Khi tạo module mới, đảm bảo đã làm:

- [ ] Tạo đủ cấu trúc thư mục (domain, infra, dto)
- [ ] Tạo Entity với domain logic
- [ ] Tạo Repository Port (abstract class)
- [ ] Tạo Repository Adapter implement Port
- [ ] Tạo DTOs với validation decorators
- [ ] Tạo Service với business logic
- [ ] Tạo Controller xử lý HTTP
- [ ] Tạo Module với DI binding (Port → Adapter)
- [ ] Import Module vào AppModule
- [ ] Test các endpoints

## 🎯 Best Practices

1. **Dependency Inversion**: Service luôn inject Port, không inject Adapter
2. **Single Responsibility**: Mỗi layer chỉ làm một việc
3. **Domain Logic**: Đặt trong Entity, không đặt trong Service
4. **Error Handling**: Dùng NestJS exceptions (NotFoundException, etc.)
5. **Validation**: Dùng DTOs với class-validator
6. **Logging**: Dùng Logger với context rõ ràng
7. **Cache**: Sử dụng CachePort cho các queries thường xuyên
8. **Events**: Publish events cho các hành động quan trọng

## 📚 Tài liệu Liên quan

- [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md) - Quy ước lập trình
- [REPOSITORY_GUIDE.md](./REPOSITORY_GUIDE.md) - Chi tiết về Repository pattern
- [API_CONTRACT.md](./API_CONTRACT.md) - Tài liệu API
