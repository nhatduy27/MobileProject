import { Module } from '@nestjs/common';
import { BuyersService } from './services/buyers.service';
import { BuyersOwnerController } from './controllers/buyers-owner.controller';
import { FirestoreBuyersRepository } from './repositories/firestore-buyers.repository';
import { OrdersModule } from '../orders/orders.module';
import { ShopsModule } from '../shops/shops.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';

@Module({
  imports: [FirebaseModule, OrdersModule, ShopsModule],
  controllers: [BuyersOwnerController],
  providers: [
    BuyersService,
    {
      provide: 'IBuyersRepository',
      useClass: FirestoreBuyersRepository,
    },
  ],
  exports: [BuyersService, 'IBuyersRepository'],
})
export class BuyersModule {}
