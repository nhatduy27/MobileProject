import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
  Type,
  mixin,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { FileFieldsInterceptor } from '@nestjs/platform-express';

type FileField = { name: string; maxCount?: number };

/**
 * CloudFunctionFileFieldsInterceptor
 *
 * Handles named file fields in Cloud Functions and Local Development.
 * - If RawBodyMiddleware already parsed files into req.files (array), reshape and skip Multer
 * - Otherwise, delegate to Multer FileFieldsInterceptor
 */
export function CloudFunctionFileFieldsInterceptor(
  fields: FileField[],
): Type<NestInterceptor> {
  @Injectable()
  class MixinInterceptor implements NestInterceptor {
    private multerInterceptor: NestInterceptor;

    constructor() {
      const InterceptorClass = FileFieldsInterceptor(fields);
      this.multerInterceptor = new InterceptorClass();
    }

    async intercept(context: ExecutionContext, next: CallHandler): Promise<Observable<any>> {
      const request = context.switchToHttp().getRequest();

      if (Array.isArray(request.files) && request.files.length > 0) {
        const filesByField: Record<string, Express.Multer.File[]> = {};
        for (const file of request.files) {
          const fieldName = file.fieldname;
          if (!filesByField[fieldName]) {
            filesByField[fieldName] = [];
          }
          filesByField[fieldName].push(file);
        }
        request.files = filesByField;
        return next.handle();
      }

      return this.multerInterceptor.intercept(context, next);
    }
  }

  return mixin(MixinInterceptor);
}
