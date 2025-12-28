package com.example.foodapp.pages.user.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.foodapp.data.repository.FirebaseRepository

sealed class UserNameState {
    object Idle : UserNameState()
    object Loading : UserNameState()
    data class Success(val userName: String) : UserNameState()
    data class Error(val message: String) : UserNameState()
    object Empty : UserNameState()
}

class HomeViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    // State dùng cho Compose Observe
    private val _userNameState = MutableLiveData<UserNameState>(UserNameState.Idle)
    val userNameState: LiveData<UserNameState> = _userNameState

    // Lưu tên user dạng String đơn giản
    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> = _userName

    fun fetchUserName() {
        _userNameState.value = UserNameState.Loading

        // Gọi repository để lấy tên từ Firebase
        repository.getCurrentUserName { name ->
            if (!name.isNullOrBlank()) {
                _userNameState.postValue(UserNameState.Success(name))
                _userName.postValue(name)
            } else {
                _userNameState.postValue(UserNameState.Error("Không tìm thấy người dùng"))
                _userName.postValue(null)
            }
        }
    }

    fun clearUserName() {
        _userNameState.value = UserNameState.Empty
        _userName.value = null
    }

    companion object {
        // Tạo Factory cho ViewModel
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val repository = FirebaseRepository(context)
                HomeViewModel(repository)
            }
        }
    }
}