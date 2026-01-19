import { Module, forwardRef } from '@nestjs/common';
import { OrderStateMachineService, OrdersService } from './services';
import { FirestoreOrdersRepository } from './repositories';
import { OrdersController, OrdersOwnerController, OrdersShipperController } from './controllers';
import { ProductsModule } from '../products/products.module';
import { ShopsModule } from '../shops/shops.module';
import { CartModule } from '../cart/cart.module';
import { ShippersModule } from '../shippers/shippers.module';
import { UsersModule } from '../users/users.module';
import { ConfigModule } from '../../core/config/config.module';
import { ORDERS_REPOSITORY } from './interfaces';

@Module({
  imports: [
    ConfigModule,
    ProductsModule,
    ShopsModule,
    CartModule,
    UsersModule,
    forwardRef(() => ShippersModule),
  ],
  controllers: [OrdersOwnerController, OrdersShipperController, OrdersController],
  providers: [
    OrderStateMachineService,
    OrdersService,
    {
      provide: ORDERS_REPOSITORY,
      useClass: FirestoreOrdersRepository,
    },
  ],
  exports: [OrderStateMachineService, OrdersService],
})
export class OrdersModule {}
