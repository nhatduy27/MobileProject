import { Injectable } from '@nestjs/common';
import { FirebaseService } from '../../core/firebase/firebase.service';
import * as crypto from 'crypto';

/**
 * Storage Service
 * 
 * Handles file uploads to Firebase Storage
 */
@Injectable()
export class StorageService {
  constructor(private readonly firebase: FirebaseService) {}

  /**
   * Upload avatar image to Firebase Storage
   * 
   * @param userId - User ID
   * @param buffer - Image buffer
   * @param mimetype - Image mime type (image/jpeg, image/png)
   * @returns Public URL of uploaded image
   */
  async uploadAvatar(userId: string, buffer: Buffer, mimetype: string): Promise<string> {
    // Get bucket with explicit name (Firebase new format: .firebasestorage.app)
    const bucketName = 'foodappproject-7c136.firebasestorage.app';
    const bucket = this.firebase.storage.bucket(bucketName);
    
    // Generate unique filename
    const ext = mimetype === 'image/png' ? 'png' : 'jpg';
    const hash = crypto.randomBytes(8).toString('hex');
    const filename = `avatars/${userId}/${hash}.${ext}`;

    // Generate download token
    const downloadToken = crypto.randomBytes(16).toString('hex');

    // Upload file
    const file = bucket.file(filename);
    await file.save(buffer, {
      metadata: {
        contentType: mimetype,
        metadata: {
          firebaseStorageDownloadTokens: downloadToken,
        },
      },
    });

    // Make file publicly accessible
    await file.makePublic();
    
    // Return public URL with token for Firebase Storage
    const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodeURIComponent(filename)}?alt=media&token=${downloadToken}`;

    return publicUrl;
  }

  /**
   * Upload product image to Firebase Storage
   * 
   * @param shopId - Shop ID
   * @param productId - Product ID
   * @param buffer - Image buffer
   * @param mimetype - Image mime type (image/jpeg, image/png)
   * @returns Public URL of uploaded image
   */
  async uploadProductImage(
    shopId: string,
    productId: string,
    buffer: Buffer,
    mimetype: string,
  ): Promise<string> {
    const bucketName = 'foodappproject-7c136.firebasestorage.app';
    const bucket = this.firebase.storage.bucket(bucketName);

    // Generate filename: products/{shopId}/{productId}.{ext}
    const ext = mimetype === 'image/png' ? 'png' : 'jpg';
    const filename = `products/${shopId}/${productId}.${ext}`;

    // Generate download token
    const downloadToken = crypto.randomBytes(16).toString('hex');

    // Upload file
    const file = bucket.file(filename);
    await file.save(buffer, {
      metadata: {
        contentType: mimetype,
        metadata: {
          firebaseStorageDownloadTokens: downloadToken,
        },
      },
    });

    // Make file publicly accessible
    await file.makePublic();

    // Return public URL
    const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodeURIComponent(filename)}?alt=media&token=${downloadToken}`;

    return publicUrl;
  }

  /**
   * Delete avatar image from Firebase Storage
   * 
   * @param avatarUrl - Full URL of avatar to delete
   */
  async deleteAvatar(avatarUrl: string): Promise<void> {
    try {
      if (!avatarUrl) return;

      const bucketName = 'foodappproject-7c136.firebasestorage.app';
      const bucket = this.firebase.storage.bucket(bucketName);
      
      // Extract filename from URL
      // Format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{filename}?alt=media&token={token}
      // or: https://storage.googleapis.com/{bucket}/{filename}
      let filename = '';
      
      if (avatarUrl.includes('firebasestorage.googleapis.com')) {
        const match = avatarUrl.match(/\/o\/([^?]+)/);
        if (match) {
          filename = decodeURIComponent(match[1]);
        }
      } else {
        const urlParts = avatarUrl.split('/');
        const bucketIndex = urlParts.findIndex(part => part.includes('.appspot.com'));
        if (bucketIndex !== -1) {
          filename = urlParts.slice(bucketIndex + 1).join('/');
        }
      }
      
      if (!filename) return;
      
      const file = bucket.file(filename);
      const [exists] = await file.exists();
      
      if (exists) {
        await file.delete();
      }
    } catch (error) {
      // Silently fail if avatar doesn't exist
      console.warn('Failed to delete avatar:', error);
    }
  }
}
