package com.example.foodapp.pages.client.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.Client
import com.example.foodapp.data.remote.client.response.profile.*
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import kotlinx.coroutines.launch

// Sealed class cho các trạng thái
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: Client, val addresses: List<AddressResponse>) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

// Thêm state cho thêm địa chỉ
sealed class CreateAddressState {
    object Idle : CreateAddressState()
    object Loading : CreateAddressState()
    data class Success(val message: String) : CreateAddressState()
    data class Error(val message: String) : CreateAddressState()
}

// Thêm state cho xóa địa chỉ
sealed class DeleteAddressState {
    object Idle : DeleteAddressState()
    object Loading : DeleteAddressState()
    data class Success(val message: String) : DeleteAddressState()
    data class Error(val message: String) : DeleteAddressState()
}

// Thêm state cho cập nhật địa chỉ
sealed class UpdateAddressState {
    object Idle : UpdateAddressState()
    object Loading : UpdateAddressState()
    data class Success(val message: String) : UpdateAddressState()
    data class Error(val message: String) : UpdateAddressState()
}

// Thêm state cho đặt địa chỉ mặc định
sealed class SetDefaultAddressState {
    object Idle : SetDefaultAddressState()
    object Loading : SetDefaultAddressState()
    data class Success(val message: String) : SetDefaultAddressState()
    data class Error(val message: String) : SetDefaultAddressState()
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // State cho việc lấy thông tin người dùng
    private val _userState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val userState: LiveData<ProfileState> = _userState

    // State cho việc đổi mật khẩu
    private val _changePasswordState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    // State cho việc thêm địa chỉ
    private val _createAddressState = MutableLiveData<CreateAddressState>(CreateAddressState.Idle)
    val createAddressState: LiveData<CreateAddressState> = _createAddressState

    // State cho việc xóa địa chỉ
    private val _deleteAddressState = MutableLiveData<DeleteAddressState>(DeleteAddressState.Idle)
    val deleteAddressState: LiveData<DeleteAddressState> = _deleteAddressState

    // State cho việc cập nhật địa chỉ
    private val _updateAddressState = MutableLiveData<UpdateAddressState>(UpdateAddressState.Idle)
    val updateAddressState: LiveData<UpdateAddressState> = _updateAddressState

    // State cho việc đặt địa chỉ mặc định
    private val _setDefaultAddressState = MutableLiveData<SetDefaultAddressState>(SetDefaultAddressState.Idle)
    val setDefaultAddressState: LiveData<SetDefaultAddressState> = _setDefaultAddressState

    // State cho logout
    private val _logoutState = MutableLiveData<Boolean>(false)
    val logoutState: LiveData<Boolean> = _logoutState

    // Current user data
    private val _currentUser = MutableLiveData<Client?>()
    val currentUser: LiveData<Client?> = _currentUser

    // Current addresses
    private val _addresses = MutableLiveData<List<AddressResponse>>(emptyList())
    val addresses: LiveData<List<AddressResponse>> = _addresses

    // Load thông tin của người dùng ngay khi vừa vào app
    init {
        fetchUserData()
    }

    // user refresh thủ công
    fun refreshUserData() {
        fetchUserData()
    }

    // Lấy thông tin người dùng và địa chỉ từ database
    fun fetchUserData() {
        viewModelScope.launch {
            _userState.value = ProfileState.Loading

            try {
                // Lấy profile và addresses đồng thời
                val userResult = profileRepository.getUserProfile()
                val addressesResult = profileRepository.getAddresses()

                when (userResult) {
                    is ApiResult.Success -> {
                        val userData = userResult.data

                        // Kiểm tra userData có null hoặc empty không
                        if (userData.id.isNullOrEmpty()) {
                            _userState.value = ProfileState.Error("Không có dữ liệu người dùng")
                        } else {
                            val user = Client.fromApiResponse(userData)
                            _currentUser.value = user

                            // Xử lý địa chỉ
                            val addresses = when (addressesResult) {
                                is ApiResult.Success -> addressesResult.data
                                is ApiResult.Failure -> emptyList()
                            }

                            _addresses.value = addresses
                            _userState.value = ProfileState.Success(user, addresses)
                        }
                    }
                    is ApiResult.Failure -> {
                        _userState.value = ProfileState.Error(
                            userResult.exception.message ?: "Lỗi không xác định"
                        )
                    }
                }
            } catch (e: Exception) {
                _userState.value = ProfileState.Error(
                    e.message ?: "Lỗi không xác định"
                )
            }
        }
    }

