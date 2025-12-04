import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { SharedModule } from './shared/shared.module';
import { AuthModule } from './modules/auth/auth.module';
import { OrdersModule } from './modules/orders/orders.module';

/**
 * App Module (Root Module)
 * 
 * Main application module that imports all feature modules.
 * - SharedModule: Provides global shared services (cache, notifications, events)
 * - AuthModule: Authentication and user management
 * - OrdersModule: Order management with full integration of shared services
 */
@Module({
  imports: [
    SharedModule,
    AuthModule,
    OrdersModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
