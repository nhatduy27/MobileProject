package com.example.foodapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LanguageViewModel : ViewModel() {
    private val _isVietnamese = MutableStateFlow(true)
    val isVietnamese: StateFlow<Boolean> = _isVietnamese

    fun setLanguage(isVietnamese: Boolean) {
        _isVietnamese.value = isVietnamese
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            _isVietnamese.value = !_isVietnamese.value
        }
    }
}