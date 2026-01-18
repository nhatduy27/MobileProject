import { Module } from '@nestjs/common';
import { CartService } from './services';
import { FirestoreCartRepository } from './repositories';
import { CartController } from './controllers';
import { ProductsModule } from '../products/products.module';
import { ShopsModule } from '../shops/shops.module';
import { CART_REPOSITORY } from './interfaces';

@Module({
  imports: [ProductsModule, ShopsModule],
  controllers: [CartController],
  providers: [
    CartService,
    {
      provide: CART_REPOSITORY,
      useClass: FirestoreCartRepository,
    },
  ],
  exports: [CartService],
})
export class CartModule {}
