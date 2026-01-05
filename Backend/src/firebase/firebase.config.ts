import { Injectable, Global } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';

@Global()  
@Injectable()
export class FirebaseConfig {
  public firestore: admin.firestore.Firestore;
  
  constructor(private configService: ConfigService) {
    this.initializeFirebase();
  }

  private initializeFirebase() {
    if (!admin.apps.length) {
      const serviceAccount = {
        projectId: this.configService.get('FIREBASE_PROJECT_ID'),
        privateKey: this.configService.get('FIREBASE_PRIVATE_KEY')?.replace(/\\n/g, '\n'),
        clientEmail: this.configService.get('FIREBASE_CLIENT_EMAIL'),
      };

      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
      });
    }

    this.firestore = admin.firestore();
  }

  // Helper methods
  getFirestore() {
    return this.firestore;
  }

  collection(name: string) {
    return this.firestore.collection(name);
  }
}