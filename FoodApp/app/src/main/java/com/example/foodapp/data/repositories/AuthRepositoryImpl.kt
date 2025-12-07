package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.entities.UserRole
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.data.remote.sdk.FirebaseAuthDataSource
import com.example.foodapp.data.remote.sdk.FirestoreUserDataSource
import com.example.foodapp.data.mapper.UserMapper
import com.example.foodapp.data.remote.model.UserRemote

class AuthRepositoryImpl(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {
    
    override suspend fun register(email: String, password: String, displayName: String?): User {
        val firebaseUser = firebaseAuthDataSource.register(email, password)
        val userRemote = UserRemote(
            id = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = displayName,
            phoneNumber = firebaseUser.phoneNumber,
            role = UserRole.BUYER.name,
            avatarUrl = firebaseUser.photoUrl?.toString(),
            createdAt = System.currentTimeMillis(),
            isActive = true,
            isVerified = false
        )
        val createdUserRemote = firestoreUserDataSource.createUserProfile(userRemote)
        return UserMapper.fromRemote(createdUserRemote)
    }
    
    override suspend fun login(email: String, password: String): User {
        val firebaseUser = firebaseAuthDataSource.login(email, password)
        val userRemote = firestoreUserDataSource.getUserProfile(firebaseUser.uid)
            ?: UserRemote(
                id = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                phoneNumber = firebaseUser.phoneNumber,
                role = UserRole.BUYER.name,
                avatarUrl = firebaseUser.photoUrl?.toString(),
                isActive = true,
                isVerified = firebaseUser.isEmailVerified
            )
        return UserMapper.fromRemote(userRemote)
    }
    
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuthDataSource.getCurrentUser() ?: return null
        val userRemote = firestoreUserDataSource.getUserProfile(firebaseUser.uid) ?: return null
        return UserMapper.fromRemote(userRemote)
    }
    
    override suspend fun logout() {
        firebaseAuthDataSource.logout()
    }
}
