/*
package com.example.foodapp.pages.client.profile


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.Client
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    private val userRepository: UserFirebaseRepository,
    private val profileRepository: ProfileRepository
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

    init {
        fetchUserData()
    }

    // Lấy thông tin người dùng
    // Trong ViewModel
    fun fetchUserData() {
        viewModelScope.launch {
            _userState.value = ProfileState.Loading

            when (val result = profileRepository.getUserProfile()) {
                is ApiResult.Success -> {
                    val response = result.data
                    if (response.success) {
                        response.data?.let { apiData ->
                            val user = Client.fromApiResponse(apiData)
                            _currentUser.value = user
                            _userState.value = ProfileState.Success(user)
                        } ?: run {
                            _userState.value = ProfileState.Error(
                                response.message ?: "Không có dữ liệu người dùng"
                            )
                        }
                    } else {
                        _userState.value = ProfileState.Error(
                            response.message ?: "Lỗi khi lấy thông tin"
                        )
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

    // Cập nhật thông tin profile
    fun updateProfile(){

    }

    // Đổi mật khẩu
    fun changePassword(){

    }

    // Đăng xuất
    fun logout() {

    }

    // Format ngày tham gia
    fun formatJoinDate(){

    }

    // Get formatted join date cho current user
    fun getFormattedJoinDate(){

    }

    // Validate phone number
    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^[0-9]{10,11}\$".toRegex()
        return phone.matches(phoneRegex)
    }

    // Reset update state
    fun resetUpdateState() {
        _updateState.value = UpdateProfileState.Idle
    }

    // Reset change password state
    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    // Clear error states
    fun clearErrors() {
        if (_userState.value is ProfileState.Error) {
            _userState.value = ProfileState.Idle
        }
        if (_updateState.value is UpdateProfileState.Error) {
            _updateState.value = UpdateProfileState.Idle
        }
        if (_changePasswordState.value is ChangePasswordState.Error) {
            _changePasswordState.value = ChangePasswordState.Idle
        }
    }
}
*/
