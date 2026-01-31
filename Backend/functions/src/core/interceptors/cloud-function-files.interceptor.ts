import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Type,
  mixin,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { FilesInterceptor } from '@nestjs/platform-express';

/**
 * CloudFunctionFilesInterceptor
 *
 * Handles multiple file uploads in Cloud Functions and Local Development.
 * - If RawBodyMiddleware already parsed files into req.files, skip Multer
 * - Otherwise, delegate to Multer FilesInterceptor
 */
export function CloudFunctionFilesInterceptor(
  fieldName: string,
  maxCount = 10,
): Type<NestInterceptor> {
  @Injectable()
  class MixinInterceptor implements NestInterceptor {
    private multerInterceptor: NestInterceptor;

    constructor() {
      const InterceptorClass = FilesInterceptor(fieldName, maxCount);
      this.multerInterceptor = new InterceptorClass();
    }

    async intercept(context: ExecutionContext, next: CallHandler): Promise<Observable<any>> {
      const request = context.switchToHttp().getRequest();

      if (Array.isArray(request.files) && request.files.length > 0) {
        console.log(
          `[CloudFunctionFilesInterceptor] Files pre-parsed by middleware: ${request.files.length}`,
        );
        return next.handle();
      }

      console.log('[CloudFunctionFilesInterceptor] Delegating to Multer');
      return this.multerInterceptor.intercept(context, next);
    }
  }

  return mixin(MixinInterceptor);
}
