/**
 * GPS Module
 *
 * Handles shipper route optimization and trip management for KTX delivery.
 * Integrates with Google Routes API for waypoint optimization.
 */

import { Module } from '@nestjs/common';
import { GpsService } from './services/gps.service';
import { GoogleRoutesService } from './services/google-routes.service';
import { DeliveryPointsRepository } from './repositories/delivery-points.repository';
import { ShipperTripsRepository } from './repositories/shipper-trips.repository';
import { GpsController } from './controllers/gps.controller';
import { CoreModule } from '../../core/core.module';
import { OrdersModule } from '../orders/orders.module';
import { ShippersModule } from '../shippers/shippers.module';
import { DeliveryPointsModule } from '../delivery-points/delivery-points.module';

@Module({
  imports: [CoreModule, OrdersModule, ShippersModule, DeliveryPointsModule],
  controllers: [GpsController],
  providers: [GpsService, GoogleRoutesService, DeliveryPointsRepository, ShipperTripsRepository],
  exports: [GpsService],
})
export class GpsModule {}
