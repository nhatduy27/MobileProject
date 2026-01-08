import { Injectable, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Firebase Service
 *
 * Singleton service quản lý Firebase Admin SDK initialization.
 * Cung cấp access đến Auth, Firestore, Storage, Messaging.
 */
@Injectable()
export class FirebaseService {
  private readonly logger = new Logger(FirebaseService.name);

  private _app: admin.app.App;
  private _auth: admin.auth.Auth;
  private _firestore: admin.firestore.Firestore;
  private _storage: admin.storage.Storage;
  private _messaging: admin.messaging.Messaging;

  constructor() {
    this.initialize();
  }

  private initialize() {
    try {
      if (!admin.apps.length) {
        // Check if running in Firebase Functions environment
        if (process.env.FIREBASE_CONFIG) {
          // Firebase Functions auto-initializes
          this._app = admin.initializeApp();
          this.logger.log('Firebase Admin SDK initialized (Firebase env)');
        } else {
          // Local development - load service account
          const serviceAccountPath = this.findServiceAccount();
          if (serviceAccountPath) {
            // eslint-disable-next-line @typescript-eslint/no-var-requires
            const serviceAccount = require(serviceAccountPath);
            this._app = admin.initializeApp({
              credential: admin.credential.cert(serviceAccount),
              storageBucket: `${serviceAccount.project_id}.firebasestorage.app`,
            });
            this.logger.log(`Firebase Admin SDK initialized with: ${serviceAccountPath}`);
          } else {
            throw new Error('service-account.json not found');
          }
        }
      } else {
        this._app = admin.apps[0]!;
        this.logger.log('Firebase Admin SDK already initialized');
      }

      this._auth = admin.auth();
      this._firestore = admin.firestore();
      this._storage = admin.storage();
      this._messaging = admin.messaging();
    } catch (error) {
      this.logger.error('Failed to initialize Firebase Admin SDK', error);
      throw error;
    }
  }

  /**
   * Find service-account.json in possible locations
   */
  private findServiceAccount(): string | null {
    const possiblePaths = [
      path.join(process.cwd(), 'service-account.json'),        // Backend/service-account.json
      path.join(process.cwd(), '../service-account.json'),     // functions/../service-account.json
      path.join(process.cwd(), '../../service-account.json'),  // Fallback
    ];

    for (const p of possiblePaths) {
      if (fs.existsSync(p)) {
        return p;
      }
    }

    this.logger.warn('service-account.json not found in: ' + possiblePaths.join(', '));
    return null;
  }

  get app(): admin.app.App {
    return this._app;
  }

  get auth(): admin.auth.Auth {
    return this._auth;
  }

  get firestore(): admin.firestore.Firestore {
    return this._firestore;
  }

  get storage(): admin.storage.Storage {
    return this._storage;
  }

  get messaging(): admin.messaging.Messaging {
    return this._messaging;
  }
}
