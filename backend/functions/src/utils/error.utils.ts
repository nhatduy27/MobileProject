/**
 * Error Utilities
 *
 * Helper functions cho error handling
 */

import {HttpsError} from "firebase-functions/v2/https";

/**
 * Convert error thành HttpsError cho callable functions.
 * @param {unknown} error Unknown error object
 * @return {HttpsError} Normalized https error
 */
export function toHttpsError(error: unknown): HttpsError {
  // Nếu đã là HttpsError thì return luôn
  if (error instanceof HttpsError) {
    return error;
  }

  // Parse error message
  const rawMessage = (error as {message?: unknown})?.message;
  let message: string;
  if (typeof rawMessage === "string") {
    message = rawMessage;
  } else {
    message = String(rawMessage ?? "Unknown error");
  }

  // Map common errors to appropriate codes
  if (
    message.includes("Unauthenticated") ||
    message.includes("authentication")
  ) {
    return new HttpsError("unauthenticated", message);
  }

  if (message.includes("Permission denied") || message.includes("Forbidden")) {
    return new HttpsError("permission-denied", message);
  }

  if (message.includes("Not found") || message.includes("does not exist")) {
    return new HttpsError("not-found", message);
  }

  if (message.includes("Already exists") || message.includes("duplicate")) {
    return new HttpsError("already-exists", message);
  }

  if (message.includes("Invalid") || message.includes("validation")) {
    return new HttpsError("invalid-argument", message);
  }

  // Default to internal error
  return new HttpsError("internal", message);
}

/**
 * Log error với context.
 * @param {string} context Context name
 * @param {unknown} error Error object
 */
export function logError(context: string, error: unknown): void {
  console.error(`[ERROR] ${context}:`, {
    message: (error as {message?: unknown})?.message,
    stack: (error as {stack?: unknown})?.stack,
    code: (error as {code?: unknown})?.code,
  });
}
