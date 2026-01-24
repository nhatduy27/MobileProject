/**
 * Firestore Utilities for Voucher Module
 *
 * Helpers to ensure valid Firestore documents (no undefined values)
 */

/**
 * Strip undefined values from an object before writing to Firestore
 *
 * Firestore does not allow undefined values in documents.
 * This function recursively removes all undefined fields.
 *
 * @param obj - Object to sanitize
 * @returns New object with undefined values removed
 *
 * @example
 * stripUndefined({ a: 1, b: undefined, c: null })
 * // Returns: { a: 1, c: null }
 */
export function stripUndefined<T extends Record<string, any>>(obj: T): Partial<T> {
  const result = {} as Partial<T>;

  for (const key in obj) {
    const value = obj[key];

    // Skip undefined values
    if (value === undefined) {
      continue;
    }

    // Keep null, false, 0, empty string (all valid Firestore values)
    result[key] = value;
  }

  return result;
}

/**
 * Deep strip undefined values (for nested objects)
 *
 * @param obj - Object to sanitize
 * @returns New object with undefined values removed (including nested)
 */
export function stripUndefinedDeep<T extends Record<string, any>>(obj: T): Partial<T> {
  const result = {} as Partial<T>;

  for (const key in obj) {
    const value = obj[key];

    // Skip undefined values
    if (value === undefined) {
      continue;
    }

    // Recursively handle nested objects
    if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
      result[key] = stripUndefinedDeep(value) as any;
    } else {
      result[key] = value;
    }
  }

  return result;
}
