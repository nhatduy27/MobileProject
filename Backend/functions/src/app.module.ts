import { Module } from '@nestjs/common';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { EmailModule } from './modules/email';

// Feature modules - EPIC 01
import { CategoriesModule } from './modules/categories/categories.module';
import { AdminModule } from './modules/admin/admin.module';

// Feature modules - EPIC 02
import { AuthModule } from './modules/auth/auth.module';

// Feature modules - EPIC 03
import { UsersModule } from './modules/users/users.module';
import { FavoritesModule } from './modules/favorites/favorites.module';
import { ShopsModule } from './modules/shops/shops.module';
import { ProductsModule } from './modules/products/products.module';
import { ShippersModule } from './modules/shippers/shippers.module';
import { CartModule } from './modules/cart/cart.module';
import { OrdersModule } from './modules/orders/orders.module';
import { VouchersModule } from './modules/vouchers/vouchers.module';
import { NotificationsModule } from './modules/notifications/notifications.module';
import { PaymentsModule } from './modules/payments/payments.module';
import { WalletsModule } from './modules/wallets/wallets.module';
import { ChatbotModule } from './modules/chatbot/chatbot.module';
import { BuyersModule } from './modules/buyers/buyers.module';
import { ChatModule } from './modules/chat/chat.module';
import { RevenueModule } from './modules/revenue/revenue.module';
import { GpsModule } from './modules/gps/gps.module';
import { DeliveryPointsModule } from './modules/delivery-points/delivery-points.module';

@Module({
  imports: [
    // ============================================
    // Core infrastructure (always enabled)
    // ============================================
    CoreModule,
    SharedModule,
    EmailModule, // Global module for sending emails

    // EPIC 02: Auth ✅
    AuthModule,

    // ============================================
    // Feature modules
    // ============================================
    // EPIC 01: Admin Core ✅
    CategoriesModule,
    AdminModule,

    // EPIC 03: User Management ✅
    UsersModule,
    FavoritesModule,

    // EPIC 04: Marketplace Core ✅
    ShopsModule,
    ProductsModule,
    ShippersModule,
    CartModule,
    OrdersModule,
    VouchersModule,
    BuyersModule,
    RevenueModule,

    // EPIC 05: Notifications (CORE)
    NotificationsModule,

    // EPIC 06: Payments & Wallets ✅
    PaymentsModule,
    WalletsModule,

    // AI Features
    ChatbotModule,

    // Chat
    ChatModule,
    // Shared Master Data
    DeliveryPointsModule,

    // GPS / Shipper Route Optimization ✅
    GpsModule,
  ],
})
export class AppModule {}
