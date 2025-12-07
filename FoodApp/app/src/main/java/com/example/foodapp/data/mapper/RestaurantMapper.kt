package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.data.remote.model.RestaurantRemote

object RestaurantMapper {
    
    fun fromRemote(restaurantRemote: RestaurantRemote): Restaurant {
        return Restaurant(
            id = restaurantRemote.id ?: "",
            ownerId = restaurantRemote.ownerId ?: "",
            name = restaurantRemote.name ?: "",
            description = restaurantRemote.description,
            category = restaurantRemote.category ?: "",
            phoneNumber = restaurantRemote.phoneNumber,
            email = restaurantRemote.email,
            address = restaurantRemote.address ?: "",
            averageRating = restaurantRemote.averageRating ?: 0.0,
            totalReviews = restaurantRemote.totalReviews ?: 0,
            isOpen = restaurantRemote.isOpen ?: false,
            deliveryFee = restaurantRemote.deliveryFee ?: 0L,
            minOrderAmount = restaurantRemote.minOrderAmount ?: 0L
        )
    }
    
    fun toRemote(restaurant: Restaurant): RestaurantRemote {
        return RestaurantRemote(
            id = restaurant.id,
            ownerId = restaurant.ownerId,
            name = restaurant.name,
            description = restaurant.description,
            category = restaurant.category,
            phoneNumber = restaurant.phoneNumber,
            email = restaurant.email,
            address = restaurant.address,
            averageRating = restaurant.averageRating,
            totalReviews = restaurant.totalReviews,
            isOpen = restaurant.isOpen,
            deliveryFee = restaurant.deliveryFee,
            minOrderAmount = restaurant.minOrderAmount
        )
    }
}
