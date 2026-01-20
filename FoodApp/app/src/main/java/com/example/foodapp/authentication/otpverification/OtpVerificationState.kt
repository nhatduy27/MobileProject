package com.example.foodapp.authentication.otpverification

/**
 * Đại diện cho các trạng thái khác nhau của màn hình xác thực OTP.
 */
sealed class OtpVerificationState {
    /** Trạng thái chờ, không có hoạt động nào đang diễn ra. */
    object Idle : OtpVerificationState()

    /** Đang tải email của người dùng. */
    object LoadingEmail : OtpVerificationState()

    /** Đang trong quá trình gửi mã OTP đến email người dùng. */
    object Sending : OtpVerificationState()

    /** Đang xác thực mã OTP do người dùng nhập. */
    object Verifying : OtpVerificationState()

    /** Quá trình xác thực OTP thành công. */
    object Success : OtpVerificationState()

    /** Xảy ra lỗi trong quá trình xử lý. */
    data class Error(val message: String) : OtpVerificationState()
}
