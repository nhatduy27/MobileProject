import { Module, Global } from '@nestjs/common';
import { APP_FILTER, APP_INTERCEPTOR } from '@nestjs/core';
import { ConfigModule } from './config/config.module';
import { FirebaseModule } from './firebase/firebase.module';
import { HttpExceptionFilter } from './filters/http-exception.filter';
import { LoggingInterceptor } from './interceptors/logging.interceptor';
import { TransformInterceptor } from './interceptors/transform.interceptor';

/**
 * Core Module
 *
 * Chứa infrastructure code dùng chung cho toàn app:
 * - Config service
 * - Firebase services
 * - Global filters
 * - Global interceptors
 *
 * Guards và Decorators được export nhưng apply ở controller level.
 */
@Global()
@Module({
  imports: [ConfigModule, FirebaseModule],
  providers: [
    // Global exception filter
    {
      provide: APP_FILTER,
      useClass: HttpExceptionFilter,
    },
    // Global logging interceptor
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
    // Global transform interceptor (wrap response)
    {
      provide: APP_INTERCEPTOR,
      useClass: TransformInterceptor,
    },
  ],
  exports: [ConfigModule, FirebaseModule],
})
export class CoreModule {}
