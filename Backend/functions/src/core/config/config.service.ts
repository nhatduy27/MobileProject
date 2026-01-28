import { Injectable, Logger } from '@nestjs/common';
import * as dotenv from 'dotenv';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Config Service
 *
 * Đọc environment variables từ .env file hoặc process.env.
 *
 * Thứ tự ưu tiên:
 * 1. process.env (Firebase Functions tự inject khi deploy)
 * 2. .env file ở Backend/ root (local development)
 */
@Injectable()
export class ConfigService {
  private readonly logger = new Logger(ConfigService.name);
  private readonly envConfig: Record<string, string>;

  constructor() {
    // Chỉ load .env khi chạy local (không phải Firebase Functions environment)
    if (!process.env.FIREBASE_CONFIG) {
      this.loadEnvFile();
    } else {
      this.logger.log('Running in Firebase Functions environment');
    }

    this.envConfig = process.env as Record<string, string>;
  }

  /**
   * Load .env file từ Backend/ root
   * Tìm từ cwd() thay vì dùng relative path cứng
   */
  private loadEnvFile(): void {
    // Các vị trí có thể có .env (ưu tiên theo thứ tự)
    const possiblePaths = [
      path.join(process.cwd(), '.env'), // Backend/.env (khi chạy từ Backend/)
      path.join(process.cwd(), '../.env'), // functions/../.env = Backend/.env
      path.join(process.cwd(), '../../.env'), // Fallback
    ];

    for (const envPath of possiblePaths) {
      if (fs.existsSync(envPath)) {
        const result = dotenv.config({ path: envPath });
        if (!result.error) {
          this.logger.log(`Loaded .env from: ${envPath}`);
          return;
        }
      }
    }

    this.logger.warn('.env file not found. Using process.env only.');
  }

  /**
   * Get environment variable
   */
  get(key: string, defaultValue?: string): string {
    return this.envConfig[key] || defaultValue || '';
  }

  /**
   * Get required environment variable (throws if not found)
   */
  getOrThrow(key: string): string {
    const value = this.envConfig[key];
    if (!value) {
      throw new Error(`Missing required environment variable: ${key}`);
    }
    return value;
  }

  /**
   * Check if running in production
   */
  get isProduction(): boolean {
    return this.get('NODE_ENV') === 'production';
  }

  /**
   * Check if running in development
   */
  get isDevelopment(): boolean {
    return this.get('NODE_ENV') !== 'production';
  }

  // ============================================
  // Firestore Pagination Fallback (Dev Only)
  // ============================================

  /**
   * Enable fallback pagination when Firestore indexes are building
   * WARNING: Use for development only. Performance degrades with large datasets.
   */
  get enableFirestorePaginationFallback(): boolean {
    return this.get('ENABLE_FIRESTORE_PAGINATION_FALLBACK', 'false').toLowerCase() === 'true';
  }

  // ============================================
  // Firebase Config
  // ============================================

  get firebaseProjectId(): string {
    return this.get('FIREBASE_PROJECT_ID', '');
  }

  get firebaseRegion(): string {
    return this.get('FIREBASE_REGION', 'asia-southeast1');
  }

  // ============================================
  // ZaloPay Config
  // ============================================

  get zaloPayAppId(): string {
    return this.get('ZALOPAY_APP_ID', '');
  }

  get zaloPayKey1(): string {
    return this.get('ZALOPAY_KEY1', '');
  }

  get zaloPayKey2(): string {
    return this.get('ZALOPAY_KEY2', '');
  }

  get zaloPayEndpoint(): string {
    return this.get('ZALOPAY_ENDPOINT', 'https://sb-openapi.zalopay.vn/v2');
  }

  // ============================================
  // MoMo Config
  // ============================================

  get momoPartnerCode(): string {
    return this.get('MOMO_PARTNER_CODE', '');
  }

  get momoAccessKey(): string {
    return this.get('MOMO_ACCESS_KEY', '');
  }

  get momoSecretKey(): string {
    return this.get('MOMO_SECRET_KEY', '');
  }

  get momoEndpoint(): string {
    return this.get('MOMO_ENDPOINT', 'https://test-payment.momo.vn');
  }

  // ============================================
  // SePay Config
  // ============================================

  get sePayApiUrl(): string {
    return this.get('SEPAY_API_URL', 'https://my.sepay.vn/userapi');
  }

  get sePaySecretKey(): string {
    return this.get('SEPAY_SECRET_KEY', '');
  }

  get sePayAccountNumber(): string {
    return this.get('SEPAY_ACCOUNT_NUMBER', '');
  }

  get sePayAccountName(): string {
    return this.get('SEPAY_ACCOUNT_NAME', '');
  }

  get sePayBankCode(): string {
    return this.get('SEPAY_BANK_CODE', 'MB');
  }

  get sePayQrTemplate(): string {
    return this.get(
      'SEPAY_TEMPLATE_QR',
      'https://qr.sepay.vn/img?acc={account}&bank={bank}&amount={amount}&des={content}&template=compact',
    );
  }

  get sePayPollLimit(): number {
    return parseInt(this.get('SEPAY_POLL_LIMIT', '50'), 10);
  }

  get sePayPollWindowMinutes(): number {
    return parseInt(this.get('SEPAY_POLL_WINDOW_MINUTES', '60'), 10);
  }

  // ============================================
  // Google Routes API (GPS Module)
  // ============================================

  /**
   * Get Google Routes API key (server-side)
   * Required for GPS route optimization
   * @throws Error if not configured
   */
  get googleRoutesApiKey(): string {
    const key = this.get('GOOGLE_ROUTES_API_KEY');
    if (!key) {
      throw new Error(
        'Missing GOOGLE_ROUTES_API_KEY environment variable. ' +
        'Please configure in Backend/.env for local development or ' +
        'set as Cloud Functions secret for production deployment.'
      );
    }
    return key;
  }

  /**
   * Get Google Routes API key without throwing (for optional features)
   */
  get googleRoutesApiKeyOptional(): string | null {
    return this.get('GOOGLE_ROUTES_API_KEY') || null;
  }
}
