# Admin Panel - Setup Complete ✅

## ✨ Status

**ALL TODOS COMPLETED** - Admin panel is ready for demo!

- ✅ Project initialization with Vite + React + TypeScript
- ✅ Firebase Auth + API client configured
- ✅ Admin role verification implemented
- ✅ Layout with navigation
- ✅ Dashboard page
- ✅ Users management page
- ✅ Shops management page
- ✅ Categories management page
- ✅ Payouts management page (bonus)

---

## 🚀 Quick Start

### 1. Start Backend

```bash
cd MobileProject/Backend/functions
npm run serve
```

### 2. Start Admin Panel

```bash
cd MobileProject/Admin
npm run dev
```

### 3. Access Admin Panel

Open: http://localhost:5173

---

## 🔐 Creating Admin User

**IMPORTANT:** You need an admin user to login!

### Option 1: Using Backend Script

```bash
cd MobileProject/Backend/functions
npx ts-node scripts/set-admin-role.ts <admin-email>
```

### Option 2: Manual Firestore Update

1. Go to Firebase Console → Firestore
2. Find user document in `users` collection
3. Set `role = "ADMIN"`
4. Go to Firebase Console → Authentication → Users
5. Click on user → Custom claims
6. Add: `{"role": "ADMIN"}`

---

## 📋 Features Implemented

### 🔐 Authentication & Security

- Firebase email/password authentication
- **Role verification** via `/me` endpoint
- Access denied for non-admin users
- Auto-logout on 401/403 errors
- Protected routes with auth guard

### 📊 Dashboard

- User statistics (total, new today)
- Shop statistics (total, active, pending approval)
- Order statistics (today, week, month)
- Revenue statistics (today, week, month)
- Payout statistics (pending count and amount)

### 👥 Users Management

- List all users with pagination
- Filter by role (Customer, Owner, Shipper)
- Filter by status (Active, Banned)
- Search by name or email
- **Ban/Unban users**
- View user details

### 🏪 Shops Management

- List all shops with pagination
- Filter by status (Open, Closed, Suspended, Banned, Pending Approval)
- Search by shop name
- **Approve pending shops**
- **Suspend/Activate shops**
- **Ban shops**
- View shop logo, rating, owner info

### 📦 Categories Management

- List all categories
- **Create new category**
- **Edit category ** (name, slug, icon, sort order, active status)
- **Delete category** (with safety check)
- View product count per category

### 💰 Payouts Management

- List payouts with pagination
- Filter by status (Pending, Approved, Rejected, Transferred)
- View detailed payout information (bank details)
- **Approve payout requests**
- **Reject payout requests** (with reason)
- **Mark as transferred** (with transfer note)

---

## 🛠 Tech Stack

- **Frontend**: React 18 + TypeScript
- **Build Tool**: Vite
- **UI Library**: Ant Design 5
- **Routing**: React Router v6
- **HTTP**: Axios
- **Auth**: Firebase Auth
- **State**: React Context API
- **Date**: Day.js

---

## 📁 Project Structure

```
Admin/
├── src/
│   ├── api/
│   │   └── client.ts              # Axios instance with auth interceptor
│   ├── components/
│   │   ├── Layout.tsx             # Main layout with sidebar & header
│   │   └── ProtectedRoute.tsx    # Auth guard with role check
│   ├── config/
│   │   └── firebase.ts            # Firebase initialization
│   ├── contexts/
│   │   └── AuthContext.tsx        # Auth state + role verification
│   ├── pages/
│   │   ├── Login.tsx              # Login page
│   │   ├── Dashboard.tsx          # Dashboard with stats
│   │   ├── Users.tsx              # User management
│   │   ├── Shops.tsx              # Shop management
│   │   ├── Categories.tsx         # Category CRUD
│   │   └── Payouts.tsx            # Payout management
│   ├── types/
│   │   └── index.ts               # TypeScript interfaces
│   ├── App.tsx                    # Routes configuration
│   ├── main.tsx                   # Entry point
│   └── vite-env.d.ts              # Vite types
├── .env                           # Environment variables
├── vite.config.ts                 # Vite configuration
├── tsconfig.json                  # TypeScript configuration
└── package.json                   # Dependencies
```

---

## 🔧 Environment Variables

File: `.env`

