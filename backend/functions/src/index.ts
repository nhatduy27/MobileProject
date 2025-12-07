// Boilerplate do Firebase tạo khi khởi tạo dự án Functions TypeScript.
/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {setGlobalOptions} from "firebase-functions";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({maxInstances: 10});

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

// Còn dưới đây là phần code
// entry point – export all triggers here
/**
 * Firebase Cloud Functions Entry Point
 *
 * Export tất cả functions để Firebase CLI deploy
 */

// Khởi tạo Firebase Admin SDK
import * as admin from "firebase-admin";
admin.initializeApp();

// Export các triggers
export * from "./triggers/api.order";
export * from "./triggers/api.promotion";
export * from "./triggers/auth.trigger";
export * from "./triggers/order.trigger";

/**
 * Deployment structure:
 *
 * Callable Functions (HTTPS):
 * - placeOrder
 * - cancelOrder
 * - applyPromotion
 *
 * Auth Triggers:
 * - onUserCreated
 *
 * Firestore Triggers:
 * - onOrderCreated
 *
 * TODO: Add more functions as needed:
 * - Scheduled functions (cron jobs)
 * - Storage triggers
 * - Pub/Sub triggers
 * - Additional API endpoints
 */

