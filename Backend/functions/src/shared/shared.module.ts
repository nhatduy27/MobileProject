import { Module } from '@nestjs/common';

/**
 * Shared Module
 *
 * Chứa utilities, DTOs, và constants dùng chung.
 * Không cần Global vì import khi cần.
 */
@Module({
  providers: [],
  exports: [],
})
export class SharedModule {}
