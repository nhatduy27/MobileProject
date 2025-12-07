package com.example.foodapp.domain.usecase.restaurant

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.repositories.RestaurantRepository

class GetRestaurantsUseCase(private val restaurantRepository: RestaurantRepository) {
    suspend operator fun invoke(): List<Restaurant> {
        return restaurantRepository.getRestaurants()
    }
}
