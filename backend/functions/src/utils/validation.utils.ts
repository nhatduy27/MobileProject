/**
 * Validation Utilities
 *
 * Helper functions cho data validation
 */

/**
 * Check if value is not null/undefined/empty
 * @param {unknown} value Giá trị cần kiểm tra
 * @return {boolean} true nếu có giá trị
 */
export function isNotEmpty(value: unknown): boolean {
  return value !== null && value !== undefined && value !== "";
}

/**
 * Validate email format
 * @param {string} email Email cần kiểm tra
 * @return {boolean} true nếu hợp lệ
 */
export function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate phone number format (Vietnam)
 * @param {string} phone Số điện thoại cần kiểm tra
 * @return {boolean} true nếu hợp lệ
 */
export function isValidPhoneNumber(phone: string): boolean {
  const phoneRegex = /^(\+84|0)[3|5|7|8|9][0-9]{8}$/;
  return phoneRegex.test(phone);
}

/**
 * Validate positive number
 * @param {number} value Số cần kiểm tra
 * @return {boolean} true nếu > 0
 */
export function isPositiveNumber(value: number): boolean {
  return typeof value === "number" && value > 0;
}

/**
 * Validate array is not empty
 * @param {unknown} arr Mảng cần kiểm tra
 * @return {boolean} true nếu là mảng không rỗng
 */
export function isNonEmptyArray(arr: unknown): boolean {
  return Array.isArray(arr) && arr.length > 0;
}
