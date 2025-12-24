package com.example.foodapp.authentication.roleselection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RoleSelectionViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    var selectedRole by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var success by mutableStateOf(false)
        private set

    fun selectRole(role: String) {
        selectedRole = role
        errorMessage = null
    }

    fun saveRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val role = selectedRole

        if (userId == null || role == null) {
            errorMessage = "Vui lòng chọn vai trò"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                // Gọi repository để lưu role
                repository.saveUserRole(userId, role) { isSuccessful, message ->
                    isLoading = false
                    if (isSuccessful) {
                        success = true
                    } else {
                        errorMessage = message
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Lỗi: ${e.message}"
            }
        }
    }
}