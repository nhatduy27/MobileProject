import { Module, Global } from '@nestjs/common';
import { EmailService } from './email.service';

/**
 * Email Module
 * 
 * Global module for sending emails via SendGrid.
 * Can be used across all modules without importing.
 */
@Global()
@Module({
  providers: [EmailService],
  exports: [EmailService],
})
export class EmailModule {}
