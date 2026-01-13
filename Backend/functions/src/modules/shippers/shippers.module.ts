import { Module } from '@nestjs/common';
import { ShippersService } from './shippers.service';
import { ShipperApplicationsController } from './shipper-applications.controller';
import { OwnerShippersController } from './owner-shippers.controller';
import { FirestoreShippersRepository } from './repositories/firestore-shippers.repository';
import { UsersModule } from '../users/users.module';
import { ShopsModule } from '../shops/shops.module';
import { FirebaseModule } from '../../core/firebase/firebase.module';
import { SharedModule } from '../../shared/shared.module';

@Module({
  imports: [FirebaseModule, UsersModule, ShopsModule, SharedModule],
  controllers: [ShipperApplicationsController, OwnerShippersController],
  providers: [
    ShippersService,
    {
      provide: 'IShippersRepository',
      useClass: FirestoreShippersRepository,
    },
  ],
  exports: [ShippersService],
})
export class ShippersModule {}
