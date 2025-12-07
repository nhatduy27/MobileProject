package com.example.foodapp.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.domain.repositories.RestaurantRepository
import com.example.foodapp.domain.repositories.OrderRepository
import com.example.foodapp.data.repositories.AuthRepositoryImpl
import com.example.foodapp.data.repositories.RestaurantRepositoryImpl
import com.example.foodapp.data.repositories.OrderRepositoryImpl
import com.example.foodapp.data.remote.FirebaseAuthDataSource
import com.example.foodapp.data.remote.FirestoreUserDataSource
import com.example.foodapp.data.remote.FirestoreRestaurantDataSource
import com.example.foodapp.data.remote.FirestoreOrderDataSource

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthDataSource: FirebaseAuthDataSource,
        firestoreUserDataSource: FirestoreUserDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuthDataSource, firestoreUserDataSource)
    }
    
    @Provides
    @Singleton
    fun provideRestaurantRepository(
        firestoreRestaurantDataSource: FirestoreRestaurantDataSource
    ): RestaurantRepository {
        return RestaurantRepositoryImpl(firestoreRestaurantDataSource)
    }
    
    @Provides
    @Singleton
    fun provideOrderRepository(
        firestoreOrderDataSource: FirestoreOrderDataSource,
        authRepository: AuthRepository
    ): OrderRepository {
        return OrderRepositoryImpl(firestoreOrderDataSource, authRepository)
    }
}
