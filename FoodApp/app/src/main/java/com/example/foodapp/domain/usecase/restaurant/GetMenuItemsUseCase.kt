package com.example.foodapp.domain.usecase.restaurant

import com.example.foodapp.domain.entities.MenuItem
import com.example.foodapp.domain.repositories.RestaurantRepository

class GetMenuItemsUseCase(private val restaurantRepository: RestaurantRepository) {
    suspend operator fun invoke(restaurantId: String): List<MenuItem> {
        return restaurantRepository.getMenuItems(restaurantId)
    }
}
