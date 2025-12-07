package com.example.foodapp.di.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.foodapp.data.remote.sdk.FirebaseAuthDataSource
import com.example.foodapp.data.remote.sdk.FirestoreUserDataSource
import com.example.foodapp.data.remote.sdk.FirestoreRestaurantDataSource
import com.example.foodapp.data.remote.sdk.FirestoreOrderDataSource

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(firebaseAuth: FirebaseAuth): FirebaseAuthDataSource {
        return FirebaseAuthDataSource(firebaseAuth)
    }
    
    @Provides
    @Singleton
    fun provideFirestoreUserDataSource(firestore: FirebaseFirestore): FirestoreUserDataSource {
        return FirestoreUserDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirestoreRestaurantDataSource(firestore: FirebaseFirestore): FirestoreRestaurantDataSource {
        return FirestoreRestaurantDataSource(firestore)
    }
    
    @Provides
    @Singleton
    fun provideFirestoreOrderDataSource(firestore: FirebaseFirestore): FirestoreOrderDataSource {
        return FirestoreOrderDataSource(firestore)
    }
}
