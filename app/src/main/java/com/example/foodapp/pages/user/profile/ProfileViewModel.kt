package com.example.foodapp.pages.user.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.User
import com.example.foodapp.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

sealed class UserDataState {
    object Idle : UserDataState()
    object Loading : UserDataState()
    data class Success(val user: User) : UserDataState()
    data class Error(val message: String) : UserDataState()
}



class ProfileViewModel(private val repository: FirebaseRepository) : ViewModel() {

    private val _userDataState = MutableLiveData<UserDataState>(UserDataState.Idle)
    val userDataState: LiveData<UserDataState> = _userDataState


    fun fetchUserData() {
        viewModelScope.launch {
            _userDataState.value = UserDataState.Loading
            repository.getCurrentUserWithDetails { user ->
                if (user != null) {
                    _userDataState.value = UserDataState.Success(user)
                } else {
                    _userDataState.value = UserDataState.Error("Không thể tải thông tin người dùng")
                }
            }
        }
    }

    fun updateProfile(fullName: String? = null, phone: String? = null) {
        viewModelScope.launch {
            // Hiển thị loading trên chính UI đang dùng userDataState
            _userDataState.value = UserDataState.Loading

            repository.updateProfile(fullName = fullName, phone = phone) { success, errorMessage ->
                if (success) {
                    //fetch lại toàn bộ User
                    fetchUserData()
                } else {
                    _userDataState.value = UserDataState.Error(errorMessage ?: "Lỗi cập nhật")
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(FirebaseRepository(context)) as T
                }
            }
        }
    }
}