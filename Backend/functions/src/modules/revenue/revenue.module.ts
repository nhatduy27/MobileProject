import { Module } from '@nestjs/common';
import { RevenueController } from './controllers/revenue.controller';
import { RevenueService } from './services/revenue.service';
import { RevenueRepository } from './repositories/revenue.repository';
import { ShopsModule } from '../shops/shops.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';

@Module({
  imports: [FirebaseModule, ShopsModule],
  controllers: [RevenueController],
  providers: [RevenueService, RevenueRepository],
  exports: [RevenueService, RevenueRepository],
})
export class RevenueModule {}
