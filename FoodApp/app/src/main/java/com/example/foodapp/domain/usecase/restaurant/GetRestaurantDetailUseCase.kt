package com.example.foodapp.domain.usecase.restaurant

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.repositories.RestaurantRepository

class GetRestaurantDetailUseCase(private val restaurantRepository: RestaurantRepository) {
    suspend operator fun invoke(restaurantId: String): Restaurant {
        return restaurantRepository.getRestaurantDetail(restaurantId)
    }
}