```bash
# Firebase
VITE_FIREBASE_API_KEY=AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA
VITE_FIREBASE_AUTH_DOMAIN=foodappproject-7c136.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=foodappproject-7c136
VITE_FIREBASE_STORAGE_BUCKET=foodappproject-7c136.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=884959847866
VITE_FIREBASE_APP_ID=1:884959847866:android:b1afd808dd654666762983

# API
VITE_API_BASE_URL=http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api
```

---

## 🔗 API Endpoints Used

| Endpoint                         | Method | Description                               |
| -------------------------------- | ------ | ----------------------------------------- |
| `/me`                            | GET    | Get current user (with role verification) |
| `/admin/dashboard`               | GET    | Dashboard statistics                      |
| `/admin/users`                   | GET    | List users with filters                   |
| `/admin/users/:id/status`        | PUT    | Ban/unban user                            |
| `/admin/shops`                   | GET    | List shops with filters                   |
| `/admin/shops/:id/status`        | PUT    | Update shop status                        |
| `/admin/categories`              | GET    | List all categories                       |
| `/admin/categories`              | POST   | Create category                           |
| `/admin/categories/:id`          | PUT    | Update category                           |
| `/admin/categories/:id`          | DELETE | Delete category                           |
| `/admin/payouts`                 | GET    | List payouts                              |
| `/admin/payouts/:id/approve`     | POST   | Approve payout                            |
| `/admin/payouts/:id/reject`      | POST   | Reject payout                             |
| `/admin/payouts/:id/transferred` | POST   | Mark transferred                          |

---

## ⚠️ Important Notes

### Role Verification

The app implements **critical role verification**:

1. **On Login**: Check role via `/me` endpoint
2. **On App Start**: Verify role if token exists
3. **Route Guard**: Block non-admin users at route level
4. **API Level**: All endpoints protected by `AdminGuard`

### Error Handling

- **401 Unauthorized**: Auto-logout + redirect to login
- **403 Forbidden**: Access denied message
- **Network Errors**: Retry mechanism
- **Form Validation**: Inline error messages

### Security

- Tokens stored in localStorage
- Auto-refresh on session restore
- Logout clears all auth data
- API interceptor adds token automatically

---

## 🎯 Next Steps (Optional)

### TODO 4 - Payouts Page Strategy (Already Implemented!)

All payout endpoints are working:

- ✅ List payouts with filters
- ✅ Approve payouts
- ✅ Reject payouts with reason
- ✅ Mark as transferred with note

### Additional Enhancements (Nice to Have)

1. **Order Management**
   - View all orders
   - Order details modal
   - Refund management

2. **Analytics**
   - Charts for revenue trends
   - User growth graphs
   - Shop performance metrics

3. **Notifications**
   - Real-time notifications
   - Email alerts for pending actions

4. **Audit Log**
   - Track admin actions
   - View change history

---

## 🐛 Troubleshooting

### Login fails with "Access Denied"

- Ensure user has `role: "ADMIN"` in Firestore
- Check Firebase custom claims
- Verify `/me` endpoint returns correct role

### API calls return 404

- Check backend is running: `http://localhost:5001`
- Verify API base URL in `.env`
- Check Functions endpoint in Firebase Console

### Build errors

- Clear node_modules: `rm -rf node_modules && npm install`
- Clear cache: `rm -rf dist && npm run build`

### Token expired

- Logout and login again
- Check Firebase token expiration (1 hour default)

---

## 📸 Demo Flow

1. **Start Backend**: Functions emulator running
2. **Create Admin**: Run set-admin-role script
3. **Start Admin Panel**: `npm run dev`
4. **Login**: Use admin credentials
5. **Dashboard**: View system overview
6. **Users**: Ban/unban test users
7. **Shops**: Approve pending shops
8. **Categories**: Create/edit categories
9. **Payouts**: Review and approve requests

---

## ✅ Checklist - All Done!

- [x] TODO 1: Environment + Config configured ✅
- [x] TODO 2: Admin Role Verification implemented ✅
- [x] TODO 3: Smoke test all pages work ✅
- [x] TODO 4: Payouts page completed ✅

**Status: READY FOR DEMO** 🎉

---

## 📞 Support

For issues or questions:

1. Check console logs in browser DevTools
2. Check terminal for API errors
3. Verify Firebase Auth custom claims
4. Test `/me` endpoint directly in Postman

---

**Built with ❤️ for KTX Delivery**
