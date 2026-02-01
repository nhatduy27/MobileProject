import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Type,
  mixin,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { FileInterceptor } from '@nestjs/platform-express';

/**
 * CloudFunctionFileInterceptor
 *
 * Custom interceptor để xử lý file upload trong Cloud Functions và Local Development.
 *
 * Trong Cloud Functions:
 * - RawBodyMiddleware đã parse file từ rawBody và attach vào req.file
 * - Interceptor này skip Multer nếu file đã có
 *
 * Trong Local Development:
 * - Stream còn nguyên, delegate sang Multer FileInterceptor
 */
export function CloudFunctionFileInterceptor(fieldName: string): Type<NestInterceptor> {
  @Injectable()
  class MixinInterceptor implements NestInterceptor {
    private multerInterceptor: NestInterceptor;

    constructor() {
      // Create Multer FileInterceptor instance
      const InterceptorClass = FileInterceptor(fieldName);
      this.multerInterceptor = new InterceptorClass();
    }

    async intercept(context: ExecutionContext, next: CallHandler): Promise<Observable<any>> {
      const request = context.switchToHttp().getRequest();

      // Check if file was already parsed by RawBodyMiddleware (Cloud Functions)
      if (request.file) {
        console.log(
          `[CloudFunctionFileInterceptor] File pre-parsed by middleware: ${request.file.originalname}, size: ${request.file.size}`,
        );
        // File already parsed, skip Multer
        return next.handle();
      }

      // No pre-parsed file, use Multer (Local Development)
      console.log('[CloudFunctionFileInterceptor] Delegating to Multer');
      return this.multerInterceptor.intercept(context, next);
    }
  }

  return mixin(MixinInterceptor);
}
