import { Module } from '@nestjs/common';
import { CoreModule } from '../core/core.module';
import { OtpService, StorageService } from './services';

/**
 * Shared Module
 *
 * Chứa utilities, DTOs, services và constants dùng chung.
 * NOTE: Email service is in modules/email, not here.
 */
@Module({
  imports: [CoreModule],
  providers: [OtpService, StorageService],
  exports: [OtpService, StorageService],
})
export class SharedModule {}
