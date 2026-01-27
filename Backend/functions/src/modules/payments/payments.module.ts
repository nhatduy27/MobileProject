import { Module } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { PaymentsController, WebhooksController } from './controllers';
import { PaymentsRepository } from './repositories';
import { PAYMENTS_REPOSITORY_TOKEN } from './interfaces';
import { OrdersModule } from '../orders/orders.module';

@Module({
  imports: [OrdersModule],
  controllers: [PaymentsController, WebhooksController],
  providers: [
    PaymentsService,
    {
      provide: PAYMENTS_REPOSITORY_TOKEN,
      useClass: PaymentsRepository,
    },
  ],
  exports: [PaymentsService],
})
export class PaymentsModule {}
