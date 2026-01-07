import { Module, Global } from '@nestjs/common';
import { ConfigService } from './config.service';

/**
 * Config Module
 *
 * Quản lý environment variables và app configuration.
 * Global module - không cần import lại trong các feature modules.
 */
@Global()
@Module({
  providers: [ConfigService],
  exports: [ConfigService],
})
export class ConfigModule {}
