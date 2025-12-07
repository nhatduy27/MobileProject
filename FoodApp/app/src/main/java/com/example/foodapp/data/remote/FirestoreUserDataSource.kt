package com.example.foodapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.domain.entities.User
import com.example.foodapp.di.InjectionConstants

class FirestoreUserDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun createUserProfile(user: User): User {
        val userDoc = mapOf(
            "id" to user.id,
            "email" to user.email,
            "displayName" to user.displayName,
            "phoneNumber" to user.phoneNumber,
            "role" to user.role.name,
            "avatarUrl" to user.avatarUrl,
            "createdAt" to user.createdAt,
            "isActive" to user.isActive,
            "isVerified" to user.isVerified
        )
        firestore.collection(InjectionConstants.USERS_COLLECTION).document(user.id).set(userDoc).await()
        return user
    }
    
    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection(InjectionConstants.USERS_COLLECTION).document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
