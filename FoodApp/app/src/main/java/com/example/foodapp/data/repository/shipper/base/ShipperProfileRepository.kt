package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.ProfileMenuItem
import com.example.foodapp.data.model.shipper.ShipperProfile

/**
 * Interface cho Profile Repository của Shipper.
 * Định nghĩa các phương thức mà bất kỳ implementation nào (Mock/Real) phải tuân theo.
 */
interface ShipperProfileRepository {
    
    /**
     * Lấy thông tin profile của shipper
     */
    fun getProfile(): ShipperProfile
    
    /**
     * Lấy danh sách menu items cho phần Tài khoản
     */
    fun getAccountItems(profile: ShipperProfile): List<ProfileMenuItem>
    
    /**
     * Lấy danh sách menu items cho phần Cài đặt
     */
    fun getSettingsItems(): List<ProfileMenuItem>
    
    /**
     * Lấy danh sách menu items cho phần Khác
     */
    fun getOtherItems(): List<ProfileMenuItem>
}
