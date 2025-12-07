package com.example.foodapp.data.remote.sdk

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.data.remote.model.UserRemote
import com.example.foodapp.di.InjectionConstants

class FirestoreUserDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun createUserProfile(userRemote: UserRemote): UserRemote {
        val userDoc = mapOf(
            "id" to userRemote.id,
            "email" to userRemote.email,
            "displayName" to userRemote.displayName,
            "phoneNumber" to userRemote.phoneNumber,
            "role" to userRemote.role,
            "avatarUrl" to userRemote.avatarUrl,
            "createdAt" to userRemote.createdAt,
            "isActive" to userRemote.isActive,
            "isVerified" to userRemote.isVerified
        )
        userRemote.id?.let {
            firestore.collection(InjectionConstants.USERS_COLLECTION).document(it).set(userDoc).await()
        }
        return userRemote
    }
    
    suspend fun getUserProfile(uid: String): UserRemote? {
        return try {
            val doc = firestore.collection(InjectionConstants.USERS_COLLECTION).document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(UserRemote::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
