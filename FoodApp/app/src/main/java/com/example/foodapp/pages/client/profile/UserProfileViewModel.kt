package com.example.foodapp.pages.client.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.Client
import com.google.firebase.messaging.FirebaseMessaging
import com.example.foodapp.data.model.client.profile.*
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.model.client.profile.ApiResult
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Sealed class cho các trạng thái
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: Client) : ProfileState()
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

    // State cho logout
    private val _logoutState = MutableLiveData<Boolean>(false)
    val logoutState: LiveData<Boolean> = _logoutState

    // Current user data
    private val _currentUser = MutableLiveData<Client?>()
    val currentUser: LiveData<Client?> = _currentUser

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

    // Lấy thông tin người dùng từ database
    fun fetchUserData() {
        viewModelScope.launch {
            _userState.value = ProfileState.Loading

            when (val result = profileRepository.getUserProfile()) { //Gọi API
                is ApiResult.Success -> {
                    // Bây giờ result.data là UserProfileData, không phải GetProfileResponse
                    val userData = result.data

                    // Kiểm tra userData có null hoặc empty không
                    if (userData.id.isNullOrEmpty()) {
                        _userState.value = ProfileState.Error("Không có dữ liệu người dùng")
                    } else {
                        val user = Client.fromApiResponse(userData)
                        _currentUser.value = user
                        _userState.value = ProfileState.Success(user)
                    }
                }
                is ApiResult.Failure -> {
                    _userState.value = ProfileState.Error(
                        result.exception.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
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
                            _userState.value = ProfileState.Success(updatedUser)
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