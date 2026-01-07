import { Module, Global } from '@nestjs/common';
import { FirebaseService } from './firebase.service';
import { FirestoreService } from './firestore.service';

/**
 * Firebase Module
 *
 * Cung cấp Firebase Admin SDK services cho toàn app.
 * Global module - không cần import lại trong các feature modules.
 */
@Global()
@Module({
  providers: [FirebaseService, FirestoreService],
  exports: [FirebaseService, FirestoreService],
})
export class FirebaseModule {}
