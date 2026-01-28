/**
 * Delivery Points Module
 *
 * Module dùng chung để quản lý điểm giao hàng (tòa nhà KTX).
 * Được sử dụng bởi:
 * - Customer: Chọn tòa nhà khi đặt hàng
 * - Shipper: Route optimization
 * - Admin: Quản lý active/inactive points
 */

import { Module } from '@nestjs/common';
import { DeliveryPointsService } from './services/delivery-points.service';
import { DeliveryPointsController } from './controllers/delivery-points.controller';
import { DeliveryPointsRepository } from '../gps/repositories/delivery-points.repository';
import { CoreModule } from '../../core/core.module';

@Module({
  imports: [CoreModule],
  controllers: [DeliveryPointsController],
  providers: [DeliveryPointsService, DeliveryPointsRepository],
  exports: [DeliveryPointsService, DeliveryPointsRepository],
})
export class DeliveryPointsModule {}
