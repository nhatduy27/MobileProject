package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.data.remote.model.RestaurantRemote

object RestaurantMapper {
    
    fun fromRemote(restaurantRemote: RestaurantRemote): Restaurant {
        // Convert address Map to String (use street or full address)
        val addressString = restaurantRemote.address?.let { addressMap ->
            addressMap["street"] as? String ?: ""
        } ?: ""
        
        return Restaurant(
            id = restaurantRemote.id ?: "",
            ownerId = restaurantRemote.ownerId ?: "",
            name = restaurantRemote.name ?: "",
            description = restaurantRemote.description,
            category = restaurantRemote.category ?: "",
            phoneNumber = restaurantRemote.phoneNumber,
            email = restaurantRemote.email,
            address = addressString,
            averageRating = restaurantRemote.averageRating ?: 0.0,
            totalReviews = restaurantRemote.totalReviews ?: 0,
            isOpen = restaurantRemote.isOpen ?: false,
            deliveryFee = restaurantRemote.deliveryFee ?: 0L,
            minOrderAmount = restaurantRemote.minOrderAmount ?: 0L
        )
    }
    
    fun toRemote(restaurant: Restaurant): RestaurantRemote {
        // Convert address String to Map structure
        val addressMap = mapOf(
            "street" to restaurant.address,
            "city" to "",
            "ward" to "",
            "district" to ""
        )
        
        return RestaurantRemote(
            id = restaurant.id,
            ownerId = restaurant.ownerId,
            name = restaurant.name,
            description = restaurant.description,
            category = restaurant.category,
            phoneNumber = restaurant.phoneNumber,
            email = restaurant.email,
            address = addressMap,
            averageRating = restaurant.averageRating,
            totalReviews = restaurant.totalReviews,
            logoUrl = null,
            bannerUrl = null,
            isOpen = restaurant.isOpen,
            operatingHours = null,
            deliveryFee = restaurant.deliveryFee,
            minOrderAmount = restaurant.minOrderAmount,
            orderCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
