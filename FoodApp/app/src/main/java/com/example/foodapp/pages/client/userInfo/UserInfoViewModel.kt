package com.example.foodapp.pages.client.userInfo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.model.client.Client
import com.example.foodapp.data.remote.client.response.profile.ApiResult
import com.example.foodapp.data.remote.client.response.profile.UpdateProfileRequest
import com.example.foodapp.data.repository.client.profile.ProfileRepository
import kotlinx.coroutines.launch
import java.io.File

// Sealed class cho các trạng thái
sealed class UserInfoState {
    object Idle : UserInfoState()
    object Loading : UserInfoState()
    data class Success(val user: Client) : UserInfoState()
    data class Error(val message: String) : UserInfoState()
}

sealed class UpdateUserInfoState {
    object Idle : UpdateUserInfoState()
    object Loading : UpdateUserInfoState()
    data class Success(val message: String) : UpdateUserInfoState()
    data class Error(val message: String) : UpdateUserInfoState()
}

sealed class UploadAvatarState {
    object Idle : UploadAvatarState()
    object Loading : UploadAvatarState()
    data class Success(val avatarUrl: String) : UploadAvatarState()
    data class Error(val message: String) : UploadAvatarState()
}

class UserInfoViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // State cho việc lấy thông tin người dùng
    private val _userState = MutableLiveData<UserInfoState>(UserInfoState.Idle)
    val userState: LiveData<UserInfoState> = _userState

    // State cho việc cập nhật profile
    private val _updateState = MutableLiveData<UpdateUserInfoState>(UpdateUserInfoState.Idle)
    val updateState: LiveData<UpdateUserInfoState> = _updateState

    // State cho upload avatar
    private val _uploadAvatarState = MutableLiveData<UploadAvatarState>(UploadAvatarState.Idle)
    val uploadAvatarState: LiveData<UploadAvatarState> = _uploadAvatarState

    // Current user data
    private val _currentUser = MutableLiveData<Client?>()
    val currentUser: LiveData<Client?> = _currentUser

    // Load thông tin của người dùng
    init {
        fetchUserData()
    }

    // Lấy thông tin người dùng
    fun fetchUserData() {
        viewModelScope.launch {
            _userState.value = UserInfoState.Loading

            try {
                val result = profileRepository.getUserProfile()

                when (result) {
                    is ApiResult.Success -> {
                        val userData = result.data

                        // Kiểm tra userData có null hoặc empty không
                        if (userData.id.isNullOrEmpty()) {
                            _userState.value = UserInfoState.Error("Không có dữ liệu người dùng")
                        } else {
                            val user = Client.fromApiResponse(userData)
                            _currentUser.value = user
                            _userState.value = UserInfoState.Success(user)
                        }
                    }
                    is ApiResult.Failure -> {
                        _userState.value = UserInfoState.Error(
                            result.exception.message ?: "Lỗi không xác định"
                        )
                    }
                }
            } catch (e: Exception) {
                _userState.value = UserInfoState.Error(
                    e.message ?: "Lỗi không xác định"
                )
            }
        }
    }

    // Refresh dữ liệu - có thể thêm logic bổ sung nếu cần
    fun refreshUserData() {
        fetchUserData()
    }

    // Cập nhật thông tin người dùng
    fun updateProfile(displayName: String?, phone: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateUserInfoState.Loading

            try {
                // Tạo request
                val request = UpdateProfileRequest(
                    displayName = displayName?.trim().takeIf { !it.isNullOrBlank() },
                    phone = phone?.trim().takeIf { !it.isNullOrBlank() },
                    avatarUrl = null
                )

                println("DEBUG: [UserInfoViewModel] Update request: $request")

                val result = profileRepository.updateProfile(request)

                when (result) {
                    is ApiResult.Success -> {
                        val updatedUserData = result.data
                        println("DEBUG: [UserInfoViewModel] Update successful: ${updatedUserData}")

                        // Cập nhật current user với dữ liệu mới
                        val currentUser = _currentUser.value
                        currentUser?.let { user ->
                            val updatedUser = user.copy(
                                fullName = updatedUserData.displayName ?: user.fullName,
                                phone = updatedUserData.phone ?: user.phone,
                                imageAvatar = updatedUserData.avatarUrl ?: user.imageAvatar
                            )
                            _currentUser.value = updatedUser
                            _userState.value = UserInfoState.Success(updatedUser)
                        }

                        _updateState.value = UpdateUserInfoState.Success("Cập nhật thông tin thành công")
                        println("DEBUG: [UserInfoViewModel] Update state set to success")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Cập nhật thất bại"
                        _updateState.value = UpdateUserInfoState.Error(errorMessage)
                        println("DEBUG: [UserInfoViewModel] Update failed: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi cập nhật"
                _updateState.value = UpdateUserInfoState.Error(errorMessage)
                println("DEBUG: [UserInfoViewModel] Update exception: $errorMessage")
            }
        }
    }

    // Upload avatar
    fun uploadAvatar(imageFile: File) {
        viewModelScope.launch {
            _uploadAvatarState.value = UploadAvatarState.Loading

            try {
                println("DEBUG: [UserInfoViewModel] Starting upload avatar: ${imageFile.name}")

                // Validate file type
                val fileName = imageFile.name.lowercase()
                val isValidType = fileName.endsWith(".jpg") ||
                        fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png")

                if (!isValidType) {
                    _uploadAvatarState.value = UploadAvatarState.Error(
                        "Chỉ chấp nhận file ảnh PNG hoặc JPEG"
                    )
                    println("DEBUG: [UserInfoViewModel] Invalid file type: $fileName")
                    return@launch
                }

                // Validate file size (5MB max)
                val maxSize = 5 * 1024 * 1024 // 5MB
                if (imageFile.length() > maxSize) {
                    _uploadAvatarState.value = UploadAvatarState.Error(
                        "Kích thước file không được vượt quá 5MB"
                    )
                    println("DEBUG: [UserInfoViewModel] File too large: ${imageFile.length()}")
                    return@launch
                }

                // Validate file exists and can be read
                if (!imageFile.exists() || !imageFile.canRead()) {
                    _uploadAvatarState.value = UploadAvatarState.Error(
                        "Không thể đọc file ảnh"
                    )
                    println("DEBUG: [UserInfoViewModel] File cannot be read: ${imageFile.exists()}, ${imageFile.canRead()}")
                    return@launch
                }

                println("DEBUG: [UserInfoViewModel] File validated: $fileName, size: ${imageFile.length()}")
                val result = profileRepository.uploadAvatar(imageFile)

                when (result) {
                    is ApiResult.Success -> {
                        val avatarUrl = result.data
                        println("DEBUG: [UserInfoViewModel] Upload avatar successful: $avatarUrl")

                        // Cập nhật current user với avatar mới
                        val currentUser = _currentUser.value
                        currentUser?.let { user ->
                            val updatedUser = user.copy(imageAvatar = avatarUrl)
                            _currentUser.value = updatedUser
                            _userState.value = UserInfoState.Success(updatedUser)
                        }

                        _uploadAvatarState.value = UploadAvatarState.Success(avatarUrl)
                        println("DEBUG: [UserInfoViewModel] Upload state set to success")
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Tải lên ảnh thất bại"
                        _uploadAvatarState.value = UploadAvatarState.Error(errorMessage)
                        println("DEBUG: [UserInfoViewModel] Upload failed: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Lỗi không xác định khi tải lên ảnh"
                _uploadAvatarState.value = UploadAvatarState.Error(errorMessage)
                println("DEBUG: [UserInfoViewModel] Upload exception: $errorMessage")
            }
        }
    }

    // Reset các state
    fun resetUpdateState() {
        _updateState.value = UpdateUserInfoState.Idle
    }

    fun resetUploadAvatarState() {
        _uploadAvatarState.value = UploadAvatarState.Idle
    }

    // Reset tất cả state về idle
    fun resetAllStates() {
        _userState.value = UserInfoState.Idle
        _updateState.value = UpdateUserInfoState.Idle
        _uploadAvatarState.value = UploadAvatarState.Idle
    }

    // Force refresh với xóa cache hoặc logic bổ sung (nếu cần)
    fun forceRefresh() {
        // Có thể thêm logic như xóa cache local trước khi fetch
        resetAllStates()
        fetchUserData()
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val profileRepository = ProfileRepository()
                UserInfoViewModel(profileRepository)
            }
        }
    }
}