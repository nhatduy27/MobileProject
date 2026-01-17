import { Module } from '@nestjs/common';
import { FavoritesController } from './favorites.controller';
import { FavoritesService } from './favorites.service';
import { FirestoreFavoritesRepository } from './repositories/firestore-favorites.repository';
import { FAVORITES_REPOSITORY } from './interfaces';
import { FirebaseModule } from '../../core/firebase/firebase.module';
import { ProductsModule } from '../products/products.module'; 

@Module({
  imports: [FirebaseModule, ProductsModule],
  controllers: [FavoritesController],
  providers: [
    FavoritesService,
    {
      provide: FAVORITES_REPOSITORY,
      useClass: FirestoreFavoritesRepository,
    },
  ],
  exports: [FavoritesService],
})
export class FavoritesModule {}