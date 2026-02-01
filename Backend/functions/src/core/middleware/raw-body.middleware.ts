import { Injectable, NestMiddleware, Logger } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import Busboy from 'busboy';

/**
 * Check if DEBUG_HTTP is enabled
 */
const isDebugEnabled = () => process.env.DEBUG_HTTP === '1';

/**
 * RawBodyMiddleware
 *
 * Xử lý multipart/form-data trong Cloud Functions v2 (Cloud Run).
 *
 * Vấn đề:
 * - Cloud Functions đã parse body và lưu vào req.rawBody
 * - Stream gốc đã bị consume, Multer không thể đọc
 *
 * Giải pháp:
 * - Parse req.rawBody bằng Busboy
 * - Attach file vào req.file để controller có thể sử dụng
 *
 * Debugging:
 * - Set DEBUG_HTTP=1 to enable detailed logging (no PII)
 */
@Injectable()
export class RawBodyMiddleware implements NestMiddleware {
  private readonly logger = new Logger(RawBodyMiddleware.name);

  use(req: Request, _res: Response, next: NextFunction) {
    const debugHttp = isDebugEnabled();
    const contentType = req.headers['content-type'] || '';
    const contentLength = req.headers['content-length'] || 'unknown';

    // Debug log: request headers (no auth tokens)
    if (debugHttp) {
      this.logger.log(`[DEBUG_HTTP] === Incoming Request ===`);
      this.logger.log(`[DEBUG_HTTP] Method: ${req.method}, Path: ${req.path}`);
      this.logger.log(`[DEBUG_HTTP] Content-Type: ${contentType}`);
      this.logger.log(`[DEBUG_HTTP] Content-Length: ${contentLength}`);
      this.logger.log(`[DEBUG_HTTP] typeof req.body: ${typeof (req as any).body}`);
      this.logger.log(`[DEBUG_HTTP] Array.isArray(req.body): ${Array.isArray((req as any).body)}`);
      if ((req as any).body && typeof (req as any).body === 'object') {
        const keys = Object.keys((req as any).body).slice(0, 30);
        this.logger.log(`[DEBUG_HTTP] Object.keys(req.body).slice(0,30): ${JSON.stringify(keys)}`);
      }
      if (typeof (req as any).body === 'string') {
        this.logger.log(`[DEBUG_HTTP] req.body (first 100 chars): ${((req as any).body as string).substring(0, 100)}`);
      }
      this.logger.log(`[DEBUG_HTTP] rawBody present: ${!!(req as any).rawBody}`);
      if ((req as any).rawBody) {
        this.logger.log(`[DEBUG_HTTP] rawBody length: ${(req as any).rawBody.length}`);
      }
    }

    // ========================================
    // For multipart/form-data: Parse with Busboy first, then check
    // For other content types: Check for malformed body
    // ========================================
    
    // Chỉ xử lý multipart/form-data
    if (!contentType.includes('multipart/form-data')) {
      if (debugHttp) {
        this.logger.log(`[DEBUG_HTTP] Not multipart/form-data, skipping middleware`);
      }
      return next();
    }

    // Kiểm tra xem có rawBody không (Cloud Functions environment)
    const rawBody = (req as any).rawBody;
    if (!rawBody) {
      // Không có rawBody, để Multer xử lý bình thường (local dev)
      if (debugHttp) {
        this.logger.log(`[DEBUG_HTTP] No rawBody present, delegating to Multer`);
      }
      return next();
    }

    if (debugHttp) {
      this.logger.log(`[DEBUG_HTTP] Processing multipart with Busboy, rawBody size: ${rawBody.length}`);
    }
    this.logger.log(
      `Processing multipart/form-data with rawBody, size: ${rawBody.length} bytes`,
    );

    try {
      const busboy = Busboy({ headers: req.headers });
      const fields: Record<string, string> = {};
      const filesData: Array<{
        fieldname: string;
        originalname: string;
        encoding: string;
        mimetype: string;
        buffer: Buffer;
        size: number;
      }> = [];

      // Handle text fields
      busboy.on('field', (fieldname: string, val: string) => {
        fields[fieldname] = val;
      });

      // Handle file
      busboy.on(
        'file',
        (
          fieldname: string,
          file: NodeJS.ReadableStream,
          info: { filename: string; encoding: string; mimeType: string },
        ) => {
          const { filename, encoding, mimeType } = info;
          const chunks: Buffer[] = [];

          file.on('data', (data: Buffer) => {
            chunks.push(data);
          });

          file.on('end', () => {
            const buffer = Buffer.concat(chunks);
            filesData.push({
              fieldname,
              originalname: filename,
              encoding,
              mimetype: mimeType,
              buffer,
              size: buffer.length,
            });
            this.logger.debug(
              `File received: ${filename}, size: ${buffer.length} bytes, type: ${mimeType}`,
            );
          });
        },
      );

      busboy.on('finish', () => {
        // IMPORTANT: Replace body entirely - the existing body may be corrupted
        // (e.g., rawBody bytes spread as {0, 1, 2, ...} by Cloud Run)
        (req as any).body = fields;
        if (filesData.length > 0) {
          (req as any).files = filesData;
          (req as any).file = filesData[0];
          this.logger.log(`File(s) parsed successfully: ${filesData.length}`);
          this.logger.log(`Parsed fields: ${Object.keys(fields).join(', ')}`);
        } else {
          this.logger.warn('No file found in multipart request');
        }
        next();
      });

      busboy.on('error', (error: Error) => {
        this.logger.error('Busboy parsing error:', error);
        next(error);
      });

      // Write rawBody to busboy
      busboy.end(rawBody);
    } catch (error) {
      this.logger.error('Failed to parse multipart data:', error);
      next(error);
    }
  }
}
