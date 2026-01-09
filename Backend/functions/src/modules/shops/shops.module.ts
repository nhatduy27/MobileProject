import { Module } from '@nestjs/common';
import { OwnerShopsController } from './controllers/owner-shops.controller';
import { ShopsController } from './controllers/shops.controller';
import { ShopsService } from './services/shops.service';
import { AnalyticsService } from './services/analytics.service';
import { FirestoreShopsRepository } from './repositories/firestore-shops.repository';
import { SHOPS_REPOSITORY } from './interfaces';
import { FirebaseModule } from '../../core/firebase/firebase.module';
import { SharedModule } from '../../shared/shared.module';

@Module({
  imports: [FirebaseModule, SharedModule],
  controllers: [OwnerShopsController, ShopsController],
  providers: [
    ShopsService,
    AnalyticsService,
    {
      provide: SHOPS_REPOSITORY,
      useClass: FirestoreShopsRepository,
    },
  ],
  exports: [ShopsService, SHOPS_REPOSITORY],
})
export class ShopsModule {}
