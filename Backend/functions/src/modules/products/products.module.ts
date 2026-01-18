import { Module } from '@nestjs/common';
import { ProductsService } from './services';
import { FirestoreProductsRepository } from './repositories';
import { OwnerProductsController, ProductsController } from './controllers';
import { ShopsModule } from '../shops/shops.module';
import { SharedModule } from '../../shared/shared.module';
import { CategoriesModule } from '../categories/categories.module';

@Module({
  imports: [ShopsModule, SharedModule, CategoriesModule],
  controllers: [OwnerProductsController, ProductsController],
  providers: [
    ProductsService,
    {
      provide: 'PRODUCTS_REPOSITORY',
      useClass: FirestoreProductsRepository,
    },
  ],
  exports: [ProductsService, 'PRODUCTS_REPOSITORY'],
})
export class ProductsModule {}
