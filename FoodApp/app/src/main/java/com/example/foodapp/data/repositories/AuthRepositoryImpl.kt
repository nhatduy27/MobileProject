package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.entities.UserRole
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.data.remote.FirebaseAuthDataSource
import com.example.foodapp.data.remote.FirestoreUserDataSource
import com.example.foodapp.data.mapper.UserMapper

class AuthRepositoryImpl(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {
    
    override suspend fun register(email: String, password: String, displayName: String?): User {
        val firebaseUser = firebaseAuthDataSource.register(email, password)
        val user = UserMapper.fromFirebaseUser(firebaseUser, UserRole.BUYER).copy(
            displayName = displayName
        )
        return firestoreUserDataSource.createUserProfile(user)
    }
    
    override suspend fun login(email: String, password: String): User {
        val firebaseUser = firebaseAuthDataSource.login(email, password)
        val user = UserMapper.fromFirebaseUser(firebaseUser)
        
        // Fetch profile from Firestore to get custom role and other fields
        val firestoreUser = firestoreUserDataSource.getUserProfile(firebaseUser.uid)
        return firestoreUser ?: user
    }
    
    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuthDataSource.getCurrentUser() ?: return null
        return firestoreUserDataSource.getUserProfile(firebaseUser.uid)
    }
    
    override suspend fun logout() {
        firebaseAuthDataSource.logout()
    }
}
