/**
 * Environment Configuration
 * 
 * Loads and validates environment variables.
 * In production, use @nestjs/config with .env files.
 */
export class EnvironmentConfig {
  static readonly PORT = parseInt(process.env.PORT || '3000', 10);
  static readonly NODE_ENV = process.env.NODE_ENV || 'development';
  
  // Firebase Configuration (TODO: Add actual Firebase config)
  static readonly FIREBASE_PROJECT_ID = process.env.FIREBASE_PROJECT_ID || '';
  static readonly FIREBASE_CLIENT_EMAIL = process.env.FIREBASE_CLIENT_EMAIL || '';
  static readonly FIREBASE_PRIVATE_KEY = process.env.FIREBASE_PRIVATE_KEY || '';
  
  // JWT Configuration (TODO: Add JWT secret)
  static readonly JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';
  static readonly JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '1d';
  
  static isDevelopment(): boolean {
    return this.NODE_ENV === 'development';
  }
  
  static isProduction(): boolean {
    return this.NODE_ENV === 'production';
  }
}
