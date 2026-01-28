import { Module } from '@nestjs/common';
import { ChatbotService } from './services';
import { ChatbotController } from './controllers';
import { ConfigModule } from '../../core/config/config.module';

@Module({
  imports: [ConfigModule],
  controllers: [ChatbotController],
  providers: [ChatbotService],
  exports: [ChatbotService],
})
export class ChatbotModule {}
