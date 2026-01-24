import { Module } from '@nestjs/common';
import { VouchersService } from './vouchers.service';
import { FirestoreVouchersRepository } from './repositories';
import { OwnerVouchersController, VouchersController } from './controllers';
import { ShopsModule } from '../shops/shops.module';

@Module({
  imports: [ShopsModule],
  controllers: [OwnerVouchersController, VouchersController],
  providers: [
    VouchersService,
    {
      provide: 'VOUCHERS_REPOSITORY',
      useClass: FirestoreVouchersRepository,
    },
  ],
  exports: [VouchersService, 'VOUCHERS_REPOSITORY'],
})
export class VouchersModule {}
