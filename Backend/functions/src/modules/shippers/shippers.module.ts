import { Module } from '@nestjs/common';
import { ShippersService } from './shippers.service';
import { ShipperApplicationsController } from './shipper-applications.controller';
import { OwnerShippersController } from './owner-shippers.controller';
import { ShipperNotificationsController } from './shipper-notifications.controller';
import { FirestoreShippersRepository } from './repositories/firestore-shippers.repository';
import { UsersModule } from '../users/users.module';
import { ShopsModule } from '../shops/shops.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';
import { SharedModule } from '../../shared/shared.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { WalletsModule } from '../wallets/wallets.module';

@Module({
  imports: [FirebaseModule, UsersModule, ShopsModule, SharedModule, NotificationsModule, WalletsModule],
  controllers: [
    ShipperApplicationsController,
    OwnerShippersController,
    ShipperNotificationsController,
  ],
  providers: [
    ShippersService,
    {
      provide: 'IShippersRepository',
      useClass: FirestoreShippersRepository,
    },
  ],
  exports: [ShippersService, 'IShippersRepository'],
})
export class ShippersModule {}
