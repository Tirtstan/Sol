# Firestore & Firebase Auth Setup Guide

## Quick Setup Steps

### 1. Enable Firebase Authentication

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `sol-app-a4b05`
3. Navigate to **Authentication** → **Get Started**
4. Click on **Email/Password**
5. Toggle **Enable** 
6. Click **Save**

**No API keys needed** - your `google-services.json` already has everything configured!

### 2. Create Firestore Database

1. In Firebase Console, go to **Firestore Database**
2. Click **Create Database**
3. Choose **Start in test mode** (for development)
4. Select a location closest to your users
5. Click **Enable**

### 3. Set Security Rules

Go to **Firestore Database** → **Rules** and paste this:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Subcollections
      match /transactions/{transactionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /categories/{categoryId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /budgets/{budgetId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

Click **Publish** to save.

### 4. Collection Structure

Your Firestore will automatically create these collections when users register:

```
users/
  └── {firebaseAuthUID}/          (User document)
      ├── username: "john_doe"
      ├── createdAt: Timestamp
      │
      ├── transactions/            (Subcollection)
      │   └── {autoId}/
      │       ├── name, amount, date, categoryId, type, note, imagePath
      │
      ├── categories/              (Subcollection)
      │   └── {autoId}/
      │       ├── name, color, icon
      │
      └── budgets/                 (Subcollection)
          └── {autoId}/
              ├── name, description, categoryId, minGoalAmount, maxGoalAmount, startDate, endDate
```

## Important Changes

- **Authentication**: Now uses email/password (not username)
- **IDs**: All IDs are now Strings (Firebase UIDs)
- **Real-time**: Firestore automatically syncs data across devices
- **Offline**: Works offline automatically with local caching

## Testing

1. Run the app
2. Register with email/password
3. Create transactions, categories, budgets
4. Check Firebase Console → Firestore Database to see your data

That's it! No API keys or additional files needed - everything is configured via `google-services.json`.

