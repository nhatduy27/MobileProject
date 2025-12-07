package com.example.foodapp.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.domain.repositories.RestaurantRepository
import com.example.foodapp.domain.repositories.OrderRepository
import com.example.foodapp.domain.usecase.auth.LoginUseCase
import com.example.foodapp.domain.usecase.auth.RegisterUseCase
import com.example.foodapp.domain.usecase.auth.GetCurrentUserUseCase
import com.example.foodapp.domain.usecase.auth.LogoutUseCase
import com.example.foodapp.domain.usecase.restaurant.GetRestaurantsUseCase
import com.example.foodapp.domain.usecase.restaurant.GetRestaurantDetailUseCase
import com.example.foodapp.domain.usecase.restaurant.GetMenuItemsUseCase
import com.example.foodapp.domain.usecase.order.PlaceOrderUseCase
import com.example.foodapp.domain.usecase.order.GetUserOrdersUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    // Auth Use Cases
    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }
    
    @Provides
    @Singleton
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(authRepository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }
    
    @Provides
    @Singleton
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }
    
    // Restaurant Use Cases
    @Provides
    @Singleton
    fun provideGetRestaurantsUseCase(restaurantRepository: RestaurantRepository): GetRestaurantsUseCase {
        return GetRestaurantsUseCase(restaurantRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetRestaurantDetailUseCase(restaurantRepository: RestaurantRepository): GetRestaurantDetailUseCase {
        return GetRestaurantDetailUseCase(restaurantRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetMenuItemsUseCase(restaurantRepository: RestaurantRepository): GetMenuItemsUseCase {
        return GetMenuItemsUseCase(restaurantRepository)
    }
    
    // Order Use Cases
    @Provides
    @Singleton
    fun providePlaceOrderUseCase(orderRepository: OrderRepository): PlaceOrderUseCase {
        return PlaceOrderUseCase(orderRepository)
    }
    
    @Provides
    @Singleton
    fun provideGetUserOrdersUseCase(orderRepository: OrderRepository): GetUserOrdersUseCase {
        return GetUserOrdersUseCase(orderRepository)
    }
}
