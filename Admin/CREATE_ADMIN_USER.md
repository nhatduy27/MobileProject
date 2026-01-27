# Quick Guide - Creating Admin User

## Method 1: By Email (Easiest)

If you already have a user account, you can set it as admin by email:

```bash
cd MobileProject/Backend/functions

# Find user by email and get UID
npx ts-node -e "
import * as admin from 'firebase-admin';
const serviceAccount = require('./service-account.json');
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

const email = 'YOUR_EMAIL@example.com'; // CHANGE THIS

admin.auth().getUserByEmail(email)
  .then(user => {
    console.log('Found user UID:', user.uid);
    console.log('Now run: npx ts-node scripts/set-admin-role.ts', user.uid);
  })
  .catch(err => console.error('Error:', err));
"
```

Then use the UID from the output:

```bash
npx ts-node scripts/set-admin-role.ts <UID_FROM_PREVIOUS_COMMAND>
```

## Method 2: Combined Script (Copy-Paste)

Save this to `scripts/set-admin-by-email.ts`:

```typescript
import * as admin from "firebase-admin";
import * as path from "path";

const serviceAccountPath = path.join(__dirname, "..", "service-account.json");
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const auth = admin.auth();
const db = admin.firestore();
const { Timestamp } = admin.firestore;

async function setAdminByEmail(email: string) {
  console.log(`🔐 Setting admin role for: ${email}\n`);

  try {
    // 1. Find user by email
    const userRecord = await auth.getUserByEmail(email);
    const uid = userRecord.uid;
    console.log(`  ✅ Found user UID: ${uid}`);

    // 2. Set custom claims
    await auth.setCustomUserClaims(uid, { role: "ADMIN" });
    console.log("  ✅ Set role: ADMIN");

    // 3. Update Firestore
    const now = Timestamp.now();
    await db.collection("users").doc(uid).update({
      role: "ADMIN",
      updatedAt: now,
    });
    console.log("  ✅ Updated Firestore");

    // 4. Create admin record
    const adminDoc = await db.collection("admins").doc(uid).get();
    if (!adminDoc.exists) {
      await db
        .collection("admins")
        .doc(uid)
        .set({
          userId: uid,
          email: userRecord.email,
          displayName: userRecord.displayName || null,
          permissions: ["all"],
          createdAt: now,
          updatedAt: now,
        });
      console.log("  ✅ Created admin record");
    }

    console.log("\n🎉 Success!\n");
    console.log("⚠️  User must sign out and sign in again.\n");
  } catch (error: any) {
    console.error("❌ Error:", error.message);
    throw error;
  }
}

const email = process.argv[2];
if (!email) {
  console.error("Usage: npx ts-node scripts/set-admin-by-email.ts <email>");
  process.exit(1);
}

setAdminByEmail(email)
  .then(() => process.exit(0))
  .catch(() => process.exit(1));
```

Run it:

```bash
npx ts-node scripts/set-admin-by-email.ts your-email@example.com
```

## Method 3: Firebase Console (Manual)

1. Go to Firebase Console
2. **Firestore Database**:
   - Find user in `users` collection
   - Set `role = "ADMIN"`
3. **Authentication**:
   - Select user
   - Click "Custom claims"
   - Add: `{"role": "ADMIN"}`

## Testing

After setting admin role:

1. **Logout** from any active sessions
2. **Login** to admin panel: http://localhost:5173
3. Should see dashboard successfully!

## Troubleshooting

**"Access Denied" error:**

- Double-check Firestore `users/{uid}/role` is "ADMIN"
- Double-check Auth custom claims has `{"role": "ADMIN"}`
- User MUST logout and login again to refresh token

**"User not found":**

- User must register first through mobile app or auth endpoint
- Then run set-admin script

**Backend `/me` returns wrong role:**

- Check Backend TODO 0 (Role Sync) is completed
- Verify AuthGuard checks custom claims first, then Firestore
