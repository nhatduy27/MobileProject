import { Module, forwardRef } from '@nestjs/common';
import {
  OrderStateMachineService,
  OrdersService,
  ReviewsService,
  REVIEWS_REPOSITORY,
} from './services';
import { FirestoreOrdersRepository, FirestoreReviewsRepository } from './repositories';
import {
  OrdersController,
  OrdersOwnerController,
  OrdersShipperController,
  OrdersAdminController,
  ReviewsController,
  OwnerReviewsController,
} from './controllers';
import { ProductsModule } from '../products/products.module';
import { ShopsModule } from '../shops/shops.module';
import { CartModule } from '../cart/cart.module';
import { ShippersModule } from '../shippers/shippers.module';
import { UsersModule } from '../users/users.module';
import { VouchersModule } from '../vouchers/vouchers.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { WalletsModule } from '../wallets/wallets.module';
import { PaymentsModule } from '../payments/payments.module';
import { ConfigModule } from '../../core/config/config.module';
import { ORDERS_REPOSITORY } from './interfaces';

@Module({
  imports: [
    ConfigModule,
    ProductsModule,
    ShopsModule,
    CartModule,
    UsersModule,
    VouchersModule,
    NotificationsModule,
    WalletsModule,
    forwardRef(() => PaymentsModule),
    forwardRef(() => ShippersModule),
  ],
  controllers: [
    OrdersOwnerController,
    OrdersShipperController,
    OrdersAdminController,
    OrdersController,
    ReviewsController,
    OwnerReviewsController,
  ],
  providers: [
    OrderStateMachineService,
    OrdersService,
    ReviewsService,
    {
      provide: ORDERS_REPOSITORY,
      useClass: FirestoreOrdersRepository,
    },
    {
      provide: REVIEWS_REPOSITORY,
      useClass: FirestoreReviewsRepository,
    },
  ],
  exports: [
    OrderStateMachineService,
    OrdersService,
    ReviewsService,
    ORDERS_REPOSITORY,
    REVIEWS_REPOSITORY,
  ],
})
export class OrdersModule {}
