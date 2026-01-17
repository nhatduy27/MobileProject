package com.example.foodapp.authentication.forgotpassword.verifyotp


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.otp.ApiResult
import com.example.foodapp.data.repository.OtpRepository
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.model.shared.otp.*
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

class VeridyOTPViewModel(
    private val userRepository: UserFirebaseRepository,
    private val otpRepository: OtpRepository
) : ViewModel() {

    private val _otpState = MutableLiveData<OtpVerificationState>(OtpVerificationState.LoadingEmail) // 👈 Ban đầu là LoadingEmail
    val otpState: LiveData<OtpVerificationState> = _otpState

    private val _remainingTime = MutableLiveData(0)
    val remainingTime: LiveData<Int> = _remainingTime

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private var timerJob: kotlinx.coroutines.Job? = null


    fun setEmail(email : String){
        _userEmail.value = email
    }

    fun startTimer(expiryTimeString: String? = null) {
        timerJob?.cancel()

        val totalSeconds = if (expiryTimeString != null) {
            calculateRemainingSeconds(expiryTimeString)
        } else {
            5 * 60
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
            5 * 60
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun sendOtpResetPassword(email: String) {
        viewModelScope.launch {
            // State đã là Sending rồi (được set ở hàm trên)

            when (val result = otpRepository.sendOtpResetPassword(email)) {
                is ApiResult.Success -> {
                    // Parse và bắt đầu timer dựa trên expiry time từ API
                    startTimer(result.data.expiresAt)

                    // State 3: Chuyển về Idle (sẵn sàng nhập OTP)
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
                    // If we reach here, verification was successful
                    userRepository.setUserVerified { success ->
                        _otpState.value = OtpVerificationState.Success
                        stopTimer()
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
            _otpState.value = OtpVerificationState.Sending
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
                    if (modelClass.isAssignableFrom(VeridyOTPViewModel::class.java)) {
                        val userRepository = UserFirebaseRepository(context)
                        val otpRepository = OtpRepository()
                        return VeridyOTPViewModel(userRepository, otpRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}