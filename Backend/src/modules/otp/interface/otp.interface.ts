export interface IOtp {
  id?: string;
  email: string;
  code: string;
  expiresAt: Date;
  verified: boolean;
  attempts: number;
  createdAt: Date;
}

export interface ISendOtpResponse {
  success: boolean;
  message: string;
  expiresAt?: Date;
}

export interface IVerifyOtpResponse {
  success: boolean;
  message: string;
  verified: boolean;
}

// THÊM MỚI
export interface IEmailVerification {
  id?: string;
  email: string;
  verified: boolean;
  verifiedAt: Date;
  createdAt: Date;
}

export interface IVerificationStatus {
  isVerified: boolean;
  verifiedAt?: Date;
  email: string;
}