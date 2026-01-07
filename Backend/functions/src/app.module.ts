import { Module } from '@nestjs/common';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';

// Feature modules - uncomment khi làm đến Epic tương ứng
// import { CategoriesModule } from './modules/categories/categories.module';  // Epic 0: Admin
import { AuthModule } from './modules/auth/auth.module';                       // Epic 1: Auth ✅
// import { UsersModule } from './modules/users/users.module';                 // Epic 2: User
// import { ShopsModule } from './modules/shops/shops.module';                 // Epic 3: Shop
// import { ProductsModule } from './modules/products/products.module';        // Epic 3: Product
// import { OrdersModule } from './modules/orders/orders.module';              // Epic 4: Order
// import { PaymentsModule } from './modules/payments/payments.module';        // Epic 5: Payment
// import { WalletsModule } from './modules/wallets/wallets.module';           // Epic 6: Wallet
// import { NotificationsModule } from './modules/notifications/notifications.module'; // Epic 7

@Module({
  imports: [
    // ============================================
    // Core infrastructure (always enabled)
    // ============================================
    CoreModule,
    SharedModule,

    // ============================================
    // Feature modules - uncomment theo Epic order
    // ============================================
    // CategoriesModule,   // Epic 0: Admin Categories
    AuthModule,            // Epic 1: Authentication ✅
    // UsersModule,        // Epic 2: User Management
    // ShopsModule,        // Epic 3: Shop & Product
    // ProductsModule,     // Epic 3: Shop & Product
    // OrdersModule,       // Epic 4: Order
    // PaymentsModule,     // Epic 5: Payment
    // WalletsModule,      // Epic 6: Wallet
    // NotificationsModule,// Epic 7: Notification
  ],
})
export class AppModule {}
