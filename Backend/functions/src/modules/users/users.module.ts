import { Module } from '@nestjs/common';
import { MeController } from './controllers/me.controller';
import { UsersService } from './users.service';
import { FirestoreUsersRepository } from './repositories/firestore-users.repository';
import { FirestoreAddressesRepository } from './repositories/firestore-addresses.repository';
import { USERS_REPOSITORY, ADDRESSES_REPOSITORY } from './interfaces';
import { FirebaseModule } from '../../core/firebase/firebase.module';
import { SharedModule } from '../../shared/shared.module';

@Module({
  imports: [FirebaseModule, SharedModule],
  controllers: [MeController],
  providers: [
    UsersService,
    {
      provide: USERS_REPOSITORY,
      useClass: FirestoreUsersRepository,
    },
    {
      provide: ADDRESSES_REPOSITORY,
      useClass: FirestoreAddressesRepository,
    },
  ],
  exports: [UsersService, USERS_REPOSITORY],
})
export class UsersModule {}
