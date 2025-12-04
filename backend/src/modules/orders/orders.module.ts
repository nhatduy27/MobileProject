import { Module } from '@nestjs/common';
import { OrdersController } from './orders.controller';
import { OrdersService } from './orders.service';
import { OrderRepository } from './domain/order.repository';
import { FirebaseOrderRepository } from './infra/firebase-order.repository';

/**
 * Orders Module
 * 
 * Feature module for order operations.
 * Uses Dependency Inversion by binding OrderRepository to FirebaseOrderRepository.
 * Automatically gets access to SharedModule services (cache, notifications, events).
 */
@Module({
  controllers: [OrdersController],
  providers: [
    OrdersService,
    // Bind abstract OrderRepository to concrete FirebaseOrderRepository
    {
      provide: OrderRepository,
      useClass: FirebaseOrderRepository,
    },
  ],
  exports: [OrdersService],
})
export class OrdersModule {}
