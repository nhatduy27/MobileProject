package com.example.foodapp.di.module

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.foodapp.data.remote.FirebaseAuthDataSource
import com.example.foodapp.data.remote.FirestoreUserDataSource
import com.example.foodapp.data.remote.FirestoreRestaurantDataSource
import com.example.foodapp.data.remote.FirestoreOrderDataSource

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
