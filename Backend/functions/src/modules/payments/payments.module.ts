import { Module, forwardRef } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { PaymentsController } from './controllers';
import { PaymentsRepository } from './repositories';
import { PAYMENTS_REPOSITORY_TOKEN } from './interfaces';
import { OrdersModule } from '../orders/orders.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [
    forwardRef(() => OrdersModule),
    NotificationsModule,
  ],
  controllers: [PaymentsController],
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
