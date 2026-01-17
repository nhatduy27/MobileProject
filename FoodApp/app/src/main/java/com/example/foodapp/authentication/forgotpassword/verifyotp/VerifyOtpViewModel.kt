package com.example.foodapp.authentication.forgotpassword.verifyotp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.otp.*
import com.example.foodapp.data.repository.OtpRepository
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class OtpVerificationState {
    object Idle : OtpVerificationState()
    object Sending : OtpVerificationState()
    object Verifying : OtpVerificationState()
    object Success : OtpVerificationState()
    data class Error(val message: String) : OtpVerificationState()
}

class VerifyOTPViewModel(
    private val userRepository: UserFirebaseRepository,
    private val otpRepository: OtpRepository
) : ViewModel() {

    private val _otpState = MutableLiveData<OtpVerificationState>(OtpVerificationState.Idle)
    val otpState: LiveData<OtpVerificationState> = _otpState

    private val _remainingTime = MutableLiveData(0)
    val remainingTime: LiveData<Int> = _remainingTime

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private var timerJob: kotlinx.coroutines.Job? = null

    fun setEmail(email: String) {
        _userEmail.value = email
    }

    fun startTimer(expiryTimeString: String? = null) {
        timerJob?.cancel()

        val totalSeconds = if (expiryTimeString != null) {
            calculateRemainingSeconds(expiryTimeString)
        } else {
            5 * 60 // Mặc định 5 phút
        }

        if (totalSeconds > 0) {
            _remainingTime.value = totalSeconds

            timerJob = viewModelScope.launch {
                while (_remainingTime.value ?: 0 > 0) {
                    kotlinx.coroutines.delay(1000)
                    _remainingTime.postValue((_remainingTime.value ?: 0) - 1)
                }

                _otpState.postValue(OtpVerificationState.Error("OTP đã hết hạn"))
            }
        }
    }

    private fun calculateRemainingSeconds(expiryTimeString: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val expiryDate = dateFormat.parse(expiryTimeString)
            val now = Date()

            val diffInMillis = expiryDate.time - now.time
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis).toInt()
            maxOf(0, seconds)
        } catch (e: Exception) {
            5 * 60 // Mặc định 5 phút nếu parse lỗi
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun sendOtpResetPassword(email: String) {
        viewModelScope.launch {
            _otpState.value = OtpVerificationState.Sending

            when (val result = otpRepository.sendOtpResetPassword(email)) {
                is ApiResult.Success -> {
                    // Với model mới, result.data là SimpleMessageData
                    // Không có expiresAt trong SimpleMessageData, dùng mặc định
                    startTimer() // Bắt đầu timer với mặc định

                    _otpState.value = OtpVerificationState.Idle
                }
                is ApiResult.Failure -> {
                    _otpState.value = OtpVerificationState.Error(
                        result.exception.message ?: "Gửi OTP thất bại"
                    )
                }
            }
        }
    }

    fun verifyOtp(otpCode: String) {
        if (otpCode.length != 6 || !otpCode.all { it.isDigit() }) {
            _otpState.value = OtpVerificationState.Error("Mã OTP phải có 6 chữ số")
            return
        }

        val email = _userEmail.value
        if (email.isNullOrEmpty()) {
            _otpState.value = OtpVerificationState.Error("Không tìm thấy email")
            return
        }

        viewModelScope.launch {
            _otpState.value = OtpVerificationState.Verifying

            when (val result = otpRepository.verifyOtp(email, otpCode, OTPType.PASSWORD_RESET)) {
                is ApiResult.Success -> {
                    // result.data là SimpleMessageData
                    val message = result.data.message

                    // Kiểm tra nếu xác thực thành công dựa trên message
                    if (message.contains("thành công", ignoreCase = true) ||
                        message.contains("success", ignoreCase = true) ||
                        message.contains("xác thực", ignoreCase = true)) {

                        _otpState.value = OtpVerificationState.Success
                        stopTimer()
                    } else {
                        _otpState.value = OtpVerificationState.Error(message)
                    }
                }
                is ApiResult.Failure -> {
                    _otpState.value = OtpVerificationState.Error(
                        result.exception.message ?: "Xác thực OTP thất bại"
                    )
                }
            }
        }
    }

    fun resendOtp() {
        val email = _userEmail.value
        if (!email.isNullOrEmpty()) {
            sendOtpResetPassword(email)
        } else {
            _otpState.value = OtpVerificationState.Error("Không tìm thấy email để gửi lại OTP")
        }
    }

    fun resetState() {
        _otpState.value = OtpVerificationState.Idle
        stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(VerifyOTPViewModel::class.java)) {
                        val userRepository = UserFirebaseRepository(context)
                        val otpRepository = OtpRepository()
                        return VerifyOTPViewModel(userRepository, otpRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}