    fun refreshAddresses() {
        viewModelScope.launch {
            try {
                val result = profileRepository.getAddresses()
                when (result) {
                    is ApiResult.Success -> {
                        val addresses = result.data
                        _addresses.value = addresses

                        // Cập nhật state nếu có current user
                        _currentUser.value?.let { user ->
                            _userState.value = ProfileState.Success(user, addresses)
                        }
                    }
                    is ApiResult.Failure -> {
                        // Không cần thông báo lỗi ở đây, vì đây chỉ là refresh background
                        println("DEBUG: [ViewModel] Refresh addresses failed: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: [ViewModel] Refresh addresses exception: ${e.message}")
            }
        }
    }

    // Hàm thêm địa chỉ mới
    fun createAddress(
        label: String,
        fullAddress: String,
        building: String? = null,
        room: String? = null,
        note: String? = null,
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            _createAddressState.value = CreateAddressState.Loading

            try {
                // Tạo request
                val request = CreateAddressRequest(
                    label = label,
                    fullAddress = fullAddress,
                    building = building,
                    room = room,
                    note = note,
                    isDefault = isDefault
                )

                val result = profileRepository.createAddress(request)

                when (result) {
                    is ApiResult.Success -> {
                        val addressData = result.data
                        // Refresh danh sách địa chỉ
                        refreshAddresses()
                        _createAddressState.value = CreateAddressState.Success("Thêm địa chỉ thành công")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Thêm địa chỉ thất bại"
                        _createAddressState.value = CreateAddressState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi thêm địa chỉ"
                _createAddressState.value = CreateAddressState.Error(errorMessage)
            }
        }
    }

    // Hàm xóa địa chỉ
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _deleteAddressState.value = DeleteAddressState.Loading

            try {
                val result = profileRepository.deleteAddress(addressId)

                when (result) {
                    is ApiResult.Success -> {
                        // Xóa thành công
                        if (result.data) {
                            // Refresh danh sách địa chỉ
                            refreshAddresses()
                            _deleteAddressState.value = DeleteAddressState.Success("Xóa địa chỉ thành công")
                        } else {
                            _deleteAddressState.value = DeleteAddressState.Error("Xóa địa chỉ thất bại")
                        }
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Xóa địa chỉ thất bại"
                        _deleteAddressState.value = DeleteAddressState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi xóa địa chỉ"
                _deleteAddressState.value = DeleteAddressState.Error(errorMessage)
            }
        }
    }

    // Hàm cập nhật địa chỉ
    fun updateAddress(
        addressId: String,
        label: String,
        fullAddress: String,
        building: String? = null,
        room: String? = null,
        note: String? = null,
        isDefault: Boolean = false
    ) {
        viewModelScope.launch {
            _updateAddressState.value = UpdateAddressState.Loading

            try {
                // Tạo request
                val request = UpdateAddressRequest(
                    label = label,
                    fullAddress = fullAddress,
                    building = building,
                    room = room,
                    note = note,
                    isDefault = isDefault
                )

                val result = profileRepository.updateAddress(addressId, request)

                when (result) {
                    is ApiResult.Success -> {
                        val addressData = result.data
                        // Refresh danh sách địa chỉ
                        refreshAddresses()
                        _updateAddressState.value = UpdateAddressState.Success("Cập nhật địa chỉ thành công")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Cập nhật địa chỉ thất bại"
                        _updateAddressState.value = UpdateAddressState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi cập nhật địa chỉ"
                _updateAddressState.value = UpdateAddressState.Error(errorMessage)
            }
        }
    }

    // Hàm đặt địa chỉ mặc định
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _setDefaultAddressState.value = SetDefaultAddressState.Loading

            try {
                val result = profileRepository.setDefaultAddress(addressId)

                when (result) {
                    is ApiResult.Success -> {
                        // Đặt mặc định thành công
                        if (result.data) {
                            // Refresh danh sách địa chỉ
                            refreshAddresses()
                            _setDefaultAddressState.value = SetDefaultAddressState.Success("Đặt địa chỉ mặc định thành công")
                        } else {
                            _setDefaultAddressState.value = SetDefaultAddressState.Error("Đặt địa chỉ mặc định thất bại")
                        }
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Đặt địa chỉ mặc định thất bại"
                        _setDefaultAddressState.value = SetDefaultAddressState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi đặt địa chỉ mặc định"
                _setDefaultAddressState.value = SetDefaultAddressState.Error(errorMessage)
            }
        }
    }

    // Reset các state
    fun resetCreateAddressState() {
        _createAddressState.value = CreateAddressState.Idle
    }

    fun resetDeleteAddressState() {
        _deleteAddressState.value = DeleteAddressState.Idle
    }

    fun resetUpdateAddressState() {
        _updateAddressState.value = UpdateAddressState.Idle
    }

    fun resetSetDefaultAddressState() {
        _setDefaultAddressState.value = SetDefaultAddressState.Idle
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val profileRepository = ProfileRepository()
                ProfileViewModel(profileRepository)
            }
        }
    }
}