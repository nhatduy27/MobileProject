import { Module } from '@nestjs/common';
import {
  ShipperRemovalRequestsService,
  SHIPPER_REMOVAL_REQUESTS_REPOSITORY,
} from './services/shipper-removal-requests.service';
import { FirestoreShipperRemovalRequestsRepository } from './repositories/firestore-shipper-removal-requests.repository';
import { ShipperRemovalRequestsController } from './controllers/shipper-removal-requests.controller';
import { OwnerRemovalRequestsController } from './controllers/owner-removal-requests.controller';
import { UsersModule } from '../users/users.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';

@Module({
  imports: [UsersModule, NotificationsModule, FirebaseModule],
  controllers: [ShipperRemovalRequestsController, OwnerRemovalRequestsController],
  providers: [
    ShipperRemovalRequestsService,
    {
      provide: SHIPPER_REMOVAL_REQUESTS_REPOSITORY,
      useClass: FirestoreShipperRemovalRequestsRepository,
    },
  ],
  exports: [ShipperRemovalRequestsService],
})
export class ShipperRemovalRequestsModule {}
