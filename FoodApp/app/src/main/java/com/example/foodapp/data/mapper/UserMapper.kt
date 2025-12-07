package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.entities.UserRole
import com.example.foodapp.data.remote.model.UserRemote
import com.google.firebase.auth.FirebaseUser

object UserMapper {
    
    fun fromRemote(userRemote: UserRemote): User {
        return User(
            id = userRemote.id ?: "",
            email = userRemote.email ?: "",
            displayName = userRemote.displayName,
            phoneNumber = userRemote.phoneNumber,
            role = try {
                UserRole.valueOf(userRemote.role ?: "BUYER")
            } catch (e: Exception) {
                UserRole.BUYER
            },
            avatarUrl = userRemote.avatarUrl,
            createdAt = userRemote.createdAt,
            isActive = userRemote.isActive ?: true,
            isVerified = userRemote.isVerified ?: false
        )
    }
    
    fun toRemote(user: User): UserRemote {
        return UserRemote(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            phoneNumber = user.phoneNumber,
            role = user.role.name,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt,
            updatedAt = System.currentTimeMillis(),
            isActive = user.isActive,
            isVerified = user.isVerified,
            addresses = null // Will be managed separately if needed
        )
    }
    
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
}
