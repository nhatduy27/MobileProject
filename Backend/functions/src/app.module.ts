import { Module, MiddlewareConsumer, NestModule, RequestMethod } from '@nestjs/common';
import { CoreModule } from './core/core.module';
import { SharedModule } from './shared/shared.module';
import { EmailModule } from './modules/email';
import { RawBodyMiddleware } from './core/middleware';

// Utility modules
import { HealthModule } from './modules/health';

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
import { ShipperRemovalRequestsModule } from './modules/shipper-removal-requests/shipper-removal-requests.module';

@Module({
  imports: [
    // ============================================
    // Core infrastructure (always enabled)
    // ============================================
    CoreModule,
    SharedModule,
    EmailModule, // Global module for sending emails

    // Utilities
    HealthModule, // Health check endpoint

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

    // Shipper Features
    ShipperRemovalRequestsModule,
  ],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    // Apply RawBodyMiddleware for multipart/form-data in Cloud Functions
    // This middleware parses rawBody for file uploads since Cloud Run pre-consumes the stream
    consumer.apply(RawBodyMiddleware).forRoutes(
      // Owner product image upload
      // Owner product images upload (multiple)
      { path: 'owner/products/:id/images', method: RequestMethod.POST },
      // Owner product create (with optional image)
      { path: 'owner/products', method: RequestMethod.POST },
      // Owner product update (with optional image)
      { path: 'owner/products/:id', method: RequestMethod.PUT },
      // User avatar upload
      { path: 'me/avatar', method: RequestMethod.POST },
      // Shipper driver license upload
      { path: 'me/vehicle/driver-license', method: RequestMethod.POST },
      // Shipper application (multipart)
      { path: 'shipper-applications', method: RequestMethod.POST },
    );
  }
}
