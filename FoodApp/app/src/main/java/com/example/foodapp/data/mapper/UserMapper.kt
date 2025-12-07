package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.entities.UserRole
import com.google.firebase.auth.FirebaseUser

object UserMapper {
    
    fun fromFirebaseUser(firebaseUser: FirebaseUser, role: UserRole = UserRole.BUYER): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            phoneNumber = firebaseUser.phoneNumber,
            role = role,
            avatarUrl = firebaseUser.photoUrl?.toString(),
            createdAt = firebaseUser.metadata?.creationTimestamp,
            isActive = true,
            isVerified = firebaseUser.isEmailVerified
        )
    }
    
    fun toMap(user: User): Map<String, Any?> {
        return mapOf(
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
    }
    
    fun fromMap(map: Map<String, Any?>): User {
        return User(
            id = map["id"] as? String ?: "",
            email = map["email"] as? String ?: "",
            displayName = map["displayName"] as? String,
            phoneNumber = map["phoneNumber"] as? String,
            role = try {
                UserRole.valueOf((map["role"] as? String) ?: "BUYER")
            } catch (e: Exception) {
                UserRole.BUYER
            },
            avatarUrl = map["avatarUrl"] as? String,
            createdAt = (map["createdAt"] as? Number)?.toLong(),
            isActive = map["isActive"] as? Boolean ?: true,
            isVerified = map["isVerified"] as? Boolean ?: false
        )
    }
}
