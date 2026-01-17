package com.example.foodapp.authentication.otpverification

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
    object LoadingEmail : OtpVerificationState()
    object Sending : OtpVerificationState()
    object Verifying : OtpVerificationState()
    object Success : OtpVerificationState()
    data class Error(val message: String) : OtpVerificationState()
}

class OtpVerificationViewModel(
    private val userRepository: UserFirebaseRepository,
    private val otpRepository: OtpRepository
) : ViewModel() {

    private val _otpState = MutableLiveData<OtpVerificationState>(OtpVerificationState.LoadingEmail)
    val otpState: LiveData<OtpVerificationState> = _otpState

    private val _remainingTime = MutableLiveData(0)
    val remainingTime: LiveData<Int> = _remainingTime

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        getCurrentUserEmail()
    }

    fun getCurrentUserEmail() {
        viewModelScope.launch {
            _otpState.value = OtpVerificationState.LoadingEmail

            userRepository.getUserEmailByUid { email ->
                if (email != null) {
                    _userEmail.postValue(email)
                    // Tự động gửi OTP khi có email
                    sendOtp(email)
                } else {
                    _userEmail.postValue("")
                    _otpState.postValue(OtpVerificationState.Error("Không tìm thấy email người dùng"))
                }
            }
        }
    }

    fun startTimer(expiryTimeString: String? = null) {
        timerJob?.cancel()

        val totalSeconds = if (expiryTimeString != null) {
            calculateRemainingSeconds(expiryTimeString)
        } else {
            5 * 60 // Mặc định 5 phút nếu không có expiry time
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

    private fun sendOtp(email: String) {
        viewModelScope.launch {
            _otpState.value = OtpVerificationState.Sending

            when (val result = otpRepository.sendOtp(email)) {
                is ApiResult.Success -> {
                    // Với model mới, result.data là SimpleMessageData
                    // Nếu API trả về expiresAt, cần điều chỉnh model
                    // Hiện tại không có expiresAt trong SimpleMessageData

                    // Bắt đầu timer với mặc định
                    startTimer()

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

            when (val result = otpRepository.verifyOtp(email, otpCode, OTPType.EMAIL_VERIFICATION)) {
                is ApiResult.Success -> {
                    // result.data là SimpleMessageData
                    val message = result.data.message

                    // Kiểm tra nếu xác thực thành công
                    if (message.contains("thành công", ignoreCase = true) ||
                        message.contains("success", ignoreCase = true)) {

                        // Cập nhật trạng thái verified trong Firebase
                        userRepository.setUserVerified { success ->
                            if (success) {
                                _otpState.value = OtpVerificationState.Success
                                stopTimer()
                            } else {
                                _otpState.value = OtpVerificationState.Error("Không thể cập nhật trạng thái xác thực")
                            }
                        }
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
            sendOtp(email)
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
                    if (modelClass.isAssignableFrom(OtpVerificationViewModel::class.java)) {
                        val userRepository = UserFirebaseRepository(context)
                        val otpRepository = OtpRepository()
                        return OtpVerificationViewModel(userRepository, otpRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}