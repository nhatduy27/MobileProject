package com.example.foodapp.pages.client.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.Client
import com.example.foodapp.data.model.client.DeliveryAddress
import com.example.foodapp.data.remote.client.response.profile.*
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Sealed class cho các trạng thái
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: Client, val addresses: List<AddressResponse>) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    data class Success(val message: String) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
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

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val authManager: AuthManager,
) : ViewModel() {

    // State cho việc lấy thông tin người dùng
    private val _userState = MutableLiveData<ProfileState>(ProfileState.Idle)
    val userState: LiveData<ProfileState> = _userState

    // State cho việc cập nhật profile
    private val _updateState = MutableLiveData<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: LiveData<UpdateProfileState> = _updateState

    // State cho việc đổi mật khẩu
    private val _changePasswordState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    // State cho việc thêm địa chỉ
    private val _createAddressState = MutableLiveData<CreateAddressState>(CreateAddressState.Idle)
    val createAddressState: LiveData<CreateAddressState> = _createAddressState

    // State cho việc xóa địa chỉ
    private val _deleteAddressState = MutableLiveData<DeleteAddressState>(DeleteAddressState.Idle)
    val deleteAddressState: LiveData<DeleteAddressState> = _deleteAddressState

    // State cho logout
    private val _logoutState = MutableLiveData<Boolean>(false)
    val logoutState: LiveData<Boolean> = _logoutState

    // Current user data
    private val _currentUser = MutableLiveData<Client?>()
    val currentUser: LiveData<Client?> = _currentUser

    // Current addresses
    private val _addresses = MutableLiveData<List<AddressResponse>>(emptyList())
    val addresses: LiveData<List<AddressResponse>> = _addresses

    // Format date cho ngày tham gia
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    //Load thông tin của người dùng ngay khi vừa vào app
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
                                is ApiResult.Success -> (addressesResult.data as? List<AddressResponse>) ?: emptyList()
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

    // Tải lại danh sách địa chỉ
    fun refreshAddresses() {
        viewModelScope.launch {
            try {
                val result = profileRepository.getAddresses()
                when (result) {
                    is ApiResult.Success -> {
                        val addresses = (result.data as? List<AddressResponse>) ?: emptyList()
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


    // Reset delete address state
    fun resetDeleteAddressState() {
        _deleteAddressState.value = DeleteAddressState.Idle
    }

    fun updateProfile(displayName: String?, phone: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            try {
                // Tạo request
                val request = UpdateProfileRequest(
                    displayName = displayName?.trim().takeIf { !it.isNullOrBlank() },
                    phone = phone?.trim().takeIf { !it.isNullOrBlank() },
                    avatarUrl = null
                )

                println("DEBUG: [ViewModel] Update request: $request")

                val result = profileRepository.updateProfile(request)

                when (result) {
                    is ApiResult.Success -> {
                        val updatedUserData = result.data
                        println("DEBUG: [ViewModel] Update successful: ${updatedUserData}")

                        // Cập nhật current user với dữ liệu mới
                        val currentUser = _currentUser.value
                        currentUser?.let { user ->
                            val updatedUser = user.copy(
                                fullName = updatedUserData.displayName ?: user.fullName,
                                phone = updatedUserData.phone ?: user.phone,
                                imageAvatar = updatedUserData.avatarUrl ?: user.imageAvatar
                            )
                            _currentUser.value = updatedUser
                            // Giữ nguyên danh sách địa chỉ
                            _userState.value = ProfileState.Success(updatedUser, _addresses.value ?: emptyList())
                        }

                        _updateState.value = UpdateProfileState.Success("Cập nhật thông tin thành công")
                        println("DEBUG: [ViewModel] Update state set to success")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Cập nhật thất bại"
                        _updateState.value = UpdateProfileState.Error(errorMessage)
                        println("DEBUG: [ViewModel] Update failed: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi cập nhật"
                _updateState.value = UpdateProfileState.Error(errorMessage)
                println("DEBUG: [ViewModel] Update exception: $errorMessage")
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^[0-9]{10,11}\$".toRegex()
        return phone.matches(phoneRegex)
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

                println("DEBUG: [ViewModel] Create address request: $request")

                val result = profileRepository.createAddress(request)

                when (result) {
                    is ApiResult.Success -> {
                        val addressData = result.data
                        println("DEBUG: [ViewModel] Address created successfully: ${addressData}")

                        // Refresh danh sách địa chỉ
                        refreshAddresses()

                        _createAddressState.value = CreateAddressState.Success("Thêm địa chỉ thành công")
                        println("DEBUG: [ViewModel] Create address state set to success")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Thêm địa chỉ thất bại"
                        _createAddressState.value = CreateAddressState.Error(errorMessage)
                        println("DEBUG: [ViewModel] Create address failed: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi thêm địa chỉ"
                _createAddressState.value = CreateAddressState.Error(errorMessage)
                println("DEBUG: [ViewModel] Create address exception: $errorMessage")
            }
        }
    }

    // Reset create address state
    fun resetCreateAddressState() {
        _createAddressState.value = CreateAddressState.Idle
    }

    // Factory cho ViewModel
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val authRepository = AuthRepository()
                val profileRepository = ProfileRepository()
                val authManager = AuthManager(context)
                return ProfileViewModel(authRepository, profileRepository, authManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

        companion object {
            @Volatile
            private var INSTANCE: Factory? = null

            fun getInstance(context: Context): Factory {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Factory(context.applicationContext).also {
                        INSTANCE = it
                    }
                }
            }
        }
    }
}