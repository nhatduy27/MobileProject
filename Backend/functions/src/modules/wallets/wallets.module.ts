import { Module } from '@nestjs/common';
import { WalletsService } from './wallets.service';
import { WalletsController } from './controllers';
import { WalletsRepository } from './repositories';
import { WALLETS_REPOSITORY_TOKEN } from './interfaces';

@Module({
  controllers: [WalletsController],
  providers: [
    WalletsService,
    {
      provide: WALLETS_REPOSITORY_TOKEN,
      useClass: WalletsRepository,
    },
  ],
  exports: [WalletsService],
})
export class WalletsModule {}
