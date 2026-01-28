import { Module } from '@nestjs/common';
import { BuyersService } from './services/buyers.service';
import { BuyersStatsService } from './services/buyers-stats.service';
import { BuyersOwnerController } from './controllers/buyers-owner.controller';
import { FirestoreBuyersRepository } from './repositories/firestore-buyers.repository';
import { ShopsModule } from '../shops/shops.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';

@Module({
  imports: [FirebaseModule, ShopsModule],
  controllers: [BuyersOwnerController],
  providers: [
    BuyersService,
    BuyersStatsService,
    {
      provide: 'IBuyersRepository',
      useClass: FirestoreBuyersRepository,
    },
  ],
  exports: [BuyersService, BuyersStatsService, 'IBuyersRepository'],
})
export class BuyersModule {}
